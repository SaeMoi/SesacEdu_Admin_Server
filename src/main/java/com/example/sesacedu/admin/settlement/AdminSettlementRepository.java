package com.example.sesacedu.admin.settlement;

import com.example.sesacedu.admin.settlement.dto.SettlementDetailResponse;
import com.example.sesacedu.admin.settlement.dto.SettlementItem;
import com.example.sesacedu.admin.settlement.dto.SettlementLectureItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 정산 JDBC. (확정 DDL 기준)
 *   settlements 에 status 컬럼이 실제로 존재 → 필터·변경 모두 status 직접 사용.
 *   강사명은 instructors.iname 기준 (목록 표시 통일).
 */
@Repository
public class AdminSettlementRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminSettlementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static String toDate(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime().toLocalDate().toString();
    }

    // status(바인드) + period(바인드) 필터 공통
    //   approved(승인완료) 필터는 지급완료(paid)까지 함께 보여준다.
    private static void appendFilters(StringBuilder sql, List<Object> args, String status, String period) {
        if (status != null && !status.isBlank()) {
            if ("approved".equals(status.trim())) {
                sql.append("AND s.status IN ('approved', 'paid') ");   // 승인완료 = 승인 + 지급완료
            } else {
                sql.append("AND s.status = ? ");
                args.add(status.trim());
            }
        }
        if (period != null && !period.isBlank()) {
            sql.append("AND s.period = ? ");
            args.add(period.trim());
        }
    }

    private static final RowMapper<SettlementItem> ITEM_MAPPER = (rs, n) ->
            new SettlementItem(
                    rs.getLong("sid"),
                    rs.getString("iname"),
                    rs.getString("period"),
                    rs.getLong("revenue"),
                    rs.getLong("deduction"),
                    rs.getLong("amount"),
                    rs.getString("status"),
                    toDate(rs.getTimestamp("approved_at")),
                    toDate(rs.getTimestamp("rejected_at")),
                    toDate(rs.getTimestamp("paid_at")));

    public List<SettlementItem> findPage(String status, String period, int offset, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.sid, i.iname, s.period, s.revenue, s.deduction, s.amount, s.status, " +
                        "       s.approved_at, s.rejected_at, s.paid_at " +
                        "FROM settlements s JOIN instructors i ON s.user_id = i.user_id WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, status, period);
        sql.append("ORDER BY s.requested DESC NULLS LAST, s.sid DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        args.add(offset);
        args.add(size);
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER, args.toArray());
    }

    public int count(String status, String period) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM settlements s JOIN instructors i ON s.user_id = i.user_id WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, status, period);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return c == null ? 0 : c;
    }

    // ---------------- 상세 ----------------

    /** 상세 헤더 + 강의별 내역 조회에 필요한 강사 user_id 를 함께 담는 홀더 */
    public static class HeaderRow {
        public final SettlementDetailResponse body;
        public final long instructorUid;   // settlements.user_id (강사)
        public HeaderRow(SettlementDetailResponse body, long instructorUid) {
            this.body = body;
            this.instructorUid = instructorUid;
        }
    }

    /** 정산 헤더 조회 (없으면 null) */
    public HeaderRow findHeader(long sid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT s.sid, s.user_id, i.iname, s.period, s.revenue, s.deduction, s.amount, s.status, " +
                            "       s.bank, s.account, s.requested, s.approved_at, s.approved_by, " +
                            "       s.rejected_at, s.reject_reason, s.paid_at " +
                            "FROM settlements s JOIN instructors i ON s.user_id = i.user_id WHERE s.sid = ?",
                    (rs, n) -> {
                        SettlementDetailResponse d = new SettlementDetailResponse();
                        d.setSid(rs.getLong("sid"));
                        d.setIname(rs.getString("iname"));
                        d.setPeriod(rs.getString("period"));
                        d.setRevenue(rs.getLong("revenue"));
                        d.setDeduction(rs.getLong("deduction"));
                        d.setAmount(rs.getLong("amount"));
                        d.setStatus(rs.getString("status"));
                        d.setBank(rs.getString("bank"));
                        d.setAccount(rs.getString("account"));
                        d.setRequestedAt(toDate(rs.getTimestamp("requested")));
                        d.setApprovedAt(toDate(rs.getTimestamp("approved_at")));
                        long approvedBy = rs.getLong("approved_by");
                        d.setApprovedBy(rs.wasNull() ? null : approvedBy);
                        d.setRejectedAt(toDate(rs.getTimestamp("rejected_at")));
                        d.setRejectReason(rs.getString("reject_reason"));
                        d.setPaidAt(toDate(rs.getTimestamp("paid_at")));
                        return new HeaderRow(d, rs.getLong("user_id"));
                    }, sid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // 강의별 매출: 해당 강사의 강의 + 결제완료(paid) 주문 + period(YYYY-MM, paid_at 기준) 집계
    private static final String BREAKDOWN_FROM =
            "FROM order_items oi " +
                    "JOIN orders o   ON oi.oid = o.oid " +
                    "JOIN lectures l ON oi.Lid = l.Lid " +
                    "WHERE l.user_id = ? AND o.status = 'paid' AND TO_CHAR(o.paid_at, 'YYYY-MM') = ? ";

    private static final RowMapper<SettlementLectureItem> LECTURE_MAPPER = (rs, n) ->
            new SettlementLectureItem(
                    rs.getLong("lid"),
                    rs.getString("lname"),
                    rs.getLong("sales_count"),
                    rs.getLong("revenue"));

    public List<SettlementLectureItem> findLectureBreakdown(long instructorUid, String period, int offset, int size) {
        String sql =
                "SELECT l.Lid AS lid, l.name AS lname, COUNT(*) AS sales_count, SUM(oi.price) AS revenue " +
                        BREAKDOWN_FROM +
                        "GROUP BY l.Lid, l.name " +
                        "ORDER BY revenue DESC, l.Lid DESC " +
                        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return jdbcTemplate.query(sql, LECTURE_MAPPER, instructorUid, period, offset, size);
    }

    public int countLectureBreakdown(long instructorUid, String period) {
        String sql =
                "SELECT COUNT(*) FROM (" +
                        "  SELECT l.Lid " + BREAKDOWN_FROM + "GROUP BY l.Lid, l.name" +
                        ")";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, instructorUid, period);
        return c == null ? 0 : c;
    }

    /** 현재 상태 조회 (없으면 null) — 승인/거절 전 검증용 */
    public String findStatus(long sid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT status FROM settlements WHERE sid = ?", String.class, sid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** 승인: status=approved, approved_at, approved_by(관리자 uid) 기록 */
    public void approve(long sid, long adminUid) {
        jdbcTemplate.update(
                "UPDATE settlements SET status = 'approved', approved_at = SYSTIMESTAMP, approved_by = ? " +
                        "WHERE sid = ?",
                adminUid, sid);
    }

    /** 거절: status=rejected, rejected_at, reject_reason 기록 */
    public void reject(long sid, String reason) {
        jdbcTemplate.update(
                "UPDATE settlements SET status = 'rejected', rejected_at = SYSTIMESTAMP, reject_reason = ? " +
                        "WHERE sid = ?",
                reason, sid);
    }
}
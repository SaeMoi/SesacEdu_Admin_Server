package com.example.sesacedu.admin.settlement;

import com.example.sesacedu.admin.settlement.dto.SettlementDetailResponse;
import com.example.sesacedu.admin.settlement.dto.SettlementItem;
import com.example.sesacedu.admin.settlement.dto.SettlementLectureItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/*
 * ===================== [취약 진단용 - admin/settlement] JWT·필수값검증 유지 =====================
 *  (V-SQLI/A03) status·period·offset/size·sid·instructorUid·reject_reason 바인딩 제거 → 문자열 연결
 *  (V-XSS/A03)  reject_reason 미살균 저장 → 상세 조회 시 그대로 반환되어 저장형 XSS
 *  approved=승인+지급완료(paid) 함께 노출하는 로직은 유지(흐름 불변).
 * ==============================================================================================
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

    // [V-SQLI] status/period 문자열 직접 연결 (approved→paid 포함 로직 유지)
    private static void appendFilters(StringBuilder sql, String status, String period) {
        if (status != null && !status.isBlank()) {
            if ("approved".equals(status.trim())) {
                sql.append("AND s.status IN ('approved', 'paid') ");   // 승인완료 = 승인 + 지급완료
            } else {
                sql.append("AND s.status = '" + status.trim() + "' ");
            }
        }
        if (period != null && !period.isBlank()) {
            sql.append("AND s.period = '" + period.trim() + "' ");
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
        appendFilters(sql, status, period);
        sql.append("ORDER BY s.requested DESC NULLS LAST, s.sid DESC " +
                "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY");
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER);
    }

    public int count(String status, String period) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM settlements s JOIN instructors i ON s.user_id = i.user_id WHERE 1=1 ");
        appendFilters(sql, status, period);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
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

    /** [V-SQLI] 정산 헤더 조회: sid 문자열 연결 (없으면 null) */
    public HeaderRow findHeader(long sid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT s.sid, s.user_id, i.iname, s.period, s.revenue, s.deduction, s.amount, s.status, " +
                            "       s.bank, s.account, s.requested, s.approved_at, s.approved_by, " +
                            "       s.rejected_at, s.reject_reason, s.paid_at " +
                            "FROM settlements s JOIN instructors i ON s.user_id = i.user_id WHERE s.sid = " + sid,
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
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // [V-SQLI] 강의별 매출: instructorUid/period 문자열 연결
    private static String breakdownFrom(long instructorUid, String period) {
        return "FROM order_items oi " +
                "JOIN orders o   ON oi.oid = o.oid " +
                "JOIN lectures l ON oi.Lid = l.Lid " +
                "WHERE l.user_id = " + instructorUid + " AND o.status = 'paid' " +
                "AND TO_CHAR(o.paid_at, 'YYYY-MM') = '" + period + "' ";
    }

    private static final RowMapper<SettlementLectureItem> LECTURE_MAPPER = (rs, n) ->
            new SettlementLectureItem(
                    rs.getLong("lid"),
                    rs.getString("lname"),
                    rs.getLong("sales_count"),
                    rs.getLong("revenue"));

    public List<SettlementLectureItem> findLectureBreakdown(long instructorUid, String period, int offset, int size) {
        String sql =
                "SELECT l.Lid AS lid, l.name AS lname, COUNT(*) AS sales_count, SUM(oi.price) AS revenue " +
                        breakdownFrom(instructorUid, period) +
                        "GROUP BY l.Lid, l.name " +
                        "ORDER BY revenue DESC, l.Lid DESC " +
                        "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        return jdbcTemplate.query(sql, LECTURE_MAPPER);
    }

    public int countLectureBreakdown(long instructorUid, String period) {
        String sql =
                "SELECT COUNT(*) FROM (" +
                        "  SELECT l.Lid " + breakdownFrom(instructorUid, period) + "GROUP BY l.Lid, l.name" +
                        ")";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class);
        return c == null ? 0 : c;
    }

    /** [V-SQLI] 현재 상태 조회: sid 문자열 연결 (없으면 null) */
    public String findStatus(long sid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT status FROM settlements WHERE sid = " + sid, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** [V-SQLI] 승인: sid/approved_by 문자열 연결 */
    public void approve(long sid, long adminUid) {
        jdbcTemplate.update(
                "UPDATE settlements SET status = 'approved', approved_at = SYSTIMESTAMP, " +
                        "approved_by = " + adminUid + " WHERE sid = " + sid);
    }

    /** [V-SQLI/V-XSS] 거절: reject_reason/sid 문자열 연결 → 저장형 SQLi/XSS */
    public void reject(long sid, String reason) {
        jdbcTemplate.update(
                "UPDATE settlements SET status = 'rejected', rejected_at = SYSTIMESTAMP, " +
                        "reject_reason = '" + reason + "' WHERE sid = " + sid);
    }
}
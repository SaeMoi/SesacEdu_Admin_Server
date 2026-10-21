package com.example.sesacedu.admin.instructor;

import com.example.sesacedu.admin.instructor.dto.InstructorApplicationDetail;
import com.example.sesacedu.admin.instructor.dto.InstructorApplicationItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 강사 신청 JDBC. (확정 DDL 기준)
 *   목록/카운트: instructors 단독 (이름·검색 모두 공개 강사명 iname 기준)
 *   상세: instructors JOIN users (이메일용)
 *   상태(status)는 컬럼이 없고 approved_at / rejected_at 로 파생.
 */
@Repository
public class AdminInstructorRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String fileBaseUrl;

    public AdminInstructorRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${app.file.local.base-url:http://localhost:8080/files}") String fileBaseUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileBaseUrl = fileBaseUrl.endsWith("/")
                ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) : fileBaseUrl;
    }

    private static String toStatus(Timestamp approvedAt, Timestamp rejectedAt) {
        if (approvedAt != null) return "approved";
        if (rejectedAt != null) return "rejected";
        return "pending";
    }

    private static String toDate(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime().toLocalDate().toString();
    }

    // status 필터(리터럴) + keyword 필터(바인드) — 검색은 공개 강사명(iname)
    private static void appendFilters(StringBuilder sql, List<Object> args, String status, String keyword) {
        if ("approved".equals(status))      sql.append("AND i.approved_at IS NOT NULL ");
        else if ("pending".equals(status))  sql.append("AND i.approved_at IS NULL AND i.rejected_at IS NULL ");
        else if ("rejected".equals(status)) sql.append("AND i.rejected_at IS NOT NULL ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND LOWER(i.iname) LIKE '%' || LOWER(?) || '%' ");
            args.add(keyword.trim());
        }
    }

    // ---------------- 목록 ----------------
    private static final RowMapper<InstructorApplicationItem> ITEM_MAPPER = (rs, n) ->
            new InstructorApplicationItem(
                    rs.getLong("user_id"),
                    rs.getString("iname"),
                    rs.getString("field"),
                    toDate(rs.getTimestamp("applied_at")),
                    toStatus(rs.getTimestamp("approved_at"), rs.getTimestamp("rejected_at")));

    public List<InstructorApplicationItem> findPage(String status, String keyword, int offset, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.user_id, i.iname, i.field, i.applied_at, i.approved_at, i.rejected_at " +
                        "FROM instructors i WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, status, keyword);
        sql.append("ORDER BY i.applied_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        args.add(offset);
        args.add(size);
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER, args.toArray());
    }

    public int count(String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM instructors i WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, status, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return c == null ? 0 : c;
    }

    // ---------------- 상세 ----------------
    public InstructorApplicationDetail findDetail(long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT i.user_id, i.iname, u.mail AS email, i.bio, i.career, i.field, " +
                            "       i.career_filename, i.career_file, i.approved_at, i.rejected_at, i.applied_at " +
                            "FROM instructors i JOIN users u ON i.user_id = u.user_id WHERE i.user_id = ?",
                    (rs, n) -> {
                        InstructorApplicationDetail d = new InstructorApplicationDetail();
                        d.setUserId(rs.getLong("user_id"));
                        d.setIname(rs.getString("iname"));
                        d.setEmail(rs.getString("email"));
                        d.setBio(rs.getString("bio"));
                        d.setCareer(rs.getString("career"));
                        d.setField(rs.getString("field"));
                        d.setCareerFilename(rs.getString("career_filename"));
                        String key = rs.getString("career_file");
                        d.setCareerFileUrl(key == null || key.isBlank() ? null : fileBaseUrl + "/" + key);
                        d.setStatus(toStatus(rs.getTimestamp("approved_at"), rs.getTimestamp("rejected_at")));
                        d.setAppliedAt(toDate(rs.getTimestamp("applied_at")));
                        return d;
                    }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ---------------- 상태 확인 ----------------
    public boolean exists(long userId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM instructors WHERE user_id = ?", Integer.class, userId);
        return c != null && c > 0;
    }

    public boolean isProcessed(long userId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM instructors " +
                        "WHERE user_id = ? AND (approved_at IS NOT NULL OR rejected_at IS NOT NULL)",
                Integer.class, userId);
        return c != null && c > 0;
    }

    // ---------------- 승인 / 거절 ----------------
    /** 승인: instructors.approved_at 기록 + users.auth = 2(강사) */
    public void approve(long userId) {
        jdbcTemplate.update(
                "UPDATE instructors SET approved_at = SYSTIMESTAMP, updated = SYSTIMESTAMP WHERE user_id = ?",
                userId);
        jdbcTemplate.update(
                "UPDATE users SET auth = 2, updated = SYSTIMESTAMP WHERE user_id = ?",
                userId);
    }

    /** 거절: instructors.rejected_at + reject_reason 기록 (auth는 변경하지 않음) */
    public void reject(long userId, String reason) {
        jdbcTemplate.update(
                "UPDATE instructors SET rejected_at = SYSTIMESTAMP, reject_reason = ?, updated = SYSTIMESTAMP " +
                        "WHERE user_id = ?",
                reason, userId);
        jdbcTemplate.update(
                "UPDATE users SET auth = 1, updated = SYSTIMESTAMP WHERE user_id = ?",
                userId);
    }
}
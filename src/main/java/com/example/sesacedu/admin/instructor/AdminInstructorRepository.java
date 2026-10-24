package com.example.sesacedu.admin.instructor;

import com.example.sesacedu.admin.instructor.dto.InstructorApplicationDetail;
import com.example.sesacedu.admin.instructor.dto.InstructorApplicationItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/*
 * ===================== [취약 진단용 - admin/instructor] JWT·필수값검증 유지 =====================
 *  (V-SQLI/A03) keyword(iname 검색)·offset/size·userId·reject_reason 바인딩 제거 → 문자열 연결
 *  (V-XSS/A03)  reject_reason 미살균 저장 → 조회 시 그대로 반환되어 저장형 XSS
 * ===============================================================================================
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

    // [V-SQLI] status 리터럴 + keyword 문자열 연결(iname 검색)
    private static void appendFilters(StringBuilder sql, String status, String keyword) {
        if ("approved".equals(status))      sql.append("AND i.approved_at IS NOT NULL ");
        else if ("pending".equals(status))  sql.append("AND i.approved_at IS NULL AND i.rejected_at IS NULL ");
        else if ("rejected".equals(status)) sql.append("AND i.rejected_at IS NOT NULL ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND LOWER(i.iname) LIKE LOWER('%" + keyword.trim() + "%') ");
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
        appendFilters(sql, status, keyword);
        sql.append("ORDER BY i.applied_at DESC OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY");
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER);
    }

    public int count(String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM instructors i WHERE 1=1 ");
        appendFilters(sql, status, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return c == null ? 0 : c;
    }

    // ---------------- 상세 ----------------
    public InstructorApplicationDetail findDetail(long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT i.user_id, i.iname, u.mail AS email, i.bio, i.career, i.field, " +
                            "       i.career_filename, i.career_file, i.approved_at, i.rejected_at, i.applied_at " +
                            "FROM instructors i JOIN users u ON i.user_id = u.user_id WHERE i.user_id = " + userId,
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
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ---------------- 상태 확인 ----------------
    public boolean exists(long userId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM instructors WHERE user_id = " + userId, Integer.class);
        return c != null && c > 0;
    }

    public boolean isProcessed(long userId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM instructors " +
                        "WHERE user_id = " + userId + " AND (approved_at IS NOT NULL OR rejected_at IS NOT NULL)",
                Integer.class);
        return c != null && c > 0;
    }

    // ---------------- 승인 / 거절 ----------------
    /** 승인: instructors.approved_at 기록 + users.auth = 2(강사) */
    public void approve(long userId) {
        jdbcTemplate.update(
                "UPDATE instructors SET approved_at = SYSTIMESTAMP, updated = SYSTIMESTAMP WHERE user_id = " + userId);
        jdbcTemplate.update(
                "UPDATE users SET auth = 2, updated = SYSTIMESTAMP WHERE user_id = " + userId);
    }

    /** [V-SQLI/V-XSS] 거절: reject_reason 문자열 연결 → 저장형 SQLi/XSS. auth=1 로 되돌림. */
    public void reject(long userId, String reason) {
        jdbcTemplate.update(
                "UPDATE instructors SET rejected_at = SYSTIMESTAMP, reject_reason = '" + reason + "', " +
                        "updated = SYSTIMESTAMP WHERE user_id = " + userId);
        jdbcTemplate.update(
                "UPDATE users SET auth = 1, updated = SYSTIMESTAMP WHERE user_id = " + userId);
    }
}
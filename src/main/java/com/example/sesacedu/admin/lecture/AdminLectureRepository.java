package com.example.sesacedu.admin.lecture;

import com.example.sesacedu.admin.lecture.dto.LectureItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/*
 * ===================== [취약 진단용 - admin/lecture] JWT·필수값검증 유지 =====================
 *  (V-SQLI/A03) status·keyword(강의명)·offset/size·lid 바인딩 제거 → 문자열 연결
 *  삭제건(deleted) 항상 제외 로직은 유지(흐름 불변).
 * ===========================================================================================
 */
@Repository
public class AdminLectureRepository {

    private static final String DELETED = "deleted";

    private final JdbcTemplate jdbcTemplate;

    public AdminLectureRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static String toDate(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime().toLocalDate().toString();
    }

    // [V-SQLI] status/keyword 문자열 직접 연결 (삭제건 제외 로직은 유지)
    private static void appendFilters(StringBuilder sql, String status, String keyword) {
        sql.append("AND l.status <> '" + DELETED + "' ");                 // 삭제건 항상 제외
        if (status != null && !status.isBlank() && !DELETED.equals(status.trim())) {
            sql.append("AND l.status = '" + status.trim() + "' ");
        }
        if (keyword != null && !keyword.isBlank()) {
            for (String token : keyword.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                sql.append("AND LOWER(l.name) LIKE LOWER('%" + token + "%') ");
            }
        }
    }

    // ---------------- 목록 ----------------
    private static final RowMapper<LectureItem> ITEM_MAPPER = (rs, n) ->
            new LectureItem(
                    rs.getLong("lid"),
                    rs.getString("name"),
                    rs.getString("instructor_name"),
                    rs.getString("category"),
                    rs.getString("llevel"),
                    rs.getLong("lprice"),
                    rs.getString("status"),
                    toDate(rs.getTimestamp("created")));

    public List<LectureItem> findPage(String status, String keyword, int offset, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT l.Lid AS lid, l.name, i.iname AS instructor_name, l.category, " +
                        "       l.Llevel AS llevel, l.Lprice AS lprice, l.status, l.created " +
                        "FROM lectures l JOIN instructors i ON l.user_id = i.user_id WHERE 1=1 ");
        appendFilters(sql, status, keyword);
        sql.append("ORDER BY l.created DESC NULLS LAST, l.Lid DESC " +
                "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY");
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER);
    }

    public int count(String status, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM lectures l JOIN instructors i ON l.user_id = i.user_id WHERE 1=1 ");
        appendFilters(sql, status, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return c == null ? 0 : c;
    }

    // ---------------- 소프트 삭제 ----------------
    /** 현재 상태 조회 (없으면 null) */
    public String findStatus(long lid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT status FROM lectures WHERE Lid = " + lid, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** [V-SQLI] 소프트 삭제: lid 문자열 연결 */
    public void softDelete(long lid) {
        jdbcTemplate.update(
                "UPDATE lectures SET status = '" + DELETED + "', updated = SYSTIMESTAMP WHERE Lid = " + lid);
    }
}
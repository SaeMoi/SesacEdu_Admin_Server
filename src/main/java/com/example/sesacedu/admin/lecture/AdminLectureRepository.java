package com.example.sesacedu.admin.lecture;

import com.example.sesacedu.admin.lecture.dto.LectureItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 강의 관리 JDBC. (lectures 테이블)
 *   삭제는 소프트 삭제 — status='deleted' 로 변경.
 *   목록은 삭제건(deleted)을 항상 제외한다.
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

    // status + keyword(강의명 단어별 부분검색) 필터 공통
    //   삭제건(deleted)은 항상 제외. status 값이 있으면 그 상태만(단, deleted는 무시).
    private static void appendFilters(StringBuilder sql, List<Object> args, String status, String keyword) {
        sql.append("AND l.status <> '" + DELETED + "' ");                 // 삭제건 항상 제외
        if (status != null && !status.isBlank() && !DELETED.equals(status.trim())) {
            sql.append("AND l.status = ? ");
            args.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            for (String token : keyword.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                sql.append("AND LOWER(l.name) LIKE '%' || LOWER(?) || '%' ");
                args.add(token);
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
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, status, keyword);
        sql.append("ORDER BY l.created DESC NULLS LAST, l.Lid DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        args.add(offset);
        args.add(size);
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER, args.toArray());
    }

    public int count(String status, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM lectures l JOIN instructors i ON l.user_id = i.user_id WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, status, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return c == null ? 0 : c;
    }

    // ---------------- 소프트 삭제 ----------------
    /** 현재 상태 조회 (없으면 null) */
    public String findStatus(long lid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT status FROM lectures WHERE Lid = ?", String.class, lid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** 소프트 삭제: status='deleted' 로 변경 */
    public void softDelete(long lid) {
        jdbcTemplate.update(
                "UPDATE lectures SET status = '" + DELETED + "', updated = SYSTIMESTAMP WHERE Lid = ?",
                lid);
    }
}
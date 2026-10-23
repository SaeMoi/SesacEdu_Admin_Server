package com.example.sesacedu.admin.qna;

import com.example.sesacedu.admin.qna.dto.QnaDetail;
import com.example.sesacedu.admin.qna.dto.QnaItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/*
 * ===================== [취약 진단용 - admin/qna] JWT·필수값검증 유지 =====================
 *  (V-SQLI/A03) keyword(제목)·offset/size·iqid·answer·adminUid 바인딩 제거 → 문자열 연결
 *  (V-XSS/A03)  answer 미살균 저장 → 상세 조회 시 그대로 반환되어 저장형 XSS
 * =======================================================================================
 */
@Repository
public class AdminQnaRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminQnaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static String toStatus(Timestamp answeredAt) {
        return answeredAt != null ? "done" : "none";   // done=답변완료, none=답변없음
    }

    private static String toDate(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime().toLocalDate().toString();
    }

    // [V-SQLI] status 리터럴 + keyword 토큰 문자열 연결(제목 부분검색)
    private static void appendFilters(StringBuilder sql, String status, String keyword) {
        if ("done".equals(status))      sql.append("AND iq.answered_at IS NOT NULL ");
        else if ("none".equals(status)) sql.append("AND iq.answered_at IS NULL ");
        if (keyword != null && !keyword.isBlank()) {
            for (String token : keyword.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                sql.append("AND LOWER(iq.title) LIKE LOWER('%" + token + "%') ");
            }
        }
    }

    // ---------------- 목록 ----------------
    private static final RowMapper<QnaItem> ITEM_MAPPER = (rs, n) ->
            new QnaItem(
                    rs.getLong("iqid"),
                    rs.getString("title"),
                    rs.getString("asker_name"),
                    toStatus(rs.getTimestamp("answered_at")),
                    toDate(rs.getTimestamp("created")),
                    toDate(rs.getTimestamp("answered_at")));

    public List<QnaItem> findPage(String status, String keyword, int offset, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT iq.iqid, iq.title, u.nickname AS asker_name, iq.created, iq.answered_at " +
                        "FROM inquiries iq JOIN users u ON iq.user_id = u.user_id WHERE 1=1 ");
        appendFilters(sql, status, keyword);
        sql.append("ORDER BY iq.created DESC NULLS LAST, iq.iqid DESC " +
                "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY");
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER);
    }

    public int count(String status, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM inquiries iq JOIN users u ON iq.user_id = u.user_id WHERE 1=1 ");
        appendFilters(sql, status, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return c == null ? 0 : c;
    }

    // ---------------- 상세 ----------------
    public QnaDetail findDetail(long iqid) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT iq.iqid, iq.title, iq.content, iq.answer, iq.answered_by, iq.answered_at, iq.created, " +
                            "       u.nickname AS asker_name, u.mail AS asker_email, a.uname AS answered_by_name " +
                            "FROM inquiries iq " +
                            "JOIN users u      ON iq.user_id = u.user_id " +
                            "LEFT JOIN users a ON iq.answered_by = a.user_id " +
                            "WHERE iq.iqid = " + iqid,
                    (rs, n) -> {
                        QnaDetail d = new QnaDetail();
                        d.setIqid(rs.getLong("iqid"));
                        d.setTitle(rs.getString("title"));
                        d.setContent(rs.getString("content"));
                        d.setAskerName(rs.getString("asker_name"));
                        d.setAskerEmail(rs.getString("asker_email"));
                        d.setAnswer(rs.getString("answer"));
                        long answeredBy = rs.getLong("answered_by");
                        d.setAnsweredBy(rs.wasNull() ? null : answeredBy);
                        d.setAnsweredByName(rs.getString("answered_by_name"));
                        Timestamp answeredAt = rs.getTimestamp("answered_at");
                        d.setAnsweredAt(toDate(answeredAt));
                        d.setCreatedAt(toDate(rs.getTimestamp("created")));
                        d.setStatus(toStatus(answeredAt));
                        return d;
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ---------------- 상태 확인 ----------------
    public boolean exists(long iqid) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inquiries WHERE iqid = " + iqid, Integer.class);
        return c != null && c > 0;
    }

    public boolean isAnswered(long iqid) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inquiries WHERE iqid = " + iqid + " AND answered_at IS NOT NULL",
                Integer.class);
        return c != null && c > 0;
    }

    // ---------------- 답변 등록/수정 ----------------
    /** [V-SQLI/V-XSS] POST·PUT 공통: answer 문자열 연결 → 저장형 SQLi/XSS */
    public void saveAnswer(long iqid, String answer, long adminUid) {
        jdbcTemplate.update(
                "UPDATE inquiries SET answer = '" + answer + "', answered_by = " + adminUid + ", " +
                        "answered_at = SYSTIMESTAMP, updated = SYSTIMESTAMP WHERE iqid = " + iqid);
    }
}
package com.example.sesacedu.admin.users;

import com.example.sesacedu.admin.users.dto.UserDetail;
import com.example.sesacedu.admin.users.dto.UserItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

/*
 * ===================== [취약 진단용 - admin/users] JWT·필수값검증 유지 =====================
 *  (V-SQLI/A03) keyword(uname/mail)·offset/size·userId·status 바인딩 제거 → 문자열 연결
 *  (V-XSS/A03)  status 미살균 저장 → 조회 시 그대로 반환되어 저장형 XSS
 * ==========================================================================================
 */
@Repository
public class AdminUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static String toRole(int auth) {
        if (auth == 3) return "ADMIN";
        if (auth == 2) return "INSTRUCTOR";
        return "GENERAL";
    }

    private static String toDate(Date d) {
        return d == null ? null : d.toLocalDate().toString();
    }

    // [V-SQLI] keyword 토큰을 문자열로 직접 연결(uname/mail 부분검색)
    private static void appendFilters(StringBuilder sql, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            for (String token : keyword.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                sql.append("AND (LOWER(u.uname) LIKE LOWER('%" + token + "%') " +
                        "OR LOWER(u.mail) LIKE LOWER('%" + token + "%')) ");
            }
        }
    }

    // ---------------- 목록 ----------------
    private static final RowMapper<UserItem> ITEM_MAPPER = (rs, n) ->
            new UserItem(
                    rs.getLong("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    toRole(rs.getInt("auth")),
                    rs.getString("status"));

    public List<UserItem> findPage(String keyword, int offset, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.user_id, u.uname AS name, u.mail AS email, u.auth, u.status " +
                        "FROM users u WHERE 1=1 ");
        appendFilters(sql, keyword);
        sql.append("ORDER BY u.created DESC NULLS LAST, u.user_id DESC " +
                "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY");
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER);
    }

    public int count(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1 ");
        appendFilters(sql, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return c == null ? 0 : c;
    }

    // ---------------- 상세 ----------------
    public UserDetail findDetail(long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT user_id, uname, nickname, mail, phone, b_date, address, auth, status " +
                            "FROM users WHERE user_id = " + userId,
                    (rs, n) -> {
                        UserDetail d = new UserDetail();
                        d.setUserId(rs.getLong("user_id"));
                        d.setName(rs.getString("uname"));
                        d.setNickname(rs.getString("nickname"));
                        d.setEmail(rs.getString("mail"));
                        d.setPhone(rs.getString("phone"));
                        d.setBirthDate(toDate(rs.getDate("b_date")));
                        d.setAddress(rs.getString("address"));
                        d.setRole(toRole(rs.getInt("auth")));
                        d.setStatus(rs.getString("status"));
                        return d;
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ---------------- 상태 변경 ----------------
    public boolean exists(long userId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = " + userId, Integer.class);
        return c != null && c > 0;
    }

    /** [V-SQLI/V-XSS] 상태 변경: status 문자열 연결 → 저장형 SQLi/XSS */
    public void updateStatus(long userId, String status) {
        jdbcTemplate.update(
                "UPDATE users SET status = '" + status + "', updated = SYSTIMESTAMP " +
                        "WHERE user_id = " + userId);
    }
}
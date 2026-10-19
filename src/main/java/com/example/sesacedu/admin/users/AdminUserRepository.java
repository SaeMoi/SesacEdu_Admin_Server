package com.example.sesacedu.admin.users;

import com.example.sesacedu.admin.users.dto.UserDetail;
import com.example.sesacedu.admin.users.dto.UserItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 회원 관리 JDBC. (users 테이블)
 *   role 은 users.auth 파생: 3=ADMIN, 2=INSTRUCTOR, 그 외 GENERAL.
 *   status 는 users.status 컬럼(active/restricted) 그대로.
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

    // keyword: 공백으로 나눈 각 단어가 이름(uname) 또는 이메일(mail)에 포함되면 매칭
    private static void appendFilters(StringBuilder sql, List<Object> args, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            for (String token : keyword.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                sql.append("AND (LOWER(u.uname) LIKE '%' || LOWER(?) || '%' " +
                        "OR LOWER(u.mail) LIKE '%' || LOWER(?) || '%') ");
                args.add(token);
                args.add(token);
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
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, keyword);
        sql.append("ORDER BY u.created DESC NULLS LAST, u.user_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        args.add(offset);
        args.add(size);
        return jdbcTemplate.query(sql.toString(), ITEM_MAPPER, args.toArray());
    }

    public int count(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        appendFilters(sql, args, keyword);
        Integer c = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return c == null ? 0 : c;
    }

    // ---------------- 상세 ----------------
    public UserDetail findDetail(long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT user_id, uname, nickname, mail, phone, b_date, address, auth, status " +
                            "FROM users WHERE user_id = ?",
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
                    }, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // ---------------- 상태 변경 ----------------
    public boolean exists(long userId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = ?", Integer.class, userId);
        return c != null && c > 0;
    }

    /** 상태 변경: users.status 갱신 (reason 은 저장하지 않음) */
    public void updateStatus(long userId, String status) {
        jdbcTemplate.update(
                "UPDATE users SET status = ?, updated = SYSTIMESTAMP WHERE user_id = ?",
                status, userId);
    }
}
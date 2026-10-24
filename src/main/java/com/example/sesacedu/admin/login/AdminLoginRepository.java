package com.example.sesacedu.admin.login;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 관리자 로그인 JDBC. (users 테이블)
 *   - 로그인 아이디 = mail (UNIQUE)
 *   - 비밀번호   = pw (MD5 해시 저장. 서비스에서 해시해서 넘어온다)
 *   - 관리자     = auth = 3
 *   - 활성 계정  = status = 'active'
 *
 * ⚠️ [실습용 취약] 바인딩(?)을 제거하고 mail/pw 를 문자열로 직접 연결한다.
 *    → mail(id) 입력으로 SQL Injection 로그인 우회 가능 (예: id = x' OR '1'='1' --).
 *    교육용이며 운영에서는 절대 사용 금지.
 */
@Repository
public class AdminLoginRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminLoginRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 로그인에 성공한 관리자 정보 */
    public static class Admin {
        public final long userId;
        public final int auth;
        public Admin(long userId, int auth) {
            this.userId = userId;
            this.auth = auth;
        }
    }

    /**
     * mail + pw(MD5) 가 일치하고, auth=3(관리자) + status='active' 인 계정을 찾는다.
     * 없으면 null (아이디·비번 불일치 / 관리자 아님 / 비활성 모두 포함 → 사용자 열거 방지).
     */
    public Admin findActiveAdmin(String mail, String pwMd5) {
        try {
            // [실습용 취약] 바인딩 제거 → 문자열 연결 (SQL Injection)
            String sql = "SELECT user_id, auth FROM users " +
                    "WHERE mail = '" + mail + "' AND pw = '" + pwMd5 + "' " +
                    "AND auth = 3 AND status = 'active'";
            return jdbcTemplate.queryForObject(
                    sql,
                    (rs, n) -> new Admin(rs.getLong("user_id"), rs.getInt("auth")));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
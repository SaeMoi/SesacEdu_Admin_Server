package com.example.sesacedu.admin.dashboard;

import com.example.sesacedu.admin.dashboard.dto.DashboardResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 관리자 대시보드 JDBC. 대기/미처리 건수를 dual 대상 스칼라 서브쿼리로 한 번에 집계.
 */
@Repository
public class AdminDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardResponse getSummary() {
        String sql =
                "SELECT " +
                        "  (SELECT COUNT(*) FROM instructors WHERE approved_at IS NULL AND rejected_at IS NULL) AS instructor_pending, " +
                        "  (SELECT COUNT(*) FROM settlements WHERE status = 'requested') AS settlement_pending, " +
                        "  (SELECT COUNT(*) FROM inquiries WHERE answered_at IS NULL) AS unanswered_inquiries " +
                        "FROM dual";
        return jdbcTemplate.queryForObject(sql, (rs, n) -> new DashboardResponse(
                rs.getLong("instructor_pending"),
                rs.getLong("settlement_pending"),
                rs.getLong("unanswered_inquiries")));
    }
}
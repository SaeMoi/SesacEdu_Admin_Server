package com.example.sesacedu.admin.dashboard;

import com.example.sesacedu.admin.dashboard.dto.DashboardResponse;
import org.springframework.stereotype.Service;

/**
 * 관리자 대시보드 로직. 집계 결과 조회.
 * 권한(관리자)은 컨트롤러에서 JWT role("ADMIN")로 확인.
 */
@Service
public class AdminDashboardService {

    private final AdminDashboardRepository repository;

    public AdminDashboardService(AdminDashboardRepository repository) {
        this.repository = repository;
    }

    public DashboardResponse getSummary() {
        return repository.getSummary();
    }
}
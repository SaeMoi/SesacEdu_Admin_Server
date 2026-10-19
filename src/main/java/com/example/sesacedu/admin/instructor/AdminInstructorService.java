package com.example.sesacedu.admin.instructor;

import com.example.sesacedu.admin.instructor.dto.InstructorApplicationDetail;
import com.example.sesacedu.admin.instructor.dto.InstructorApplicationItem;
import com.example.sesacedu.admin.instructor.dto.InstructorApplicationPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 강사 신청 로직. 목록/상세 조회 + 승인/거절.
 * 권한(관리자)은 컨트롤러에서 JWT role("ADMIN")로 확인.
 */
@Service
public class AdminInstructorService {

    private static final int PAGE_SIZE = 5;   // 페이지당 고정

    private final AdminInstructorRepository repository;

    public AdminInstructorService(AdminInstructorRepository repository) {
        this.repository = repository;
    }

    /** 목록 (status·keyword 필터, 페이지네이션) */
    public InstructorApplicationPageResponse getApplications(String status, String keyword, int page) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<InstructorApplicationItem> content = repository.findPage(status, keyword, offset, PAGE_SIZE);
        int total = repository.count(status, keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);

        return new InstructorApplicationPageResponse(content, safePage, PAGE_SIZE, total, totalPages);
    }

    /** 상세. 없으면 404. */
    public InstructorApplicationDetail getDetail(long userId) {
        InstructorApplicationDetail d = repository.findDetail(userId);
        if (d == null) {
            throw new NotFound();
        }
        return d;
    }

    /** 승인: approved_at 기록 + auth=2. 없으면 404, 이미 처리면 409. */
    @Transactional
    public void approve(long userId) {
        if (!repository.exists(userId)) throw new NotFound();
        if (repository.isProcessed(userId)) throw new AlreadyProcessed();
        repository.approve(userId);
    }

    /** 거절: rejected_at + reject_reason 기록. 없으면 404, 이미 처리면 409. */
    @Transactional
    public void reject(long userId, String reason) {
        if (!repository.exists(userId)) throw new NotFound();
        if (repository.isProcessed(userId)) throw new AlreadyProcessed();
        repository.reject(userId, reason);
    }

    /** 신청 정보 없음 (404) */
    public static class NotFound extends RuntimeException {
    }

    /** 이미 승인/거절된 신청 (409) */
    public static class AlreadyProcessed extends RuntimeException {
    }
}
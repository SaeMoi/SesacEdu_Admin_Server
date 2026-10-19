package com.example.sesacedu.admin.lecture;

import com.example.sesacedu.admin.lecture.dto.LectureItem;
import com.example.sesacedu.admin.lecture.dto.LecturePageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 강의 관리 로직. 목록 조회 + 소프트 삭제.
 * 권한(관리자)은 컨트롤러에서 JWT role("ADMIN")로 확인.
 */
@Service
public class AdminLectureService {

    private static final int PAGE_SIZE = 5;   // 페이지당 고정
    private static final String DELETED = "deleted";

    private final AdminLectureRepository repository;

    public AdminLectureService(AdminLectureRepository repository) {
        this.repository = repository;
    }

    /** 목록 (status·keyword 필터, 페이지네이션) */
    public LecturePageResponse getList(String status, String keyword, int page) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<LectureItem> content = repository.findPage(status, keyword, offset, PAGE_SIZE);
        int total = repository.count(status, keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);

        return new LecturePageResponse(content, safePage, PAGE_SIZE, total, totalPages);
    }

    /** 소프트 삭제. 없으면 404, 이미 삭제면 409. */
    @Transactional
    public void delete(long lid) {
        String status = repository.findStatus(lid);
        if (status == null) throw new NotFound();
        if (DELETED.equals(status)) throw new AlreadyDeleted();
        repository.softDelete(lid);
    }

    /** 강의 정보 없음 (404) */
    public static class NotFound extends RuntimeException {
    }

    /** 이미 삭제된 강의 (409) */
    public static class AlreadyDeleted extends RuntimeException {
    }
}
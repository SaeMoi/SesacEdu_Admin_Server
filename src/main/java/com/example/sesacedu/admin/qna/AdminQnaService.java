package com.example.sesacedu.admin.qna;

import com.example.sesacedu.admin.qna.dto.QnaDetail;
import com.example.sesacedu.admin.qna.dto.QnaItem;
import com.example.sesacedu.admin.qna.dto.QnaPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 서비스 문의(inquiries) 로직. 목록/상세 + 답변 등록(POST)/수정(PUT).
 * 권한(관리자)은 컨트롤러에서 JWT role("ADMIN")로 확인.
 */
@Service
public class AdminQnaService {

    private static final int PAGE_SIZE = 5;   // 페이지당 고정

    private final AdminQnaRepository repository;

    public AdminQnaService(AdminQnaRepository repository) {
        this.repository = repository;
    }

    /** 목록 (status·keyword 필터, 페이지네이션) */
    public QnaPageResponse getList(String status, String keyword, int page) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<QnaItem> content = repository.findPage(status, keyword, offset, PAGE_SIZE);
        int total = repository.count(status, keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);

        return new QnaPageResponse(content, safePage, PAGE_SIZE, total, totalPages);
    }

    /** 상세. 없으면 404. */
    public QnaDetail getDetail(long iqid) {
        QnaDetail d = repository.findDetail(iqid);
        if (d == null) throw new NotFound();
        return d;
    }

    /** 답변 최초 등록(POST). 문의 없으면 404, 이미 답변돼 있으면 409(수정은 PUT). */
    @Transactional
    public void createAnswer(long iqid, String answer, long adminUid) {
        if (!repository.exists(iqid)) throw new NotFound();
        if (repository.isAnswered(iqid)) throw new Conflict("이미 답변된 문의입니다. 수정은 PUT을 사용하세요.");
        repository.saveAnswer(iqid, answer, adminUid);
    }

    /** 답변 수정(PUT). 문의 없으면 404, 답변이 아직 없으면 409(등록은 POST). */
    @Transactional
    public void updateAnswer(long iqid, String answer, long adminUid) {
        if (!repository.exists(iqid)) throw new NotFound();
        if (!repository.isAnswered(iqid)) throw new Conflict("아직 답변이 없습니다. 등록은 POST를 사용하세요.");
        repository.saveAnswer(iqid, answer, adminUid);
    }

    /** 문의 정보 없음 (404) */
    public static class NotFound extends RuntimeException {
    }

    /** 상태 충돌 (409) */
    public static class Conflict extends RuntimeException {
        public Conflict(String message) { super(message); }
    }
}
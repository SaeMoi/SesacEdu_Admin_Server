package com.example.sesacedu.admin.settlement;

import com.example.sesacedu.admin.settlement.dto.SettlementDetailResponse;
import com.example.sesacedu.admin.settlement.dto.SettlementItem;
import com.example.sesacedu.admin.settlement.dto.SettlementLectureItem;
import com.example.sesacedu.admin.settlement.dto.SettlementPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 정산 로직. 목록 조회 + 승인/거절.
 * 권한(관리자)은 컨트롤러에서 JWT role("ADMIN")로 확인.
 */
@Service
public class AdminSettlementService {

    private static final int PAGE_SIZE = 5;   // 페이지당 고정

    private final AdminSettlementRepository repository;

    public AdminSettlementService(AdminSettlementRepository repository) {
        this.repository = repository;
    }

    /** 목록 (status·period 필터, 페이지네이션) */
    public SettlementPageResponse getList(String status, String period, int page) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<SettlementItem> content = repository.findPage(status, period, offset, PAGE_SIZE);
        int total = repository.count(status, period);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);

        return new SettlementPageResponse(content, safePage, PAGE_SIZE, total, totalPages);
    }

    /** 상세: 헤더(저장값) + 강의별 매출 내역(페이지네이션). 없으면 404. */
    public SettlementDetailResponse getDetail(long sid, int page) {
        AdminSettlementRepository.HeaderRow header = repository.findHeader(sid);
        if (header == null) throw new NotFound();

        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;
        String period = header.body.getPeriod();

        List<SettlementLectureItem> items =
                repository.findLectureBreakdown(header.instructorUid, period, offset, PAGE_SIZE);
        int total = repository.countLectureBreakdown(header.instructorUid, period);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);

        SettlementDetailResponse body = header.body;
        body.setItems(items);
        body.setPage(safePage);
        body.setSize(PAGE_SIZE);
        body.setTotalElements(total);
        body.setTotalPages(totalPages);
        return body;
    }

    /** 승인: requested 상태만 가능. 없으면 404, 이미 처리면 409. */
    @Transactional
    public void approve(long sid, long adminUid) {
        requireRequested(sid);
        repository.approve(sid, adminUid);
    }

    /** 거절: requested 상태만 가능. 없으면 404, 이미 처리면 409. */
    @Transactional
    public void reject(long sid, String reason) {
        requireRequested(sid);
        repository.reject(sid, reason);
    }

    /** 신청(requested) 상태 검증. 없으면 404, 그 외 상태면 409. */
    private void requireRequested(long sid) {
        String status = repository.findStatus(sid);
        if (status == null) throw new NotFound();
        if (!"requested".equals(status)) throw new AlreadyProcessed();
    }

    /** 정산 정보 없음 (404) */
    public static class NotFound extends RuntimeException {
    }

    /** 이미 승인/거절/지급된 정산 (409) */
    public static class AlreadyProcessed extends RuntimeException {
    }
}
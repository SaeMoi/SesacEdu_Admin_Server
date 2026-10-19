package com.example.sesacedu.admin.settlement.dto;

import java.util.List;

/** 정산 목록 페이지 응답. 페이지당 5개 고정. */
public class SettlementPageResponse {

    private List<SettlementItem> content;
    private int page;           // 현재 페이지 (1부터)
    private int size;           // 페이지 크기 (5 고정)
    private long totalElements; // 전체 건수
    private int totalPages;     // 전체 페이지 수

    public SettlementPageResponse() {
    }

    public SettlementPageResponse(List<SettlementItem> content, int page, int size,
                                  long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<SettlementItem> getContent() { return content; }
    public void setContent(List<SettlementItem> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
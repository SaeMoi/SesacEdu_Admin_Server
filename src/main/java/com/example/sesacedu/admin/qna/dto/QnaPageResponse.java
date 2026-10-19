package com.example.sesacedu.admin.qna.dto;

import java.util.List;

/** 문의 목록 페이지 응답. 페이지당 5개 고정. */
public class QnaPageResponse {

    private List<QnaItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public QnaPageResponse() {
    }

    public QnaPageResponse(List<QnaItem> content, int page, int size,
                           long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<QnaItem> getContent() { return content; }
    public void setContent(List<QnaItem> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
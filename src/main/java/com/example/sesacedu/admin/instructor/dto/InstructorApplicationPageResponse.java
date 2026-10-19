package com.example.sesacedu.admin.instructor.dto;

import java.util.List;

/** 강사 신청 목록(페이지네이션) 응답. size 는 백엔드 고정값(5). */
public class InstructorApplicationPageResponse {

    private List<InstructorApplicationItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public InstructorApplicationPageResponse() {
    }

    public InstructorApplicationPageResponse(List<InstructorApplicationItem> content, int page,
                                             int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<InstructorApplicationItem> getContent() { return content; }
    public void setContent(List<InstructorApplicationItem> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
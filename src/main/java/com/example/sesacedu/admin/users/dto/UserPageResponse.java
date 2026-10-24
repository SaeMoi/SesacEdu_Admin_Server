package com.example.sesacedu.admin.users.dto;

import java.util.List;

/** 회원 목록 페이지 응답. 페이지당 5개 고정. */
public class UserPageResponse {

    private List<UserItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public UserPageResponse() {
    }

    public UserPageResponse(List<UserItem> content, int page, int size,
                            long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<UserItem> getContent() { return content; }
    public void setContent(List<UserItem> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
package com.example.sesacedu.admin.settlement.dto;

import java.util.List;

/**
 * 정산 상세 응답.
 *   - 상단: 정산 헤더 (settlements 저장값 그대로)
 *   - 하단: 강의별 매출 내역 (페이지당 5개 고정)
 */
public class SettlementDetailResponse {

    // ---- 헤더 ----
    private long sid;
    private String iname;        // 강사명 (instructors.iname)
    private String period;       // 정산월 (YYYY-MM)
    private long revenue;        // 매출 (저장값)
    private long deduction;      // 공제 (저장값)
    private long amount;         // 정산액 (저장값)
    private String status;       // requested / approved / rejected / paid
    private String bank;         // 지급 은행
    private String account;      // 지급 계좌
    private String requestedAt;  // 신청일 (nullable)
    private String approvedAt;   // 승인일 (nullable)
    private Long approvedBy;     // 승인 관리자 user_id (nullable)
    private String rejectedAt;   // 거절일 (nullable)
    private String rejectReason; // 반려 사유 (nullable)
    private String paidAt;       // 지급일 (nullable)

    // ---- 강의별 내역 (페이지네이션) ----
    private List<SettlementLectureItem> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public SettlementDetailResponse() {
    }

    public long getSid() { return sid; }
    public void setSid(long sid) { this.sid = sid; }

    public String getIname() { return iname; }
    public void setIname(String iname) { this.iname = iname; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public long getRevenue() { return revenue; }
    public void setRevenue(long revenue) { this.revenue = revenue; }

    public long getDeduction() { return deduction; }
    public void setDeduction(long deduction) { this.deduction = deduction; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getRequestedAt() { return requestedAt; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public String getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(String rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }

    public List<SettlementLectureItem> getItems() { return items; }
    public void setItems(List<SettlementLectureItem> items) { this.items = items; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
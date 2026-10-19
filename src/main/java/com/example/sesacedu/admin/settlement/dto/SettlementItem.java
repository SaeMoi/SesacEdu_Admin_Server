package com.example.sesacedu.admin.settlement.dto;

/** 정산 목록의 한 행. */
public class SettlementItem {

    private long sid;
    private String iname;        // 강사명 (instructors.iname)
    private String period;       // 정산월 (YYYY-MM)
    private long revenue;        // 매출
    private long deduction;      // 공제
    private long amount;         // 정산액
    private String status;       // requested / approved / rejected / paid
    private String approvedAt;   // 승인일 (nullable)
    private String rejectedAt;   // 거절일 (nullable)
    private String paidAt;       // 지급일 (nullable)

    public SettlementItem() {
    }

    public SettlementItem(long sid, String iname, String period, long revenue, long deduction,
                          long amount, String status, String approvedAt, String rejectedAt, String paidAt) {
        this.sid = sid;
        this.iname = iname;
        this.period = period;
        this.revenue = revenue;
        this.deduction = deduction;
        this.amount = amount;
        this.status = status;
        this.approvedAt = approvedAt;
        this.rejectedAt = rejectedAt;
        this.paidAt = paidAt;
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

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(String rejectedAt) { this.rejectedAt = rejectedAt; }

    public String getPaidAt() { return paidAt; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
}
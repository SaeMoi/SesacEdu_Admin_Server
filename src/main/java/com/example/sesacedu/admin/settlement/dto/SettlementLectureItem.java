package com.example.sesacedu.admin.settlement.dto;

/** 정산 상세의 강의별 매출 한 줄. */
public class SettlementLectureItem {

    private Long lid;          // 강의 ID (삭제 강의는 조회에서 제외되므로 항상 존재)
    private String lname;      // 강의명
    private long salesCount;   // 판매 건수 (결제완료 주문 품목 수)
    private long revenue;      // 매출 합계 (품목 price 합)

    public SettlementLectureItem() {
    }

    public SettlementLectureItem(Long lid, String lname, long salesCount, long revenue) {
        this.lid = lid;
        this.lname = lname;
        this.salesCount = salesCount;
        this.revenue = revenue;
    }

    public Long getLid() { return lid; }
    public void setLid(Long lid) { this.lid = lid; }

    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }

    public long getSalesCount() { return salesCount; }
    public void setSalesCount(long salesCount) { this.salesCount = salesCount; }

    public long getRevenue() { return revenue; }
    public void setRevenue(long revenue) { this.revenue = revenue; }
}
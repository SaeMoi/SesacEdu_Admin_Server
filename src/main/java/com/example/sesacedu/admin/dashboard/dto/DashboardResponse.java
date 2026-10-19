package com.example.sesacedu.admin.dashboard.dto;

/** 관리자 대시보드 집계 응답. (대기/미처리 건수) */
public class DashboardResponse {

    private long instructorPending;    // 강사 신청 대기 수 (미승인·미거절)
    private long settlementPending;    // 정산 대기 수 (status='requested')
    private long unansweredInquiries;  // 미답변 서비스 문의 수 (answered_at 없음)

    public DashboardResponse() {
    }

    public DashboardResponse(long instructorPending, long settlementPending, long unansweredInquiries) {
        this.instructorPending = instructorPending;
        this.settlementPending = settlementPending;
        this.unansweredInquiries = unansweredInquiries;
    }

    public long getInstructorPending() { return instructorPending; }
    public void setInstructorPending(long instructorPending) { this.instructorPending = instructorPending; }

    public long getSettlementPending() { return settlementPending; }
    public void setSettlementPending(long settlementPending) { this.settlementPending = settlementPending; }

    public long getUnansweredInquiries() { return unansweredInquiries; }
    public void setUnansweredInquiries(long unansweredInquiries) { this.unansweredInquiries = unansweredInquiries; }
}
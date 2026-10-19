package com.example.sesacedu.admin.settlement.dto;

/** 정산 거절(반려) 요청 바디. */
public class RejectRequest {

    private String reason;  // 반려 사유 (필수, 최대 500자)

    public RejectRequest() {
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
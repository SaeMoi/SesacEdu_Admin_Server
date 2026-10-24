package com.example.sesacedu.admin.users.dto;

/** 회원 상태 변경 요청 바디. */
public class UserStatusUpdateRequest {

    private String status;  // 필수: active / restricted
    private String reason;  // 선택: 변경 사유 (현재 저장하지 않음)

    public UserStatusUpdateRequest() {
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
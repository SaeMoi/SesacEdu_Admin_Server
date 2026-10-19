package com.example.sesacedu.admin.instructor.dto;

/** 강사 신청 목록의 한 행. */
public class InstructorApplicationItem {

    private long userId;
    private String iname;      // 공개 강사명 (instructors.iname)
    private String field;      // 전문 분야
    private String appliedAt;  // 신청일 (YYYY-MM-DD)
    private String status;     // pending / approved / rejected (파생)

    public InstructorApplicationItem() {
    }

    public InstructorApplicationItem(long userId, String iname, String field,
                                     String appliedAt, String status) {
        this.userId = userId;
        this.iname = iname;
        this.field = field;
        this.appliedAt = appliedAt;
        this.status = status;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getIname() { return iname; }
    public void setIname(String iname) { this.iname = iname; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getAppliedAt() { return appliedAt; }
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
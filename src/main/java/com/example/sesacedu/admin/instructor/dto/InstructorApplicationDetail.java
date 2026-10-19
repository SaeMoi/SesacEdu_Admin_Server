package com.example.sesacedu.admin.instructor.dto;

/** 강사 신청 상세. */
public class InstructorApplicationDetail {

    private long userId;
    private String iname;           // 공개 강사명 (instructors.iname)
    private String email;           // users.mail
    private String bio;             // 한 줄 소개
    private String career;          // 강사 소개 (CLOB)
    private String field;           // 전문 분야
    private String careerFilename;  // 증빙 원본 파일명
    private String careerFileUrl;   // 증빙 다운로드 URL
    private String status;          // pending / approved / rejected
    private String appliedAt;       // 신청일 (YYYY-MM-DD)

    public InstructorApplicationDetail() {
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getIname() { return iname; }
    public void setIname(String iname) { this.iname = iname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCareer() { return career; }
    public void setCareer(String career) { this.career = career; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getCareerFilename() { return careerFilename; }
    public void setCareerFilename(String careerFilename) { this.careerFilename = careerFilename; }

    public String getCareerFileUrl() { return careerFileUrl; }
    public void setCareerFileUrl(String careerFileUrl) { this.careerFileUrl = careerFileUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAppliedAt() { return appliedAt; }
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }
}
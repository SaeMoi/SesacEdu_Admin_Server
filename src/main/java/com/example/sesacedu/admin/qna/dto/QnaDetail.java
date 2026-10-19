package com.example.sesacedu.admin.qna.dto;

/** 문의 상세. (inquiries) */
public class QnaDetail {

    private long iqid;
    private String title;           // 제목
    private String content;         // 문의 본문 (CLOB)
    private String askerName;       // 문의자 (users.nickname)
    private String askerEmail;      // 문의자 이메일 (users.mail)
    private String answer;          // 답변 본문 (CLOB, nullable)
    private Long answeredBy;        // 답변 관리자 user_id (nullable)
    private String answeredByName;  // 답변 관리자 이름 (nullable)
    private String answeredAt;      // 답변일 (nullable)
    private String createdAt;       // 문의일
    private String status;          // none(답변없음) / done(답변완료)

    public QnaDetail() {
    }

    public long getIqid() { return iqid; }
    public void setIqid(long iqid) { this.iqid = iqid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAskerName() { return askerName; }
    public void setAskerName(String askerName) { this.askerName = askerName; }

    public String getAskerEmail() { return askerEmail; }
    public void setAskerEmail(String askerEmail) { this.askerEmail = askerEmail; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Long getAnsweredBy() { return answeredBy; }
    public void setAnsweredBy(Long answeredBy) { this.answeredBy = answeredBy; }

    public String getAnsweredByName() { return answeredByName; }
    public void setAnsweredByName(String answeredByName) { this.answeredByName = answeredByName; }

    public String getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(String answeredAt) { this.answeredAt = answeredAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
package com.example.sesacedu.admin.qna.dto;

/** 문의 목록의 한 행. (inquiries) */
public class QnaItem {

    private long iqid;
    private String title;       // 문의 제목
    private String askerName;   // 문의자 (users.nickname)
    private String status;      // none(답변없음) / done(답변완료) (answered_at 파생)
    private String createdAt;   // 문의일 (YYYY-MM-DD)
    private String answeredAt;  // 답변일 (nullable)

    public QnaItem() {
    }

    public QnaItem(long iqid, String title, String askerName, String status,
                   String createdAt, String answeredAt) {
        this.iqid = iqid;
        this.title = title;
        this.askerName = askerName;
        this.status = status;
        this.createdAt = createdAt;
        this.answeredAt = answeredAt;
    }

    public long getIqid() { return iqid; }
    public void setIqid(long iqid) { this.iqid = iqid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAskerName() { return askerName; }
    public void setAskerName(String askerName) { this.askerName = askerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(String answeredAt) { this.answeredAt = answeredAt; }
}
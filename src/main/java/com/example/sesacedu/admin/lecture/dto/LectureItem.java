package com.example.sesacedu.admin.lecture.dto;

/** 강의 목록의 한 행. */
public class LectureItem {

    private long lid;
    private String name;            // 강의명
    private String instructorName;  // 강사명 (instructors.iname)
    private String category;        // 카테고리
    private String level;           // 난이도 (lectures.Llevel)
    private long price;             // 가격 (lectures.Lprice)
    private String status;          // draft / published / deleted 등
    private String createdAt;       // 등록일 (YYYY-MM-DD)

    public LectureItem() {
    }

    public LectureItem(long lid, String name, String instructorName, String category,
                       String level, long price, String status, String createdAt) {
        this.lid = lid;
        this.name = name;
        this.instructorName = instructorName;
        this.category = category;
        this.level = level;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getLid() { return lid; }
    public void setLid(long lid) { this.lid = lid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
package com.example.sesacedu.admin.users.dto;

/** 회원 목록의 한 행. */
public class UserItem {

    private long userId;
    private String name;    // 실명 (users.uname)
    private String email;   // users.mail
    private String role;    // GENERAL / INSTRUCTOR / ADMIN (users.auth 파생)
    private String status;  // active / restricted (users.status)

    public UserItem() {
    }

    public UserItem(long userId, String name, String email, String role, String status) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
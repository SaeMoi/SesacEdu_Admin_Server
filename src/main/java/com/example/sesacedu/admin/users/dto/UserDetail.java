package com.example.sesacedu.admin.users.dto;

/** 회원 상세. */
public class UserDetail {

    private long userId;
    private String name;       // 실명 (users.uname)
    private String nickname;   // 닉네임 (users.nickname)
    private String email;      // users.mail
    private String phone;      // users.phone
    private String birthDate;  // users.b_date (YYYY-MM-DD)
    private String address;    // users.address
    private String role;       // GENERAL / INSTRUCTOR / ADMIN (users.auth 파생)
    private String status;     // active / restricted

    public UserDetail() {
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
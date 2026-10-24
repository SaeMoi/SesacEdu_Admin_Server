package com.example.sesacedu.admin.login.dto;

/** 관리자 로그인 요청. id = 로그인 아이디(= users.mail), pw = 평문 비밀번호(서버에서 MD5 해시 비교). */
public class LoginRequest {

    private String id;   // users.mail
    private String pw;   // 평문

    public LoginRequest() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPw() { return pw; }
    public void setPw(String pw) { this.pw = pw; }
}
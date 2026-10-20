package com.example.sesacedu.admin.login.dto;

/** 관리자 로그인 응답. token 을 Authorization: Bearer 로 쓰면 된다. */
public class LoginResponse {

    private long userId;
    private String role;                 // ADMIN
    private String token;                // JWT (Bearer 제외)
    private String authorizationHeader;  // "Bearer xxx..." (그대로 헤더에 사용)

    public LoginResponse() {
    }

    public LoginResponse(long userId, String role, String token, String authorizationHeader) {
        this.userId = userId;
        this.role = role;
        this.token = token;
        this.authorizationHeader = authorizationHeader;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getAuthorizationHeader() { return authorizationHeader; }
    public void setAuthorizationHeader(String authorizationHeader) { this.authorizationHeader = authorizationHeader; }
}
package com.example.sesacedu.admin.login;

import com.example.sesacedu.admin.login.dto.LoginResponse;
import com.example.sesacedu.global.config.JwtUtil;
import com.example.sesacedu.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 관리자 로그인 로직.
 *   1) 평문 pw 를 MD5 로 해시
 *   2) mail + pw(MD5) + auth=3 + status=active 인 계정 조회
 *   3) 있으면 JwtUtil 로 로그인 토큰 발급, 없으면 401
 */
@Service
public class AdminLoginService {

    private final AdminLoginRepository repository;
    private final JwtUtil jwtUtil;

    public AdminLoginService(AdminLoginRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String id, String pw) {
        String pwMd5 = md5(pw);
        AdminLoginRepository.Admin admin = repository.findActiveAdmin(id, pwMd5);
        if (admin == null) {
            // 아이디·비번 불일치 / 관리자 아님 / 비활성 전부 동일 메시지 (계정 존재 여부 노출 방지)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED",
                    "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        String token = jwtUtil.createLoginToken(admin.userId, admin.auth);
        return new LoginResponse(
                admin.userId,
                JwtUtil.roleOf(admin.auth),   // "ADMIN"
                token,
                jwtUtil.toHeaderValue(token));
    }

    /** MD5 → 소문자 16진 문자열. (저장된 해시가 소문자 hex 라고 가정) */
    private static String md5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                    .append(Character.forDigit(b & 0xF, 16));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 미지원", e);
        }
    }
}
package com.example.sesacedu.admin.login;

import com.example.sesacedu.admin.login.dto.LoginRequest;
import com.example.sesacedu.admin.login.dto.LoginResponse;
import com.example.sesacedu.global.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 로그인. 인증 없이 접근하는 공개 엔드포인트(토큰 발급용).
 */
@Tag(name = "관리자 - 로그인")
@RestController
@RequestMapping("/admin")
public class AdminLoginController {

    private final AdminLoginService service;

    public AdminLoginController(AdminLoginService service) {
        this.service = service;
    }

    @Operation(summary = "관리자 로그인",
            description = "id(=이메일)·pw 를 받아 MD5 해시로 비교하고, 관리자(auth=3)·활성 계정이면 JWT 를 발급한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (토큰 발급)"),
            @ApiResponse(responseCode = "400", description = "id/pw 누락"),
            @ApiResponse(responseCode = "401", description = "로그인 실패 (불일치/관리자 아님/비활성)"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody(required = false) LoginRequest req) {
        if (req == null || isBlank(req.getId()) || isBlank(req.getPw())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "아이디와 비밀번호를 입력해 주세요.");
        }
        return ResponseEntity.ok(service.login(req.getId().trim(), req.getPw()));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
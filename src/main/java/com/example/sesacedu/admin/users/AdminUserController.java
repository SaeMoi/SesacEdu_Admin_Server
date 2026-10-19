package com.example.sesacedu.admin.users;

import com.example.sesacedu.admin.users.dto.UserDetail;
import com.example.sesacedu.admin.users.dto.UserPageResponse;
import com.example.sesacedu.admin.users.dto.UserStatusUpdateRequest;
import com.example.sesacedu.global.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

@Tag(name = "관리자 - 회원 관리")
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final Set<String> ALLOWED_STATUS = Set.of("active", "restricted");

    private final AdminUserService service;
    private final JwtUtil jwtUtil;

    public AdminUserController(AdminUserService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    private void requireAdmin(HttpServletRequest request) {
        Claims claims = jwtUtil.parse(request);     // 401: 토큰 없음/만료/위조
        jwtUtil.requireRole(claims, "ADMIN");       // 403: 관리자 아님
    }

    // ---------------- 목록 ----------------
    @Operation(summary = "회원 목록 조회",
            description = "이름·이메일 검색으로 조회한다. 페이지당 5개 고정.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<UserPageResponse> list(
            @Parameter(description = "이름/이메일 검색 (공백으로 나눈 단어가 모두 포함되면 매칭)")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "페이지 번호 (기본 1)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(service.getList(keyword, page));
    }

    // ---------------- 상세 ----------------
    @Operation(summary = "회원 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetail> detail(
            @PathVariable("userId") long userId, HttpServletRequest request) {
        requireAdmin(request);
        try {
            return ResponseEntity.ok(service.getDetail(userId));
        } catch (AdminUserService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보 없음");
        }
    }

    // ---------------- 상태 변경 ----------------
    @Operation(summary = "회원 상태 변경", description = "회원 상태를 active / restricted 로 변경한다. reason 은 저장하지 않음.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "status 누락/잘못됨"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "회원 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable("userId") long userId,
            @RequestBody UserStatusUpdateRequest req,
            HttpServletRequest request) {
        requireAdmin(request);

        String status = req == null ? null : req.getStatus();
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status 값이 필요합니다.");
        }
        status = status.trim();
        if (!ALLOWED_STATUS.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status 는 active 또는 restricted 여야 합니다.");
        }
        try {
            service.updateStatus(userId, status);
        } catch (AdminUserService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 정보 없음");
        }
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "status", status,
                "message", "회원 상태가 변경되었습니다."));
    }
}
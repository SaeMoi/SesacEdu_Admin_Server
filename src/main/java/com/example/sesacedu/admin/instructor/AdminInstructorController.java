package com.example.sesacedu.admin.instructor;

import com.example.sesacedu.admin.instructor.dto.InstructorApplicationDetail;
import com.example.sesacedu.admin.instructor.dto.InstructorApplicationPageResponse;
import com.example.sesacedu.admin.instructor.dto.RejectRequest;
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

/*
 * ===================== [취약 진단용 - admin/instructor] JWT·필수값검증 유지 =====================
 *  (V-IMPROPER-INPUT/A04) 반려 사유 길이(≤500자) 검증 제거 → 과도한 길이 허용.
 *       반려 사유 필수(누락 400) 체크는 유지. Repository 로 전달되어 SQLi/저장형 XSS 로 이어짐.
 * ==============================================================================================
 */
@Tag(name = "관리자 - 강사 신청")
@RestController
@RequestMapping("/admin/instructors")
public class AdminInstructorController {

    private final AdminInstructorService service;
    private final JwtUtil jwtUtil;

    public AdminInstructorController(AdminInstructorService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    /** JWT에서 관리자 확인. (user_id 자체는 쓰지 않지만 인증·권한 체크용) */
    private void requireAdmin(HttpServletRequest request) {
        Claims claims = jwtUtil.parse(request);     // 401: 토큰 없음/만료/위조
        jwtUtil.requireRole(claims, "ADMIN");       // 403: 관리자 아님
    }

    // ---------------- 목록 ----------------
    @Operation(summary = "강사 신청 목록 조회",
            description = "신청 상태·이름으로 필터링해 조회한다. 페이지당 5개 고정.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<InstructorApplicationPageResponse> list(
            @Parameter(description = "상태 필터 approved / pending (미지정=전체)")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "신청자 이름 검색")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "페이지 번호 (기본 1)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(service.getApplications(status, keyword, page));
    }

    // ---------------- 상세 ----------------
    @Operation(summary = "강사 신청 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "신청 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<InstructorApplicationDetail> detail(
            @PathVariable("userId") long userId, HttpServletRequest request) {
        requireAdmin(request);
        try {
            return ResponseEntity.ok(service.getDetail(userId));
        } catch (AdminInstructorService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청 정보 없음");
        }
    }

    // ---------------- 승인 ----------------
    @Operation(summary = "강사 신청 승인", description = "승인 처리 + 회원 권한을 강사(auth=2)로 변경한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "신청 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/{userId}/approve")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable("userId") long userId, HttpServletRequest request) {
        requireAdmin(request);
        try {
            service.approve(userId);
        } catch (AdminInstructorService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청 정보 없음");
        } catch (AdminInstructorService.AlreadyProcessed e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 신청입니다.");
        }
        return ResponseEntity.ok(Map.of("userId", userId, "status", "approved"));
    }

    // ---------------- 거절 ----------------
    @Operation(summary = "강사 신청 거절", description = "반려 사유와 함께 거절 처리한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "400", description = "반려 사유 누락"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "신청 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/{userId}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable("userId") long userId,
            @RequestBody RejectRequest req,
            HttpServletRequest request) {
        requireAdmin(request);

        // [필수값 유지] 반려 사유 누락 시 400
        String reason = req.getReason();
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "반려 사유가 필요합니다.");
        }
        // [V-IMPROPER-INPUT] 길이 제한(≤500자) 검증 제거 → 과도한 길이 그대로 전달
        try {
            service.reject(userId, reason);
        } catch (AdminInstructorService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신청 정보 없음");
        } catch (AdminInstructorService.AlreadyProcessed e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 신청입니다.");
        }
        return ResponseEntity.ok(Map.of("userId", userId, "status", "rejected"));
    }
}
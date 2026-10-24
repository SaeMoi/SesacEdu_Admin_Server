package com.example.sesacedu.admin.settlement;

import com.example.sesacedu.admin.settlement.dto.RejectRequest;
import com.example.sesacedu.admin.settlement.dto.SettlementDetailResponse;
import com.example.sesacedu.admin.settlement.dto.SettlementPageResponse;
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
 * ===================== [취약 진단용 - admin/settlement] JWT·필수값검증 유지 =====================
 *  (V-IMPROPER-INPUT/A04) 반려 사유 길이(≤500자) 검증 제거 → 과도한 길이 허용.
 *       반려 사유 필수(누락 400) 체크는 유지. Repository 로 전달되어 SQLi/저장형 XSS 로 이어짐.
 * ==============================================================================================
 */
@Tag(name = "관리자 - 정산")
@RestController
@RequestMapping("/admin/settlements")
public class AdminSettlementController {

    private final AdminSettlementService service;
    private final JwtUtil jwtUtil;

    public AdminSettlementController(AdminSettlementService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    /** JWT 파싱 + 관리자 권한 확인 후 Claims 반환 */
    private Claims requireAdmin(HttpServletRequest request) {
        Claims claims = jwtUtil.parse(request);     // 401: 토큰 없음/만료/위조
        jwtUtil.requireRole(claims, "ADMIN");       // 403: 관리자 아님
        return claims;
    }

    // ---------------- 목록 ----------------
    @Operation(summary = "정산 목록 조회",
            description = "상태·정산월로 필터링해 조회한다. 페이지당 5개 고정.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<SettlementPageResponse> list(
            @Parameter(description = "상태 필터 requested / approved / rejected (approved 선택 시 지급완료(paid) 포함, 미지정=전체)")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "정산월 필터 (YYYY-MM)")
            @RequestParam(value = "period", required = false) String period,
            @Parameter(description = "페이지 번호 (기본 1)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(service.getList(status, period, page));
    }

    // ---------------- 상세 ----------------
    @Operation(summary = "정산 상세 조회",
            description = "정산 헤더(저장값) + 강의별 매출 내역을 반환한다. 내역은 페이지당 5개 고정.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "정산 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{sid}")
    public ResponseEntity<SettlementDetailResponse> detail(
            @PathVariable("sid") long sid,
            @Parameter(description = "강의별 내역 페이지 번호 (기본 1)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request) {
        requireAdmin(request);
        try {
            return ResponseEntity.ok(service.getDetail(sid, page));
        } catch (AdminSettlementService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "정산 정보 없음");
        }
    }

    // ---------------- 승인 ----------------
    @Operation(summary = "정산 승인", description = "requested 상태의 정산을 승인한다. (approved_by=관리자)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "정산 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 정산"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/{sid}/approve")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable("sid") long sid, HttpServletRequest request) {
        Claims claims = requireAdmin(request);
        long adminUid = jwtUtil.getUid(claims);
        try {
            service.approve(sid, adminUid);
        } catch (AdminSettlementService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "정산 정보 없음");
        } catch (AdminSettlementService.AlreadyProcessed e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 정산입니다.");
        }
        return ResponseEntity.ok(Map.of("sid", sid, "status", "approved"));
    }

    // ---------------- 거절 ----------------
    @Operation(summary = "정산 거절", description = "반려 사유와 함께 requested 상태의 정산을 거절한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "400", description = "반려 사유 누락"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "정산 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 정산"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/{sid}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable("sid") long sid,
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
            service.reject(sid, reason);
        } catch (AdminSettlementService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "정산 정보 없음");
        } catch (AdminSettlementService.AlreadyProcessed e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 정산입니다.");
        }
        return ResponseEntity.ok(Map.of("sid", sid, "status", "rejected"));
    }
}
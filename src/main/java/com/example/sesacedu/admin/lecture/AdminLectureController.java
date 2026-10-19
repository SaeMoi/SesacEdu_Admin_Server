package com.example.sesacedu.admin.lecture;

import com.example.sesacedu.admin.lecture.dto.LecturePageResponse;
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

@Tag(name = "관리자 - 강의 관리")
@RestController
@RequestMapping("/admin/lectures")
public class AdminLectureController {

    private final AdminLectureService service;
    private final JwtUtil jwtUtil;

    public AdminLectureController(AdminLectureService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    /** JWT 파싱 + 관리자 권한 확인 */
    private void requireAdmin(HttpServletRequest request) {
        Claims claims = jwtUtil.parse(request);     // 401: 토큰 없음/만료/위조
        jwtUtil.requireRole(claims, "ADMIN");       // 403: 관리자 아님
    }

    // ---------------- 목록 ----------------
    @Operation(summary = "강의 목록 조회",
            description = "강의명 검색·상태로 필터링해 조회한다. 페이지당 5개 고정. 삭제건(deleted)은 항상 제외.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<LecturePageResponse> list(
            @Parameter(description = "강의명 검색 (공백으로 나눈 단어가 모두 포함되면 매칭)")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "상태 필터 draft / published 등 (삭제건은 항상 제외, deleted 지정은 무시)")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "페이지 번호 (기본 1)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(service.getList(status, keyword, page));
    }

    // ---------------- 소프트 삭제 ----------------
    @Operation(summary = "강의 삭제 (소프트 삭제)", description = "강의 상태를 deleted 로 변경한다. 실제 행은 삭제하지 않음.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "강의 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 삭제된 강의"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @DeleteMapping("/{Lid}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable("Lid") long lid, HttpServletRequest request) {
        requireAdmin(request);
        try {
            service.delete(lid);
        } catch (AdminLectureService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "강의 정보 없음");
        } catch (AdminLectureService.AlreadyDeleted e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 삭제된 강의입니다.");
        }
        return ResponseEntity.ok(Map.of("Lid", lid, "status", "deleted"));
    }
}
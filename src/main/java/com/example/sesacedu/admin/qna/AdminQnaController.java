package com.example.sesacedu.admin.qna;

import com.example.sesacedu.admin.qna.dto.AnswerRequest;
import com.example.sesacedu.admin.qna.dto.QnaDetail;
import com.example.sesacedu.admin.qna.dto.QnaPageResponse;
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

@Tag(name = "관리자 - 서비스 문의")
@RestController
@RequestMapping("/admin/qna")
public class AdminQnaController {

    private final AdminQnaService service;
    private final JwtUtil jwtUtil;

    public AdminQnaController(AdminQnaService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    /** JWT 파싱 + 관리자 권한 확인 후 Claims 반환 */
    private Claims requireAdmin(HttpServletRequest request) {
        Claims claims = jwtUtil.parse(request);     // 401: 토큰 없음/만료/위조
        jwtUtil.requireRole(claims, "ADMIN");       // 403: 관리자 아님
        return claims;
    }

    private String validateAnswer(AnswerRequest req) {
        String answer = req == null ? null : req.getAnswer();
        if (answer == null || answer.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변 내용이 필요합니다.");
        }
        return answer;
    }

    // ---------------- 목록 ----------------
    @Operation(summary = "서비스 문의 목록 조회",
            description = "상태·제목으로 필터링해 조회한다. 페이지당 5개 고정.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping
    public ResponseEntity<QnaPageResponse> list(
            @Parameter(description = "상태 필터 done(답변완료) / none(답변없음) (미지정=전체)")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "문의 제목 검색")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "페이지 번호 (기본 1)")
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(service.getList(status, keyword, page));
    }

    // ---------------- 상세 ----------------
    @Operation(summary = "서비스 문의 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "문의 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/{iqid}")
    public ResponseEntity<QnaDetail> detail(
            @PathVariable("iqid") long iqid, HttpServletRequest request) {
        requireAdmin(request);
        try {
            return ResponseEntity.ok(service.getDetail(iqid));
        } catch (AdminQnaService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 정보 없음");
        }
    }

    // ---------------- 답변 등록 (POST) ----------------
    @Operation(summary = "문의 답변 등록", description = "답변이 없는 문의에 답변을 최초 등록한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "답변 내용 누락"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "문의 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 답변된 문의"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/{iqid}/answer")
    public ResponseEntity<Map<String, Object>> createAnswer(
            @PathVariable("iqid") long iqid,
            @RequestBody AnswerRequest req,
            HttpServletRequest request) {
        Claims claims = requireAdmin(request);
        String answer = validateAnswer(req);
        try {
            service.createAnswer(iqid, answer, jwtUtil.getUid(claims));
        } catch (AdminQnaService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 정보 없음");
        } catch (AdminQnaService.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return ResponseEntity.ok(Map.of("iqid", iqid, "status", "done"));
    }

    // ---------------- 답변 수정 (PUT) ----------------
    @Operation(summary = "문의 답변 수정", description = "이미 답변된 문의의 답변을 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "답변 내용 누락"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "문의 정보 없음"),
            @ApiResponse(responseCode = "409", description = "답변이 없는 문의 (등록은 POST)"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PutMapping("/{iqid}/answer")
    public ResponseEntity<Map<String, Object>> updateAnswer(
            @PathVariable("iqid") long iqid,
            @RequestBody AnswerRequest req,
            HttpServletRequest request) {
        Claims claims = requireAdmin(request);
        String answer = validateAnswer(req);
        try {
            service.updateAnswer(iqid, answer, jwtUtil.getUid(claims));
        } catch (AdminQnaService.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의 정보 없음");
        } catch (AdminQnaService.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return ResponseEntity.ok(Map.of("iqid", iqid, "status", "done"));
    }
}
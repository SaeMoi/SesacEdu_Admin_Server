package com.example.sesacedu.admin.users;

import com.example.sesacedu.admin.users.dto.UserDetail;
import com.example.sesacedu.admin.users.dto.UserItem;
import com.example.sesacedu.admin.users.dto.UserPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 회원 관리 로직. 목록/상세 + 상태(active/restricted) 변경.
 * 권한(관리자)은 컨트롤러에서 JWT role("ADMIN")로 확인.
 */
@Service
public class AdminUserService {

    private static final int PAGE_SIZE = 5;   // 페이지당 고정

    private final AdminUserRepository repository;

    public AdminUserService(AdminUserRepository repository) {
        this.repository = repository;
    }

    /** 목록 (keyword 필터, 페이지네이션) */
    public UserPageResponse getList(String keyword, int page) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<UserItem> content = repository.findPage(keyword, offset, PAGE_SIZE);
        int total = repository.count(keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / PAGE_SIZE);

        return new UserPageResponse(content, safePage, PAGE_SIZE, total, totalPages);
    }

    /** 상세. 없으면 404. */
    public UserDetail getDetail(long userId) {
        UserDetail d = repository.findDetail(userId);
        if (d == null) throw new NotFound();
        return d;
    }

    /** 상태 변경. 없으면 404. (status 유효성은 컨트롤러에서 검증) */
    @Transactional
    public void updateStatus(long userId, String status) {
        if (!repository.exists(userId)) throw new NotFound();
        repository.updateStatus(userId, status);
    }

    /** 회원 정보 없음 (404) */
    public static class NotFound extends RuntimeException {
    }
}
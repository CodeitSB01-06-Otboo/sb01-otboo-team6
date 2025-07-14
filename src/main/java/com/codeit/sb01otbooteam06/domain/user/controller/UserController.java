package com.codeit.sb01otbooteam06.domain.user.controller;

import com.codeit.sb01otbooteam06.domain.user.dto.*;
import com.codeit.sb01otbooteam06.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid UserCreateRequest request) {
        UserDto user = userService.create(request);
        return ResponseEntity.status(201).body(user);
    }

    /**
     * 계정 목록 조회
     */
    @GetMapping
    public ResponseEntity<UserListResponse> list(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) UUID idAfter,
            @RequestParam int limit,
            @RequestParam String sortBy,
            @RequestParam String sortDirection,
            @RequestParam(required = false) String emailLike,
            @RequestParam(required = false) String roleEqual,
            @RequestParam(required = false) Boolean locked
    ) {
        UserListResponse response = userService.list(cursor, idAfter, limit, sortBy, sortDirection, emailLike, roleEqual, locked);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 권한 수정
     */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDto> changeRole(
            @PathVariable UUID userId,
            @RequestBody @Valid UserRoleUpdateRequest request
    ) {
        UserDto updatedUser = userService.changeRole(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 사용자 잠금 상태 수정
     */
    @PatchMapping("/{userId}/lock")
    public ResponseEntity<String> changeLocked(
            @PathVariable UUID userId,
            @RequestBody @Valid UserLockUpdateRequest request
    ) {
        userService.changeLocked(userId, request);
        return ResponseEntity.ok(userId.toString());
    }

    /**
     * 비밀번호 변경
     */
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID userId,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}

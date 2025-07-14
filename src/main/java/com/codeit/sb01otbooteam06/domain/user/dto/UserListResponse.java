package com.codeit.sb01otbooteam06.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * 계정 목록 조회 API 응답 DTO (Swagger 명세 기반)
 */
@Getter
@Builder
public class UserListResponse {

    /**
     * 계정 목록
     */
    private List<UserDto> data;

    /**
     * 다음 페이지 조회를 위한 커서 문자열 (프론트 그대로 전달)
     */
    private String nextCursor;

    /**
     * 다음 페이지 조회를 위한 마지막 사용자 ID
     */
    private UUID nextIdAfter;

    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNext;

    /**
     * 전체 계정 수
     */
    private long totalCount;

    /**
     * 정렬 기준 필드명
     */
    private String sortBy;

    /**
     * 정렬 방향 (ASCENDING, DESCENDING)
     */
    private String sortDirection;
}

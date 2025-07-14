package com.codeit.sb01otbooteam06.domain.dm.dto;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public record DirectMessageListResponse(
    List<DirectMessageDto> data,
    UUID nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    int totalCount,
    String sortBy,
    String sortDirection) {

    public static DirectMessageListResponse fromSlice(
        Slice<DirectMessageDto> slice,
        String sortBy,
        String sortDirection) {

        List<DirectMessageDto> list = slice.getContent();
        UUID nextCursor   = slice.hasNext() ? list.get(list.size() - 1).id() : null;
        UUID nextIdAfter  = nextCursor;       // 프로토타입 정의 그대로
        return new DirectMessageListResponse(
            list,
            nextCursor,
            nextIdAfter,
            slice.hasNext(),
            list.size(),
            sortBy,
            sortDirection);
    }
}

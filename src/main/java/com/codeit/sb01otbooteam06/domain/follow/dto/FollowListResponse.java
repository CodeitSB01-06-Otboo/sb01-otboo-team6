package com.codeit.sb01otbooteam06.domain.follow.dto;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Slice;

public record FollowListResponse(
    List<FollowDto> data,
    UUID nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    int totalCount,
    String sortBy,
    String sortDirection) {

    public static FollowListResponse fromSlice(Slice<FollowDto> slice,
        String sortBy,
        String sortDirection) {
        List<FollowDto> list = slice.getContent();
        UUID lastId = slice.hasNext() ? list.get(list.size()-1).id() : null;

        return new FollowListResponse(
            list,
            lastId,     // nextCursor
            lastId,     // nextIdAfter
            slice.hasNext(),
            list.size(),
            sortBy,
            sortDirection);
    }
}

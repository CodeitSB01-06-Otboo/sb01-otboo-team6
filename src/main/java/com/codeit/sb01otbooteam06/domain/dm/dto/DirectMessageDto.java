package com.codeit.sb01otbooteam06.domain.dm.dto;

import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import com.codeit.sb01otbooteam06.domain.user.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto(
    UUID id,
    UserSummary sender,
    UserSummary receiver,
    String content,
    Instant createdAt) {

    public static DirectMessageDto from(DirectMessage dm) {
        return new DirectMessageDto(
            dm.getId(),
            UserSummary.from(dm.getSender()),
            UserSummary.from(dm.getReceiver()),
            dm.getContent(),
            dm.getCreatedAt());
    }
}

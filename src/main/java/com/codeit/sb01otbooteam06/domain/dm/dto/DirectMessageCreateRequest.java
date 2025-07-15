package com.codeit.sb01otbooteam06.domain.dm.dto;

import java.util.UUID;

public record DirectMessageCreateRequest(
    UUID senderId,
    UUID receiverId,
    String content) { }

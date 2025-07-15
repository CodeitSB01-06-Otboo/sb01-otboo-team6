package com.codeit.sb01otbooteam06.domain.follow.dto;

import java.util.UUID;

public record FollowCreateRequest(UUID followerId, UUID followeeId) { }

package com.codeit.sb01otbooteam06.domain.feed.service;

import com.codeit.sb01otbooteam06.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDtoCursorResponse;
import java.time.Instant;
import java.util.UUID;

public interface CommentService {

  CommentDto createComment(UUID feedId ,CommentCreateRequest request);

  CommentDtoCursorResponse getCommentsByCursor(UUID feedId, Instant cursor, UUID idAfter,
      int limit);

}

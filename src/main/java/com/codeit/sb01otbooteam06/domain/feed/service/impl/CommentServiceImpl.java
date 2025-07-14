package com.codeit.sb01otbooteam06.domain.feed.service.impl;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDtoCursorResponse;
import com.codeit.sb01otbooteam06.domain.feed.entity.Comment;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.repository.CommentRepository;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedRepository;
import com.codeit.sb01otbooteam06.domain.feed.service.CommentService;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final AuthService authService;

  @Transactional
  @Override
  public CommentDto createComment(UUID feedId, CommentCreateRequest request) {

    UUID authorId = authService.getCurrentUserId();
    User author = userRepository.findById(authorId)
        .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new OtbooException(ErrorCode.FEED_NOT_FOUND));

    Comment comment = Comment.of(request.getContent(), author.getName(), author, feed);

    // 댓글을 레포지토리에 저장안한 이유? cascade = ALL 설정, 피드가 저장 되면 같이 저장.
    feed.addComment(comment);
    feedRepository.save(feed);
    // todo : 이벤트 발행, 댓글 생성 시에 알림 가도록 나중에 추가 해야함.

    return CommentDto.fromEntity(comment);
  }

  @Transactional(readOnly = true)
  @Override
  public CommentDtoCursorResponse getCommentsByCursor(UUID feedId, Instant cursor, UUID idAfter,
      int limit) {
    PageRequest pageReq = PageRequest.of(0, limit);

    List<Comment> comments = commentRepository.findCommentsByCreatedAtCursor(feedId, cursor,
        idAfter, pageReq);

    List<CommentDto> data = comments.stream().map(CommentDto::fromEntity).toList();

    boolean hasNext = data.size() == limit;
    String nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext) {
      Comment last = comments.get(comments.size() - 1);
      nextCursor = last.getCreatedAt().toString();
      nextIdAfter = last.getId();
    }

    long totalCount = commentRepository.countByFeedId(feedId);

    return new CommentDtoCursorResponse(data, nextCursor, nextIdAfter, hasNext, totalCount,
        "createdAt", "DESCENDING");
  }
}

package com.codeit.sb01otbooteam06.domain.feed.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDtoCursorResponse;
import com.codeit.sb01otbooteam06.domain.feed.entity.Comment;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.repository.CommentRepository;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedRepository;
import com.codeit.sb01otbooteam06.domain.feed.service.impl.CommentServiceImpl;
import com.codeit.sb01otbooteam06.domain.notification.service.NotificationService;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

  @InjectMocks
  private CommentServiceImpl commentService;

  @Mock
  private CommentRepository commentRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private AuthService authService;
  @Mock
  private FeedRepository feedRepository;
  @Mock
  private NotificationService notificationService;

  private final UUID userId = UUID.randomUUID();
  private final UUID feedId = UUID.randomUUID();
  private final Weather mockWeather = mock(Weather.class);
  private final Profile mockProfile = mock(Profile.class);
  private final Feed mockFeed = mock(Feed.class);


  @Nested
  @DisplayName("댓글 생성 테스트")
  class createComment {

    User user = User.builder()
        .name("tester")
        .email("tester@codeit.com")
        .password("password")
        .profile(mockProfile)
        .build();

    Feed feed = Feed.of("피드", user, mockWeather);

    CommentCreateRequest request = CommentCreateRequest.builder()
        .authorId(userId)
        .feedId(feedId)
        .content("댓글")
        .build();


    @DisplayName("사용자에 대한 검증을 하고 피드를 찾고, 댓글을 생성한다.")
    @Test
    void createComment_Success() {

      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(feedRepository.save(any(Feed.class))).willReturn(feed);
      willDoNothing().given(notificationService).notifyFeedCommented(any(User.class), any(User.class), anyString());


      //when
      CommentDto result = commentService.createComment(feedId, request);

      //then
      assertNotNull(result);
      assertEquals("댓글", result.getContent());
      assertEquals("tester", result.getAuthor().getName());

      verify(authService).getCurrentUserId();
      verify(userRepository).findById(userId);
      verify(feedRepository).findById(feedId);
      verify(feedRepository).save(feed);

    }

    @DisplayName("댓글을 생성 할때, 인증된 유저가 아니면 댓글을 생성할 수 없다")
    @Test
    void createComment_Not_Found_User_Fail() {

      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      //when, then
      assertThrows(OtbooException.class, () -> commentService.createComment(feedId, request));

    }

    @DisplayName("댓글을 생성 할때, 인증된 유저가 아니면 댓글을 생성할 수 없다")
    @Test
    void createComment_Not_Found_Feed_Fail() {

      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedRepository.findById(feedId)).willReturn(Optional.empty());

      //when, then
      assertThrows(OtbooException.class, () -> commentService.createComment(feedId, request));

    }

  }

  @Nested
  @DisplayName("댓글 커서 페이지네이션 조회 테스트")
  class getCursorComments {
    UUID commentId1 = UUID.randomUUID();
    UUID commentId2 = UUID.randomUUID();
    Instant createdAt1 = Instant.parse("2025-07-17T10:00:00Z");
    Instant createdAt2 = Instant.parse("2025-07-17T09:00:00Z");

    User user1 = User.builder()
        .name("tester1")
        .email("tester@codeit.com")
        .password("password1")
        .profile(mockProfile)
        .build();

    User user2 = User.builder()
        .name("tester2")
        .email("tester2@codeit.com")
        .password("password2")
        .profile(mockProfile)
        .build();

    @Test
    @DisplayName("커서가 null이면 최신 댓글부터 limit 개수 만큼 조회한다.")
    void getCommentsByCursor_noCursor() {

      //given
      Comment comment1 = mock(Comment.class);
      Comment comment2 = mock(Comment.class);

      given(comment1.getId()).willReturn(commentId1);
      given(comment1.getCreatedAt()).willReturn(createdAt1);
      given(comment2.getId()).willReturn(commentId2);
      given(comment2.getCreatedAt()).willReturn(createdAt2);
      given(comment1.getFeed()).willReturn(mockFeed);
      given(comment2.getFeed()).willReturn(mockFeed);

      List<Comment> commentList = List.of(comment1, comment2);
      given(commentRepository.findByFeedId(eq(feedId), any(PageRequest.class))).willReturn(commentList);
      given(commentRepository.countByFeedId(eq(feedId))).willReturn(10L);

      given(comment1.getUser()).willReturn(user1);
      given(comment2.getUser()).willReturn(user2);
      given(comment1.getContent()).willReturn("첫 번째 댓글");
      given(comment2.getContent()).willReturn("두 번째 댓글");

      //when
      CommentDtoCursorResponse result = commentService.getCommentsByCursor(feedId, null,
          null, 2);

      //then
      assertEquals(2 , result.getData().size());
      assertTrue(result.isHasNext());

      assertEquals(commentId2, result.getNextIdAfter());
      assertEquals(createdAt2.toString(), result.getNextCursor());
      assertEquals(10L, result.getTotalCount());

      verify(commentRepository).findByFeedId(eq(feedId), any(PageRequest.class));
      verify(commentRepository, never()).findCommentsByCreatedAtCursor(any(), any(), any(), any());
    }

    @DisplayName("마지막 댓글의 createdAt이 null이면 빈 문자열을 반환한다.")
    @Test
    void getCommentsByCursor_createdAtIsNull() {
      // given
      Comment comment1 = mock(Comment.class);
      given(comment1.getId()).willReturn(commentId1);
      given(comment1.getCreatedAt()).willReturn(null);
      given(comment1.getFeed()).willReturn(mockFeed);
      given(comment1.getUser()).willReturn(user1);
      given(comment1.getContent()).willReturn("댓글");

      List<Comment> commentList = List.of(comment1);
      given(commentRepository.findByFeedId(eq(feedId), any(PageRequest.class))).willReturn(commentList);
      given(commentRepository.countByFeedId(feedId)).willReturn(1L);

      // when
      CommentDtoCursorResponse result = commentService.getCommentsByCursor(feedId, null, null, 1);

      // then
      assertEquals("", result.getNextCursor());
    }

    @DisplayName("댓글 수가 limit보다 적으면 hasNext는 false이며 nextCursor는 null이다.")
    @Test
    void getCommentsByCursor_hasNextFalse() {
      // given
      Comment comment1 = mock(Comment.class);
      given(comment1.getId()).willReturn(commentId1);
      given(comment1.getCreatedAt()).willReturn(createdAt1);
      given(comment1.getFeed()).willReturn(mockFeed);
      given(comment1.getUser()).willReturn(user1);
      given(comment1.getContent()).willReturn("댓글");

      List<Comment> commentList = List.of(comment1);
      given(commentRepository.findByFeedId(eq(feedId), any(PageRequest.class))).willReturn(commentList);
      given(commentRepository.countByFeedId(feedId)).willReturn(1L);

      // when
      CommentDtoCursorResponse result = commentService.getCommentsByCursor(feedId, null, null, 5);

      // then
      assertFalse(result.isHasNext());
      assertNull(result.getNextCursor());
      assertNull(result.getNextIdAfter());
    }

    @DisplayName("커서가 주어지면 createdAt 기준으로 이후 댓글을 limit만큼 조회한다.")
    @Test
    void getCommentsByCursor_withCursor() {
      // given
      Comment comment1 = mock(Comment.class);
      Comment comment2 = mock(Comment.class);

      given(comment1.getId()).willReturn(commentId1);
      given(comment1.getCreatedAt()).willReturn(createdAt1);
      given(comment2.getId()).willReturn(commentId2);
      given(comment2.getCreatedAt()).willReturn(createdAt2);

      given(comment1.getFeed()).willReturn(mockFeed);
      given(comment2.getFeed()).willReturn(mockFeed);

      given(comment1.getUser()).willReturn(user1);
      given(comment2.getUser()).willReturn(user2);

      given(comment1.getContent()).willReturn("세 번째 댓글");
      given(comment2.getContent()).willReturn("네 번째 댓글");

      List<Comment> commentList = List.of(comment1, comment2);
      given(commentRepository.findCommentsByCreatedAtCursor(
          eq(feedId), eq(createdAt1), eq(commentId1), any(PageRequest.class)
      )).willReturn(commentList);

      given(commentRepository.countByFeedId(feedId)).willReturn(10L);

      // when
      CommentDtoCursorResponse result = commentService.getCommentsByCursor(
          feedId, createdAt1, commentId1, 2
      );

      // then
      assertEquals(2, result.getData().size());
      assertTrue(result.isHasNext());
      assertEquals(commentId2, result.getNextIdAfter());
      assertEquals(createdAt2.toString(), result.getNextCursor());

      verify(commentRepository).findCommentsByCreatedAtCursor(eq(feedId), eq(createdAt1), eq(commentId1), any(PageRequest.class));
      verify(commentRepository, never()).findByFeedId(any(), any());
    }

  }


}

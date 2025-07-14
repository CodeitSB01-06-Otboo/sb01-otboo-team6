package com.codeit.sb01otbooteam06.domain.feed.controller;

import com.codeit.sb01otbooteam06.domain.feed.dto.request.CommentCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.CommentDtoCursorResponse;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.FeedDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.sb01otbooteam06.domain.feed.service.CommentService;
import com.codeit.sb01otbooteam06.domain.feed.service.FeedLikeService;
import com.codeit.sb01otbooteam06.domain.feed.service.FeedService;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Slf4j
public class FeedController {

  private final FeedService feedService;
  private final FeedLikeService likeService;
  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<FeedDto> createFeed(@RequestBody @Valid FeedCreateRequest request) {
    FeedDto feed = feedService.createFeed(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(feed);
  }

  @GetMapping
  public ResponseEntity<FeedDtoCursorResponse> listFeedsByCursor(
      @RequestParam(name = "cursor", required = false) Instant cursor,
      @RequestParam(name = "idAfter", required = false) UUID idAfter,
      @RequestParam(name = "cursorLikeCount" , required = false) Long cursorLikeCount,
      @RequestParam(name = "limit") int limit,
      @RequestParam(name = "sortBy") String sortBy,
      @RequestParam(name = "sortDirection") String sortDirection,
      @RequestParam(name = "keywordLike", required = false) String keywordLikeRaw,
      @RequestParam(name = "skyStatusEqual", required = false) SkyStatus skyStatusEqual,
      @RequestParam(name = "precipitationTypeEqual", required = false) PrecipitationType precipitationTypeEqual,
      @RequestParam(name = "authorIdEqual", required = false) UUID authorIdEqual) {


    String keywordLike = (keywordLikeRaw != null && !keywordLikeRaw.trim().isEmpty())
        ? keywordLikeRaw
        : null;


    log.info(" sortedBy: {}, sortDirection: {}, keywordLike: {}", sortBy, sortDirection, keywordLike);



    Instant cursorCreatedAt = null;
    Long likeCursor = null;

    if ("likeCount".equalsIgnoreCase(sortBy)) {
      likeCursor = cursorLikeCount;
    } else {
      cursorCreatedAt = cursor;
    }


    FeedDtoCursorResponse feedsByCursor = feedService.getFeedsByCursor(keywordLike, skyStatusEqual,
        precipitationTypeEqual, cursorCreatedAt, idAfter, likeCursor, limit, sortBy);

    return ResponseEntity.status(HttpStatus.OK).body(feedsByCursor);
  }

  @PatchMapping("/{feedId}")
  public ResponseEntity<FeedDto> updateFeed(@PathVariable UUID feedId,
      @RequestBody @Valid FeedUpdateRequest request) {

    FeedDto feed = feedService.updateFeed(feedId, request);

    return ResponseEntity.status(HttpStatus.OK).body(feed);
  }

  @DeleteMapping("/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable UUID feedId) {
    feedService.deleteFeed(feedId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/{feedId}/like")
  public ResponseEntity<FeedDto> likeFeed(@PathVariable UUID feedId) {
    likeService.likeFeed(feedId);
    return ResponseEntity.status(HttpStatus.OK).body(feedService.getFeed(feedId));
  }

  @DeleteMapping("/{feedId}/like")
  public ResponseEntity<Void> unlikeFeed(@PathVariable UUID feedId) {
    likeService.unlikeFeed(feedId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/{feedId}/comments")
  public ResponseEntity<CommentDto> createComment(@PathVariable UUID feedId,
      @RequestBody @Valid CommentCreateRequest request) {
    CommentDto comment = commentService.createComment(feedId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(comment);
  }

  @GetMapping("/{feedId}/comments")
  public ResponseEntity<CommentDtoCursorResponse> listComments(@PathVariable UUID feedId,
      @RequestParam(required = false) Instant cursor, @RequestParam(required = false) UUID idAfter, @RequestParam int limit) {
    CommentDtoCursorResponse commentsByCursor = commentService.getCommentsByCursor(feedId, cursor,
        idAfter, limit);
    return ResponseEntity.status(HttpStatus.OK).body(commentsByCursor);
  }


}

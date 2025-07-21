package com.codeit.sb01otbooteam06.domain.feed.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.codeit.sb01otbooteam06.domain.user.dto.AuthorDto;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FeedController.class)
@AutoConfigureMockMvc(addFilters = false)
class FeedControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FeedService feedService;

  @Autowired
  private FeedLikeService likeService;

  @Autowired
  private CommentService commentService;

  private final UUID feedId = UUID.randomUUID();


  private final FeedDto dummyFeedDto = FeedDto.builder()
      .id(feedId)
      .author(AuthorDto.builder()
          .name("user")
          .profileImageUrl("profile")
          .build())
      .content("내용")
      .likeCount(0L)
      .build();

  @TestConfiguration
  static class MockServiceConfig {
    @Bean
    FeedService feedService() {
      return mock(FeedService.class);
    }

    @Bean
    FeedLikeService likeService() {
      return mock(FeedLikeService.class);
    }

    @Bean
    CommentService commentService() {
      return mock(CommentService.class);
    }
  }

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("피드 생성 API - 성공")
  void createFeed_success() throws Exception {
    FeedCreateRequest request = FeedCreateRequest.builder()
        .authorId(UUID.randomUUID())
        .weatherId(UUID.randomUUID())
        .clothesIds(List.of())
        .content("오늘의 코디")
        .build();

    FeedDto responseDto = mock(FeedDto.class);

    given(feedService.createFeed(any())).willReturn(responseDto);

    mockMvc.perform(post("/api/feeds")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").exists());

    verify(feedService).createFeed(any());
  }

  @Test
  @DisplayName("피드 좋아요 API - 성공")
  void likeFeed_success() throws Exception {
    UUID feedId = UUID.randomUUID();
    FeedDto responseDto = mock(FeedDto.class);
    given(feedService.getFeed(feedId)).willReturn(responseDto);

    mockMvc.perform(post("/api/feeds/" + feedId + "/like"))
        .andExpect(status().isOk());

    verify(likeService).likeFeed(feedId);
    verify(feedService).getFeed(feedId);
  }

  @Test
  @DisplayName("피드 좋아요 취소 API - 성공")
  void unlikeFeed_success() throws Exception {
    UUID feedId = UUID.randomUUID();

    mockMvc.perform(delete("/api/feeds/" + feedId + "/like"))
        .andExpect(status().isNoContent());

    verify(likeService).unlikeFeed(feedId);
  }

  @Test
  @DisplayName("댓글 생성 API - 성공")
  void createComment_success() throws Exception {
    UUID feedId = UUID.randomUUID();
    CommentCreateRequest request = CommentCreateRequest.builder()
        .feedId(feedId)
        .authorId(UUID.randomUUID())
        .content("좋은 코디네요!")
        .build();

    CommentDto responseDto = mock(CommentDto.class);
    given(commentService.createComment(eq(feedId), any())).willReturn(responseDto);

    mockMvc.perform(post("/api/feeds/" + feedId + "/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(commentService).createComment(eq(feedId), any());
  }

  @Test
  @DisplayName("댓글 목록 커서 조회 API - 성공")
  void listComments_success() throws Exception {
    UUID feedId = UUID.randomUUID();
    CommentDtoCursorResponse response = mock(CommentDtoCursorResponse.class);

    given(commentService.getCommentsByCursor(any(), any(), any(), anyInt()))
        .willReturn(response);

    mockMvc.perform(get("/api/feeds/" + feedId + "/comments")
            .param("limit", "5"))
        .andExpect(status().isOk());

    verify(commentService).getCommentsByCursor(eq(feedId), any(), any(), eq(5));
  }

  @Test
  @DisplayName("피드 삭제 - 성공")
  void deleteFeed_success() throws Exception {

    mockMvc.perform(delete("/api/feeds/" + feedId))
        .andExpect(status().isNoContent());

    verify(feedService).deleteFeed(feedId);
  }

  @Test
  @DisplayName("피드 수정 - 성공")
  void updateFeed_success() throws Exception {
    FeedUpdateRequest updateRequest = FeedUpdateRequest.builder()
        .content("수정된 내용")
        .build();

    given(feedService.updateFeed(eq(feedId), any())).willReturn(
        dummyFeedDto.toBuilder().content("수정된 내용").build());

    mockMvc.perform(patch("/api/feeds/" + feedId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 내용"));
  }

  @Test
  @DisplayName("기본 정렬(최신순)으로 피드 목록 조회")
  void listFeedsByCursor_withDefaultSort_success() throws Exception {
    Mockito.when(feedService.getFeedsByCursor(
       any() ,any(), any(), any(), any(), any(), anyInt(), anyString()
    )).thenReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"));

    mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
            .param("limit", "10")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andDo(print());
  }

  @Test
  @DisplayName("좋아요 정렬로 피드 목록 조회")
  void listFeedsByCursor_withLikeSort_success() throws Exception {
    Mockito.when(feedService.getFeedsByCursor(
      any() , any(), any(), any(), any(), any(), anyInt(), eq("likeCount")
    )).thenReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "likeCount", "DESCENDING"));

    mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
            .param("limit", "5")
            .param("sortBy", "likeCount")
            .param("sortDirection", "DESC")
            .param("cursorLikeCount", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sortBy").value("likeCount"))
        .andDo(print());
  }

  @Test
  @DisplayName("키워드 검색 포함 피드 목록 조회")
  void listFeedsByCursor_withKeyword_success() throws Exception {
    Mockito.when(feedService.getFeedsByCursor(
        eq("OOTD"), any(), any(), any(), any(), anyLong(), anyInt(), anyString()
    )).thenReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"));

    mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
            .param("limit", "5")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC")
            .param("keywordLike", "OOTD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andDo(print());
  }

  @Test
  @DisplayName("날씨 및 강수 검색 조건 포함 피드 목록 조회")
  void listFeedsByCursor_withWeatherFilters_success() throws Exception {
    Mockito.when(feedService.getFeedsByCursor(
        any(), eq(SkyStatus.CLEAR), eq(PrecipitationType.RAIN), any(), any(), anyLong(), anyInt(), anyString()
    )).thenReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"));

    mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
            .param("limit", "5")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC")
            .param("skyStatusEqual", "CLEAR")
            .param("precipitationTypeEqual", "RAIN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andDo(print());
  }

  @Test
  @DisplayName("작성자 ID 조건 포함 피드 목록 조회")
  void listFeedsByCursor_withAuthorId_success() throws Exception {
    UUID authorId = UUID.randomUUID();

    Mockito.when(feedService.getFeedsByCursor(
        any(), any(), any(), any(), eq(authorId), anyLong(), anyInt(), anyString()
    )).thenReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"));

    mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
            .param("limit", "5")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC")
            .param("authorIdEqual", authorId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andDo(print());
  }

//  @Test
//  @DisplayName("keywordLike가 null인 경우, null로 처리되어야 한다.")
//  void keywordLike_isNull() throws Exception {
//    // given
//    given(feedService.getFeedsByCursor(
//        isNull(), any(), any(), any(), any(), any(), eq(10), anyString()
//    )).willReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"));
//
//    // when
//    mockMvc.perform(get("/api/feeds")
//            .param("limit", "10")
//            .param("sortBy", "createdAt")
//            .param("sortDirection", "DESC"))
//        .andExpect(status().isOk());
//
//    // then
//    then(feedService).should(times(1)).getFeedsByCursor(
//        isNull(), any(), any(), any(), any(), any(), eq(10), anyString()
//    );
//  }
//
//  @Test
//  @DisplayName("keywordLike가 공백 문자열인 경우, null로 처리되어야 한다.")
//  void keywordLike_isBlank() throws Exception {
//    // given
//    given(feedService.getFeedsByCursor(
//        isNull(), any(), any(), any(), any(), any(), eq(10), anyString()
//    )).willReturn(new FeedDtoCursorResponse(Collections.emptyList(), null, null, false, 0L, "createdAt", "DESCENDING"));
//
//    // when
//    mockMvc.perform(get("/api/feeds")
//            .param("limit", "10")
//            .param("sortBy", "createdAt")
//            .param("sortDirection", "DESC")
//            .param("keywordLike", "   "))
//        .andExpect(status().isOk());
//
//    // then
//    then(feedService).should(times(1)).getFeedsByCursor(
//        isNull(), any(), any(), any(), any(), any(), eq(10), anyString()
//    );
//  }

  @Test
  @DisplayName("keywordLike가 유효한 문자열이면 그대로 전달되어야 한다.")
  void keywordLike_isValid() throws Exception {
    mockMvc.perform(get("/api/feeds")
            .param("limit", "10")
            .param("sortBy", "createdAt")
            .param("sortDirection", "DESC")
            .param("keywordLike", "봄 코디"))
        .andExpect(status().isOk());

    then(feedService).should().getFeedsByCursor(
        eq("봄 코디"), any(), any(), any(), any(), any(),  eq(10), anyString()
    );
  }

}

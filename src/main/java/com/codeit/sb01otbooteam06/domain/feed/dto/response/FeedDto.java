package com.codeit.sb01otbooteam06.domain.feed.dto.response;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.OotdDto;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesMapper;
import com.codeit.sb01otbooteam06.domain.feed.entity.ClothesFeed;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.dto.AuthorDto;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.mapper.WeatherDtoMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedDto {

  private UUID id;
  private Instant createdAt;
  private Instant updatedAt;

  private AuthorDto author;
  private WeatherDto weather;
  private List<OotdDto> ootds;

  private String content;
  private long likeCount;
  private int commentsCount;
  private boolean likedByMe;

  public FeedDto(UUID id, Instant createdAt, Instant updatedAt, AuthorDto author,
      WeatherDto weather,
      List<OotdDto> ootds, String content, long likeCount, int commentsCount, boolean likedByMe) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.author = author;
    this.weather = weather;
    this.ootds = ootds;
    this.content = content;
    this.likeCount = likeCount;
    this.commentsCount = commentsCount;
    this.likedByMe = likedByMe;
  }

  public static FeedDto fromEntity(Feed feed, ClothesMapper clothesMapper, WeatherDtoMapper weatherMapper) {
    User author = feed.getUser();
    Profile profile = author.getProfile();
    AuthorDto authorDto = AuthorDto.builder()
        .userId(author.getId())
        .name(author.getName())
        .profileImageUrl(profile != null ? profile.getProfileImageUrl() : null)
        .build();

    Weather weather = feed.getWeather();
    WeatherDto weatherDto = weatherMapper.toDto(weather);

    List<ClothesFeed> clothesFeeds = feed.getClothesFeeds();
    List<OotdDto> ootdDtos = clothesFeeds.stream()
        .map(ClothesFeed::getClothes)
        .map(clothesMapper::toDto)
        .map(OotdDto::toDto)
        .toList();

    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        authorDto,
        weatherDto,
        ootdDtos,
        feed.getContent(),
        feed.getLikeCount(),
        feed.getCommentCount(),
        feed.isLikedByMe()
    );
  }

}

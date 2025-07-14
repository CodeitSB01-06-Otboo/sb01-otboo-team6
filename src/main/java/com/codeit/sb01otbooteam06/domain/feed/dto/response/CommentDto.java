package com.codeit.sb01otbooteam06.domain.feed.dto.response;

import com.codeit.sb01otbooteam06.domain.feed.entity.Comment;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.dto.AuthorDto;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentDto {

  private UUID id;
  private Instant createdAt;
  private UUID feedId;
  private AuthorDto author;
  private String content;

  public CommentDto(UUID id, Instant createdAt, UUID feedId, AuthorDto author, String content) {
    this.id = id;
    this.createdAt = createdAt;
    this.feedId = feedId;
    this.author = author;
    this.content = content;
  }

  public static CommentDto fromEntity(Comment comment) {
    User user = comment.getUser();
    Profile profile = user.getProfile();
    AuthorDto author = AuthorDto.builder()
        .userId(user.getId())
        .name(user.getName())
        .profileImageUrl(profile.getProfileImageUrl())
        .build();

    return new CommentDto(
        comment.getId(),
        comment.getCreatedAt(),
        comment.getFeed().getId(),
        author,
        comment.getContent()
    );
  }
}

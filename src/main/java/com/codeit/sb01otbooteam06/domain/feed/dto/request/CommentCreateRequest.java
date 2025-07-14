package com.codeit.sb01otbooteam06.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentCreateRequest {

  @NotNull(message = "피드 ID는 필수입니다")
  private UUID feedId;

  @NotNull(message = "작성자 ID는 필수입니다")
  private UUID authorId;

  @NotBlank(message = "댓글 내용은 필수 입니다.")
  private String content;

}

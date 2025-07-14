package com.codeit.sb01otbooteam06.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedUpdateRequest {
  @NotBlank(message = "변경할 피드 내용이 비어있으면 안됩니다.")
  private String content;
}

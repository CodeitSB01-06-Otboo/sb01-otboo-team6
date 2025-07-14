package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;


import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class PageResponse<T> {

  private final List<T> data;
  private final String nextCursor;
  private final String nextIdAfter;
  private final boolean hasNext;
  private final int totalCount;
  private final String sortBy;
  private final String sortDirection;


}

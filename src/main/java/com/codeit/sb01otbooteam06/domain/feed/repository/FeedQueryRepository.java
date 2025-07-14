package com.codeit.sb01otbooteam06.domain.feed.repository;

import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface FeedQueryRepository {

  Page<Feed> findFeedsByCursorAndSort(String keyword, SkyStatus skyStatus,
      PrecipitationType precipitationType, Object cursorValue, UUID cursorId, Pageable pageable);

  long countByFilters(String keyword, SkyStatus skyStatus, PrecipitationType precipitationType);
}

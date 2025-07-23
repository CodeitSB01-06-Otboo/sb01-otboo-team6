package com.codeit.sb01otbooteam06.domain.feed.repository;

import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.entity.FeedLike;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

  boolean existsByFeedAndUser(Feed feed, User user);

  Optional<FeedLike> findByFeedAndUser(Feed feed, User user);

  void deleteByFeedAndUser(Feed feed, User user);

  @Query("SELECT fl.feed.id FROM FeedLike fl WHERE fl.user.id = :userId AND fl.feed.id IN :feedIds")
  Set<UUID> findFeedIdsLikedByUser(@Param("userId") UUID userId,
      @Param("feedIds") List<UUID> feedIds);

}

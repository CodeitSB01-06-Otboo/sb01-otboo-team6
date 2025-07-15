package com.codeit.sb01otbooteam06.domain.follow.repository;


import com.codeit.sb01otbooteam06.domain.follow.entity.Follow;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerAndFollowee(User follower, User followee);
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    Optional<Follow> findByFollowerAndFollowee(User follower, User followee);
    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    @Modifying
    void deleteByFollowerAndFollowee(User follower, User followee);

    long countByFollowerId(UUID followerId);
    long countByFolloweeId(UUID followeeId);


    @Query("""
          SELECT f
            FROM Follow f
           WHERE f.followee.id = :followeeId
             AND (:cursor IS NULL OR f.id < :cursor)
        ORDER BY f.id DESC
        """)
    List<Follow> findFollowers(@Param("followeeId") UUID followeeId,
        @Param("cursor") UUID cursor,
        Pageable pageable);

    @Query("""
          SELECT f
            FROM Follow f
           WHERE f.follower.id = :followerId
             AND (:cursor IS NULL OR f.id < :cursor)
        ORDER BY f.id DESC
        """)
    List<Follow> findFollowings(@Param("followerId") UUID followerId,
        @Param("cursor") UUID cursor,
        Pageable pageable);
}

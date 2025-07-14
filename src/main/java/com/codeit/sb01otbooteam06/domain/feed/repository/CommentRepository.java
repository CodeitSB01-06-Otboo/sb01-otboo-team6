package com.codeit.sb01otbooteam06.domain.feed.repository;

import com.codeit.sb01otbooteam06.domain.feed.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {


  @Query("""
      SELECT c
      FROM Comment c
      JOIN FETCH c.user u
      WHERE c.feed.id = :feedId
        AND ( :cursor IS NULL
              OR (c.createdAt < :cursor
                  OR (c.createdAt = :cursor AND c.id < :idAfter))
            )
      ORDER BY c.createdAt DESC, c.id DESC
      """)
  List<Comment> findCommentsByCreatedAtCursor(
      @Param("feedId") UUID feedId,
      @Param("cursor") Instant cursor,
      @Param("idAfter") UUID idAfter,
      Pageable pageable
  );


  @Query("""
      SELECT COUNT(c)
      FROM Comment c
      WHERE c.feed.id = :feedId
      """)
  long countByFeedId(
      @Param("feedId") UUID feedId
  );

}

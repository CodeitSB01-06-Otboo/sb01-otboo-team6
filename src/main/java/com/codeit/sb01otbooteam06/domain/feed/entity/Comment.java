package com.codeit.sb01otbooteam06.domain.feed.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseUpdatableEntity {

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(nullable = false)
  private String authorName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id", nullable = false)
  private Feed feed;

  public Comment(String content, String authorName, User user, Feed feed) {
    this.content = content;
    this.authorName = authorName;
    this.user = user;
    this.feed = feed;
  }

  public static Comment of(String content, String authorName, User user, Feed feed) {
    return new Comment(content, authorName, user, feed);
  }

  protected void setFeed(Feed feed) {
    this.feed = feed;
  }

}

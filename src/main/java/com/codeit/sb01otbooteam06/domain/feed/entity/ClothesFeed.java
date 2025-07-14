package com.codeit.sb01otbooteam06.domain.feed.entity;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clothes_feeds",
    uniqueConstraints = @UniqueConstraint(name = "uq_clothes_feed", columnNames = {"clothes_id", "feed_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesFeed {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clothes_id", nullable = false)
  private Clothes clothes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id", nullable = false)
  private Feed feed;

  public ClothesFeed(Clothes clothes, Feed feed) {
    this.clothes = clothes;
    this.feed = feed;
  }

  public static ClothesFeed of(Clothes clothes, Feed feed) {
    return new ClothesFeed(clothes, feed);
  }
}

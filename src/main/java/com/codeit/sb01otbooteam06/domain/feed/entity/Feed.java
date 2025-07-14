package com.codeit.sb01otbooteam06.domain.feed.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private long likeCount = 0L;

  @Column(nullable = false)
  private int commentCount = 0;

  @Column(nullable = false)
  private boolean likedByMe = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "weather_id", nullable = false)
  private Weather weather;


  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ClothesFeed> clothesFeeds = new ArrayList<>();


  public Feed(String content, User user, Weather weather) {
    this.content = content;
    this.user = user;
    this.weather = weather;
  }

  public static Feed of(String content, User user, Weather weather) {
    return new Feed(content, user, weather);
  }

  public void updateContent(String newContent) {
    if (newContent != null && !newContent.equals(this.content)) {
      this.content = newContent;
    }
  }

  public void addComment(Comment comment) {
    this.comments.add(comment);
    comment.setFeed(this);
    this.commentCount++;
  }

  public void removeComment(Comment comment) {
    this.comments.remove(comment);
    comment.setFeed(null);
    this.commentCount--;
  }

  public void like() {
    this.likeCount++;
  }

  public void unlike() {
    this.likeCount--;
  }

  public void setClothesFeeds(List<Clothes> clothesList) {
    this.clothesFeeds.clear();
    List<ClothesFeed> associations = clothesList.stream()
        .map(clothes -> new ClothesFeed(clothes, this))
        .toList();
    this.clothesFeeds.addAll(associations);
  }

}

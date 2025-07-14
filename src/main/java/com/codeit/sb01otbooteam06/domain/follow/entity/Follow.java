package com.codeit.sb01otbooteam06.domain.follow.entity;


import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "follows",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_follows_pair",
        columnNames = {"follower_id", "followee_id"}
    ))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

  //아직 User 엔티티 없으므로 FK 대신 UUID
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "followee_id", nullable = false)
  private User followee;

  private Follow(User follower, User followee) {
    this.follower = follower;
    this.followee = followee;
  }

  public static Follow from(User follower, User followee) {
    // 자기 자신 팔로우 방지 정도만 사전 체크
    if (follower.equals(followee)) {
      throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
    }
    return new Follow(follower, followee);
  }
}


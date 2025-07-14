package com.codeit.sb01otbooteam06.domain.dm.entity;


import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direct_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessage extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  /* ── 정적 팩터리 ── */
  private DirectMessage(User sender, User receiver, String content) {
    this.sender = sender;
    this.receiver = receiver;
    this.content = content;
  }

  public static DirectMessage from(User sender, User receiver, String content) {
    return new DirectMessage(sender, receiver, content);
  }

  /** 두 UUID를 오름차순으로 연결해 생성하는 임시 키 – DB 저장 X */
  public static String generateKey(UUID a, UUID b) {
    return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
  }
}

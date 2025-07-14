package com.codeit.sb01otbooteam06.domain.user.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import jakarta.persistence.*;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
  private Profile profile;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder.Default
  @Column(nullable = false)
  private boolean locked = false;

  @ElementCollection
  @CollectionTable(name = "user_linked_oauth_providers", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "provider")
  private List<String> linkedOAuthProviders;

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public void changeRole(Role role) {
    this.role = role;
  }

  public void changeLocked(boolean locked) {
    this.locked = locked;
  }

  public void changePassword(String password) {
    this.password = password;
  }
}

package com.codeit.sb01otbooteam06.domain.user.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

  @Column
  private String temporaryPassword;

  @Column
  private LocalDateTime temporaryPasswordExpiration;

  @Column(nullable = false)
  @Builder.Default
  private boolean mustChangePassword = false;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Builder.Default
  @Column(nullable = false)
  private boolean locked = false;

  @Builder.Default
  @Column(nullable = false)
  private boolean forceLogout = false; //  추가

  @ElementCollection
  @CollectionTable(name = "user_linked_oauth_providers", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "provider")
  private List<String> linkedOAuthProviders = new ArrayList<>();

  @Column(nullable = true)
  private String provider;

  @Column(nullable = true)
  private String providerId;

  // ------------------------- 메서드 ---------------------------

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public void changeRole(Role role) {
    this.role = role;
  }

  public void setForceLogout(boolean forceLogout) {
    this.forceLogout = forceLogout;
  }

  public void changeLocked(boolean locked) {
    this.locked = locked;
  }

  public void changePassword(String password) {
    this.password = password;
    this.temporaryPassword = null;
    this.temporaryPasswordExpiration = null;
    this.mustChangePassword = false;
  }

  public void setTemporaryPassword(String tempPassword, LocalDateTime expiration) {
    this.temporaryPassword = tempPassword;
    this.temporaryPasswordExpiration = expiration;
    this.mustChangePassword = true;
  }

  public void clearTemporaryPassword() {
    this.temporaryPassword = null;
    this.temporaryPasswordExpiration = null;
    this.mustChangePassword = false;
  }

  public boolean isTemporaryPasswordValid(String rawPassword, LocalDateTime now) {
    return this.temporaryPassword != null &&
            this.temporaryPassword.equals(rawPassword) &&
            this.temporaryPasswordExpiration != null &&
            now.isBefore(this.temporaryPasswordExpiration);
  }

  public static User createSocialUser(String provider, String providerId, String email, String name) {
    return User.builder()
            .email(email)
            .password("SOCIAL")
            .name(name)
            .role(Role.USER)
            .locked(false)
            .linkedOAuthProviders(new ArrayList<>(List.of(provider)))
            .provider(provider)
            .providerId(providerId)
            .build();
  }

  public void addOAuthProvider(String provider) {
    if (!this.linkedOAuthProviders.contains(provider)) {
      this.linkedOAuthProviders.add(provider);
    }
  }
}

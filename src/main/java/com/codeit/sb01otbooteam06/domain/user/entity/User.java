package com.codeit.sb01otbooteam06.domain.user.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

  @ElementCollection
  @CollectionTable(name = "user_linked_oauth_providers", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "provider")
  private List<String> linkedOAuthProviders = new ArrayList<>();

  // ------------------------- 메서드 ---------------------------

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

  // Oauth 부분 일단 주석 처리

  /* //  소셜 로그인 연동 provider 추가
  public void addOAuthProvider(String provider) {
    if (!this.linkedOAuthProviders.contains(provider)) {
      this.linkedOAuthProviders.add(provider);
    }
  }/*

  /*소셜 로그인 회원가입용 생성자
  public static User ofSocial(String email, String name, String provider) {
    return User.builder()
            .email(email)
            .password("SOCIAL") // 실제 로그인은 패스워드 사용 안 함
            .name(name)
            .role(Role.USER)
            .locked(false)
            .linkedOAuthProviders(new ArrayList<>(List.of(provider)))
            .build(); */
  }


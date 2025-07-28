package com.codeit.sb01otbooteam06.domain.auth.jwt;

import com.codeit.sb01otbooteam06.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스 (액세스 토큰, 리프레시 토큰 분리 발급)
 */
@Component
@Slf4j
public class JwtTokenProvider {

  private final Key key;
  private final long accessTokenValidityInMillis;
  private final long refreshTokenValidityInMillis;

  @PostConstruct
  public void logSecretEnv() {
    String jwtSecret = System.getenv("JWT_SECRET");
    log.info("JWT_SECRET from env: {}", jwtSecret != null ? "[REDACTED]" : "NULL (Not Set)");
  }

  public JwtTokenProvider(
          @Value("${jwt.secret}") String secret,
          @Value("${jwt.access-expiration}") long accessExpiration,
          @Value("${jwt.refresh-expiration}") long refreshExpiration,
          @Value("${spring.profiles.active:}") String activeProfile
  ) {
    try {
      log.info("JWT_SECRET: {}", secret);
      //  무조건 Base64 디코딩
      byte[] keyBytes = Base64.getDecoder().decode(secret);
      this.key = Keys.hmacShaKeyFor(keyBytes);

    } catch (IllegalArgumentException | WeakKeyException e) {
      log.info("JWT Secret Key 오류 발생 : {}", e.getMessage());
      throw e;
    }

    this.accessTokenValidityInMillis = accessExpiration;
    this.refreshTokenValidityInMillis = refreshExpiration;
  }


  /**
   * 액세스 토큰 생성 (user 객체 전체 사용, role 포함)
   */
  public String generateAccessToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenValidityInMillis);

    return Jwts.builder()
            .claim("userId", user.getId().toString())
            .claim("email", user.getEmail())
            .claim("name", user.getName())
            .claim("role", user.getRole().name()) //  role 포함
            .setSubject(user.getId().toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * 기존 UUID 기반 액세스 토큰 생성
   */
  public String generateAccessToken(UUID userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenValidityInMillis);

    return Jwts.builder()
            .claim("userId", userId.toString())
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * 리프레시 토큰 생성
   */
  public String generateRefreshToken(UUID userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMillis);

    return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * 토큰에서 사용자 ID 추출
   */
  public UUID getUserId(String token) {
    Claims claims = parseClaims(token);
    return UUID.fromString(claims.getSubject());
  }

  /**
   * 토큰 유효성 검증
   */
  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 내부 메서드 - 토큰 파싱 및 클레임 추출
   */
  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }
}

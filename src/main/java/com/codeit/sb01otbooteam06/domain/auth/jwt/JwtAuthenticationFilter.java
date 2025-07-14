package com.codeit.sb01otbooteam06.domain.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT 인증 필터 - 요청마다 실행
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String bearer = request.getHeader("Authorization");
        System.out.println(">>> [JwtFilter] Authorization 헤더 원본 값: " + bearer);

        String token = resolveToken(request);
        System.out.println(">>> [JwtFilter] resolveToken 결과: " + token);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            UUID userId = jwtTokenProvider.getUserId(token);
            System.out.println(">>> [JwtFilter] 토큰 유효, userId: " + userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, null);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.out.println(">>> [JwtFilter] 토큰 없음 또는 유효하지 않음");
        }

        filterChain.doFilter(request, response);
    }


    /**
     * 요청 헤더에서 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        System.out.println(">>> [JwtFilter] resolveToken 내부 - Authorization 헤더: " + bearer);

        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            System.out.println(">>> [JwtFilter] Bearer 제거 후 토큰: " + token);
            return token;
        }
        return null;
    }

}

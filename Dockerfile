
FROM openjdk:17-jdk-slim AS builder


# --- 런타임 필수 패키지 설치 --------------------------
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl=7.74.0-1.3+deb11u15 && \
    rm -rf /var/lib/apt/lists/* && \
    adduser --disabled-password --gecos '' appuser

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --stacktrace --info

COPY src ./src

RUN ./gradlew build -x test --no-daemon

# 권장: non-root 실행
USER appuser

# 2. Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 포트 선언
EXPOSE 8080

# 런타임 헬스체크
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

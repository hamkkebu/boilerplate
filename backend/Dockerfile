# 1단계: Gradle 이미지로 빌드
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 build.gradle과 settings.gradle을 먼저 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드 (캐싱 최적화)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# clean build 실행
RUN gradle clean build --no-daemon -x test

# 2단계: 실행용 경량 이미지
FROM amazoncorretto:17-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

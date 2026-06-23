# ===== build: Gradle bootJar =====
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
# 의존성 레이어 캐시: 빌드 스크립트/래퍼 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
# 소스 복사 후 실행 가능 jar 생성 (테스트는 CI가 별도로 — 이미지 빌드는 jar만)
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

# ===== run: JRE 17 =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

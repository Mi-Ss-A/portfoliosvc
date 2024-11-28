# 1. Gradle 빌드 이미지 (빌드 단계)
FROM gradle:7.5.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 2. 실행 이미지 (최종 배포 단계)
FROM openjdk:17-jdk-slim
WORKDIR /app
# 필수 라이브러리 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    libfreetype6 libfreetype6-dev fonts-dejavu-core && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/build/libs/missa-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# gradle wrapper와 설정 파일 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# && 이후는 선택사항 - 그러나 추천
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || return 0

# 나머지 소스 복사
COPY . .

RUN chmod +x gradlew


# 실제 빌드 (테스트 스킵하고 싶으면 -x test 추가)
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app.jar
EXPOSE 8070
ENTRYPOINT ["java", "-jar", "/app.jar"]
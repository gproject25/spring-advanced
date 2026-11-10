FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .

RUN ls -l ./gradlew
RUN head -1 ./gradlew | od -An -t x1

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
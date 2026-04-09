# ── Stage 1: Build ────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn -q -DskipTests clean package

# ── Stage 2: Runtime (minimal Image) ──────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root User für Security
RUN addgroup -S eg && adduser -S eg -G eg
USER eg

COPY --from=builder /app/target/electrogutachten-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

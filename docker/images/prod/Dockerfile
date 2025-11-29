FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN set -eux \
  && apt-get update \
  && apt-get install -y --no-install-recommends ca-certificates curl \
  && apt-get purge -y --auto-remove \
  && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/target/*.jar /app/app.jar

RUN set -eux \
  && groupadd -r appgroup || true \
  && useradd -r -g appgroup -s /usr/sbin/nologin appuser || true \
  && chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseParallelGC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]

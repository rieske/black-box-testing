FROM eclipse-temurin:17.0.4_8-jre

EXPOSE 8080

ENV JAVA_OPTS -Xmx64m -Xms64m

ENTRYPOINT /opt/app/bin/app
HEALTHCHECK --interval=5s CMD curl -s -f http://localhost:8080/ping || exit 1

ADD app.tar /opt/
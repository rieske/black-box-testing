package io.github.rieske;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.JsonConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class TestEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEnvironment.class);

    private static final Network NETWORK = Network.newNetwork();
    private static final String SERVICE_NAME = "black-box-testing";
    private static final int APP_PORT = 8080;

    private static final String DB_HOST_ALIAS = "db";
    private static final String DB_SCHEMA = "db";
    private static final String JDBC_URL = "jdbc:postgresql://" + DB_HOST_ALIAS + ":5432/" + DB_SCHEMA;
    private static final String DB_USERNAME = "test";
    private static final String DB_PASSWORD = "test";

    private static final int WIREMOCK_PORT = 8080;
    private static final String WIREMOCK_HOST_ALIAS = "wiremock";

    private static final GenericContainer<?> SERVICE_CONTAINER = new GenericContainer<>(DockerImageName.parse(SERVICE_NAME + ":snapshot"))
            .withNetwork(NETWORK)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix(SERVICE_NAME))
            .withExposedPorts(APP_PORT)
            .waitingFor(Wait.forListeningPort())
            .withEnv("JDBC_URL", JDBC_URL)
            .withEnv("DB_USER", DB_USERNAME)
            .withEnv("DB_PASSWORD", DB_PASSWORD);

    private static final GenericContainer<?> DATABASE_CONTAINER = new GenericContainer<>(DockerImageName.parse("postgres:15.3-alpine"))
            .withNetwork(NETWORK)
            .withNetworkAliases(DB_HOST_ALIAS)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix(DB_HOST_ALIAS))
            .waitingFor(Wait.forListeningPort())
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"))
            .withEnv("POSTGRES_USER", DB_USERNAME)
            .withEnv("POSTGRES_PASSWORD", DB_PASSWORD)
            .withEnv("POSTGRES_DB", DB_SCHEMA);

    private static final GenericContainer<?> WIREMOCK_CONTAINER = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:2.32.0-alpine"))
            .withNetwork(NETWORK)
            .withNetworkAliases(WIREMOCK_HOST_ALIAS)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER).withPrefix(WIREMOCK_HOST_ALIAS))
            .withExposedPorts(WIREMOCK_PORT)
            .waitingFor(Wait.forListeningPort());

    static {
        DATABASE_CONTAINER.start();
        WIREMOCK_CONTAINER.start();
        SERVICE_CONTAINER.start();

        var logConfig = LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
        var config = RestAssuredConfig.config()
                .logConfig(logConfig)
                .jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(apiUrl())
                .setContentType(ContentType.JSON)
                .setConfig(config)
                .build();
    }

    private static final WireMock WIREMOCK = new WireMock(WIREMOCK_CONTAINER.getHost(), WIREMOCK_CONTAINER.getMappedPort(WIREMOCK_PORT));

    private static String apiUrl() {
        return "http://" + SERVICE_CONTAINER.getHost() + ":" + SERVICE_CONTAINER.getMappedPort(APP_PORT);
    }
}

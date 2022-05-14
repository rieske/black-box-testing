package io.github.rieske;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import spark.Request;

import javax.sql.DataSource;

import java.util.UUID;

import static spark.Spark.awaitInitialization;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

class Server {
    private final Service service;

    public Server(String jdbcUrl, String dbUser, String dbPassword) {
        var dataSource = pooledDataSource(postgresDataSource(jdbcUrl, dbUser, dbPassword));
        Flyway.configure().dataSource(dataSource).load().migrate();
        this.service = new Service(new Repository(dataSource));
    }

    int start(int port) {
        port(port);

        path("/api", () -> path("/messages", () -> {
            post("", ((request, response) -> {
                var id = service.addMessage(request.body());
                response.header("Location", "/api/messages/" + id);
                response.status(201);
                return "";
            }));
            get("/:id", (request, response) -> {
                var id = getUUID(request, "id");
                return service.findMessage(id).map(Message::payload).orElse(null);
            });
            delete("/:id", ((request, response) -> {
                var id = getUUID(request, "id");
                service.deleteMessage(id);
                response.status(204);
                return "";
            }));
        }));

        get("/ping", (req, res) -> "");

        exception(IllegalArgumentException.class, (exception, request, response) -> {
            response.body(exception.getMessage());
            response.status(400);
        });

        awaitInitialization();
        return port();
    }

    private static UUID getUUID(Request request, String param) {
        try {
            return UUID.fromString(request.params(param));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static DataSource postgresDataSource(String jdbcUrl, String username, String password) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private static DataSource pooledDataSource(DataSource dataSource) {
        var config = new HikariConfig();
        config.setPoolName("db");
        config.setMaximumPoolSize(5);
        config.setInitializationFailTimeout(1000 * 5);
        config.setDataSource(dataSource);
        return new HikariDataSource(config);
    }
}

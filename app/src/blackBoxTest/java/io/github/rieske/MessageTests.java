package io.github.rieske;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@BlackBoxTest
class MessageTests {
    @Test
    void createsNewMessage() {
        var location = given().body("some message").when().post("/api/messages")
                .then().statusCode(201)
                .extract().header("Location");

        when().get(location).then().statusCode(200).body(equalTo("some message"));
    }

    @Test
    void badRequestOnNoBody() {
        when().post("/api/messages").then().statusCode(400);
    }

    @Test
    void notFoundWhenRequestingUsingBadUUID() {
        when().get("/api/messages/foo").then().statusCode(404);
    }

    @Test
    void notFoundWhenRequestingNonExistingMessage() {
        when().get("/api/messages/" + UUID.randomUUID()).then().statusCode(404);
    }

    @Test
    void canDeleteMessage() {
        var location = given().body("some message").when().post("/api/messages")
                .then().statusCode(201)
                .extract().header("Location");

        when().delete(location).then().statusCode(204);
        when().get(location).then().statusCode(404);
    }

    @Test
    void noContentWhenDeletingMessageUsingBadUUID() {
        when().delete("/api/messages/foo").then().statusCode(204);
    }

    @Test
    void noContentWhenDeletingNonExistingMessage() {
        when().delete("/api/messages/" + UUID.randomUUID()).then().statusCode(204);
    }
}

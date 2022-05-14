package io.github.rieske;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;

class PingTest extends BlackBoxTest {
    @Test
    void respondsWith200() {
        when().get("/ping").then().statusCode(200);
    }
}

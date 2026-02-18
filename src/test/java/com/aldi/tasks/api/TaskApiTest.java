package com.aldi.tasks.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;

public class TaskApiTest {

    private static final String BASE_URI  = "http://localhost";
    private static final int    BASE_PORT = 8080;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port    = BASE_PORT;
    }

    // used as setup by tests that need an existing task
    private int createTestTask() {
        return given()
                    .contentType(ContentType.JSON)
                    .body("""
                          {
                            "title":       "Test task",
                            "description": "Setup task"
                          }
                          """)
               .when()
                    .post("/tasks")
               .then()
                    .statusCode(201)
               .extract()
                    .path("id");
    }

    @Test
    @DisplayName("POST /tasks - valid payload returns 201 and match schema")
    void createTask_validPayload() {
        given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "title":       "Buy some stuff",
                    "description": "Milk, eggs, bread"
                  }
                  """)
        .when()
            .post("/tasks")
        .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/task-schema.json"))
            .body("title",  equalTo("Buy some stuff"))
            .body("status", equalTo("OPEN"));
    }

    @Test
    @DisplayName("POST /tasks - missing title returns 400")
    void createTask_missingTitle() {
        given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "description": "No title provided"
                  }
                  """)
        .when()
            .post("/tasks")
        .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    @DisplayName("GET /tasks/{id} – existing task should return 200 and match schema")
    void getTask_existingId() {
        int id = createTestTask();

        given()
            .log().ifValidationFails()
            .pathParam("id", id)
        .when()
            .get("/tasks/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/task-schema.json"))
            .body("id", equalTo(id));

        given().pathParam("id", id).when().delete("/tasks/{id}");
    }

    @Test
    @DisplayName("GET /tasks/{id} – non-existing task should return 404")
    void getTask_nonExistingId() {
        given()
            .log().ifValidationFails()
            .pathParam("id", 99999)
        .when()
            .get("/tasks/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    @DisplayName("PUT /tasks/{id} – updating an existing task should return 200 with updated values")
    void updateTask_existingId() {
        int id = createTestTask();

        given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON)
            .pathParam("id", id)
            .body("""
                  {
                    "title":       "Updated title",
                    "description": "Updated description",
                    "status":      "IN_PROGRESS"
                  }
                  """)
        .when()
            .put("/tasks/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("id",     equalTo(id))
            .body("title",  equalTo("Updated title"))
            .body("status", equalTo("IN_PROGRESS"));

        given().pathParam("id", id).when().delete("/tasks/{id}");
    }

    @Test
    @DisplayName("PUT /tasks/{id} – updating a non-existing task should return 404")
    void updateTask_nonExistingId() {
        given()
            .log().ifValidationFails()
            .contentType(ContentType.JSON)
            .pathParam("id", 99999)
            .body("""
                  {
                    "title":  "Not existing task",
                    "status": "OPEN"
                  }
                  """)
        .when()
            .put("/tasks/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /tasks/{id} – deleting an existing task should return 204")
    void deleteTask_existingId() {
        int id = createTestTask();

        given()
            .log().ifValidationFails()
            .pathParam("id", id)
        .when()
            .delete("/tasks/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(204)
            .body(emptyOrNullString());
    }

    @Test
    @DisplayName("DELETE /tasks/{id} – deleting a non-existing task should return 404")
    void deleteTask_nonExistingId() {
        given()
            .log().ifValidationFails()
            .pathParam("id", 99999)
        .when()
            .delete("/tasks/{id}")
        .then()
            .log().ifValidationFails()
            .statusCode(404);
    }
}

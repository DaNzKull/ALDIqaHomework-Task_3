# Task 3 – API Testing

Simple REST-assured test suite for a task management API covering the basic CRUD endpoints.

## Stack

- Java 17
- REST-assured 5.4.0
- JUnit 5
- JSON Schema Validator

## What's covered

- `POST /tasks` – create a task (valid payload + missing title)
- `GET /tasks/{id}` – fetch a task (existing + not found)
- `PUT /tasks/{id}` – update a task (existing + not found)
- `DELETE /tasks/{id}` – delete a task (existing + not found)

Each test is independent. Tests that need an existing task use a private helper to create one and clean up after themselves.

Schema validation is done on POST and GET responses using a JSON schema file under `src/test/resources/schemas/`.

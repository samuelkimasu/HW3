# HW3

# ReplyTest.java - Unit Tests for Reply Functionality

This test suite validates the reply-related features in a JavaFX-based Q&A application backed by a relational database. The `ReplyTest.java` file uses JUnit 5 to ensure that creating, reading, updating, and deleting replies associated with answers functions as expected.

## Overview

The tests are implemented using the JUnit 5 framework and interact with the database via the `DatabaseHelper` class. Additional domain classes used include `Question` and `Answer`.

Each test prints clear and descriptive output to the console to help trace the test logic and verify that data is being correctly handled.

## Test Structure

### Test Lifecycle

Each test method follows this structure:

- **Setup (@BeforeEach):** Connects to the database, adds a test question and a base answer.
- **Test Execution:** Runs a specific CRUD operation involving replies.
- **Teardown (@AfterEach):** Deletes all answers for the test question, deletes the test question itself, and closes the database connection.

### Create Tests

| Method | Purpose |
|--------|---------|
| `testAddReply_Positive` | Adds a valid reply and verifies it was inserted correctly. |
| `testAddReply_Negative_EmptyReply` | Attempts to add an empty reply, expecting a `SQLException`. |

### Read Tests

| Method | Purpose |
|--------|---------|
| `testGetReplies_Positive_MultipleReplies` | Adds two replies and confirms both are retrieved. |
| `testGetReplies_Negative_NoReplies` | Ensures that no replies are returned if none were added. |

### Update Tests

| Method | Purpose |
|--------|---------|
| `testUpdateReply_Positive` | Updates an existing reply and verifies the new content. |
| `testUpdateReply_Negative_EmptyContent` | Updates a reply to an empty string. Assumes the database allows this and verifies the result. |

### Delete Tests

| Method | Purpose |
|--------|---------|
| `testDeleteReply_Positive` | Adds and then deletes a reply. Verifies it no longer exists. |
| `testDeleteReply_Negative_NonExistent` | Attempts to delete a reply that does not exist. Expects no crash or exception, and verifies the state remains unchanged. |

## Running the Tests

To execute the tests, use your preferred Java IDE (such as IntelliJ IDEA or Eclipse) or run via command line with a build tool like Maven or Gradle that includes JUnit 5 support.

Make sure your database is properly configured and accessible via the `DatabaseHelper` class before running the tests.

## Zoom Recording

**Zoom Recording Link**: [SCREENCAST]([https://zoom.us/](https://asu.zoom.us/rec/share/Ea4JTwTJjvHkGWos6uMV9VBtYQ6ujrEW3gT2ExsUkWtZk5THGmObY1srU4r1cOAX.R_iLGdaROR8pCKMv?startTime=1742882062000
Passcode: Q@7EBUmz))

---


package application;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import databasePart1.DatabaseHelper;

class PrivateMessageTest {

    private static DatabaseHelper dbHelper;
    private static String TEST_SENDER;
    private static String TEST_RECEIVER;
    private static String TEST_QUESTION_ID;

    @BeforeAll
    static void setUp() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper.connectToDatabase();
        
        // Generate unique identifiers for each test run
        TEST_SENDER = "testSender_" + UUID.randomUUID().toString().substring(0, 8);
        TEST_RECEIVER = "testReceiver_" + UUID.randomUUID().toString().substring(0, 8);
        TEST_QUESTION_ID = UUID.randomUUID().toString();

        // Ensure test users exist
        List<String> roles = new ArrayList<>();
        roles.add("student");
        dbHelper.register(new User(TEST_SENDER, "password", roles, "sender@test.com", "Test Sender"));
        dbHelper.register(new User(TEST_RECEIVER, "password", roles, "receiver@test.com", "Test Receiver"));

        // Create a test question
        Question testQuestion = new Question(TEST_QUESTION_ID, "Test question content", "Test question description", TEST_SENDER);
        dbHelper.addQuestion(testQuestion);
    }

    @AfterAll
    static void tearDown() {
        try {
            // Delete all test private messages
            clearAllTestMessages();

            // Delete the test question
            dbHelper.deleteQuestion(TEST_QUESTION_ID);

            // Delete test users
            dbHelper.deleteUser(TEST_SENDER);
            dbHelper.deleteUser(TEST_RECEIVER);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbHelper.closeConnection();
        }
    }

    @BeforeEach
    void clearMessages() {
        clearAllTestMessages();
    }

    private static void clearAllTestMessages() {
        try {
            List<PrivateMessage> messages = dbHelper.getPrivateMessagesForUser(TEST_SENDER);
            messages.addAll(dbHelper.getPrivateMessagesForUser(TEST_RECEIVER));
            for (PrivateMessage message : messages) {
                try {
                    dbHelper.deletePrivateMessage(message.getMessageId());
                } catch (SQLException e) {
                    System.err.println("Error deleting message: " + e.getMessage());
                }
            }
            // Verify that all messages are deleted
            assertTrue(dbHelper.getPrivateMessagesForUser(TEST_SENDER).isEmpty());
            assertTrue(dbHelper.getPrivateMessagesForUser(TEST_RECEIVER).isEmpty());
        } catch (SQLException e) {
            System.err.println("Error clearing messages: " + e.getMessage());
        }
    }

    @Test
    // CRUD: CREATE, READ (Positive outcome)
    // Tests if a private message can be made, pulled from the database, and successfully read
    void testCreateValidPrivateMessage() throws SQLException {
        PrivateMessage message = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "Test message content");
        dbHelper.addPrivateMessage(message);

        PrivateMessage retrieved = dbHelper.getPrivateMessage(message.getMessageId());
        assertNotNull(retrieved);
        assertEquals("Test message content", retrieved.getContent());

        // Clean up
        dbHelper.deletePrivateMessage(message.getMessageId());
    }

    @Test
    // CRUD: CREATE (Negative outcome)
    // Tests that an empty private message cannot be created (no content) and throws an error
    void testCreatePrivateMessageWithNullContent() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, null);
        });
    }

    @Test
    // CRUD: READ (Negative outcome)
    // Tests that a private message with an invalid ID cannot be read and throws an error
    void testReadNonExistentPrivateMessage() throws SQLException {
        assertNull(dbHelper.getPrivateMessage("non_existent_id"));
    }

    @Test
    // CRUD: UPDATE, READ (Positive outcome)
    // Tests that a private message can be updated and the updated content can be read
    void testUpdateExistingPrivateMessage() throws SQLException {
        PrivateMessage message = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "Original content");
        dbHelper.addPrivateMessage(message);

        message.setContent("Updated content");
        dbHelper.updatePrivateMessage(message);

        PrivateMessage updated = dbHelper.getPrivateMessage(message.getMessageId());
        assertEquals("Updated content", updated.getContent());

        // Clean up
        dbHelper.deletePrivateMessage(message.getMessageId());
    }

    @Test
    // CRUD: UPDATE (Negative outcome)
    // Tests that a private message that has not been added to the database (nonexistent) cannot be updated
    void testUpdateNonExistentPrivateMessage() {
        PrivateMessage message = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "Non-existent");
        assertThrows(SQLException.class, () -> dbHelper.updatePrivateMessage(message));
    }

    @Test
    // CRUD: DELETE (Positive outcome)
    // Tests that a private message can be deleted and verifies it is no longer in the database
    void testDeleteExistingPrivateMessage() throws SQLException {
        PrivateMessage message = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "To be deleted");
        dbHelper.addPrivateMessage(message);

        dbHelper.deletePrivateMessage(message.getMessageId());
        assertNull(dbHelper.getPrivateMessage(message.getMessageId()));
    }

    @Test
    // CRUD: READ (Positive outcome)
    // Tests if all private messages for a user can be read
    void testGetPrivateMessagesForUser() throws SQLException {
        PrivateMessage message1 = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "Message 1");
        PrivateMessage message2 = new PrivateMessage(TEST_RECEIVER, TEST_SENDER, TEST_QUESTION_ID, "Message 2");
        dbHelper.addPrivateMessage(message1);
        dbHelper.addPrivateMessage(message2);

        List<PrivateMessage> senderMessages = dbHelper.getPrivateMessagesForUser(TEST_SENDER);
        assertEquals(2, senderMessages.size());

        List<PrivateMessage> receiverMessages = dbHelper.getPrivateMessagesForUser(TEST_RECEIVER);
        assertEquals(2, receiverMessages.size());

        // Clean up
        dbHelper.deletePrivateMessage(message1.getMessageId());
        dbHelper.deletePrivateMessage(message2.getMessageId());
    }

    @Test
    // CRUD: Read (Positive outcome)
    // Tests if a user can read the number of unread private messages they have
    void testGetUnreadPrivateMessageCount() throws SQLException {
        PrivateMessage message1 = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "Unread 1");
        PrivateMessage message2 = new PrivateMessage(TEST_SENDER, TEST_RECEIVER, TEST_QUESTION_ID, "Unread 2");
        dbHelper.addPrivateMessage(message1);
        dbHelper.addPrivateMessage(message2);

        int unreadCount = dbHelper.getUnreadPrivateMessageCount(TEST_RECEIVER);
        assertEquals(2, unreadCount);

        message1.markAsRead();
        dbHelper.updatePrivateMessage(message1);

        unreadCount = dbHelper.getUnreadPrivateMessageCount(TEST_RECEIVER);
        assertEquals(1, unreadCount);

        // Clean up
        dbHelper.deletePrivateMessage(message1.getMessageId());
        dbHelper.deletePrivateMessage(message2.getMessageId());
    }
}
package application;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ReplyTest {

    private DatabaseHelper db;
    private String questionID;
    private String parentAnswerID;

    @BeforeEach
    public void setUp() throws SQLException {
        System.out.println("=== Setting up test case ===");
        db = new DatabaseHelper();
        db.connectToDatabase();

        questionID = UUID.randomUUID().toString();
        parentAnswerID = UUID.randomUUID().toString();

        Question q = new Question(questionID, "What is Java?", "Explain OOP in Java", "testUser");
        db.addQuestion(q);
        System.out.println("Added question: '" + q.getQuestionContent() + "' by user 'testUser'");

        Answer a = new Answer(parentAnswerID, questionID, "Java is a language", "responder1");
        db.addAnswer(a);
        System.out.println("Added base answer: '" + a.getAnswerContent() + "' by user 'responder1'");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        System.out.println("Cleaning up after test...\n");
        db.deleteAllAnswersForQuestion(questionID);
        db.deleteQuestion(questionID);
        db.closeConnection();
    }

    // CREATE TESTS
    @Test
    public void testAddReply_Positive() throws SQLException {
        System.out.println(">> testAddReply_Positive: Adding reply by user 'user123'");
        db.addReply(questionID, parentAnswerID, "This is a helpful reply", "user123");
        List<Answer> replies = db.getRepliesByAnswerID(parentAnswerID);

        assertFalse(replies.isEmpty());
        assertEquals("This is a helpful reply", replies.get(0).getAnswerContent());
        System.out.println("✓ Reply added successfully: " + replies.get(0).getAnswerContent());
    }

    @Test
    public void testAddReply_Negative_EmptyReply() throws SQLException {
        System.out.println(">> testAddReply_Negative_EmptyReply: Attempting to add empty reply by 'user123'");
        assertThrows(SQLException.class, () -> {
            db.addReply(questionID, parentAnswerID, "", "user123");
        });
        System.out.println("✓ Expected SQLException thrown for empty reply");
    }

    // READ TESTS
    @Test
    public void testGetReplies_Positive_MultipleReplies() throws SQLException {
        System.out.println(">> testGetReplies_Positive_MultipleReplies: Adding 2 replies to parentAnswerID");

        db.addReply(questionID, parentAnswerID, "First reply", "user1");
        db.addReply(questionID, parentAnswerID, "Second reply", "user2");

        List<Answer> replies = db.getRepliesByAnswerID(parentAnswerID);
        System.out.println("Replies retrieved: " + replies.size());
        for (Answer reply : replies) {
            System.out.println("- " + reply.getAnswerContent() + " by " + reply.getAuthorID());
        }

        assertEquals(2, replies.size());
    }

    @Test
    public void testGetReplies_Negative_NoReplies() throws SQLException {
        System.out.println(">> testGetReplies_Negative_NoReplies: No replies expected for parentAnswerID");
        List<Answer> replies = db.getRepliesByAnswerID(parentAnswerID);
        assertTrue(replies.isEmpty());
        System.out.println("✓ No replies found, as expected");
    }

    // UPDATE TESTS
    @Test
    public void testUpdateReply_Positive() throws SQLException {
        System.out.println(">> testUpdateReply_Positive: Updating reply content for user 'userX'");

        db.addReply(questionID, parentAnswerID, "Original Reply", "userX");
        Answer reply = db.getRepliesByAnswerID(parentAnswerID).get(0);

        db.updateReply(reply.getAnswerID(), "Updated Reply Content");

        List<Answer> updatedReplies = db.getRepliesByAnswerID(parentAnswerID);
        assertEquals("Updated Reply Content", updatedReplies.get(0).getAnswerContent());
        System.out.println("✓ Reply updated to: " + updatedReplies.get(0).getAnswerContent());
    }

    @Test
    public void testUpdateReply_Negative_EmptyContent() throws SQLException {
        System.out.println(">> testUpdateReply_Negative_EmptyContent: Updating reply to empty string");

        db.addReply(questionID, parentAnswerID, "Original Reply", "userX");
        Answer reply = db.getRepliesByAnswerID(parentAnswerID).get(0);

        db.updateReply(reply.getAnswerID(), "");

        List<Answer> updatedReplies = db.getRepliesByAnswerID(parentAnswerID);
        assertEquals("", updatedReplies.get(0).getAnswerContent());
        System.out.println("✓ Reply content updated to empty string");
    }

    // DELETE TESTS
    @Test
    public void testDeleteReply_Positive() throws SQLException {
        System.out.println(">> testDeleteReply_Positive: Deleting reply from 'userDelete'");

        db.addReply(questionID, parentAnswerID, "Reply to delete", "userDelete");
        Answer reply = db.getRepliesByAnswerID(parentAnswerID).get(0);

        db.deleteReply(parentAnswerID, reply.getAnswerContent());

        List<Answer> remainingReplies = db.getRepliesByAnswerID(parentAnswerID);
        assertTrue(remainingReplies.isEmpty());
        System.out.println("✓ Reply deleted successfully");
    }

    @Test
    public void testDeleteReply_Negative_NonExistent() throws SQLException {
        System.out.println(">> testDeleteReply_Negative_NonExistent: Trying to delete non-existent reply content");
        db.deleteReply(parentAnswerID, "nonexistent reply content");

        List<Answer> replies = db.getRepliesByAnswerID(parentAnswerID);
        assertTrue(replies.isEmpty());
        System.out.println("✓ No exception thrown and no replies found, as expected");
    }
}

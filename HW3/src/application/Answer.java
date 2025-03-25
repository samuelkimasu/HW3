package application;
import java.time.*;
import java.util.ArrayList;
import java.util.List;



public class Answer {
	private String answerID;			//each answer will be assigned as unique randomized ID
    private String questionID;			//question ID to identify which question thread falls under
    private String questionContent;		//content of the question the answer is under
    private String answerContent;		//content of question
    private String authorID;			//username of the user who wrote the question
    private LocalDateTime timeCreated;	//time created
    private boolean isAccepted;			//whether or not the answer was marked as the best answer to resolve the question
    private List<String> replies;
    private String parentAnswerID;

    public Answer(String ID, String questionID, String answerContent, String authorID) {
        if (ID == null) {
            throw new IllegalArgumentException("Answer ID cannot be null or empty");
        }
        if (questionID == null) {
            throw new IllegalArgumentException("Question ID cannot be null or empty");
        }
        if (answerContent == null) {
            throw new IllegalArgumentException("Answer content cannot be null or empty");
        }
        if (authorID == null) {
            throw new IllegalArgumentException("Author ID cannot be null or empty");
        }

        this.answerID = ID;
        this.questionID = questionID;
        this.answerContent = answerContent.trim();
        this.authorID = authorID;
        this.timeCreated = LocalDateTime.now();
        this.isAccepted = false;
        this.replies = new ArrayList<>();
    }

    public String getAnswerID() {
        return answerID;
    }
    
    public String getQuestionID() {
        return questionID;
    }
    
    //Feb25 Samuel
    public String getParentAnswerID() {
        return parentAnswerID;
    }
    
    public void setParentAnswerID(String parentAnswerID) {
        this.parentAnswerID = parentAnswerID;
    }
    
    public String getAnswerContent() {
        return answerContent;
    }
    
    public void setAnswerContent(String content) {
    	//input validation: ensure that Answer content is filled out and not empty
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be null or empty");
        }
        this.answerContent = content.trim();
    }
    
    public String getAuthorID() {
        return authorID;
    }
    
    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }
    
    public boolean isAccepted() {
        return isAccepted;
    }
    
    //Feb 25 Samuel
    public void setReplies(List<String> replies) {
        this.replies = replies;
    }
    public List<String> getReplies() {
        if (replies == null) {
            replies = new ArrayList<>(); // Prevent null issues
        }
        return replies;
    }
    public void addReply(String reply) {
        replies.add(reply);
    }
    
    public void setAccepted(boolean accepted) {
        this.isAccepted = accepted;
    }
}
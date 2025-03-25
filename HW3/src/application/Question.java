package application;
import java.time.*;


public class Question {
	//class for questions
    private String questionID;
    private String questionContent;
    private String questionDesc;  // Variable for question description
    private String authorID;
    private LocalDateTime timeCreated;
    private LocalDateTime timeUpdated;
    private boolean isResolved;
    private String resolvedAnswerID;

    public Question(String ID, String questionContent, String questionDesc, String authorID) {
        if (ID == null) {
        	//throws exception if Question ID is not generated correctly
            throw new IllegalArgumentException("Question ID cannot be null or empty");
        }      
        if (questionContent == null) {
        	//throws exception if question content is empty or not received
            throw new IllegalArgumentException("Question content cannot be null or empty");
        }
        if (authorID == null) {
        	//throws exception is author ID is not received
            throw new IllegalArgumentException("Author ID cannot be null or empty");
        }
        
        this.questionID = ID;
        this.questionContent = questionContent.trim();
        this.questionDesc = (questionDesc != null) ? questionDesc.trim() : "";
        this.authorID = authorID;
        this.timeCreated = LocalDateTime.now();
        this.timeUpdated = this.timeCreated;
        this.isResolved = false;
        this.resolvedAnswerID = null;
    }

    public String getQuestionID() {
    	return questionID;
    }
    
    public String getQuestionContent() {
    	return questionContent;
    }
    
    public String getQuestionDesc() {
        return questionDesc;
    }
    
    public void setQuestionContent(String content) {
        if (content == null) {
        	//throws exception if question content is empty or not received
            throw new IllegalArgumentException("Question content cannot be null or empty");
        }
        this.questionContent = content.trim();
        this.timeUpdated = LocalDateTime.now();
    }
    
    public void setQuestionDesc(String desc) {
        if (desc == null) {
            throw new IllegalArgumentException("Question description cannot be null");
        }
        this.questionDesc = desc.trim(); // Allows empty description but not null
        this.timeUpdated = LocalDateTime.now();
    }
    
    public String getAuthorID() {
        return authorID;
    }
    
    public LocalDateTime getTimeCreated() {
    	return timeCreated;
    }
    
    public LocalDateTime getTimeUpdated() {
    	return timeUpdated;
    }
    
    public boolean isResolved() {
    	return isResolved;
    }
    
    public void setResolved(boolean resolved) {
        this.isResolved = resolved;
        if (!resolved) {
            this.resolvedAnswerID = null;
        }
        this.timeUpdated = LocalDateTime.now();
    }
    
    public String getResolvedAnswerId() {
    	return resolvedAnswerID;
    }
    
    public void setResolvedAnswerId(String answerId) {
        if (isResolved && (answerId == null || answerId.trim().isEmpty())) {
            throw new IllegalArgumentException("Resolved answer ID cannot be null or empty when question is resolved");
        }
        this.resolvedAnswerID = answerId;
        this.timeUpdated = LocalDateTime.now();
    }
}
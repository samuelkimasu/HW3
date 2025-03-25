package application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PrivateMessage class represents the object for a private message in the system.
 * It contains the details for a private message such as the messageId, senderId, 
 * receiverId, message content, and more.
 */
public class PrivateMessage {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String questionId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    // Feb24 Shanali: added attributes to implement for a reply
    private List<PrivateMessage> replies;
    private String parentMessageId;

    // Feb20 Andrew: Constructor for an original private message
    public PrivateMessage(String senderId, String receiverId, String questionId, String content) {
    	if (content == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.messageId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.questionId = questionId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        // Feb25 Shanali: added attributes to implement for a reply
        this.replies = new ArrayList<>();
        // This is an original message, so it has no parentMessageId
        this.parentMessageId = null;  
    }
    
    // Feb20 Andrew: Constructor for an reply to a private message
    public PrivateMessage(String messageId, String senderId, String receiverId, String questionId, String content, String parentMessageId) {
    	if (content == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.questionId = null; // This is a reply, which doesn't refer to a direct question
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        // Feb25 Shanali: added attributes to implement for a reply
        this.parentMessageId = parentMessageId;
        this.replies = new ArrayList<>();
    }
    // Feb20 Andrew: Getters
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    // Feb25 Shanali: getter method for added attributes
    public String getParentMessageId() { return parentMessageId;}
    public List<PrivateMessage> getReplies() {
        if (replies == null) {
        	// Creating an empty reply list for messages with no replies
            replies = new ArrayList<>(); 
        }
        return replies;
    }

    // Feb20 Andrew: Setters
    public void setContent(String content) { this.content = content; }
    public void markAsRead() { this.isRead = true; }
    // Feb25 Shanali: setter method for added attributes
    public void setReplies(List<PrivateMessage> replies) { this.replies = replies; }
    public void setParentMessageId(String parentMessageId) {this.parentMessageId = parentMessageId; }
    
    // Feb20 Andrew: Adding a reply to a private message
    public void addReply(PrivateMessage reply) {
        replies.add(reply);
    }
}


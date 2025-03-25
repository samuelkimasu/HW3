package application;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * PrivateMessageList class represents all the private messages for a user in the system.
 * It contains the user's private message details such as their private messages and 
 * number of unread private messages. 
 */
public class PrivateMessageList {
    private List<PrivateMessage> messages;

    public PrivateMessageList() {
        this.messages = new ArrayList<>();
    }

    // Feb22 Andrew: CRUD operations
    public void addMessage(PrivateMessage message) {
        messages.add(message);
    }

    public void updateMessage(PrivateMessage message) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageId().equals(message.getMessageId())) {
                messages.set(i, message);
                return;
            }
        }
    }

    public void deleteMessage(String messageId) {
        messages.removeIf(m -> m.getMessageId().equals(messageId));
    }

    // Feb22 Andrew: getting a message, unread messages, or an unread message count
    public PrivateMessage getMessage(String messageId) {
        return messages.stream()
                .filter(m -> m.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    public List<PrivateMessage> getMessagesForUser(String userId) {
        return messages.stream()
                .filter(m -> m.getSenderId().equals(userId) || m.getReceiverId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<PrivateMessage> getUnreadMessagesForUser(String userId) {
        return messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .collect(Collectors.toList());
    }

    public int getUnreadMessageCount(String userId) {
        return (int) messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .count();
    }
}


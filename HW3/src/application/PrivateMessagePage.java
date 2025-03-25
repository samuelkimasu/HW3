package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

import java.util.List;
import java.util.UUID;
import java.sql.SQLException;

/**
 * PrivateMessagePage class represents the interface that allows user to interact
 * with the private messaging features, including creating, reading, editing, and
 * deleting private messages.
 */
public class PrivateMessagePage {
    private DatabaseHelper database;
    private User currentUser;
    private VBox messageList;

    // Feb22 Andrew: Setting up the page UI
    public void show(DatabaseHelper database, Stage primaryStage, User user) {
        this.database = database;
        this.currentUser = user;

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        Label headerLabel = new Label("Private Messages");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        mainLayout.setTop(headerLabel);

        messageList = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(messageList);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);

        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> goBack(primaryStage));
        mainLayout.setBottom(backButton);

        refreshMessages();

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Private Messages");
    }

    // Feb22 Andrew: Reloading and re-displaying the list of current private messages
    private void refreshMessages() {
    	messageList.getChildren().clear();
    	
    	try {
    		// Feb26 Shanali: Getting all original messages (no replies)
    		List<PrivateMessage> messages = database.getPrivateMessagesForUser(currentUser.getUserName());
    		for (PrivateMessage message : messages) {
    			// Creating a message box for the initial private message
                VBox messageBox = createMessageBox(message);
                // Getting all current replies to this private message
                List<PrivateMessage> replyObjects = database.getRepliesToPrivateMessage(message.getMessageId());
                
                // Displaying the current replies to this private message
                if (!replyObjects.isEmpty()) {
                	VBox repliesBox = new VBox(5);
                	
                	for (PrivateMessage reply : replyObjects) {
                		// Setting parentAnswerID if it hasn't previously been set
                        reply.setParentMessageId(reply.getParentMessageId());
                        
                        HBox replyBox = new HBox(10);
                        VBox replyContentBox = new VBox(5);

                        Label replyLabel = new Label(reply.getContent());
                        replyLabel.setWrapText(true);

                        Label replyAuthorLabel = new Label("Replied by: " + reply.getSenderId());
                        replyAuthorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                        
                        // Feb26 Shanali: Displaying the reply's 'read' status
                        Label readStatusLabel = new Label(reply.isRead() ? "Read" : "Unread");
                        readStatusLabel.setStyle(reply.isRead() ? 
                            "-fx-text-fill: green; -fx-font-weight: bold;" : 
                            "-fx-text-fill: orange;");

                        replyContentBox.getChildren().addAll(replyAuthorLabel, readStatusLabel, replyLabel);
                        replyBox.getChildren().add(replyContentBox);
                        
                        // Display edit and delete buttons if the current user is the reply's author
                        // Display reply button if the current user is not the reply's author   
                        if (currentUser.getUserName().equals(reply.getSenderId())) {
                        	Button editReplyButton = new Button("Edit");
                        	editReplyButton.setOnAction(e -> editMessage(reply));
                        	
                            Button deleteReplyButton = new Button("Delete");
                            deleteReplyButton.setOnAction(e -> deleteMessage(reply));
                            
                            replyBox.getChildren().addAll(replyAuthorLabel, editReplyButton, deleteReplyButton);
                        } else {
                        	Button replyToReplyButton = new Button("Reply");
                        	replyToReplyButton.setOnAction(e -> replyToMessage(reply));
                            replyBox.getChildren().add(replyToReplyButton);
                        }
                        
                        // Add the current reply to the box of replies
                        repliesBox.getChildren().add(replyBox);
                	}
                	messageBox.getChildren().add(repliesBox);
                }
                messageList.getChildren().add(messageBox);
            }
    		
        } catch (SQLException e) {
            showError("Error loading replies: " + e.getMessage());
        }
    }

// Feb22 Andrew: CRUD for a private message
    private VBox createMessageBox(PrivateMessage message) {
        VBox messageBox = new VBox(5);
        messageBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label senderLabel = new Label("From: " + (message.getSenderId().equals(currentUser.getUserName()) ? "You" : message.getSenderId()));
        
        // Feb26 Shanali: Displaying the reply's 'read' status
        Label readStatusLabel = new Label(message.isRead() ? "Read" : "Unread");
        readStatusLabel.setStyle(message.isRead() ? 
            "-fx-text-fill: green; -fx-font-weight: bold;" : 
            "-fx-text-fill: orange;");
        
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);

        HBox buttonBox = new HBox(10);
        // Display edit and delete buttons if the current user is the reply's author
        // Display reply button if the current user is not the reply's author   
        if (message.getSenderId().equals(currentUser.getUserName())) {
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> editMessage(message));
            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> deleteMessage(message));
            buttonBox.getChildren().addAll(editButton, deleteButton);
        } else {
            Button replyButton = new Button("Reply");
            replyButton.setOnAction(e -> replyToMessage(message));
            buttonBox.getChildren().add(replyButton);
        }

        messageBox.getChildren().addAll(senderLabel, readStatusLabel, contentLabel, buttonBox);
        return messageBox;
    }

    private void editMessage(PrivateMessage message) {
        TextInputDialog dialog = new TextInputDialog(message.getContent());
        dialog.setTitle("Edit Message");
        dialog.setHeaderText("Edit your message:");
        dialog.setContentText("Message:");

        dialog.showAndWait().ifPresent(newContent -> {
            try {
                PrivateMessage updatedMessage = database.getPrivateMessage(message.getMessageId());
                if (updatedMessage == null) {
                    throw new SQLException("Message not found in the database.");
                }
                updatedMessage.setContent(newContent);
                database.updatePrivateMessage(updatedMessage);
                refreshMessages();
            } catch (SQLException e) {
                showError("Error updating message: " + e.getMessage());
            }
        });
    }

    private void deleteMessage(PrivateMessage message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Are you sure you want to delete this message?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    database.deletePrivateMessage(message.getMessageId());
                    refreshMessages();
                } catch (SQLException e) {
                    showError("Error deleting message: " + e.getMessage());
                }
            }
        });
    }

    private void replyToMessage(PrivateMessage originalMessage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply to Message");
        dialog.setHeaderText("Reply to: " + originalMessage.getSenderId());
        dialog.setContentText("Your reply:");

        dialog.showAndWait().ifPresent(replyContent -> {
            try {
            	// Feb26 Shanali: Creating a reply
                PrivateMessage reply = new PrivateMessage(
                	UUID.randomUUID().toString(),
                    currentUser.getUserName(), 
                    originalMessage.getSenderId(),
                    "", // Replies don't have a questionId that they're referring to
                    replyContent, 
                    originalMessage.getMessageId() 
                );
                database.addPrivateMessage(reply);
                // Feb 24 Shanali: Marking a message as read by the receiver once the receiver replies
                originalMessage.markAsRead(); 
                database.markAsReadInDb(originalMessage.getMessageId());
                refreshMessages();
            } catch (SQLException e) {
                showError("Error sending reply: " + e.getMessage());
            }
        });
    }

    private void goBack(Stage primaryStage) {
        new StudentHomePage().show(database, primaryStage, currentUser);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


package application;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class StudentHomePage {
    
    public void show(DatabaseHelper database, Stage primaryStage, User user) {
        VBox layout = new VBox(10); // Add spacing between elements
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label userLabel = new Label("Hello, Student!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        //Button that opens up discussion forum
        Button questionsButton = new Button("View Questions");
        questionsButton.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
        questionsButton.setOnAction(e -> {
            try {
                new QuestionListPage().show(database, primaryStage, user); // Replace with actual username
            } catch (Exception error) {
                System.out.println("Error opening questions page: " + error.getMessage());
            }
        });
        
        // Button to open up private messages forum
        Button privateMessagesButton = new Button("View Private Messages");
        privateMessagesButton.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
        privateMessagesButton.setOnAction(e -> {
        	try {
                new PrivateMessagePage().show(database, primaryStage, user);
            } catch (Exception error) {
                System.out.println("Error opening private messages page: " + error.getMessage());
            }
        });
        
        // Update private messages button with updated unread count
        try {
            int unreadCount = database.getUnreadPrivateMessageCount(user.getUserName());
            privateMessagesButton.setText("View Private Messages (" + unreadCount + " unread)");
        } catch (Exception e) {
            System.out.println("Error updating private message count: " + e.getMessage());
        }

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
        logoutButton.setOnAction(e -> {
            try {
                new SetupLoginSelectionPage(database).show(primaryStage);
                System.out.println("Successfully logged out from StudentHomePage!");
            } catch (Exception error) {
                System.out.println("ERROR! Could not log out!");
            }
        });

        layout.getChildren().addAll(userLabel, questionsButton, privateMessagesButton, logoutButton);
        Scene userScene = new Scene(layout, 800, 400);

        primaryStage.setScene(userScene);
        primaryStage.setTitle("Student Page");
    }
}
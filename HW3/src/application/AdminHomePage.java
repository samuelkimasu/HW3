package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import databasePart1.DatabaseHelper;


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(DatabaseHelper database, Stage primaryStage) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;");

        // label to display the welcome message for the admin
        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create list of users that admin can perform actions on such as delete
        UserTableComponent userTableComponent = new UserTableComponent(database);

        layout.getChildren().addAll(adminLabel, userTableComponent);
        
        // Set the scene to primary stage
        Scene adminScene = new Scene(layout, 800, 600);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Page");
    }
}


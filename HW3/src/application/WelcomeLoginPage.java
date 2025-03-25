package application;

import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.List;

import databasePart1.*;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to navigate to the user's respective page based on their role
	    
//	    Button continueButton = new Button("Continue to your Page");
	    
	    //Feb 7 Jubilee: display multiple role buttons for users with multiple roles
	   
	    
	    // Create a VBox to hold role-specific buttons
        VBox roleButtonsBox = new VBox(5);
        roleButtonsBox.setAlignment(Pos.CENTER);

        //Feb 7 Jubilee:  Create buttons for each role
        List<String> roles = user.getRole();
        for (String role : roles) {
            Button roleButton = new Button("Continue to " + role.substring(0, 1).toUpperCase() + role.substring(1) + " Page");
            roleButton.setOnAction(a -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        new AdminHomePage().show(databaseHelper, primaryStage);
                        break;
                    case "user":
                        new UserHomePage().show(databaseHelper, primaryStage);
                        break;
                    case "student":
                        new StudentHomePage().show(databaseHelper, primaryStage, user);
                        break;
                    case "staff":
                        new StaffHomePage().show(primaryStage);
                        break;
                    case "reviewer":
                        new ReviewerHomePage().show(primaryStage);
                        break;
                    case "instructor":
                        new InstructorHomePage().show(primaryStage);
                        break;
                    default:
                        new UserHomePage().show(databaseHelper, primaryStage);
                        break;
                }
            });
            roleButtonsBox.getChildren().add(roleButton);
        }
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    // "Invite" button for admin to generate invitation codes
	    // Feb3 Jubilee need to change this to contain
	    if ((user.getRole()).contains("admin")) {
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }

//	    layout.getChildren().addAll(welcomeLabel,continueButton,quitButton);
	    layout.getChildren().addAll(welcomeLabel ,roleButtonsBox, quitButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}
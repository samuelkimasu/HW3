package application;

import java.util.*;


import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;


/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName and password
    	// Feb3 Shanali - Creating input fields for a user to enter their email and full name
    	TextField userFullNameField = new TextField();
    	userFullNameField.setPromptText("Enter Admin full name");
    	userFullNameField.setMaxWidth(250);
        
        TextField userEmailField = new TextField();
        userEmailField.setPromptText("Enter Admin email");
        userEmailField.setMaxWidth(250);
    	
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Button setupButton = new Button("Setup");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            
            //Feb3 Shanali : retrieving user's email and full name input
            String userEmail = userEmailField.getText();
            String userFullName = userFullNameField.getText();
            
            // BUBBLE - Do we need to check if an email is valid??????
            
            // Use UserNameRecognizer to determine if userName is valid
            String userErrMessage = UserNameRecognizer.checkForValidUserName(userName);
            try {
            	if (userErrMessage != "") {
            		// Display the error message
    				System.out.println(userErrMessage);
    				errorLabel.setText(userErrMessage);
    				// Fetch the index where the processing of the input stopped
    				if (UserNameRecognizer.userNameRecognizerIndexofError <= -1) return;	// Should never happen
    				// Display the input line so the user can see what was entered		
    				System.out.println(userName);
    				// Display the line up to the error and the display an up arrow
    				System.out.println(userName.substring(0,UserNameRecognizer.userNameRecognizerIndexofError) + "\u21EB");
            	}
            	else {
            		System.out.println("Success! The UserName is valid.");
            		// Check if password is valid
	            	if (password.isEmpty()) {
	        		    System.out.println("No input text found in password!");
	            		errorLabel.setText("No input text found in password!");
	            	}
	        		else
	        		{
	        			String passErrMessage = PasswordEvaluator.evaluatePassword(password);
	        			if (passErrMessage != "") {
	        				System.out.println(passErrMessage);
	        				if (PasswordEvaluator.passwordIndexofError <= -1) return;
	        				System.out.println("Failure! The password is not valid. " + passErrMessage);
	        				errorLabel.setText("Failure! The password is not valid. " + passErrMessage);
	        			}
	        			else if (PasswordEvaluator.foundUpperCase && PasswordEvaluator.foundLowerCase &&
	        					PasswordEvaluator.foundNumericDigit && PasswordEvaluator.foundSpecialChar &&
	        					PasswordEvaluator.foundLongEnough) {
	        				System.out.println("Success! The password satisfies the requirements.");
	                    	// Create a new User object with admin role and register in the database
	        				
	        				// Feb3 Jubilee: updated creating admin user method with arrayList
	                    	List<String> roles = new ArrayList<>();
	                    	roles.add("admin");
	                    	
	                    	// Feb3 Shanali : Added the extra constructor parameters userEmail + userFullName
	                    	
	        				User user=new User(userName, password, roles, userEmail, userFullName);
	                        databaseHelper.register(user);
	                        System.out.println("Administrator setup completed.");
	                        
	                        // Navigate to the Welcome Login Page
	                        new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
	        			} else {
	        				System.out.println("The password as currently entered is not yet valid.");
	        				errorLabel.setText("The password as currently entered is not yet valid.");
	        			}
	        		}
            	}
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        //Feb3 Shanali: Edited the user interface to include input fields for user email and user full name
        VBox layout = new VBox(10, userNameField, passwordField, userEmailField, userFullNameField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
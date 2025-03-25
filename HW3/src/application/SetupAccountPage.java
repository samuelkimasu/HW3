package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code"
    	
    	// Feb3 Shanali - Creating input fields for a user to enter their email and full name
    	
    	TextField userFullNameField = new TextField();
    	userFullNameField.setPromptText("Enter User full name");
    	userFullNameField.setMaxWidth(250);
        
        TextField userEmailField = new TextField();
        userEmailField.setPromptText("Enter User email");
        userEmailField.setMaxWidth(250);   
        
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String code = inviteCodeField.getText();
            //Feb3 Shanali : retrieving user's email and full name input
            String userEmail = userEmailField.getText();
            String userFullName = userFullNameField.getText();
            
            // Use UserNameRecognizer to determine if userName is valid
            String userErrMessage = UserNameRecognizer.checkForValidUserName(userName);
            
            // Check for valid userName then for valid password then checks
            // for a valid invitation code
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
	            	// Check if the user already exists
	            	if(!databaseHelper.doesUserExist(userName)) {
	            		
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
		        				System.out.println("Failure! The password is not valid.");
		        				errorLabel.setText("Failure! The password is not valid. " + passErrMessage);
		        			}
		        			else if (PasswordEvaluator.foundUpperCase && PasswordEvaluator.foundLowerCase &&
		        					PasswordEvaluator.foundNumericDigit && PasswordEvaluator.foundSpecialChar &&
		        					PasswordEvaluator.foundLongEnough) {
		        				
		        				System.out.println("Success! The password satisfies the requirements.");
			            		
		        				// Validate the invitation code
		        				//Feb4 Iris: added code to display different error messages depending on if code is wrong or expired
		        				String codeStatus = databaseHelper.validateInvitationCode(code);
		        				
		        				if(codeStatus.equals("valid")) {
			            			
			        				//Feb4 Iris: creates user with a specific role dependent on invitation code inputed
			            			String userRole = switch (code.substring(0, 3)) {
			            		        case "STU" -> "student";
			            		        case "ADM" -> "admin";
			            		        case "INS" -> "instructor";
			            		        case "STA" -> "staff";
			            		        case "REV" -> "reviewer";
			            		        default -> "";
			            			};
			            			
			            			// Create a new user and register them in the database
			            			
			            			//Feb5 Jubilee: create ArrayList with correct userRole based on code prefix
			                    	List<String> roles = new ArrayList<>();
			                    	
			                    	roles.add(userRole);	
			                    	
			        				User user=new User(userName, password, roles, userEmail, userFullName);
			            			
					                databaseHelper.register(user);
					                
					             // Navigate to the Welcome Login Page
					                new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
			            		}
			            		else if (codeStatus.equals("expired")){
			            			errorLabel.setText("Invitation code has expired. Please enter a new code.");
			            		}
			            		else {
			            			errorLabel.setText("Invalid invitation code. Please try again.");
			            		}
		        			} else {
		        				System.out.println("The password as currently entered is not yet valid.");
		        			}
		        		}
	            	}
	            	else {
	            		errorLabel.setText("This username is taken!!.. Please use another to setup an account");
	            	}
            	}
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        //Feb3 Shanali: added fullName and userEmail field
        layout.getChildren().addAll(userFullNameField, userEmailField, userNameField, passwordField,inviteCodeField, setupButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
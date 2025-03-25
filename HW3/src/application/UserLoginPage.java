package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;
    //Vyas: added otp function
    private String enteredOTP;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	
    	// Feb3 Shanali - Creating input fields for a user to enter their email and full name
    	
    	TextField userFullNameField = new TextField();
        userFullNameField.setPromptText("Enter Admin full name");
        userFullNameField.setMaxWidth(250);
        
        TextField userEmailField = new TextField();
        userEmailField.setPromptText("Enter Admin email");
        userEmailField.setMaxWidth(250);
        
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");


        Button loginButton = new Button("Login");
        
        //Vyas: forgot password button
        Button forgotPassword = new Button("Forgot your Password?");
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            
          //Feb3 Shanali : retrieving user's email and full name input
            String userEmail = userEmailField.getText();
            String userFullName = userFullNameField.getText();
            
            try {
            	// Feb3 Jubilee: create new user with empty arraylist
            	List<String> roles = new ArrayList<>();	                    		                  	
				User user=new User(userName, password, roles, userEmail, userFullName);
				
            	WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
            	
            	// Feb3 Jubilee: Retrieve the user's role as an array list from the database using userName
            	roles = databaseHelper.getUserRole(userName);
            	
            	if(roles!=null) {
            		user.setRole(roles);
            		if(databaseHelper.login(user)) {
            			welcomeLoginPage.show(primaryStage,user);
            		}
            		else {
            			// Display an error if the login fails
                        errorLabel.setText("Error logging in");
            		}
            	}
            	else {
            		// Display an error if the account does not exist
                    errorLabel.setText("user account doesn't exists");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            } 
        });
        
        //Vyas: forgot password button action
        forgotPassword.setOnAction((a) -> {
            this.showOTPVerificationPage(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel, forgotPassword);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
        
    	//Vyas: OTP methods
    
        private void showOTPVerificationPage(Stage primaryStage) {
            Stage otpStage = new Stage();
            otpStage.setTitle("Verify OTP");
            TextField otpField = new TextField();
            otpField.setPromptText("Enter OTP");
            otpField.setMaxWidth(200.0);
            Label otpErrorLabel = new Label();
            otpErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            Button verifyOTPButton = new Button("Verify OTP");
            verifyOTPButton.setOnAction((a) -> {
               this.enteredOTP = otpField.getText();

               try {
                  if (this.enteredOTP == null || this.enteredOTP.isEmpty()) {
                     otpErrorLabel.setText("OTP cannot be empty!");
                     return;
                  }

                  DatabaseHelper dbHelper = new DatabaseHelper();
                  dbHelper.connectToDatabase();
                  if (dbHelper.validateOTPs(this.enteredOTP)) {
                     otpStage.close();
                     this.showResetPasswordPage(primaryStage);
                  } else {
                     otpErrorLabel.setText("Invalid or Expired OTP. Please try again!");
                  }
               } catch (SQLException var7) {
                  var7.printStackTrace();
                  otpErrorLabel.setText("Database connection failed.");
               }

            });
            VBox otpLayout = new VBox(10.0);
            otpLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");
            otpLayout.getChildren().addAll(new Node[]{otpField, verifyOTPButton, otpErrorLabel});
            otpStage.setScene(new Scene(otpLayout, 300.0, 200.0));
            otpStage.show();
         }

         private void showResetPasswordPage(Stage primaryStage) {
            Stage resetStage = new Stage();
            resetStage.setTitle("Reset Password");
            TextField userNameField = new TextField();
            userNameField.setPromptText("Enter your username");
            userNameField.setMaxWidth(200.0);
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("Enter New Password");
            newPasswordField.setMaxWidth(200.0);
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm New Password");
            confirmPasswordField.setMaxWidth(200.0);
            Label resetErrorLabel = new Label();
            resetErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            Button resetButton = new Button("Reset Password");
            resetButton.setOnAction((a) -> {
//               String userName = userNameField.getText();
//               String newPassword = newPasswordField.getText();
//               String confirmPassword = confirmPasswordField.getText();
//
//               try {
//                  DatabaseHelper dbHelper = new DatabaseHelper();
//                  dbHelper.connectToDatabase();
//                  if (!newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
//                     dbHelper.updatePasswordUsingOTP(userName, newPassword);
//                     resetStage.close();
//                  } else {
//                     resetErrorLabel.setText("Passwords do not match. Please try again!");
//                  }
//               } catch (SQLException var10) {
//                  var10.printStackTrace();
//               }
            	
                String userName = userNameField.getText();
                String newPassword = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (userName.isEmpty()) {
                    resetErrorLabel.setText("Username cannot be empty!");
                    return;
                }

                if (newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
                    resetErrorLabel.setText("Passwords do not match or are empty. Please try again!");
                    return;
                }

                try {
                    if (databaseHelper.changePassword(userName, newPassword)) {
                        resetErrorLabel.setText("Password changed successfully!");
                        resetErrorLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
                        // Close the reset stage after a short delay
                        new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    javafx.application.Platform.runLater(() -> resetStage.close());
                                }
                            }, 
                            2000 // Delay for 2 seconds
                        );
                    } else {
                        resetErrorLabel.setText("Failed to change password. User may not exist.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resetErrorLabel.setText("An error occurred. Please try again.");
                }
            	
            	

            });
            VBox resetLayout = new VBox(10.0);
            resetLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");
            resetLayout.getChildren().addAll(new Node[]{userNameField, newPasswordField, confirmPasswordField, resetButton});
            resetStage.setScene(new Scene(resetLayout, 300.0, 250.0));
            resetStage.show();
         }
      }
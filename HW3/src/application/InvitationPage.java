package application;


import java.util.Arrays;

import databasePart1.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitePage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */

public class InvitationPage {
	//Feb7 Iris: added a dropdown for the role choosing
	private ComboBox<String> roleComboBox;
	/**
     * Displays the Invite Page in the provided primary stage.
     * 
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     */
    public void show(DatabaseHelper databaseHelper,Stage primaryStage) {
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display the title of the page
	    Label userLabel = new Label("Invite ");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    //Feb7 Iris: added a new dropdown to ask admin what role they want to assign the user
        //Text field for what role admin wants to assign
	    roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Arrays.asList("admin", "student", "staff", "instructor", "reviewer"));
	    
	    // Button to generate the invitation code
	    Button showCodeButton = new Button("Generate Invitation Code");
	    
	    // Label to display the generated invitation code
	    Label inviteCodeLabel = new Label(""); ;
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
        
        
        //Feb4 Iris: added code that stores what role admin wants to assign
        showCodeButton.setOnAction(a -> {
        	String roleAssignment = roleComboBox.getValue(); // Get the entered role
        	if (roleAssignment.isEmpty()) {
                inviteCodeLabel.setText("Please enter a role before generating a code.");
            } else {
        	// Generate the invitation code using the databaseHelper and set it to the label
            String invitationCode = databaseHelper.generateInvitationCode(roleAssignment);
            inviteCodeLabel.setText(invitationCode);
            System.out.println("TEST: Successfully created invitatiopn code for " + roleAssignment + "!");
            }
        });
	    

        layout.getChildren().addAll(userLabel, roleComboBox, showCodeButton, inviteCodeLabel);
	    Scene inviteScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(inviteScene);
	    primaryStage.setTitle("Invite Page");
    	
    }
}
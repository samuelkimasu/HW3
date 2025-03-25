package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	
	//Feb7 Iris: changed method arguments to include database
	public void show(DatabaseHelper database, Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    //Feb4 Samuel: added logout button
	    Button logoutButton = new Button("Logout");
	    logoutButton.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
	    logoutButton.setOnAction((e) -> {
	    	//Feb7 Iris: got rid of the null so someone can login again after logging out
	    	try {
	      (new SetupLoginSelectionPage((database))).show(primaryStage);
	      System.out.println("TEST: Successfully logged out from UserHomePage!");
	    	}
	    	catch (Exception error){
	    		System.out.println("TEST: ERROR! Could not log out!");
	    	}
	    	});

	    layout.getChildren().addAll(new Node[]{userLabel, logoutButton});
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
    	
    }
}
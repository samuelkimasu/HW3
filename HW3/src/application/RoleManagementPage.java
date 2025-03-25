package application;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoleManagementPage extends VBox {
    private DatabaseHelper dbHelper;
    private User user;
    private boolean isAddRole;
    private ComboBox<String> roleComboBox;

    public RoleManagementPage(DatabaseHelper dbHelper, User user, boolean isAddRole) {
        this.dbHelper = dbHelper;
        this.user = user;
        this.isAddRole = isAddRole;
        this.setSpacing(10);
        this.setPadding(new javafx.geometry.Insets(20));
        this.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(isAddRole ? "Add Role" : "Delete Role");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label instructionLabel = new Label(isAddRole ? "Choose what role to add:" : "Choose what role to delete:");
        instructionLabel.setStyle("-fx-font-weight: bold;");

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Arrays.asList("admin", "student", "staff", "instructor", "reviewer"));
       
        Button doneButton = new Button("Done");
        doneButton.setOnAction(e ->         {
        	try {
        	handleRoleChange();
  	      System.out.println("TEST: Successfully changed role!");
        	}
  	    	catch (Exception error)
        {
  	    		System.out.println("TEST: ERROR! Could not change role.");
  	    	}});

        this.getChildren().addAll(titleLabel, instructionLabel, roleComboBox, doneButton);
    }

    //Method that handles whether or not admin is adding or deleting roles
    private void handleRoleChange() {
        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null) {
            showAlert("Error", "Please select a role.");
            return;
        }

        try {
            List<String> currentRoles = new ArrayList<>(dbHelper.getUserRole(user.getUserName()));
            if (isAddRole) {
            	//if "isAddRole" is true, then you go to "add role" pop up
                if (currentRoles.contains(selectedRole)) {
                    showAlert("Error", user.getUserName() + " already has that role.");
                } else {
                    currentRoles.add(selectedRole);
                    dbHelper.updateUserRoles(user.getUserName(), currentRoles);
                    showAlert("Success", "Role added successfully.");
                }
              //if "isAddRole" is false, then you go to "delete role" pop up
            } else {
                if ("admin".equals(selectedRole)) {
                    showAlert("Error", "Cannot delete the Admin role.");
                } else if (!currentRoles.contains(selectedRole)) {
                    showAlert("Error", user.getUserName() + " does not have that role.");
                } else {
                    currentRoles.remove(selectedRole);
                    dbHelper.updateUserRoles(user.getUserName(), currentRoles);
                    showAlert("Success", "Role deleted successfully.");
                }
            }
            //Closes this pop up window
            ((Stage) this.getScene().getWindow()).close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update roles: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


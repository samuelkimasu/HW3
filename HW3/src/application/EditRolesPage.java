package application;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;

public class EditRolesPage extends VBox {
    private DatabaseHelper dbHelper;
    private User user;
    private Label currentRolesLabel;

    public EditRolesPage(DatabaseHelper dbHelper, User user) throws SQLException {
        this.dbHelper = dbHelper;
        this.user = user;
        this.setSpacing(10);
        this.setPadding(new javafx.geometry.Insets(20));
        this.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Edit Roles for " + user.getUserName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        currentRolesLabel = new Label();
        currentRolesLabel.setStyle("-fx-font-size: 14px;");
        updateCurrentRoles();

        Button addRoleButton = new Button("Add Role");
        Button deleteRoleButton = new Button("Delete Role");

        addRoleButton.setOnAction((e) -> {	    
        try {
        	openAddRolePage();
  	      System.out.println("TEST: Successfully Added role!");
  	    	}
  	    	catch (Exception error)
        {
  	    		System.out.println("TEST: ERROR! Could not add role.");
  	    	}
        });
        deleteRoleButton.setOnAction(e -> {	    
            try {
            	openDeleteRolePage();
      	      System.out.println("TEST: Successfully Removed role!");
      	    	}
      	    	catch (Exception error)
            {
      	    		System.out.println("TEST: ERROR! Could not remove role.");
      	    	}
            });

        this.getChildren().addAll(titleLabel, currentRolesLabel, addRoleButton, deleteRoleButton);
    }

    private void openAddRolePage() throws SQLException {
        RoleManagementPage addRolePage = new RoleManagementPage(dbHelper, user, true);
        openRoleManagementPage(addRolePage, "Add Role");
    }

    private void openDeleteRolePage() throws SQLException {
        RoleManagementPage deleteRolePage = new RoleManagementPage(dbHelper, user, false);
        openRoleManagementPage(deleteRolePage, "Delete Role");
    }

    private void openRoleManagementPage(RoleManagementPage page, String title) throws SQLException {
        Scene scene = new Scene(page, 300, 200);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.showAndWait();
        updateCurrentRoles();
    }

    private void updateCurrentRoles() throws SQLException {
        try {
            List<String> updatedRoles = dbHelper.getUserRole(user.getUserName());
            if (updatedRoles != null && !updatedRoles.isEmpty()) {
                user.setRole(updatedRoles);
                currentRolesLabel.setText("Current Roles: " + String.join(", ", updatedRoles));
            } else {
                currentRolesLabel.setText("Current Roles: None");
            }
            System.out.println("TEST: Successfully edited roles of user!");
        } catch (Error e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update roles: " + e.getMessage());
            System.out.println("TEST: Error trying to update roles.");
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
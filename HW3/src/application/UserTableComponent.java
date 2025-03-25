package application;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;


/**
 * UserTableComponent class represents the user interface for the admin user
 * where they can view all the users in the database and perform administrative
 * actions on each user.
 */

// Creates a VBox with various children used to display the list of Users
// and other administrative features
public class UserTableComponent extends VBox {
    private DatabaseHelper dbHelper;
    private TableView<User> userTable;
    private ObservableList<User> users;

    public UserTableComponent(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.setSpacing(10);

        Button listUsersButton = new Button("Generate a List of Users");
        // Create a new table of users when "Generate a List of Users"
        // button is pressed
        listUsersButton.setOnAction(e -> refreshUserTable());

        userTable = createUserTable();
        
        // Add component to VBox to display
        this.getChildren().addAll(listUsersButton, userTable);
    }

    private TableView<User> createUserTable() {
    	// Create a table with user information and administrative
    	// action buttons
        TableView<User> table = new TableView<>();
        users = FXCollections.observableArrayList();

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        // Button to delete users
        TableColumn<User, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user.getUserName());
                });
            }
            
            // Determines whether or not to display a delete button
            // in a row, displays if a user exists in that row.
            // Indirectly called by JavaFX when table data is modified
            // in any way (e.g. initial rendering, user lists is modified)
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // If the cell is empty, display nothing
                if (empty) {
                    setGraphic(null);
                // If the cell is not empty, display delete button
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        table.getColumns().addAll(usernameCol, roleCol, emailCol, firstNameCol, deleteCol);
        table.setItems(users);

        return table;
    }
    
    // Queries database and updates the user list with new
    // user data to be displayed
    public void refreshUserTable() {
        try {
            List<User> userList = dbHelper.getUserList();
            users.setAll(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve user list: " + e.getMessage());
        }
    }

    private void deleteUser(String userName) {
    	// Creates confirmation pop up
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("Do you want to delete the user: " + userName + "?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = dbHelper.deleteUser(userName);
                    if (deleted) {
                        showAlert("Success", "User " + userName + " has been deleted.");
                        refreshUserTable();
                    } else {
                        showAlert("Error", "Failed to delete user. The user might not exist or might be an admin.");
                    }
                } catch (SQLException ex) {
                    showAlert("Error", "An error occurred while deleting the user: " + ex.getMessage());
                }
            }
        });
    }
    
    // Creates pop ups
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


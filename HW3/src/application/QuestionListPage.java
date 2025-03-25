package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.UUID;
import javafx.util.Pair;
import databasePart1.DatabaseHelper;
import javafx.scene.control.Dialog;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;

import java.sql.SQLException;

public class QuestionListPage {
	
	//Opens up a new page for the question thread
    private DatabaseHelper database;
    private User user;
    private VBox questionsList;
    //Feb25 Vyas
    private Question currentQuestion;

    public void show(DatabaseHelper database, Stage primaryStage, User user) {
        this.database = database;
        this.user = user;

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Top section with header and new question button
        HBox topSection = new HBox(10);
        Label headerLabel = new Label("Questions");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Button newQuestionButton = new Button("Ask Question");
        newQuestionButton.setOnAction(e -> showNewQuestionDialog(primaryStage));
        
        //Feb 21: Jubilee: add refresh button to see resolved/unresolved changes
        Button refreshButton = new Button("Refresh Question List");
        refreshButton.setOnAction(e -> refreshQuestions());
        
        //Vyas feb 17: Create filter dropdown button next to ask question button
        MenuButton filterButton = new MenuButton ("Filter");
        CheckMenuItem allUnresolvedQuestions = new CheckMenuItem("All unresolved questions");
       	CheckMenuItem yourUnresolvedQuestions = new CheckMenuItem(" Your unresolved questions");
       	CheckMenuItem unreadAnswers = new CheckMenuItem("Unread Answers");
       	
       	allUnresolvedQuestions.setOnAction(e -> {
       		
       		if (allUnresolvedQuestions.isSelected()) {
       			yourUnresolvedQuestions.setSelected(false);
       			unreadAnswers.setSelected(false);
       		}
       		
       		questionsList.getChildren().clear();
       		
       		if (allUnresolvedQuestions.isSelected()) {
        		try {
        			database.getUnresolvedQuestions().forEach(questions -> {
                        VBox questionBox = createQuestionBox(questions);
                        questionsList.getChildren().add(questionBox);
                    });
        			System.out.println("Successfully displayed all unresolved questions");
        		} catch (Exception f) {
        			System.out.println("Error loading all unresolved questions..." + f.getMessage());
        		}
       		} else {
       			refreshQuestions();
       		}
        });
       	
       	
       	yourUnresolvedQuestions.setOnAction(e -> {
       		
       		if (yourUnresolvedQuestions.isSelected()) {
       			allUnresolvedQuestions.setSelected(false);
       			unreadAnswers.setSelected(false);
       		}
       		
       		questionsList.getChildren().clear();
       		if (yourUnresolvedQuestions.isSelected()) {
       			try {
       				database.getUserUnresolvedQuestions(user.getUserName()).forEach(questions -> {
       					VBox questionBox = createQuestionBox(questions);
       					questionsList.getChildren().add(questionBox);
       				});
       				System.out.println("Successfully displayed the user's unresolved questions.");
       			} catch (Exception f) {
       				System.out.println("Error loading user's unresolved questions..." + f.getMessage());
       			}
       		} else {
       			refreshQuestions();
       		}
       	});
       	
       	unreadAnswers.setOnAction(e -> {
       		
       		if(unreadAnswers.isSelected()) {
       			allUnresolvedQuestions.setSelected(false);
       			yourUnresolvedQuestions.setSelected(false);
       		}
       		
       		questionsList.getChildren().clear();
       		if (unreadAnswers.isSelected()) {
       			try {
       				database.getUnreadQuestions(user.getUserName()).forEach(questions -> {
       					VBox questionBox = createQuestionBox(questions);
       					questionsList.getChildren().add(questionBox);
       				});
       				System.out.println("Successfully displayed the user's unread answers.");
       			} catch (SQLException err) {
       				System.out.println("Could not get Unread answers " + err.getMessage());
       			}
       		} else {
       			refreshQuestions();
       		}
       	});
       	
        filterButton.getItems().addAll(allUnresolvedQuestions, yourUnresolvedQuestions, unreadAnswers);
        //Vyas feb 18: End
        
        // Search box
        //Feb 24 Iris: updated search bar to work when backspace is hit as well
        TextField searchField = new TextField();
        searchField.setPromptText("Search questions...");
        searchField.setPrefWidth(200);
        searchField.setOnKeyReleased(e -> searchQuestions(searchField.getText()));

        topSection.getChildren().addAll(headerLabel, searchField, filterButton, refreshButton, newQuestionButton);
        mainLayout.setTop(topSection);

        // Center section with questions list
        questionsList = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(questionsList);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);
        
        // Back to home button
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> goBack(primaryStage));
        mainLayout.setBottom(backButton);

        //Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");
        logoutButton.setOnAction(e -> {
            try {
                new SetupLoginSelectionPage(database).show(primaryStage);
                System.out.println("Successfully logged out from StudentHomePage!");
            } catch (Exception error) {
                System.out.println("ERROR! Could not log out!");
            }
        });
        
        mainLayout.setBottom(logoutButton);
        
        // Load questions
        refreshQuestions();

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Questions List");
    }

    private void refreshQuestions() {
    	System.out.println("Questions were successfully refreshed.");
        questionsList.getChildren().clear();
        try {
            database.getAllQuestions().forEach(question -> {
                VBox questionBox = createQuestionBox(question);
                questionsList.getChildren().add(questionBox);
            });
        } catch (Exception e) {
            showError("Error loading questions: " + e.getMessage());
        }
    }

    private VBox createQuestionBox(Question question) {
        VBox questionBox = new VBox(5);
        questionBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label titleLabel = new Label(question.getQuestionContent());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label authorLabel = new Label("Asked by: " + question.getAuthorID());
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        HBox buttonBox = new HBox(10);
        Label statusLabel = new Label(question.isResolved() ? "Resolved" : "Unresolved");
        statusLabel.setStyle(question.isResolved() ? 
            "-fx-text-fill: green; -fx-font-weight: bold;" : 
            "-fx-text-fill: orange;");

        Button viewButton = new Button("View Thread");
        viewButton.setOnAction(e -> showAnswersDialog(question));

        Button sendPrivateMessageButton = new Button("Send Private Message");
        sendPrivateMessageButton.setOnAction(e -> sendPrivateMessage(question));

        buttonBox.getChildren().addAll(statusLabel, viewButton, sendPrivateMessageButton);

        // Add edit and delete buttons if the current user is the author
        if (user.getUserName().equals(question.getAuthorID())) {
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> showEditQuestionDialog(question));

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> showDeleteQuestionConfirmation(question));

            buttonBox.getChildren().addAll(editButton, deleteButton);
        }

        questionBox.getChildren().addAll(titleLabel, authorLabel, buttonBox);
        return questionBox;
    }

    private void showNewQuestionDialog(Stage primaryStage) {
    	System.out.println("showing new dialog...");
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ask a Question");
        dialog.setHeaderText("Enter your question and description below:");

        VBox content = new VBox(10); // Layout to hold both text areas
        TextArea questionArea = new TextArea();
        questionArea.setPromptText("Type your question here...");
        questionArea.setPrefRowCount(3);
        
        TextArea questiondescArea = new TextArea();
        questiondescArea.setPromptText("Type your description here...");
        questiondescArea.setPrefRowCount(7);
        
        content.getChildren().addAll(new Label("Question:"), questionArea, 
                new Label("Description:"), questiondescArea);
        

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return questionArea.getText();
            }
            return null;
        });
        


        dialog.showAndWait().ifPresent(questionContent -> {
            if (questionArea.getText() == null || questionArea.getText().trim().isEmpty()) {
                showError("Question cannot be empty");
                return;
            }
            
            String questionDesc = questiondescArea.getText(); // Get the description
            
            
            try {
                Question newQuestion = new Question(
                        UUID.randomUUID().toString(),
                        questionContent,
                        questionDesc, // Pass the description correctly
                        user.getUserName()
                );
                database.addQuestion(newQuestion);
                refreshQuestions();
            } catch (Exception e) {
                showError("Error creating question: " + e.getMessage());
            }
        });
    }

    private void showAnswersDialog(Question question) {
        Stage answerStage = new Stage();
        new AnswerPage().show(database, answerStage, question, user.getUserName());
    }

    //Feb 25 Iris: Updated searchQuestions to display if no questions match what it is searched
    private void searchQuestions(String keyword) {
        questionsList.getChildren().clear();
        try {
            java.util.List<Question> matchingQuestions = database.getAllQuestions().stream()
                .filter(q -> q.getQuestionContent().toLowerCase().contains(keyword.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());

        if (matchingQuestions.isEmpty()) {
            Label noMatchLabel = new Label("No Questions Match Search");
            noMatchLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");
            VBox.setMargin(noMatchLabel, new Insets(20, 0, 0, 0));
            questionsList.setAlignment(javafx.geometry.Pos.CENTER);
            questionsList.getChildren().add(noMatchLabel);
        } else {
            questionsList.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            matchingQuestions.forEach(question -> {
                VBox questionBox = createQuestionBoxWithHighlight(question, keyword);
                questionsList.getChildren().add(questionBox);
            });
        }
	    } catch (Exception e) {
	        showError("Error searching questions: " + e.getMessage());
	    }
    }
    
  //Feb 25 Iris: Added highlighting function that creates a box around characters
    private VBox createQuestionBoxWithHighlight(Question question, String keyword) {
        VBox questionBox = new VBox(5);
        questionBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        TextFlow titleLabel = createHighlightedText(question.getQuestionContent(), keyword);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label authorLabel = new Label("Asked by: " + question.getAuthorID());
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        HBox statusBox = new HBox(10);
        Label statusLabel = new Label(question.isResolved() ? "Resolved" : "Unresolved");
        statusLabel.setStyle(question.isResolved() ? 
            "-fx-text-fill: green; -fx-font-weight: bold;" : 
            "-fx-text-fill: orange;");

        Button viewButton = new Button("View Answers");
        viewButton.setOnAction(e -> showAnswersDialog(question));

        statusBox.getChildren().addAll(statusLabel, viewButton);

        // Add edit and delete buttons if the current user is the author
        if (user.getUserName().equals(question.getAuthorID())) {
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> showEditQuestionDialog(question));

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> showDeleteQuestionConfirmation(question));

            statusBox.getChildren().addAll(editButton, deleteButton);
        }

        questionBox.getChildren().addAll(titleLabel, authorLabel, statusBox);
        return questionBox;
    }

    //Feb 26 Iris: Added the highlighted box to the specific characters that match what is searched
    private TextFlow createHighlightedText(String text, String searchTerm) {
        TextFlow textFlow = new TextFlow();
        if (searchTerm == null || searchTerm.isEmpty()) {
            Text plainText = new Text(text);
            plainText.setStyle("-fx-font-weight: bold;");
            textFlow.getChildren().add(plainText);
            return textFlow;
        }

        String lowerCaseText = text.toLowerCase();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();
        int searchTermLength = searchTerm.length();
        int currentIndex = 0;

        while (currentIndex < text.length()) {
            int foundIndex = lowerCaseText.indexOf(lowerCaseSearchTerm, currentIndex);
            if (foundIndex == -1) {
                Text remainingText = new Text(text.substring(currentIndex));
                remainingText.setStyle("-fx-font-weight: bold;");
                textFlow.getChildren().add(remainingText);
                break;
            }

            if (foundIndex > currentIndex) {
                Text beforeMatch = new Text(text.substring(currentIndex, foundIndex));
                beforeMatch.setStyle("-fx-font-weight: bold;");
                textFlow.getChildren().add(beforeMatch);
            }

            Text match = new Text(text.substring(foundIndex, foundIndex + searchTermLength));
            match.setStyle("-fx-font-weight: bold; -fx-fill: black; -fx-background-color: #3498db;");
            match.setFill(javafx.scene.paint.Color.BLACK);
            
            // Create a background for the highlighted text
            javafx.scene.layout.Background highlightBackground = new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                    javafx.scene.paint.Color.YELLOW,
                    new javafx.scene.layout.CornerRadii(2),
                    javafx.geometry.Insets.EMPTY
                )
            );
            
            // Create a region for the highlight
            javafx.scene.layout.Region highlightRegion = new javafx.scene.layout.Region();
            highlightRegion.setBackground(highlightBackground);
            highlightRegion.setMinHeight(match.getBoundsInLocal().getHeight());
            highlightRegion.setMinWidth(match.getBoundsInLocal().getWidth());
            
            // Create a stack pane to layer the text over the highlight
            javafx.scene.layout.StackPane highlightStack = new javafx.scene.layout.StackPane(highlightRegion, match);
            textFlow.getChildren().add(highlightStack);

            currentIndex = foundIndex + searchTermLength;
        }

        return textFlow;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showEditQuestionDialog(Question question) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Edit your question and description below:");

        TextArea questionArea = new TextArea(question.getQuestionContent());
        questionArea.setPromptText("Type your question here...");
        questionArea.setPrefRowCount(3);
        
        TextArea descriptionArea = new TextArea(question.getQuestionDesc() != null ? question.getQuestionDesc() : "");
        descriptionArea.setPromptText("Type your description here...");
        descriptionArea.setPrefRowCount(5);
        
        VBox contentBox = new VBox(10, new Label("Question:"), questionArea, new Label("Description:"), descriptionArea);
        dialog.getDialogPane().setContent(contentBox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new Pair<>(questionArea.getText(), descriptionArea.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedData -> {
            try {
                question.setQuestionContent(updatedData.getKey());
                question.setQuestionDesc(updatedData.getValue().trim().isEmpty() ? null : updatedData.getValue()); // Allow null for empty descriptions
                database.updateQuestion(question);
                refreshQuestions();
            } catch (Exception e) {
                showError("Error updating question: " + e.getMessage());
            }
        });
    }

    private void showDeleteQuestionConfirmation(Question question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Question");
        alert.setHeaderText("Are you sure you want to delete this question?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    database.deleteQuestion(question.getQuestionID());
                    refreshQuestions();
                } catch (Exception e) {
                    showError("Error deleting question: " + e.getMessage());
                }
            }
        });
    }

    private void sendPrivateMessage(Question question) {
        if (user.getUserName().equals(question.getAuthorID())) {
            showError("You cannot send a private message to yourself.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Send Private Message");
        dialog.setHeaderText("Send a private message to " + question.getAuthorID());
        dialog.setContentText("Your message:");

        dialog.showAndWait().ifPresent(content -> {
            try {
                PrivateMessage message = new PrivateMessage(
                    user.getUserName(),
                    question.getAuthorID(),
                    question.getQuestionID(),
                    content
                );
                database.addPrivateMessage(message);
                showInfo("Private message sent successfully.");
            } catch (Exception e) {
                showError("Error sending private message: " + e.getMessage());
            }
        });
    }
    
    private void goBack(Stage primaryStage) {
        new StudentHomePage().show(database, primaryStage, user);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


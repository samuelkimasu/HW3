package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;


import java.util.UUID;
import databasePart1.DatabaseHelper;

public class AnswerPage {
	
	//takes the user to a different page that shows the entire question thread
    private DatabaseHelper database;
    private String currentUser;
    private Question currentQuestion;
    private VBox answersList;
    //Feb 25 Jubilee: added boolean for resolved answer
    private boolean showingOnlyResolved = false;
    private Label noResolvedAnswerLabel;

    public void show(DatabaseHelper database, Stage stage, Question question, String username) {
        this.database = database;
        this.currentQuestion = question;
        this.currentUser = username;

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Question section
        VBox questionSection = new VBox(10);
        Label questionLabel = new Label(question.getQuestionContent());
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        
        //Feb25 Samuel
        String descriptionText = (question.getQuestionDesc() == null || question.getQuestionDesc().trim().isEmpty())
                ? "No description provided." : question.getQuestionDesc();
        Label descriptionLabel = new Label(descriptionText);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");
        
        Label authorLabel = new Label("Asked by: " + question.getAuthorID());
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        
        //Feb 25: Jubilee added a filter button to show accepted answer so that user can access it easily
        ToggleButton filterButton = new ToggleButton("Show Accepted Answer Only");
        filterButton.setSelected(showingOnlyResolved);
        filterButton.setOnAction(e -> {
        	System.out.println("Successfully filtered only accepted answer.");
            showingOnlyResolved = filterButton.isSelected();
            refreshAnswers();
        });
        
        // Feb 25 Jubilee Create the no resolved answer label
        noResolvedAnswerLabel = new Label("This question has not been resolved yet. :(");
        noResolvedAnswerLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px; -fx-padding: 10px;");
        noResolvedAnswerLabel.setVisible(false); 
        
        questionSection.getChildren().addAll(questionLabel, descriptionLabel, authorLabel, filterButton, noResolvedAnswerLabel);
        mainLayout.setTop(questionSection);

        // Creates a section to display answers
        answersList = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(answersList);
        scrollPane.setFitToWidth(true);
        mainLayout.setCenter(scrollPane);

        // Creates a section to display a text box to write an answer
        // which will be added to the answers list
        VBox newAnswerSection = new VBox(10);
        TextArea newAnswerArea = new TextArea();
        newAnswerArea.setPromptText("Write your answer here...");
        newAnswerArea.setPrefRowCount(3);

        Button submitButton = new Button("Submit Answer");
        submitButton.setOnAction(e -> submitAnswer(newAnswerArea.getText()));

        newAnswerSection.getChildren().addAll(new Label("Your Answer:"), newAnswerArea, submitButton);
        mainLayout.setBottom(newAnswerSection);

        // Load answers
        refreshAnswers();

        Scene scene = new Scene(mainLayout, 600, 800);
        stage.setScene(scene);
        stage.setTitle("Answers - " + question.getQuestionContent());
        stage.show();
    }
    
    //Feb25 Jubilee changed refresh answers method to correctly show resolved answers

    private void refreshAnswers() {
        answersList.getChildren().clear();
        try {
            List<Answer> answers;
            if (showingOnlyResolved) {
                // Use the new method for accepted answers and their replies
                answers = database.getAcceptedAnswersWithReplies(currentQuestion.getQuestionID());
                if (answers.isEmpty()) {
                    noResolvedAnswerLabel.setVisible(true);
                    return;
                }
                noResolvedAnswerLabel.setVisible(false);
            } else {
                // Get all answers as before
                answers = database.getAnswersForQuestion(currentQuestion.getQuestionID());
                noResolvedAnswerLabel.setVisible(false);
            }

            // Display the answers
            for (Answer answer : answers) {
                VBox answerBox = createAnswerBox(answer);
                if (answer.isAccepted()) {
                    answerBox.setStyle(answerBox.getStyle() + 
                        "-fx-background-color: #f0f9ff; -fx-border-color: #3b82f6;");
                }
                // Add indentation for replies
                if (answer.getParentAnswerID() != null) {
                    answerBox.setStyle(answerBox.getStyle() + "-fx-margin-left: 30;");
                }
                answersList.getChildren().add(answerBox);
            }
        } catch (Exception e) {
            showError("Error loading answers: " + e.getMessage());
        }
    }

    private VBox createAnswerBox(Answer answer) {
        VBox answerBox = new VBox(5);
        answerBox.setStyle("-fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label contentLabel = new Label(answer.getAnswerContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Label authorLabel = new Label("Answered by: " + answer.getAuthorID());
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        HBox actionBox = new HBox(10);
        actionBox.setPadding(new Insets(5, 0, 0, 0));
        
        // Creates button to accept an answer only if the user is the author of the question
        // Accepted answer badge
        if (currentQuestion.getResolvedAnswerId() != null && currentQuestion.getResolvedAnswerId().equals(answer.getAnswerID())) {
            Label acceptedLabel = new Label("✔ Accepted Answer");
            acceptedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #008000; -fx-font-weight: bold;");
            actionBox.getChildren().add(acceptedLabel);
        } else if (currentUser.equals(currentQuestion.getAuthorID())) {
            Button acceptButton = new Button("Accept");
            acceptButton.setOnAction(e -> acceptAnswer(answer));
            actionBox.getChildren().add(acceptButton);
        }

        // Add edit and delete buttons to answers if the current user is the author
        if (currentUser.equals(answer.getAuthorID())) {
            Button editButton = new Button("✎ Edit");
            editButton.setOnAction(e -> showEditAnswerDialog(answer));
            editButton.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");

            Button deleteButton = new Button("🗑 Delete");
            deleteButton.setOnAction(e -> showDeleteAnswerConfirmation(answer));
            deleteButton.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");

            actionBox.getChildren().addAll(editButton, deleteButton);
        }
        
        Button replyButton = new Button("↩ Reply");
        replyButton.setOnAction(e -> showReplyDialog(answer));
        replyButton.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #0056b3;");
        actionBox.getChildren().add(replyButton);

        answerBox.getChildren().addAll(contentLabel, authorLabel, actionBox);

        // Recursively display replies with improved styling
        try {
            List<Answer> replyObjects = database.getRepliesByAnswerID(answer.getAnswerID());

            if (!replyObjects.isEmpty()) {
                VBox repliesBox = new VBox(5);
                repliesBox.setStyle("-fx-padding: 10 10 10 20; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-color: #f9f9f9;");

                for (Answer reply : replyObjects) {
                    if (reply.getParentAnswerID() != null && reply.getParentAnswerID().equals(answer.getAnswerID())) {
                        VBox replyBox = createAnswerBox(reply);
                        replyBox.setStyle("-fx-padding: 8; -fx-border-color: #c0c0c0; -fx-border-radius: 6; -fx-background-color: #f2f2f2;");
                        repliesBox.getChildren().add(replyBox);
                    }
                }

                answerBox.getChildren().add(repliesBox);
            }
        } catch (SQLException e) {
            showError("Error loading replies: " + e.getMessage());
        }

        return answerBox;
    }
    
    // Adds answer to database and updates display for user
    private void submitAnswer(String content) {
        if (content == null || content.trim().isEmpty()) {
            showError("Answer cannot be empty");
            return;
        }

        try {
            Answer newAnswer = new Answer(
                UUID.randomUUID().toString(),
                currentQuestion.getQuestionID(),
                content,
                currentUser
            );
            database.addAnswer(newAnswer);
            refreshAnswers();
        } catch (Exception e) {
            showError("Error submitting answer: " + e.getMessage());
        }
    }

    //author of question marks answer as the best answer
    private void acceptAnswer(Answer answer) {
        try {
            database.markQuestionResolved(currentQuestion.getQuestionID(), answer.getAnswerID());
            currentQuestion.setResolved(true);
            currentQuestion.setResolvedAnswerId(answer.getAnswerID());
            refreshAnswers();
        } catch (Exception e) {
            showError("Error accepting answer: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //Pop up to allow users to edit their answers
    private void showEditAnswerDialog(Answer answer) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Answer");
        dialog.setHeaderText("Edit your answer below:");

        TextArea answerArea = new TextArea(answer.getAnswerContent());
        answerArea.setPromptText("Type your answer here...");
        answerArea.setPrefRowCount(3);

        dialog.getDialogPane().setContent(answerArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return answerArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedContent -> {
            try {
                answer.setAnswerContent(updatedContent);
                database.updateAnswer(answer);
                refreshAnswers();
            } catch (Exception e) {
                showError("Error updating answer: " + e.getMessage());
            }
        });
    }
    
    private void showReplyDialog(Answer parentAnswer) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply to Answer");
        dialog.setHeaderText("Enter your reply:");
        dialog.setContentText("Reply:");

        dialog.showAndWait().ifPresent(replyContent -> {
            if (!replyContent.trim().isEmpty()) {
                try {
                    database.addReply(currentQuestion.getQuestionID(), parentAnswer.getAnswerID(), replyContent, currentUser);
                    refreshAnswers();
                } catch (SQLException e) {
                    showError("Error adding reply: " + e.getMessage());
                }
            }
        });
    }




    private void showEditReplyDialog(Answer reply) {
        TextInputDialog dialog = new TextInputDialog(reply.getAnswerContent());
        dialog.setTitle("Edit Reply");
        dialog.setHeaderText("Edit your reply:");
        dialog.setContentText("Reply:");

        dialog.showAndWait().ifPresent(updatedReply -> {
            if (!updatedReply.trim().isEmpty()) {
                try {
                    database.updateReply(reply.getAnswerID(), updatedReply);
                    refreshAnswers();
                } catch (SQLException e) {
                    showError("Error updating reply: " + e.getMessage());
                }
            }
        });
    }


    private void showDeleteReplyConfirmation(Answer reply) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Reply");
        alert.setHeaderText("Are you sure you want to delete this reply?");
        alert.setContentText(reply.getAnswerContent());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Note: Since your deleteReply method takes the parent's answer ID and reply content,
                    // we pass in reply.getParentAnswerID() and reply.getAnswerContent().
                    database.deleteReply(reply.getParentAnswerID(), reply.getAnswerContent());
                    refreshAnswers();
                } catch (SQLException e) {
                    showError("Error deleting reply: " + e.getMessage());
                }
            }
        });
    }

    // Pop up confirmation when user deletes an answer
    private void showDeleteAnswerConfirmation(Answer answer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Answer");
        alert.setHeaderText("Are you sure you want to delete this answer?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    database.deleteAnswer(answer.getAnswerID());
                    refreshAnswers();
                } catch (Exception e) {
                    showError("Error deleting answer: " + e.getMessage());
                }
            }
        });
    }
}
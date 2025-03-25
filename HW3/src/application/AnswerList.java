package application;
import java.util.*;
import java.util.stream.Collectors;

public class AnswerList {
    private List<Answer> allAnswerList;

    public AnswerList() {
        allAnswerList = new ArrayList<>();
    }

    /**
     * Adds a new answer to the list
     * @param answer The answer/post to add to the thread
     * @throws IllegalArgumentException if answer is null or already exists
     */
    public void addAnswer(Answer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("Answer cannot be null");
        }
        if (getAnswer(answer.getAnswerID()) != null) {
            throw new IllegalArgumentException("Answer with ID " + answer.getAnswerID() + " already exists");
        }
        allAnswerList.add(answer);
    }

    /**
     * Updates an existing answer
     * @param answer The updated answer, updates in the database
     * @throws IllegalArgumentException if answer is null or doesn't exist
     */
    public void updateAnswer(Answer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("Answer cannot be null");
        }
        for (int i = 0; i < allAnswerList.size(); i++) {
            if (allAnswerList.get(i).getAnswerID().equals(answer.getAnswerID())) {
                allAnswerList.set(i, answer);
                return;
            }
        }
        throw new IllegalArgumentException("Answer with ID " + answer.getAnswerID() + " not found");
    }

    /**
     * Deletes an answer by its ID
     * @param answerId The unique ID of the answer to delete
     * @throws IllegalArgumentException if answerId is null or answer doesn't exist
     */
    public void deleteAnswer(String answerId) {
        if (answerId == null || answerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer ID cannot be null or empty");
        }
        boolean removed = allAnswerList.removeIf(a -> a.getAnswerID().equals(answerId));
        if (!removed) {
            throw new IllegalArgumentException("Answer with ID " + answerId + " not found");
        }
    }

    /**
     * Retrieves an answer by its ID
     * @param answerId The unique randomized ID of the answer to retrieve
     * @return The answer if found, null otherwise
     */
    public Answer getAnswer(String answerId) {
        if (answerId == null || answerId.trim().isEmpty()) {
            return null;
        }
        return allAnswerList.stream()
                .filter(a -> a.getAnswerID().equals(answerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return A list of all answers in the database
     */
    public List<Answer> getAllAnswers() {
        return new ArrayList<>(allAnswerList);
    }

    /**
     * Gets all answers for a specific question that a user poses
     * @param questionID The unique randomized ID of the question
     * @return A list of answers for the specified question
     */
    public List<Answer> getAnswersForQuestion(String questionID) {
        if (questionID == null || questionID.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return allAnswerList.stream()
                .filter(a -> a.getQuestionID().equals(questionID))
                .collect(Collectors.toList());
    }

    /**
     * Searches answers by keyword in their content
     * @param keyword The keyword to search for
     * @return A list of answers containing the keyword
     */
    public List<Answer> searchAnswers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowercaseKeyword = keyword.toLowerCase().trim();
        return allAnswerList.stream()
                .filter(a -> a.getAnswerContent().toLowerCase().contains(lowercaseKeyword))
                .collect(Collectors.toList());
    }

    /**
     * @return A list of all accepted answers
     */
    public List<Answer> getAcceptedAnswers() {
        return allAnswerList.stream()
                .filter(Answer::isAccepted)
                .collect(Collectors.toList());
    }

    /**
     * Gets the count of unread answers for a specific question
     * @param questionID The ID of the question
     * @return The number of UNREAD answers
     */
    public int getUnreadAnswersCount(String questionID) {
        if (questionID == null || questionID.trim().isEmpty()) {
            return 0;
        }
        return (int) allAnswerList.stream()
                .filter(a -> a.getQuestionID().equals(questionID))
                .count();
    }
}
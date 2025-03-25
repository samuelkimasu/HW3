package application;
import java.util.*;
import java.util.stream.Collectors;

public class QuestionList {
    private List<Question> allQuestionList;

    public QuestionList() {
        allQuestionList = new ArrayList<>();
    }

    /**
     * Adds a new question to the list
     * @param question The question to add
     * @throws IllegalArgumentException if question is null or already exists
     */
    public void addQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        if (getQuestion(question.getQuestionID()) != null) {
            throw new IllegalArgumentException("Question with ID " + question.getQuestionID() + " already exists");
        }
        allQuestionList.add(question);
    }

    /**
     * Updates an existing question
     * @param question The updated question
     * @throws IllegalArgumentException if question is null or doesn't exist
     */
    public void updateQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        for (int i = 0; i < allQuestionList.size(); i++) {
            if (allQuestionList.get(i).getQuestionID().equals(question.getQuestionID())) {
                allQuestionList.set(i, question);
                return;
            }
        }
        throw new IllegalArgumentException("Question with ID " + question.getQuestionID() + " not found");
    }

    /**
     * Deletes a question by its ID
     * @param questionID The ID of the question to delete
     * @throws IllegalArgumentException if questionID is null or question doesn't exist
     */
    public void deleteQuestion(String questionID) {
        if (questionID == null || questionID.trim().isEmpty()) {
            throw new IllegalArgumentException("Question ID cannot be null or empty");
        }
        boolean removed = allQuestionList.removeIf(q -> q.getQuestionID().equals(questionID));
        if (!removed) {
            throw new IllegalArgumentException("Question with ID " + questionID + " not found");
        }
    }

    /**
     * Retrieves a question by its ID
     * @param questionID The ID of the question to retrieve
     * @return The question if found, null otherwise
     */
    public Question getQuestion(String questionID) {
        if (questionID == null || questionID.trim().isEmpty()) {
            return null;
        }
        return allQuestionList.stream()
                .filter(q -> q.getQuestionID().equals(questionID))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return A list of all questions
     */
    public List<Question> getAllQuestions() {
        return new ArrayList<>(allQuestionList);
    }

    /**
     * Searches questions by keyword in their content
     * @param keyword The keyword to search for
     * @return A list of questions containing the keyword
     */
    public List<Question> searchQuestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowercaseKeyword = keyword.toLowerCase().trim();
        return allQuestionList.stream()
                .filter(q -> q.getQuestionContent().toLowerCase().contains(lowercaseKeyword))
                .collect(Collectors.toList());
    }

    /**
     * @return A list of all unresolved questions
     */
    public List<Question> getUnresolvedQuestions() {
        return allQuestionList.stream()
                .filter(q -> !q.isResolved())
                .collect(Collectors.toList());
    }

    /**
     * Gets the most recent questions
     * @param limit The maximum number of questions to return
     * @return A list of the most recent questions
     */
    public List<Question> getRecentQuestions(int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        return allQuestionList.stream()
                .sorted((q1, q2) -> q2.getTimeCreated().compareTo(q1.getTimeCreated()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets all questions by a specific author
     * @param authorId The ID of the author
     * @return A list of questions by the specified author
     */
    public List<Question> getQuestionsByAuthor(String authorId) {
        if (authorId == null || authorId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return allQuestionList.stream()
                .filter(q -> q.getAuthorID().equals(authorId))
                .collect(Collectors.toList());
    }
}
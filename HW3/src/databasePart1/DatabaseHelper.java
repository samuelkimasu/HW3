package databasePart1;
import java.sql.*;
//Feb 4 Iris: imported more java libraries for time capabilities
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


import java.util.*;

import application.Answer;
import application.Question;
import application.User;
import application.PrivateMessage;
import java.sql.Timestamp;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "role VARCHAR(255),"
				+ "userEmail VARCHAR(255),"
				+ "userFullName VARCHAR(255))";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE,"
	            // Feb4 Iris: added a timestamp column to the invitation code table
	    		+ "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(invitationCodesTable);
	    
	    //Feb 4: Vyas Create the OTPs table 
	    String OTPs = "CREATE TABLE IF NOT EXISTS OTPs ("
	    		+ "otp VARCHAR(10) PRIMARY KEY, "
	    		+ "userName VARCHAR(255), "
	    		+ "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(OTPs);
	    
	    // Create questions table
	    String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
	            + "questionID VARCHAR(36) PRIMARY KEY, "
	            + "questionContent TEXT NOT NULL, "
	            //Feb 25 Samuel
	            + "questionDesc TEXT,"
	            + "authorID VARCHAR(255) NOT NULL, "
	            + "timeCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "timeUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "isResolved BOOLEAN DEFAULT FALSE, "
	            //Feb 25 Vyas
	            + "isRead BOOLEAN DEFAULT FALSE, "
	            + "resolvedAnswerID VARCHAR(36)) ";
	    statement.execute(questionsTable);
	    
	    //Feb 25 Samuel changed answers table
	    // Create answers table
	    String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
	            + "answerID VARCHAR(36) PRIMARY KEY, "
	            + "questionID VARCHAR(36) NOT NULL, "
	            + "parentAnswerID VARCHAR(36) NULL, "  // Allows replies to specific answers
	            + "answerContent TEXT NOT NULL, "
	            + "authorID VARCHAR(255) NOT NULL, "
	            + "timeCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "isAccepted BOOLEAN DEFAULT FALSE, "
	            + "isRead BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (questionID) REFERENCES questions(questionID), "
        		+ "FOREIGN KEY (parentAnswerID) REFERENCES answers(answerID) ON DELETE CASCADE"
        		+ ")";
	    statement.execute(answersTable);
	    
	    // Create private messages table
        String privateMessagesTable = "CREATE TABLE IF NOT EXISTS private_messages ("
                + "messageId VARCHAR(36) PRIMARY KEY, "
                + "senderId VARCHAR(255) NOT NULL, "
                + "receiverId VARCHAR(255) NOT NULL, "
                + "questionId VARCHAR(36), "
                + "parentMessageId VARCHAR(36),"
                + "content TEXT NOT NULL, "
                + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "isRead BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (questionId) REFERENCES questions(questionID), "
                + "FOREIGN KEY (parentMessageId) REFERENCES private_messages(messageId))";
        statement.execute(privateMessagesTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
	    String insertUser = "INSERT INTO cse360users (userName, password, role, userEmail, userFullName) VALUES (?, ?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        
	        // Convert the list of roles to a comma-separated string
	        String rolesString = String.join(",", user.getRole());
	        pstmt.setString(3, rolesString);
	        
	        pstmt.setString(4, user.getEmail());
	        pstmt.setString(5, user.getFullName());

	        pstmt.executeUpdate();
	    }
	}

	// Validates a user's login credentials.
	// Feb3 Jubilee: updated method based on role arraylist
	public boolean login(User user) throws SQLException {
		
		//Feb7 Jubilee: make sure input fields are filled

	    if (user.getUserName() == null || user.getUserName().trim().isEmpty() ||
        user.getPassword() == null || user.getPassword().trim().isEmpty()) {
	            System.out.println("Login failed: One or more required fields are empty.");
	            return false;
	        }
		
		
		// Feb3 Jubilee: change query to return role
		
	    String query = "SELECT role FROM cse360users WHERE userName = ? AND password = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());

	        // Feb3 Jubilee: match found role with user's role
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {

	            	
		        	String rolesString = rs.getString("role");
		        	System.out.println("db roles string: " + rolesString);
		        	//Feb4 Jubilee: Convert into SQL Object Array
		        	if (rolesString != null) {
		        		
		        		//Feb4 Jubilee: convert to string array
		        		
		        		String[] objArray = rolesString.split(",");
		        		System.out.println("objArray: " + Arrays.toString(objArray));
		        		
		        		//Feb4 Jubilee: return as arraylist

	                    List<String> dbRoleList = new ArrayList<>(Arrays.asList(objArray));
	                    
	                    System.out.println("dbrolelist: " + dbRoleList);	
		                for (String userRole : user.getRole()) {
			                if (!dbRoleList.contains(userRole)) {
			                	System.out.println("No match found.");
			                    return false;
			                }	
		                }
		                return true;
	                }

	            }
	        }
	    }
	    
	    System.out.println("Login failed: No matching user found or roles don't match");
	    return false;
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	// Feb3 Jubilee: need to makes sure this works
	public List<String> getUserRole(String userName) throws SQLException {
    String query = "SELECT role FROM cse360users WHERE userName = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, userName);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String rolesString = rs.getString("role");
            if (rolesString != null && !rolesString.isEmpty()) {
                return new ArrayList<>(Arrays.asList(rolesString.split(",")));
            }
        }
    }
    
    return new ArrayList<>(); // Return an empty list if no roles found
}
	
	//Feb4 Iris: added new method to determine prefix for invitation code
	// Determines the prefix based on the role entered on InvitationPage.java
    private String getRolePrefix(String role) {
        switch (role.toLowerCase()) {
            case "student":
                return "STU";
            case "admin":
                return "ADM";
            case "instructor":
                return "INS";
            case "staff":
                return "STA";
            case "reviewer":
                return "REV";
            default:
                return "null"; // Invalid role
        }
    }
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String role) {
		//Feb4 Iris: added prefixes to the random invitation code to specify which role is assigned
		String prefix = getRolePrefix(role);
		if (prefix.equals("null")) {
			return "Invalid role. Try again.";
		}
	    String randomCode = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    
	    String fullCode = prefix + randomCode;
	    
	    String query = "INSERT INTO InvitationCodes (code, timestamp) VALUES (?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, fullCode);
	        //Feb4 Iris: added code that sets timestamp of invitation code (whatever local time it is when code is generated)
	        pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return fullCode;
	}
	
	// Validates an invitation code to check if it is unused.
	//Feb4 Iris: changed return value to String
	public String validateInvitationCode(String code) {
		//Feb4 Iris: specified SELECT command to the new timestamp column
	    String query = "SELECT timestamp FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	//Feb4 Iris: added code to check timestamp
	        	Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime createdTime = timestamp.toLocalDateTime(); // Time code was created
                LocalDateTime currentTime = LocalDateTime.now(); // Current time when enter is pressed

                // Check if the code is older than 1 minutes
                if (ChronoUnit.MINUTES.between(createdTime, currentTime) > 1) {
                	markInvitationCodeAsUsed(code); // Marks code as used
                    return "expired"; // Code is not valid
                }
	        	
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return "valid";
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "invalid";
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	//Vyas: generating one time password and inserts it into the database
	public String generateOTP() {
		String otp = String.format("%06d", new Random().nextInt(1000000)); // Generate a random 6-number code similar to the invitation code
		String query = "INSERT INTO OTPs (otp) VALUES (?)";
				
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			pstmt.executeUpdate();	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return otp;
	}
	
	//Feb 4 Vyas: Validates an OTP code to check if it is unused.
	public boolean validateOTPs(String otp) {
		String query = "SELECT * FROM OTPs WHERE otp = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)){
			pstmt.setString(1, otp);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				//Mark the OTP as used
				markOTPasUsed(otp);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	//Feb 4 Vyas: Marks the OTP code as used in the database.
	private void markOTPasUsed(String otp) {
		String query = "UPDATE OTPs SET isUsed = TRUE WHERE otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, otp);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//Feb 4 Vyas: Update the Password in the database after user changes it.
	//Feb7 Jubilee: changed to boolean for debugging
    public boolean changePassword(String userName, String newPassword) {
        String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	
	// Deletes a user from the database
	public boolean deleteUser(String userName) throws SQLException {
	    // Check if the user exists and is not an admin
	    String checkUserQuery = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement checkStmt = connection.prepareStatement(checkUserQuery)) {
	        checkStmt.setString(1, userName);
	        ResultSet rs = checkStmt.executeQuery();
	        if (rs.next()) {
	            String role = rs.getString("role");
	            if ("admin".equalsIgnoreCase(role)) {
	                return false; // Cannot delete an admin
	            }
	        } else {
	            return false; // User not found
	        }
	    }

	    // Delete the user
	    String deleteQuery = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
	        deleteStmt.setString(1, userName);
	        int affectedRows = deleteStmt.executeUpdate();
	        // If the count is greater than 0, the user was deleted
	        return affectedRows > 0; // Helpful for error handling
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	//Feb3: Andrew added method
    public List<User> getUserList() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT userName, password, role, userEmail, userFullName FROM cse360users";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                
                
                //Feb3 Jubilee: changed to arrayList
                
                List<String> rolesList = new ArrayList<>();
                
                rolesList = getUserRole(userName);
                
                String email = rs.getString("userEmail");
                String fullName = rs.getString("userFullName");
                
                User user = new User(userName, password, rolesList, email, fullName);
                users.add(user);
            }
        }
        return users;
    }

    //Feb7 Iris: added method to add update the table to display the right roles
    public void updateUserRoles(String userName, List<String> roles) throws SQLException {
        String updateRoles = "UPDATE cse360users SET role = ? WHERE userName = ?";
        
        try (PreparedStatement updateStmt = connection.prepareStatement(updateRoles)) {
            // Convert the list of roles to a comma-separated string
            String rolesString = String.join(",", roles);
            
            updateStmt.setString(1, rolesString);
            updateStmt.setString(2, userName);
            updateStmt.executeUpdate();
        }
    }
    
    
    //All QUESTION AND ANSWER METHODS
    
 // Question-related methods
    public void addQuestion(Question question) throws SQLException {
        String query = "INSERT INTO questions (questionID, questionContent, questionDesc, authorID, timeCreated, timeUpdated) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, question.getQuestionID());
            pstmt.setString(2, question.getQuestionContent());
            pstmt.setString(3, question.getQuestionDesc());
            pstmt.setString(4, question.getAuthorID());
            pstmt.setTimestamp(5, Timestamp.valueOf(question.getTimeCreated()));
            pstmt.setTimestamp(6, Timestamp.valueOf(question.getTimeUpdated()));
            pstmt.executeUpdate();
        }
    }
    
    public Question getQuestion(String questionID) throws SQLException {
        String query = "SELECT * FROM questions WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, questionID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Question question = new Question(
                    rs.getString("questionID"),
                    rs.getString("questionContent"),
                    rs.getString("questionDesc"),
                    rs.getString("authorID")
                );
                // Set additional properties
                question.setResolved(rs.getBoolean("isResolved"));
                question.setResolvedAnswerId(rs.getString("resolvedAnswerID"));
                return question;
            }
        }
        return null;
    }
    
    // Returns a list of questions sorted from earliest creation to latest
    public List<Question> getAllQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        // Selects all columns from database
        String query = "SELECT * FROM questions ORDER BY timeCreated DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question question = new Question(
                    rs.getString("questionID"),
                    rs.getString("questionContent"),
                    rs.getString("questionDesc"),
                    rs.getString("authorID")
                );
                question.setResolved(rs.getBoolean("isResolved"));
                question.setResolvedAnswerId(rs.getString("resolvedAnswerID"));
                questions.add(question);
            }
        }
        return questions;
    }
    
    
    //Feb 25 Vyas
    public List<Question> getUserUnresolvedQuestions(String authorId) throws SQLException {
    	List<Question> questions = new ArrayList<>();
    	String query = "SELECT * FROM questions WHERE isResolved = FALSE AND authorID = ? ORDER BY timeCreated DESC";
    	try (PreparedStatement pstmt = connection.prepareStatement(query)) {
    		pstmt.setString(1, authorId);
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			Question question = new Question(
    				rs.getString("questionID"),
    				rs.getString("questionContent"),
    				rs.getString("questionDesc"),
    				rs.getString("authorID")
    			);
    			questions.add(question);
    		}
    	}
		return questions;
    }
    
    //Feb25 Vyas
    
    public void markAnswerRead(String questionID, String authorID) throws SQLException {
    	String query = "UPDATE questions SET isRead = TRUE WHERE questionID = ? AND authorID = ?";
    	try (PreparedStatement pstmt = connection.prepareStatement(query)) {
    		pstmt.setString(1, questionID);
    		pstmt.setString(2, authorID);
    		pstmt.executeUpdate();
    	}
    }
    
    public List<Question> getUnreadQuestions(String authorID) throws SQLException {
        List<Question> questions = new ArrayList<>();
        // Selects all columns from database
        String query = "SELECT * FROM questions WHERE authorID = ? AND isRead = FALSE ORDER BY timeCreated DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        	pstmt.setString(1, authorID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question question = new Question(
                    rs.getString("questionID"),
                    rs.getString("questionContent"),
                    rs.getString("questionDesc"),
                    rs.getString("authorID")
                );
                question.setResolved(rs.getBoolean("isResolved"));
                question.setResolvedAnswerId(rs.getString("resolvedAnswerID"));
                questions.add(question);
            }
        }
        return questions;
    }
    
    // Returns a list of unresolved questions sorted from earliest creation to latest
    public List<Question> getUnresolvedQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions WHERE isResolved = FALSE ORDER BY timeCreated DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question question = new Question(
                    rs.getString("questionID"),
                    rs.getString("questionContent"),
                    rs.getString("questionDesc"),
                    rs.getString("authorID")
                );
                questions.add(question);
            }
        }
        return questions;
    }
    
    public void updateQuestion(Question question) throws SQLException {
        String query = "UPDATE questions SET questionContent = ?, timeUpdated = ? WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, question.getQuestionContent());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3, question.getQuestionID());
            pstmt.executeUpdate();
        }
    }
    
    public void deleteQuestion(String questionID) throws SQLException {
        // First, delete all associated answers
        String deleteAnswersQuery = "DELETE FROM answers WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteAnswersQuery)) {
            pstmt.setString(1, questionID);
            pstmt.executeUpdate();
        }

        // Then, delete the question
        String deleteQuestionQuery = "DELETE FROM questions WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuestionQuery)) {
            pstmt.setString(1, questionID);
            pstmt.executeUpdate();
        }
    }
    
    // Answer-related methods
    public void addAnswer(Answer answer) throws SQLException {
        String query = "INSERT INTO answers (answerID, questionID, answerContent, authorID, timeCreated) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, answer.getAnswerID());
            pstmt.setString(2, answer.getQuestionID());
            pstmt.setString(3, answer.getAnswerContent());
            pstmt.setString(4, answer.getAuthorID());
            pstmt.setTimestamp(5, Timestamp.valueOf(answer.getTimeCreated()));
            pstmt.executeUpdate();
        }
        
        String updateQuery = "UPDATE questions SET isRead = FALSE WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)){
        	pstmt.setString(1, answer.getQuestionID());
        	pstmt.executeUpdate();
        }
    }
    
    //Feb 25 Samuel : replies method
    
    public void addReply(String questionID, String parentAnswerID, String content, String authorID) throws SQLException {
        String sql = "INSERT INTO answers (answerID, questionID, parentAnswerID, answerContent, authorID) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, UUID.randomUUID().toString());  // Generate unique answer ID
            stmt.setString(2, questionID);
            stmt.setString(3, parentAnswerID);  // This allows for replies to replies
            stmt.setString(4, content);
            stmt.setString(5, authorID);
            
            stmt.executeUpdate();
        }
    }
    public void updateReply(String replyID, String newContent) throws SQLException {
        String query = "UPDATE answers SET answerContent = ? WHERE answerID = ? AND parentAnswerID IS NOT NULL";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newContent);
            pstmt.setString(2, replyID);
            pstmt.executeUpdate();
        }
    }
    public void deleteReply(String parentAnswerID, String replyContent) throws SQLException {
        String sql = "DELETE FROM answers WHERE parentAnswerID = ? AND answerContent = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parentAnswerID);
            stmt.setString(2, replyContent);
            stmt.executeUpdate();
        }
    }
    
    public List<Answer> getRepliesByAnswerID(String parentAnswerID) throws SQLException {
        List<Answer> replies = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE parentAnswerID = ? ORDER BY timeCreated ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parentAnswerID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Answer reply = new Answer(
                    rs.getString("answerID"),
                    rs.getString("questionID"),      
                    rs.getString("answerContent"),
                    rs.getString("authorID")
                );
                reply.setParentAnswerID(rs.getString("parentAnswerID"));
                replies.add(reply);

                // Recursively get sub-replies
                List<Answer> subReplies = getRepliesByAnswerID(reply.getAnswerID());
                replies.addAll(subReplies);
            }
        }
        return replies;
    }

    
    
    
    // Returns a list of answers sorted from earliest creation to latest
    public List<Answer> getAnswersForQuestion(String questionID) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        Map<String, List<String>> replyMap = new HashMap<>(); // Store replies separately

        String sql = "SELECT answerID, questionID, parentAnswerID, answerContent, authorID, isAccepted FROM answers WHERE questionID = ? ORDER BY timeCreated ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, questionID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String answerID = rs.getString("answerID");
                String parentAnswerID = rs.getString("parentAnswerID");
                String content = rs.getString("answerContent");
                String authorID = rs.getString("authorID");
                boolean isAccepted = rs.getBoolean("isAccepted");

                if (parentAnswerID == null) {
                    // This is a top-level answer
                    Answer answer = new Answer(answerID, questionID, content, authorID);
                    answer.setAccepted(isAccepted); // Set the accepted status
                    answers.add(answer);
                } else {
                    // This is a reply - store it in the map
                    replyMap.computeIfAbsent(parentAnswerID, k -> new ArrayList<>()).add(content);
                }
            }
        }

        // Attach replies to their respective parent answers
        for (Answer answer : answers) {
            if (replyMap.containsKey(answer.getAnswerID())) {
                answer.setReplies(replyMap.get(answer.getAnswerID())); // Assuming you have a setter
            }
        }

        return answers;
    }
    
    public void updateAnswer(Answer answer) throws SQLException {
        String query = "UPDATE answers SET answerContent = ? WHERE answerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, answer.getAnswerContent());
            pstmt.setString(2, answer.getAnswerID());
            pstmt.executeUpdate();
        }
    }

    //Mar21: Jubilee updated method so that question is marked as unresolved if accepted answer is deleted
    public void deleteAnswer(String answerID) throws SQLException {
        String checkQuery = "SELECT isAccepted, questionID FROM answers WHERE answerID = ?";
        String questionID = null;
        boolean isAccepted = false;
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, answerID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                isAccepted = rs.getBoolean("isAccepted");
                questionID = rs.getString("questionID");
            }
        }

        if (isAccepted && questionID != null) {
            String updateQuery = "UPDATE questions SET isResolved = FALSE, resolvedAnswerID = NULL WHERE questionID = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setString(1, questionID);
                updateStmt.executeUpdate();
            }
        }

        String deleteQuery = "DELETE FROM answers WHERE answerID = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setString(1, answerID);
            deleteStmt.executeUpdate();
        }
   }
    
    public void markQuestionResolved(String questionID, String answerID) throws SQLException {
        String query = "UPDATE questions SET isResolved = TRUE, resolvedAnswerID = ? WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, answerID);
            pstmt.setString(2, questionID);
            pstmt.executeUpdate();
        }

        // Mark the answer as accepted
        query = "UPDATE answers SET isAccepted = TRUE WHERE answerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, answerID);
            pstmt.executeUpdate();
        }
    }
    
 // Feb20 Andrew: Private message-related methods with improved error handling
    //Feb 25 Jubilee : Added method to fix accepted answers filter
    public List<Answer> getAcceptedAnswersWithReplies(String questionID) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        
        // Check if the question has any accepted answers
        String checkQuery = "SELECT COUNT(*) FROM answers WHERE questionID = ? AND isAccepted = TRUE";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, questionID);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) == 0) {
                // No accepted answers found, return empty list
            	System.out.println("Test if empty: " + answers.isEmpty());
                return answers;
            }
        }
        
        // Get the accepted answer
        String acceptedAnswerQuery = "SELECT * FROM answers WHERE questionID = ? AND isAccepted = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(acceptedAnswerQuery)) {
            pstmt.setString(1, questionID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Answer answer = new Answer(
                    rs.getString("answerID"),
                    rs.getString("questionID"),
                    rs.getString("answerContent"),
                    rs.getString("authorID")
                );
                answer.setAccepted(true);
                answers.add(answer);
                
                // Get replies for this accepted answer
                String repliesQuery = "SELECT * FROM answers WHERE parentAnswerID = ?";
                try (PreparedStatement replyStmt = connection.prepareStatement(repliesQuery)) {
                    replyStmt.setString(1, answer.getAnswerID());
                    ResultSet replyRs = replyStmt.executeQuery();
                    
                    while (replyRs.next()) {
                        Answer reply = new Answer(
                            replyRs.getString("answerID"),
                            replyRs.getString("questionID"),
                            replyRs.getString("answerContent"),
                            replyRs.getString("authorID")
                        );
                        reply.setParentAnswerID(answer.getAnswerID());
                        answers.add(reply);
                    }
                }
            }
        }
        
        return answers;
    }
    
    //Mar 21 Jubilee added method for automated testing
    public void deleteAllAnswersForQuestion(String questionID) throws SQLException {
        String query = "DELETE FROM answers WHERE questionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, questionID);
            pstmt.executeUpdate();
        }
    }
    
 // Private message-related methods with improved error handling
    public void addPrivateMessage(PrivateMessage message) throws SQLException {
        String query = "INSERT INTO private_messages (messageId, senderId, receiverId, questionId, content, timestamp, isRead, parentMessageId) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; 
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, message.getMessageId());
            pstmt.setString(2, message.getSenderId());
            pstmt.setString(3, message.getReceiverId());
            pstmt.setString(4, message.getQuestionId());
            pstmt.setString(5, message.getContent());
            pstmt.setTimestamp(6, Timestamp.valueOf(message.getTimestamp()));
            pstmt.setBoolean(7, message.isRead());
            // Feb26 Shanali: Allows tracking of a reply to a private message
            pstmt.setString(8, message.getParentMessageId()); 
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating private message failed, no rows affected.");
            }
        }
    }
    
    // Feb22 Andrew: Method for updating a private message
    public void updatePrivateMessage(PrivateMessage message) throws SQLException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        String query = "UPDATE private_messages SET content = ?, isRead = ? WHERE messageId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, message.getContent());
            pstmt.setBoolean(2, message.isRead());
            pstmt.setString(3, message.getMessageId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating private message failed, no rows affected.");
            }
        }
    }

    // Feb22 Andrew: Method for deleting a private message
    public void deletePrivateMessage(String messageId) throws SQLException {
        String query = "DELETE FROM private_messages WHERE messageId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, messageId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating private message failed, no rows affected.");
            }
        }
        
    }

    // Feb23 Andrew: Method for getting a single private messages
    public PrivateMessage getPrivateMessage(String messageId) throws SQLException {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        String query = "SELECT * FROM private_messages WHERE messageId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, messageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PrivateMessage(
                    	rs.getString("messageId"),
                        rs.getString("senderId"),
                        rs.getString("receiverId"),
                        rs.getString("questionId"),
                        rs.getString("content"),
                        // Feb26 Shanali: Accounting for the new table column
                        rs.getString("parentMessageId") 
                    );
                } else {
                    return null;
                }
            }
        }
    }

    // Feb23 Andrew: Method for getting a user's private messages
    public List<PrivateMessage> getPrivateMessagesForUser(String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        List<PrivateMessage> messages = new ArrayList<>();
        // Feb26 Shanali: only getting original private messages (excluding replies to a private message)
        String query = "SELECT * FROM private_messages WHERE (receiverId = ? OR senderId = ?) AND parentMessageId IS NULL ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PrivateMessage message = new PrivateMessage(
                    	rs.getString("messageId"),
                        rs.getString("senderId"),
                        rs.getString("receiverId"),
                        rs.getString("questionId"),
                        rs.getString("content"),
                        // Feb26 Shanali: Accounting for the new table column
                        rs.getString("parentMessageId") 
                    );
                    if (rs.getBoolean("isRead")) {
                        message.markAsRead();
                    }
                    messages.add(message);
                }
            }
        }
        return messages;
    }
    
    // Feb26 Shanali: Method for getting the replies to a private message
    public List<PrivateMessage> getRepliesToPrivateMessage(String parentMessageID) throws SQLException {
        if (parentMessageID == null || parentMessageID.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent Message ID cannot be null or empty");
        }
        List<PrivateMessage> replies = new ArrayList<>();
        String query = "SELECT * FROM private_messages WHERE parentMessageID = ? ORDER BY timestamp ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, parentMessageID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PrivateMessage reply = new PrivateMessage(
                        rs.getString("messageId"),
                        rs.getString("senderId"),
                        rs.getString("receiverId"),
                        rs.getString("questionId"),
                        rs.getString("content"),
                        // Feb26 Shanali: Accounting for the new table column
                        rs.getString("parentMessageId")
                    );
                    if (rs.getBoolean("isRead")) {
                        reply.markAsRead();
                    }
                    replies.add(reply);
                }
            }
        }
        return replies;
    }
    
    // Feb24 Shanali: Method to update the 'read' status of a private message
    public void markAsReadInDb(String messageId) throws SQLException {
        String query = "UPDATE private_messages SET isRead = TRUE WHERE messageId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, messageId); 
            pstmt.executeUpdate();
        }
    }

    // Feb23 Andrew: Method to count a user's unread private messages
    public int getUnreadPrivateMessageCount(String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        String query = "SELECT COUNT(*) FROM private_messages WHERE receiverId = ? AND isRead = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }   
}

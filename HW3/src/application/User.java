package application;
import java.util.*;


/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    //Feb3 Jubilee: changes to arrayList type
    private List<String> role;
    
    // Feb3 Shanali: Creating attributes for a user's email and full name 
    // so that ADMIN can create a list with this info
    private String userEmail;
    private String userFullName;

    // Constructor to initialize a new User object with userName, password, and role.
    // Feb3 Jubilee: changed role from a String to an ArrayList so that multiple roles can be assigned to one user
    public User( String userName, String password, List<String> role, String userEmail, String userFullName) {
        this.userName = userName;
        this.password = password;
        this.role = new ArrayList<>(role);
        
        // Feb3 Shanali : Setting the values for the new user attributes
        this.userEmail = userEmail;
        this.userFullName = userFullName;
     
    }
    
    // Sets the role of the user.
    // Feb3 jubilee: updated methods to reflect arraylist change
    public void setRole(List<String> role) {
    	this.role= new ArrayList<>(role);
    }
    
    public void addRole(String newRole) {
    	if(!role.contains(newRole) ) {
    		role.add(newRole);
    	}
    }
    
    public void removeRole(String unwantedRole) {
    	role.remove(unwantedRole);
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public List<String> getRole() { return new ArrayList<>(role); }
    // Feb3 Shanali : Creating getter methods for a user's email and full name
    public String getEmail() { return userEmail; }
    public String getFullName() { return userFullName; }
}
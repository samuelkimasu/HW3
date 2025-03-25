package application;


public class PasswordEvaluator {
	/**
	 * <p> Title: FSM-translated Password Assessor. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Finite State Machine
	 * into an executable Java program using the Password Evaluator Finite State Machine. 
	 * The code detailed design is based on a while loop with a cascade of if statements</p>
	 * 
	 * <p> Copyright: Lynn Robert Carter © 2022 </p>
	 * 
	 * @author Lynn Robert Carter
	 * 
	 * @version 0.00		2018-02-22	Initial baseline 
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	private static int state = 0;						// The current state value
	private static int nextState = 0;					// The next state value
	private static boolean finalState = false;			// Is this state a final state?
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundOtherChar = false;
	public static boolean foundLongEnough = false;
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	/**********
	 * This private method display the input line and then on a line under it displays an up arrow
	 * at the point where an error should one be detected.  This method is designed to be used to 
	 * display the error message on the console terminal.
	 * 
	 * @param input				The input string
	 * @param currentCharNdx	The location where an error was found
	 * @return					Two lines, the entire input line followed by a line with an up arrow
	 */
	private static void displayInputState() {
		// Display the entire input line
		System.out.println(inputLine);
		System.out.println(inputLine.substring(0,currentCharNdx) + "?");
		System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
				currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
	}

	/**********
	 * This method is a mechanical transformation of a Finite State Machine into a Java
	 * method.
	 * 
	 * @param input		The input string for finite state machine processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a help description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Finite State Machine simulation
		state = 0;
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) return "*** Error *** The password is empty!";
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Finite State Machine simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		
		nextState = -1;						// There is no next state
		passwordInput = input;				// Save a copy of the input
		foundUpperCase = false;				// Reset the Boolean flag
		foundLowerCase = false;				// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundSpecialChar = false;			// Reset the Boolean flag
		foundOtherChar = false;				// Reset the Boolean flag
		foundNumericDigit = false;			// Reset the Boolean flag
		foundLongEnough = false;			// Reset the Boolean flag
		running = true;						// Start the loop

		// The Finite State Machine simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition
		while (running) {
			displayInputState();
			switch (state) {
			case 0:
				// The cascading if statement sequentially tries the current character against all of the
				// valid transitions
				if (currentChar >= 'A' && currentChar <= 'Z') {
					System.out.println("Upper case letter found");
					foundUpperCase = true;
					nextState = 0;
				} else if (currentChar >= 'a' && currentChar <= 'z') {
					System.out.println("Lower case letter found");
					foundLowerCase = true;
					nextState = 0;
				} else if (currentChar >= '0' && currentChar <= '9') {
					System.out.println("Digit found");
					foundNumericDigit = true;
					nextState = 0;
				} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) {
					System.out.println("Special character found");
					foundSpecialChar = true;
					nextState = 0;
				} else {
					passwordIndexofError = currentCharNdx;
					running = false;
					foundOtherChar = true;
					nextState = 1;
				}
				if (currentCharNdx >= 7) {
					System.out.println("At least 8 characters found");
					foundLongEnough = true;
					nextState = 0;
				}
				
				// Go to the next character if there is one
				currentCharNdx++;
				if (currentCharNdx >= inputLine.length()) {
					running = false;
					nextState = 1;
				}
				else
					currentChar = input.charAt(currentCharNdx);
				
				// Move to the next state
				state = nextState;
				finalState = false;
				// Is the new state a final state?  If so, signal this fact.
				if (state == 1) {
					finalState = true;
					System.out.println(state + " is a Final State: " + finalState);
				}
				// Ensure that one of the cases sets this to a valid value
				nextState = -1;
				System.out.println();
			}
		}
		
		String errMessage = "";
		if (!foundUpperCase)
			errMessage += "Upper case; ";
		
		if (!foundLowerCase)
			errMessage += "Lower case; ";
		
		if (!foundNumericDigit)
			errMessage += "Numeric digits; ";
			
		if (!foundSpecialChar)
			errMessage += "Special character; ";
			
		if (!foundLongEnough)
			errMessage += "Long Enough; ";
		
		if (foundOtherChar)
			return "*** Error *** An invalid character has been found!";
		
		if (errMessage == "")
			return "";
		
		passwordIndexofError = currentCharNdx;
		return errMessage + "conditions were not satisfied";

	}
}

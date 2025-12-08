package org.accesspointprogram;            // `org.accesspointprogram`, since the naming convention is based on domain names,
import org.accesspointprogram.DatabaseService;
import org.accesspointprogram.EmailService;			   //  and Access Point owns https://accesspointprogram.org.

import javafx.scene.control.PasswordField;    //  - Similar to `TextField`, but specifically for passwords and keeping the contents secure.
import javafx.application.Application;        //  - The class representing applications.
import javafx.scene.control.ComboBox;         //  - A menu which lets you choose one ov many things, usually presented as a dropdown.
import javafx.scene.control.TextField;        //  - An area to enter text.  (Equivalent to the HTML `<input type="text" />`.)
import javafx.scene.control.Button;           //  - A basic, clickable widget.
import javafx.stage.Stage;                    //  - ???        [  Ditto. But, supposedly, represents the top-level window, like GTK `ApplicationWindow`.  ]

import java.util.regex.Pattern;               //  - Enable the use ov regular-expressions.
import java.util.Optional;                    //  - Represents a value that may or may not be present.  (Similar to Rust's `Option<T>`.)

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;
// these are javafx dialog boxes that will be used for popup messages for varification and messages.

public final class App extends Application {
	private static final DatabaseService db = new DatabaseService();     // Handles MongoDB.
	private static final EmailService emailService = new EmailService(); // Handles sending verification emails.

	/** Datatype to determine whether the user is inputting new information or confirming already-entered information. */
	enum LoginState {
		/** Confirming already-entered info. */
		SignupConfirmation,
		/** Logging out. */
		LoggedOut,
		/** Logged in. */
		LoggedIn
	}
	
	private static final Pattern           EMAIL_REGEX    = Pattern.compile(
		"(?:[a-z0-9!#$%&'*+\\x2f=?^_`\\x7b-\\x7d~\\x2d]+(?:\\.[a-z0-9!#$%&'*+\\x2f=?^_`\\x7b-\\x7d~\\x2d]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9\\x2d]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9\\x2d]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9\\x2d]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
		Pattern.CASE_INSENSITIVE
	);
	
	        static       ComboBox<AccountType> accountTypeWidget = null;
	        static       PasswordField         passwordWidget    = null;
	private static       long                  heldPassHash;                                //  <-- Password hash. ┐
	        static       Button                submitButton;                                //                     │
	        static       Optional<AccountType> accountType       = Optional.empty();        //                     │
	        static       TextField             emailWidget       = null;                    //                     ├ Pay attention to these.
	        static       LoginState            loginState        = LoginState.LoggedOut;    //                     │
	private static       String                heldEmail;                                   //  <-- Email.         ┘
	
	private static       Stage         window;
	        static       Page          page           = Page.Signup;
	
	public static void main(String[] args) {
		App.launch(args);
	}
	
	@Override public void start(Stage window) {
		App.window = window;
		
		window.setHeight(450);
		window.setWidth(800);
		window.setTitle("Robot App");
		
		App.refreshUI();
		
		window.show();
	}
	
	/** Take the form inputs and figure out what to do. */
	static boolean processFormSubmission(String email, String password) {
		switch(App.page) {
			case Signup -> {
				if(App.loginState != LoginState.SignupConfirmation && App.isValidEmail(email)) {

					if (db.isEmailInUse(email)) {
						showAlert("This email is already registered");
						return false;
					} 
					App.heldPassHash = App.passwordWidget.getText().hashCode();
					App.loginState = LoginState.SignupConfirmation;
					App.accountType = Optional.of(App.accountTypeWidget.getValue());
					App.heldEmail = email;
					
					return true;
				}
				
				long hash  = App.passwordWidget.getText().hashCode();
				if(!(email.equalsIgnoreCase(App.heldEmail) && hash == App.heldPassHash))
					 return false;


				// EmailService.java generates and returns the code so that App.java can verify it.
				int verificationCode = emailService.sendVerificationCode(email);

				// This popup requests the verification code from the user. it also shows which email the code was sent to.
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Verify Your Email");
				dialog.setHeaderText("A 6-digit verification code has been sent to: \n" + email);
				dialog.setContentText("Please enter the verification code:");

				Optional<String> result = dialog.showAndWait();

                if (result.isEmpty()) {
                    showAlert("Verification cancelled.");
                    return false;
                }
				try {
					int enteredCode = Integer.parseInt(result.get().trim());
					if (enteredCode != verificationCode) {
						showAlert("Incorrect verification code. Please try again.");
						return false;
					}
				} catch (NumberFormatException e) {
					showAlert("Please enter numbers only");
					return false;
				}

				// if the code entered is correct, it will create the user in the database.
				db.createUser(email, password, App.accountType.get().toString());

				showAlert("Account created successfully! You can now log in.");

				App.loginState = LoginState.LoggedOut;
				App.page = Page.LogIn;
				
				return true;
			}
			
			case LogIn -> {
				boolean loginStatus = db.validateLogin(email, password);
				if (loginStatus) {
					App.loginState = LoginState.LoggedIn;
					App.page = Page.Home;
				}
				return loginStatus;
			}
			
			default -> { return false; }
		}
	}
	
	private static boolean isValidEmail(String emailAddress) {
		return App.EMAIL_REGEX.matcher(emailAddress).matches();  }
	
	/** "Refresh" the user-interface by resetting the child ov the window element. */
	static void refreshUI() {
		App.window.setScene(switch(App.page) {
			case Signup, LogIn -> Page.createForm();
			case Home          -> Page.createHomepage();
		});
	}
	private static void showAlert(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

}
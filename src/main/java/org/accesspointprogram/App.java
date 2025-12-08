package org.accesspointprogram;            // `org.accesspointprogram`, since the naming convention is based on domain names,
										   //  and Access Point owns https://accesspointprogram.org.

import javafx.scene.control.PasswordField;    //  - Similar to `TextField`, but specifically for passwords and keeping the contents secure.
import javafx.application.Application;        //  - The class representing applications.
import javafx.scene.control.ComboBox;         //  - A menu which lets you choose one ov many things, usually presented as a dropdown.
import javafx.scene.control.TextField;        //  - An area to enter text.  (Equivalent to the HTML `<input type="text" />`.)
import javafx.scene.control.Button;           //  - A basic, clickable widget.
import javafx.stage.Stage;                    //  - ???        [  Ditto. But, supposedly, represents the top-level window, like GTK `ApplicationWindow`.  ]

import java.util.regex.Pattern;               //  - Enable the use ov regular-expressions.
import java.util.Optional;                    //  - Represents a value that may or may not be present.  (Similar to Rust's `Option<T>`.)





public final class App extends Application {
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
		// TODO:  Hook this system into the other part(s) ov our app, instead ov just putting it in `main`.
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
					App.heldPassHash = App.passwordWidget.getText().hashCode();
					App.loginState = LoginState.SignupConfirmation;
					App.heldEmail = email;
					
					return true;
				}
				
				long hash  = App.passwordWidget.getText().hashCode();
				if(!(email.equalsIgnoreCase(App.heldEmail) && hash == App.heldPassHash)) return false;
				
				// TODO:  Put account registration logic here.
				App.loginState = LoginState.LoggedOut;
				App.page = Page.LogIn;
				
				return true;
			}
			
			case LogIn -> {
				final boolean loginStatus = App.tryToLogIn(email, password.hashCode());
				if(loginStatus) {
					App.loginState = LoginState.LoggedIn;
					App.page = Page.Home;
				}
				return loginStatus;
			}
			
			default -> { return false; }
		}
	}
	
	private static boolean isValidEmail(String emailAddress) {  return App.EMAIL_REGEX.matcher(emailAddress).matches();  }
	
	/** Logic for logging in. */
	private static boolean tryToLogIn(String email, long passwordHash) {
		System.out.printf("Trying to log in with email \"%s\" and password \"%s\".\n", App.emailWidget.getText(), App.passwordWidget.getText());
		// TODO:  Implement logic for logging in.
		
		// TODO: Replace this constant with actual logic.
		final boolean LOGIN_SUCCESSFUL = false;
		if(LOGIN_SUCCESSFUL) {
			App.loginState = LoginState.LoggedIn;
			App.page = Page.Home;
		}
		
		return LOGIN_SUCCESSFUL;
	}
	
	/** "Refresh" the user-interface by resetting the child ov the window element. */
	static void refreshUI() {
		App.window.setScene(switch(App.page) {
			case Signup, LogIn -> Page.createForm();
			case Home          -> Page.createHomepage();
		});
	}
}
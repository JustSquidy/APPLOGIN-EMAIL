package org.accesspointprogram.Login.Registration;

import javafx.scene.control.PasswordField;    //  - Similar to `TextField`, but specifically for passwords and keeping the contents secure.
import javafx.scene.control.TextFormatter;    //  - Used to control the formatting that is considered acceptable in input-fields.
import javafx.scene.control.TextField;        //  - An area to enter text.  (Equivalent to the HTML `<input type="text" />`.)
import javafx.scene.control.ComboBox;         //  - A menu which lets you choose one ov many things, usually presented as a dropdown.
import javafx.scene.AccessibleRole;           //  - The 'role' that something is described to have in a layout, used to help assistive technology read things to users.
import javafx.scene.control.Button;           //  - A basic, clickable widget.
import javafx.scene.control.Label;            //  - Text which is (always) associated with an input element.
import javafx.scene.layout.VBox;              //  - A generic container whose children are, strictly, layed out vertically.
import javafx.geometry.Pos;                   //  - The position(ing) ov something.
import javafx.scene.Scene;                    //  - ???        [  I'm not really sure how `Scene` works (yet).  ]

import java.util.function.UnaryOperator;      //  - A datatype that represents a function that takes a single value for input and returns the same type for output.





/**
* A datatype that represents the page that should be shown to the user.  
*
* This datatype also contains helper methods for making the UI.
*/
public enum Page {
	/** The sign-up page. */
	Signup,
	/** The log-in page. */
	LogIn,
	/** The home page. */
	Home;
	
	static Scene createHomepage() {
		final VBox homepageContainer = new VBox();
		
		homepageContainer.getChildren().add(new Label("Test Homepage"));
		
		return new Scene(homepageContainer);
	}
	
	/** Build the sign-up/log-in page. */
	static Scene createForm() {
		// These next SIX lines are JUST to limit the length ov the email and password fields to 256 characters.
		final UnaryOperator<TextFormatter.Change> lenFilter = change -> {
			if(change.getControlNewText().length() > 256) return null;
			return change;
		};
		final TextFormatter<String> mailLenLimiter = new TextFormatter<>(lenFilter);
		final TextFormatter<String> passLenLimiter = new TextFormatter<>(lenFilter);
		// ^^^
		
		
		final VBox formContainer = new VBox();
		formContainer.setFillWidth(true);
		
		
		// Create a "fail label", in case the user can not proceed.
		final Label failLabel = new Label();
		failLabel.setAccessibleRole(AccessibleRole.TEXT);
		failLabel.setAlignment(Pos.BASELINE_CENTER);
		failLabel.setWrapText(false);
		failLabel.setMaxWidth(Double.MAX_VALUE);
		failLabel.setManaged(false);
		failLabel.setVisible(false);
		failLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
		
		formContainer.getChildren().add(failLabel);
		
		
		// Create the email part ov the form.
		{
			final VBox secEmail = new VBox();
			secEmail.setStyle("-fx-padding: 3px;");
			
			// Build the label that describes the email input box.
			final Label emailLabel = new Label(App.loginState == App.LoginState.SignupConfirmation ?
				"Please confirm your email address:" :
				"Email:"
			);
			emailLabel.setWrapText(false);
			
			// Build the input textfield which takes the user's email address.
			App.emailWidget = new TextField();
			App.emailWidget.setAccessibleRole(AccessibleRole.TEXT_FIELD);
			App.emailWidget.setTextFormatter(mailLenLimiter);
			App.emailWidget.setPromptText("example@email.com");
			
			emailLabel.setLabelFor(App.emailWidget);
			
			secEmail.getChildren().add(emailLabel);
			secEmail.getChildren().add(App.emailWidget);
			
			formContainer.getChildren().add(secEmail);
		}
		
		
		// Create the password part ov the form.
		{
			final VBox secPassword = new VBox();
			secPassword.setStyle("-fx-padding: 3px;");
			
			// Build the label that describes the email input box.
			final Label pwLabel = new Label(App.loginState == App.LoginState.SignupConfirmation ?
				"Please confirm your password:" :
				"Password:"
			);
			pwLabel.setWrapText(false);
			
			// Build the input textfield which takes the user's email address.
			App.passwordWidget = new PasswordField();
			App.passwordWidget.setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
			App.passwordWidget.setTextFormatter(passLenLimiter);
			App.passwordWidget.setPromptText("password");
			
			pwLabel.setLabelFor(App.passwordWidget);
			
			secPassword.getChildren().add(pwLabel);
			secPassword.getChildren().add(App.passwordWidget);
			
			formContainer.getChildren().add(secPassword);
		}
		
		
		// Add the dropdown box that lets users choose what kind ov account they have.
		if(App.page == Page.Signup && App.loginState != App.LoginState.SignupConfirmation) {
			App.accountTypeWidget = new ComboBox<>();
			App.accountTypeWidget.getItems().add(AccountType.Worker);
			App.accountTypeWidget.getItems().add(AccountType.Manager);
			
			formContainer.getChildren().add(App.accountTypeWidget);
		}
		
		
		// Add the submit button to the container.
		{
			final String buttonText = switch(App.loginState) {
				case SignupConfirmation -> "Sign Up!";
				case LoggedOut -> App.page == Page.LogIn ? "Log In" : "Submit";
				
				default -> "";
			};
			
			App.submitButton = new Button(buttonText);
			App.submitButton.setAccessibleRole(AccessibleRole.BUTTON);
			App.submitButton.setOnMouseClicked(e -> {
				if(!App.processFormSubmission(App.emailWidget.getText(), App.passwordWidget.getText())) {
					failLabel.setText(String.format(
						"Something didn't match up when you %sentered your information. Please try again.",
						App.loginState == App.LoginState.SignupConfirmation ? "re-" : ""
					));
					
					failLabel.setManaged(true);
					failLabel.setVisible(true);
				} else App.refreshUI();
			});
			
			formContainer.getChildren().add(App.submitButton);
		}
		// New to Calins code - added a button to go to signup page from login page
		if (App.page == Page.LogIn) {
			Button goToSignup  = new Button("Create an Account");
			goToSignup.setOnAction(e -> {
				App.page = Page.Signup;
				App.loginState = App.LoginState.LoggedOut;
				App.refreshUI();
			});
			formContainer.getChildren().add(goToSignup);
		}
		// New to Calins code - added a back button to go to login page from signup page
		if (App.page == Page.Signup && App.loginState != App.LoginState.SignupConfirmation) {
			Button goBack  = new Button("Back to Login");
			goBack.setOnAction(e -> {
				App.page = Page.LogIn;
				App.loginState = App.LoginState.LoggedOut;
				App.refreshUI();
			});
			formContainer.getChildren().add(goBack);
		}
		
		return new Scene(formContainer);
	}
}
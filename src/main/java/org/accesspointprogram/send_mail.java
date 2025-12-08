package org.accesspointprogram;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.List;

public class send_mail {
    public static void main(String[] args) {

        // Create email service and get unverified emails
        UserEmailService emailService = new UserEmailService();
        List<String> unverifiedEmails = emailService.getUnverifiedUserEmails();

        // Define email details
        String from_email = "accesspointproject106@gmail.com"; // This is an email I setup. This is where the emails will be sent out from
        String password = System.getenv("EMAIL_PASSWORD");// A system environment variable will need to be created so that the gmail sending out messages can only be sent out by us.

        // Process each unverified email
        for (String to_email : unverifiedEmails) {

        int verification_code = (int)(Math.random()* 900000) + 100000; // Creates a random 6-digit token to be used for activation after the user registration that will need to be coded into registration program.

        // Set mail server using gmail's SMTP. Side things like security and authentication of the email and password from_email previous lines
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        //  Creates a mail session. Basically contacting GMAIL with the email and password for this workspace to send out emails.
        Session mail_session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from_email, password);  //this code basically says create a new mail session using my properties, and whenever Gmail asks for login credentials, use this authenticator to provide them.
            }
        });

        try {
            // : Composes the email message
            Message email_text = new MimeMessage(mail_session); // Uses the previously created mail session to create a new email message.
            email_text.setFrom(new InternetAddress(from_email)); // Sets who the email is from and ensures address is formatted right using InternetAddress
            email_text.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to_email)); // Sets who the email is being sent to.
            email_text.setSubject("Warehouse Robot Email Activation!"); // Sets the subject of the email
            email_text.setContent(
                    "<h2>Thank you for signing up!</h2>" +
                            "<p>Hello User,</p>" +
                            "<p>This email was sent to help you activate your account!</p>" +
                            "<p>Please enter this code into your program!</p>" +
                            "<p Your activation code is: </p>" +
                            "<h2 style='color:black;'>" + verification_code + "</h2>" +
                            "<p style='color:red;'>- The RoboRaptors Team!</p>",
                    "text/html; charset=utf-8"
            ); // Sets the body of the email using HTML

            // : Sends it
            Transport.send(email_text); //This actually sends the email by connecting to Googles SMTP server using our credentials then closes the connection
            System.out.println("Email sent to " + to_email + " successfully!"); // simple confirmation message
        } catch (MessagingException e) {
            e.printStackTrace();
        } // gives error information
        }
        // Don't forget to close the MongoDB connection
        emailService.close();
    }
}

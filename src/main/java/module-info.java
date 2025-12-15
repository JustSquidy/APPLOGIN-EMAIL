module org.accesspointprogram {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics; 

    // Jakarta email
    requires jakarta.mail;
    requires jakarta.activation;

    // MongoDB (Driver Sync requires these 3 modules)
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;

    // jBCrypt hashing library
    requires jbcrypt;

    // Open packages for FXML reflection
    opens org.accesspointprogram.Dashboard to javafx.fxml;
    opens org.accesspointprogram.Login.Registration to javafx.fxml;

    // Exported packages
    exports org.accesspointprogram.Dashboard;
    exports org.accesspointprogram.Login.Registration;
}
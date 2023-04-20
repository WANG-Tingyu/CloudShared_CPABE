package priv.sr.crypto.gui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import priv.sr.crypto.Starter;
import priv.sr.crypto.model.User;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.util.List;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private Button btnLogin;

    @FXML
    private Label lbErrorMsg;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfUsername;




    private boolean isFieldFilled() {
        String username = tfUsername.getText();
        String password = pfPassword.getText();

        // Check if the username and password fields are filled
        if (username.isEmpty() || password.isEmpty()) {
            lbErrorMsg.setText("Please enter a username and password.");
            return false;
        }
        return true;
    }

    private boolean isValid() {
        String username = tfUsername.getText();
        String password = pfPassword.getText();
        // Load the list of valid users from the external file
        List<User> validUsers;
        try {
            validUsers = User.loadUsersFromFile(LoginController.class.getResource("../users.txt").getFile());
        } catch (IOException e) {
            lbErrorMsg.setText("Error loading user list.");
            return false;
        }
        // Check if the entered username and password match a valid user
        for (User user : validUsers) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        // Show an error message if the username and password are not valid
        lbErrorMsg.setText("Invalid username or password.");
        return false;
    }

//    Node node = tfUsername;
//    Scene scene = node.getScene();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btnLogin.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                lbErrorMsg.setText("");
//
                if(isFieldFilled() && isValid()) {
                    try {
                        // Get the Stage object
                        Stage stage = (Stage) btnLogin.getScene().getWindow();
                        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("home.fxml"));
                        Parent homeParent = fxmlLoader.load();
                        HomeController homeController = fxmlLoader.getController();
                        homeController.setUsername(tfUsername.getText());
                        Scene scene = new Scene(homeParent);
                        // Create a new Scene with the loaded Parent and set it as the new scene in the Stage
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        // Handle the exception
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

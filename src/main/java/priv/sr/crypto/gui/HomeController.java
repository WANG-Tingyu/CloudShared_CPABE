package priv.sr.crypto.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private Button btnDownloadFile;

    @FXML
    private Button btnUploadFile;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lbUsername;


    @FXML
    private void handleBtnLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnUploadFile.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    Stage stage = (Stage) btnUploadFile.getScene().getWindow();
                    FXMLLoader fxmlLoader = new FXMLLoader(HomeController.class.getResource("encrypt.fxml"));
                    Parent homeParent = fxmlLoader.load();
                    EncryptController encryptController = fxmlLoader.getController();
                    encryptController.setUsername(username1);
                    Scene scene = new Scene(homeParent);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    // Handle the exception
                    e.printStackTrace();
                }
            }
        });
        btnDownloadFile.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    Stage stage = (Stage) btnDownloadFile.getScene().getWindow();
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("decrypt.fxml"));
                    Parent root = fxmlLoader.load();
                    DecryptController decryptController = fxmlLoader.getController();
                    decryptController.setUsername(username1);
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private String username1;
    public void setUsername(String username) {
        lbUsername.setText("Welcome, " + username + "!");
        this.username1 = username;
    }
}

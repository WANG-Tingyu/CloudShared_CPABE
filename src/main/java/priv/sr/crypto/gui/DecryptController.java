package priv.sr.crypto.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import priv.sr.crypto.dcpabe.GlobalParameters;
import priv.sr.crypto.dcpabe.Message;
import priv.sr.crypto.model.DO;
import priv.sr.crypto.model.User;
import priv.sr.crypto.model.UserService;
import priv.sr.crypto.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class DecryptController implements Initializable {

    @FXML
    private Button btnDownload;
    @FXML
    private Label lbFilePath;

    @FXML
    private Button btnHome;
    @FXML
    private Label lbErrorMsg;
    @FXML
    private Button btnSelectFile2;




    @FXML
    private void handleBtnHome(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
        Parent root = loader.load();
        HomeController homeController = loader.getController();
        homeController.setUsername(username1);
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }


    private String username1;
    public void setUsername(String username) {
        this.username1 = username;
    }

    private boolean isFieldFilled() {
        String filePathText = lbFilePath.getText();
        if ( filePathText.isEmpty()) {
            lbErrorMsg.setText("Please select a file.");
            return false;
        }
        return true;
    }
    private UserService userService;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userService = new UserService();

        btnSelectFile2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select a file");
                // Set initial directory (optional)
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                // Show the dialog to select a file
                File selectedFile = fileChooser.showOpenDialog(btnSelectFile2.getScene().getWindow());
                if (selectedFile != null) {
                    lbFilePath.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        btnDownload.setOnAction(event -> {
            lbErrorMsg.setText("");
            if (isFieldFilled()) {
                String filePathText = lbFilePath.getText();
                try {
                    User newUser = userService.createUser(username1);
                    newUser.decrypt(filePathText, username1);
                    lbErrorMsg.setText("Successful!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

            }
        });

    }
}

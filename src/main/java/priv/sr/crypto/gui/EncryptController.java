package priv.sr.crypto.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import priv.sr.crypto.dcpabe.*;
import priv.sr.crypto.dcpabe.ac.AccessStructure;
import priv.sr.crypto.model.DO;
import priv.sr.crypto.model.DOService;
import priv.sr.crypto.utility.Utility;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class EncryptController implements Initializable {

    @FXML
    private Button btnHome;
    @FXML
    private Button btnSelectFile;

    @FXML
    private Button btnUpload;

    @FXML
    private Label lbFilePath;

    @FXML
    private TextField tfAccessPolicy;

    @FXML
    private TextField tfAttributeSet;

    @FXML
    private Label lbErrorMsg;


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

    public void setBtnSelectFile(ActionEvent e){
        FileChooser fileChooser = new FileChooser();
        Node node = (Node) e.getSource();
        fileChooser.setTitle("Plaintext File");
        String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
        fileChooser.setInitialDirectory(new File(currentPath));
        File selectedFile = fileChooser.showOpenDialog(node.getScene().getWindow());
        while(!checkFileType(selectedFile)){
            Stage stage = (Stage) node.getScene().getWindow();
            Alert.AlertType type = Alert.AlertType.WARNING;
            Alert alert = new Alert (type, "");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(stage);
            alert.getDialogPane().setContentText ("Please select the file again!");
            alert.getDialogPane().setHeaderText ("False File Type");
            alert.showAndWait();
            selectedFile = fileChooser.showOpenDialog(node.getScene().getWindow());
        }
        lbFilePath.setText(selectedFile.toString());
    }

    public void setBtnUpload(ActionEvent e) throws IOException {
        String attribute = tfAttributeSet.getText();
        if(!checkAttributes(attribute)){
            Node node = (Node) e.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            Alert.AlertType type = Alert.AlertType.WARNING;
            Alert alert = new Alert (type, "");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(stage);
            alert.getDialogPane().setContentText ("Please input the attribute set again!");
            alert.getDialogPane().setHeaderText ("Illegal Attribute Set");
            alert.showAndWait();
        }

        String policies = tfAccessPolicy.getText();
        if(!checkPolicies(policies)) {
            Node node = (Node) e.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            Alert.AlertType type = Alert.AlertType.WARNING;
            Alert alert = new Alert(type, "");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(stage);
            alert.getDialogPane().setContentText("Please input the Access Policy again!");
            alert.getDialogPane().setHeaderText("Illegal Access Policy");
            alert.showAndWait();
        }

        String filepath = lbFilePath.getText();

        if(filepath==null){
            Node node = (Node) e.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            Alert.AlertType type = Alert.AlertType.WARNING;
            Alert alert = new Alert (type, "");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initOwner(stage);
            alert.getDialogPane().setContentText ("Please select a file!");
            alert.getDialogPane().setHeaderText ("No Upload File");
            alert.showAndWait();
        }

        System.out.println(filepath);
        System.out.println(attribute);
        System.out.println(policies);
        String[] attributes = attribute.split(",");
        encrypt(filepath,policies,attributes);
    }

    public void encrypt(String filepath, String policies,String... attributes) throws IOException {
        String base = System.getProperty("user.dir");
        //String base = "/Users/daisy/Documents/JavaProject/kpabe-proxy/src/test/resources";
        GlobalParameters GP = DCPABE.globalSetup(160);
        //上传gp到链
        Utility.writeGlobalParameters(base+"/chain/gp.txt", GP);

        DO aDo = new DO();

        aDo.setAuthorityKeys(DCPABE.authoritySetup("auth1", GP, attributes));
        //上传access policy到链
        AccessStructure accessStructure = AccessStructure.buildFromPolicy(policies);
        System.out.println(accessStructure.hashCode());


        Message message = DCPABE.generateRandomMessage(GP);
        //上传密文到云
        //String inputfilepath=base+"/testResource.txt";
        aDo.uploadEncryptedPayload(filepath, message, base+"/cloud/EA.txt");

        String outputFilePath = base+"/chain/AAccessStructure.txt";
        aDo.uploadCipher(message, accessStructure, GP, outputFilePath);


        //给用户分配access structure
        String[] attribute = {"dummy", "diabetes", "A", "asian", "white"};
        System.out.println(attributes);
        aDo.genPersonalKey("tom", attribute, GP, base+"/key/");

    }

    public boolean checkAttributes(String s){
        return !s.equals("");
    }

    public boolean checkPolicies(String s){
        return !s.equals("");
    }

    public boolean checkFileType(File file){

        return file!=null;
    }

    private String username1;
    public void setUsername(String username) {
        this.username1 = username;
    }

    private boolean isFieldFilled() {
        String attributeSetText = tfAttributeSet.getText();
        String accessPolicyText = tfAccessPolicy.getText();
        String filePathText = lbFilePath.getText();

        // Check if the attributeSet, accessPolicy, and filePath fields are filled
        if (attributeSetText.isEmpty() || accessPolicyText.isEmpty() || filePathText.isEmpty()) {
            lbErrorMsg.setText("Please fill in all fields and select a file.");
            return false;
        }
        return true;
    }

    private DOService doService;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        doService = new DOService();
        btnSelectFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select a file");
                // Set initial directory (optional)
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                // Show the dialog to select a file
                File selectedFile = fileChooser.showOpenDialog(btnSelectFile.getScene().getWindow());
                if (selectedFile != null) {
                    lbFilePath.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        btnUpload.setOnAction(event -> {
            lbErrorMsg.setText("");
            if (isFieldFilled()) {
                String attributeSet = tfAttributeSet.getText().trim();
                String accessPolicy = tfAccessPolicy.getText().trim();
                // Load authority settings from file
                Map<String, List<String>> authoritySettings = new HashMap<>();
                InputStream inputStream = EncryptController.class.getResourceAsStream("../authority.txt");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] tokens = line.trim().split("\\s+");
                        String authorityID = tokens[0];
                        List<String> attributes = Arrays.asList(tokens).subList(1, tokens.length);
                        authoritySettings.put(authorityID, attributes);
                    }
                } catch (IOException e) {
                    lbErrorMsg.setText("Error loading authority settings.");
                    return;
                }
                // Check if authority ID exists in the authority settings
                if (!authoritySettings.containsKey(attributeSet)) {
                    lbErrorMsg.setText("Invalid authority ID.");
                    return;
                }

                // Get attribute list for the authority ID
                List<String> attributeList = authoritySettings.get(attributeSet);
                for (String s : attributeList.toArray(new String[0])) {
                    System.out.println(s);
                }

                try {
                    String auth = tfAttributeSet.getText();
                    DO newDO = doService.createDO(auth, attributeList.toArray(new String[0]));
                    String filePath = lbFilePath.getText();
                    String policy = tfAccessPolicy.getText();
                    newDO.encrypt(filePath, policy);
                    lbErrorMsg.setText("Successfully!");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
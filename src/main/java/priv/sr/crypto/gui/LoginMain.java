package priv.sr.crypto.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import priv.sr.crypto.dcpabe.*;
import priv.sr.crypto.dcpabe.key.PersonalKey;
import priv.sr.crypto.model.DOService;
import priv.sr.crypto.utility.Utility;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class LoginMain extends Application {
    public static void main(String[] args) throws IOException {

        /*try {
            initialize();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
*/
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {


        Parent root = FXMLLoader.load(LoginMain.class.getResource("login.fxml"));
        stage.setTitle("Safe File Outsource System");
        stage.setScene(new Scene(root));
        stage.show();

    }

    public static void initialize() throws IOException, ClassNotFoundException {
        Map<String, PersonalKeys> userKeysMap = new HashMap<>();
        Map<String, AuthorityKeys> authorityKeysMap = new HashMap<>();
        Map<String, List<String>> authorityAttrsMap = new HashMap<>();
        GlobalParameters gp = Utility.readGlobalParameters(LoginMain.class.getResource("../cloud/gp.txt").getPath());

// Load authorities from authority.txt
        InputStream inputStream = LoginMain.class.getResourceAsStream("../authority.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        while (line != null) {
            String[] tokens  = line.split(" ");
            String authorityID = tokens[0];
            List<String> attributes = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));
            AuthorityKeys authorityKeys = DCPABE.authoritySetup(authorityID, gp, attributes.toArray(new String[0]));
            authorityKeysMap.put(authorityID, authorityKeys);
            authorityAttrsMap.put(authorityID, attributes);
            line = reader.readLine();
        }
        reader.close();

// Load users from user_attr.txt
        inputStream = LoginMain.class.getResourceAsStream("../user_attr.txt");
        reader = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            String username = tokens[0];
            PersonalKeys personalKeys = new PersonalKeys(username);
            for (int i = 1; i < tokens.length; i++) {
                String attribute = tokens[i];
                for (Map.Entry<String, List<String>> entry : authorityAttrsMap.entrySet()) {
                    String authorityId = entry.getKey();
                    if (entry.getValue().contains(attribute)) {
                        AuthorityKeys authorityKeys = authorityKeysMap.get(authorityId);
                        personalKeys.addKey(DCPABE.keyGen(username, attribute, authorityKeys.getSecretKeys().get(attribute), gp));
                    }
                }
            }
            userKeysMap.put(username, personalKeys);
        }

// Save personal keys to file
        String targetFolder = "src/main/resources/priv/sr/crypto/user/";

        for (Map.Entry<String, PersonalKeys> entry : userKeysMap.entrySet()) {
            String username = entry.getKey();
            PersonalKeys personalKeys = entry.getValue();
            String folderPath = targetFolder+username;
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
            for (String attribute : personalKeys.getAttributes()) {
                PersonalKey key = personalKeys.getKey(attribute);
                Utility.writePersonalKey(folderPath+"/"+attribute+"_keys.ser", key);
            }
        }
        System.out.println("done");

    }
}

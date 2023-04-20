package priv.sr.crypto.model;

import priv.sr.crypto.dcpabe.*;
import priv.sr.crypto.dcpabe.key.PersonalKey;
import priv.sr.crypto.utility.Utility;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {
    private PersonalKeys personalKeys;
    public String name;
    private String password;
    public GlobalParameters gp;

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public User(){}
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public User(String name, GlobalParameters gp) throws IOException, ClassNotFoundException {
        this.name = name;
        this.gp = gp;
        String path = "src/main/resources/priv/sr/crypto/user/"+name;
        PersonalKeys personalKeys = Utility.readPersonalKeysFromFolder(name,path);
        this.personalKeys = personalKeys;
    }
    public static List<User> loadUsersFromFile(String filename) throws IOException {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    throw new IOException("Invalid file format");
                }
                String name = parts[0];
                String password = parts[1];
                users.add(new User(name, password));
            }
        }
        return users;
    }

    public void setGp(GlobalParameters gp) {
        this.gp = gp;
    }

    public void decrypt(String cipherPath, String name) throws IOException, ClassNotFoundException {
        String targetFolder = "src/main/resources/priv/sr/crypto/user/"+name+"/";
        File file = new File(cipherPath);
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);
        String plaintextPath = targetFolder+fileName;


        Ciphertext cipher = Utility.readCiphertextFromFile(cipherPath);
        Message message = DCPABE.decrypt(cipher, this.personalKeys, this.gp);
        Utility.writeBytesToFile(message.getM(), plaintextPath);

        String s = new String(Utility.readFileIntoBytes(plaintextPath)).trim();
        System.out.println(s);
        try (FileWriter writer = new FileWriter(plaintextPath)) {
            writer.write(s);
        }
    }

    public Object readObject(String path){
        try( ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            //写入对象
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void setPersonalKey(){

    }
    public void setPersonalKeys (String id, String basePath){
        PersonalKeys personalKeys = new PersonalKeys(id);
        File dir = new File(basePath);
        File[] array = dir.listFiles();
        if (!dir.exists()) {
            System.out.println("目录不存在");
        }
        for (File file : array) {
            PersonalKey o = (PersonalKey) readObject(file.getPath());
            personalKeys.addKey(o);
        }
        this.personalKeys = personalKeys;
        System.out.println(this.personalKeys.getAttributes());
    }

    public Message decryptedMessage(String cipherPath, GlobalParameters GP){
        Ciphertext encryptedMessage = (Ciphertext) readObject(cipherPath);
        return DCPABE.decrypt(encryptedMessage, personalKeys, GP);
    }
    public byte[] getFileByte(String fileAPath) throws IOException {
        File fileOne = new File(fileAPath);
        byte[] bytesArray = new byte[(int) fileOne .length()];

        FileInputStream fis = new FileInputStream(fileOne );
        fis.read(bytesArray);
        fis.close();

        return bytesArray;
    }

    public byte[] decryptedPayload(String encryptedFilePath, Message message) throws IOException {
        byte[] encryptedPayload = getFileByte(encryptedFilePath);
        byte[] decryptedPayload;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(encryptedPayload)) {
            decryptedPayload = Utility.encryptAndDecrypt(message.getM(), false, bais);
        }
        return decryptedPayload;
    }

}

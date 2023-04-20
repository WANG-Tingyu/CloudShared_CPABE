package priv.sr.crypto.model;

import priv.sr.crypto.dcpabe.*;
import priv.sr.crypto.dcpabe.ac.AccessStructure;
import priv.sr.crypto.dcpabe.key.PersonalKey;
import priv.sr.crypto.utility.Utility;

import java.io.*;

public class DO {


    private AuthorityKeys authorityKeys;
    public PublicKeys publicKeys;
    public GlobalParameters gp;
    public String name;

    public DO(){}
    public DO(String name, String[] attrs, GlobalParameters gp) {
        this.name = name;
        this.gp = gp;
        this.authorityKeys = DCPABE.authoritySetup(this.name, this.gp, attrs);
        PublicKeys publicKeys = new PublicKeys();
        publicKeys.subscribeAuthority(authorityKeys.getPublicKeys());
        this.publicKeys = publicKeys;
    }

    public void setGp(GlobalParameters gp) {
        this.gp = gp;
    }

    public PublicKeys getPublicKeys() {
        return publicKeys;
    }

    public void encrypt(String filePath, String policy) throws IOException {
        AccessStructure as = AccessStructure.buildFromPolicy(policy);
        Message message = new Message(Utility.readFileIntoBytes(filePath));

        File file = new File(filePath);
        String fileName = file.getName();

        Ciphertext ciphertext = DCPABE.encrypt(message, as, this.gp, this.publicKeys);
        String targetFolder = "src/main/resources/priv/sr/crypto/cloud/";
        Utility.writeCiphertextToFile(ciphertext, targetFolder+fileName+".enc");
    }

    public PersonalKeys distributeKey(String userID, String userBaseKeyPath, String... attributes) throws IOException {
        PersonalKeys personalKeys = new PersonalKeys(userID);
        for (String attribute : attributes) {
            System.out.println(attribute);
            PersonalKey personalKey = DCPABE.keyGen(userID, attribute, this.authorityKeys.getSecretKeys().get(attribute), this.gp);
            personalKeys.addKey(personalKey);
            Utility.writePersonalKey(userBaseKeyPath+attribute+".txt", personalKey);
        }

        return personalKeys;
    }


    // no use
    public void setAuthorityKeys(AuthorityKeys authorityKeys) {
        this.authorityKeys = authorityKeys;
    }



    public byte[] getFileByte(String fileAPath) throws IOException {
        File fileOne = new File(fileAPath);
        byte[] bytesArray = new byte[(int) fileOne .length()];

        FileInputStream fis = new FileInputStream(fileOne );
        fis.read(bytesArray);
        fis.close();

        return bytesArray;
    }

    public void uploadEncryptedPayload(String inputFilePath, Message message, String outputFilePath) throws IOException {
        byte[] fileBytes = getFileByte(inputFilePath);
        byte[] encryptedPayload;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            encryptedPayload = Utility.encryptAndDecrypt(message.getM(), true, bais);
        }
        byteToFile(encryptedPayload, outputFilePath);
    }

    public void uploadCipher(Message message, AccessStructure accessStructure, GlobalParameters GP, String outputFilePath) throws IOException {
        PublicKeys publicKeys = new PublicKeys();
        publicKeys.subscribeAuthority(authorityKeys.getPublicKeys());
        Ciphertext encryptedMessage = DCPABE.encrypt(message, accessStructure, GP, publicKeys);
        uploadObject(encryptedMessage, outputFilePath);
    }
    public void byteToFile(byte[] bytes, String outputFilePath) throws IOException {
        OutputStream out = new FileOutputStream(outputFilePath);
        InputStream is = new ByteArrayInputStream(bytes);
        byte[] buff = new byte[1024];
        int len;
        while ((len = is.read(buff)) != -1) {
            out.write(buff, 0, len);
        }
        is.close();
        out.close();
    }
    public void uploadObject(Object o, String outputFilePath) throws IOException {
        ByteArrayOutputStream os =new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(os);
        out.writeObject(o);
        out.flush();
        out.close();
        byte[] bytes = os.toByteArray();
        os.close();
        byteToFile(bytes, outputFilePath);
    }

    public void genPersonalKey(String userID, String[] attributes, GlobalParameters GP, String outputFilePath) throws IOException {
        for (String attribute : attributes) {
            PersonalKey key = DCPABE.keyGen(userID, attribute, authorityKeys.getSecretKeys().get(attribute), GP);
            uploadObject(key, outputFilePath+attribute+".txt");
        }
    }
}

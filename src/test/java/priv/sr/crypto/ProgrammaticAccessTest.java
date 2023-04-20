package priv.sr.crypto;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import priv.sr.crypto.dcpabe.*;
import priv.sr.crypto.dcpabe.ac.AccessStructure;
import priv.sr.crypto.model.DO;
import priv.sr.crypto.model.User;
import priv.sr.crypto.utility.Utility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

public class ProgrammaticAccessTest {
    @Test
    public void testKeyCorrectlyDecrypted() throws IOException {
        GlobalParameters GP = DCPABE.globalSetup(160);
        AccessStructure accessStructure = AccessStructure.buildFromPolicy("A");

        AuthorityKeys authorityKeys = DCPABE.authoritySetup("auth1", GP, "A", "B", "C", "D");

        Message message = DCPABE.generateRandomMessage(GP);

        PublicKeys publicKeys = new PublicKeys();
        publicKeys.subscribeAuthority(authorityKeys.getPublicKeys());

        Ciphertext encryptedMessage = DCPABE.encrypt(message, accessStructure, GP, publicKeys);

        PersonalKeys personalKeys = new PersonalKeys("myID");
        personalKeys.addKey(DCPABE.keyGen("myID", "A", authorityKeys.getSecretKeys().get("A"), GP));
        personalKeys.addKey(DCPABE.keyGen("myID", "B", authorityKeys.getSecretKeys().get("B"), GP));
        personalKeys.addKey(DCPABE.keyGen("myID", "C", authorityKeys.getSecretKeys().get("C"), GP));
        personalKeys.addKey(DCPABE.keyGen("myID", "D", authorityKeys.getSecretKeys().get("D"), GP));

        Message decryptedMessage = DCPABE.decrypt(encryptedMessage, personalKeys, GP);

        assertArrayEquals(message.getM(), decryptedMessage.getM());
    }

    @Test
    public void testDO() throws IOException{
        String base = "/Users/daisy/Documents/JavaProject/kpabe-proxy/src/test/resources";
        GlobalParameters GP = DCPABE.globalSetup(160);
        //上传gp到链
        Utility.writeGlobalParameters(base+"/chain/gp.txt", GP);

        DO aDo = new DO();

        aDo.setAuthorityKeys(DCPABE.authoritySetup("auth1", GP, "dummy", "diabetes", "A", "asian", "white"));
        //上传access policy到链
        AccessStructure accessStructure = AccessStructure.buildFromPolicy("and dummy and diabetes A or asian white");
        System.out.println(accessStructure.hashCode());


        Message message = DCPABE.generateRandomMessage(GP);
        //上传密文到云
        aDo.uploadEncryptedPayload(base+"/testResource.txt", message, base+"/cloud/EA.txt");

        String outputFilePath = base+"/chain/AAccessStructure.txt";
        aDo.uploadCipher(message, accessStructure, GP, outputFilePath);


        //给用户分配access structure
        String[] attributes = {"dummy", "diabetes", "A", "asian", "white"};
        aDo.genPersonalKey("tom", attributes, GP, base+"/key/");

    }

    @Test
    public void testUser() throws IOException, ClassNotFoundException {
        //String base = System.getProperty("user.dir");
        String base = "/Users/daisy/Documents/JavaProject/kpabe-proxy/src/test/resources";
        //链上拿gp
        GlobalParameters GP = Utility.readGlobalParameters(base + "/chain/gp.txt");
        User user1 = new User();
        //用户拿secret key
        user1.setPersonalKeys("tom",base+"/key");
        String cipherPath = base+"/chain/AAccessStructure.txt";
        Message message = user1.decryptedMessage(cipherPath, GP);

        //从云上拿密文并解密
        String encryptedFilePath = base+"/cloud/EA.txt";
        byte[] plaintext = user1.decryptedPayload(encryptedFilePath, message);

        System.out.println(new String(plaintext));
    }
    @Test
    public void testBF() throws IOException {
        DO aDo = new DO();
        byte[] aByte = aDo.getFileByte("/testResource.txt");
        aDo.byteToFile(aByte, "/Users/daisy/Downloads/test1.txt");

    }
    @Test
    public void testGP(){
        GlobalParameters GP = DCPABE.globalSetup(160);
        System.out.println(GP);
        GP = DCPABE.globalSetup(160);
        System.out.println(GP);}

    @Test
    public void testMessageCorrectlyDecrypted() throws IOException {
        GlobalParameters GP = DCPABE.globalSetup(160);
        System.out.println(GP);
        AccessStructure accessStructure = AccessStructure.buildFromPolicy("and dummy and diabetes A or asian white");


        AuthorityKeys authorityKeys = DCPABE.authoritySetup("auth567", GP, "dummy", "diabetes", "A", "B", "asian", "white");

        byte[] fileBytes;
        try (
                InputStream inputStream = getClass().getResourceAsStream("/message.txt");
        ) {
            fileBytes = IOUtils.toByteArray(inputStream);
        }
        System.out.println(new String(fileBytes));

        Message message = DCPABE.generateRandomMessage(GP);

        byte[] encryptedPayload;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            encryptedPayload = Utility.encryptAndDecrypt(message.getM(), true, bais);
        }

        PublicKeys publicKeys = new PublicKeys();
        publicKeys.subscribeAuthority(authorityKeys.getPublicKeys());

        Ciphertext encryptedMessage = DCPABE.encrypt(message, accessStructure, GP, publicKeys);


        PersonalKeys personalKeys = new PersonalKeys("myID");
        personalKeys.addKey(DCPABE.keyGen("myID", "dummy", authorityKeys.getSecretKeys().get("dummy"), GP));
        personalKeys.addKey(DCPABE.keyGen("myID", "diabetes", authorityKeys.getSecretKeys().get("diabetes"), GP));
        personalKeys.addKey(DCPABE.keyGen("myID", "A", authorityKeys.getSecretKeys().get("A"), GP));
        personalKeys.addKey(DCPABE.keyGen("myID", "asian", authorityKeys.getSecretKeys().get("asian"), GP));

        Message decryptedMessage = DCPABE.decrypt(encryptedMessage, personalKeys, GP);
//[B@6b71769e
//[B@2752f6e2
        assertArrayEquals(message.getM(), decryptedMessage.getM());
        System.out.println(message.getM()+" "+ decryptedMessage.getM());

        byte[] decryptedPayload;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(encryptedPayload)) {
            decryptedPayload =Utility.encryptAndDecrypt(message.getM(), false, bais);
        }
        System.out.println(new String(decryptedPayload));
        assertArrayEquals(fileBytes, decryptedPayload);
    }
}

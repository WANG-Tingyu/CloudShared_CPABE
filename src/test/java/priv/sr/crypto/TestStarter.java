package priv.sr.crypto;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import priv.sr.crypto.dcpabe.*;
import priv.sr.crypto.dcpabe.key.PersonalKey;
import priv.sr.crypto.gui.LoginMain;
import priv.sr.crypto.model.DO;
import priv.sr.crypto.model.User;
import priv.sr.crypto.utility.Utility;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class TestStarter {

    public void initUser(){

    }
    public void initAuth(){

    }

    public static void main(String[] args) {
        TestStarter starter = new TestStarter();
        starter.initAuth();
        starter.initUser();

//        LoginMain.main(args);

    }

@Test
    public void testDO() throws IOException, ClassNotFoundException {
        String base = "/Users/daisy/Desktop/Temp/";
        GlobalParameters gp = DCPABE.globalSetup(160);
        Utility.writeGlobalParameters(base+"gp.txt", gp);
        String[] attrs = {"a", "b"};
        DO aDo = new DO("user1", attrs, gp);
        aDo.setGp(gp);
        String filePath = base+"test.txt";
        byte[] m = Utility.readFileIntoBytes(filePath);
        Message message = new Message(m);
        aDo.encrypt(filePath, "a and b");

//        System.out.println(ciphertext.hashCode());
//        System.out.println(new String(ciphertext.getC0()));

        String cipherPath = base+"test-encrypted.txt";

//        Utility.writeCiphertextToFile(ciphertext, cipherPath);
        Ciphertext ciphertext2 = Utility.readCiphertextFromFile(cipherPath);

//        System.out.println(new String(ciphertext2.getC0()));
//        System.out.println(ciphertext.hashCode() == ciphertext2.hashCode());
//        System.out.println(Arrays.equals(Utility.toBytes(ciphertext), Utility.readFileIntoBytes(cipherPath)));

        String uid = "user2";
        String basePKPath = base+uid+"/";
        PersonalKeys personalKeys = aDo.distributeKey(uid, basePKPath, attrs);

//        Message decryptedMessage = DCPABE.decrypt(ciphertext, personalKeys, gp);
//        System.out.println(new String(decryptedMessage.getM()));

//        return Utility.objectToBytes(ciphertext2);
    }

    @Test
    public void testUser() throws IOException, ClassNotFoundException {
//        byte[] byte1 = testDO();

        String base = "/Users/daisy/Desktop/Temp/";
        String gpPath = base+"/gp.txt";
        GlobalParameters gp = Utility.readGlobalParameters(gpPath);
        String cipherPath = base+"test-encrypted.txt";

//        System.out.println(Arrays.equals(byte1, Utility.readFileIntoBytes(cipherPath)));

        Ciphertext ciphertext = Utility.readCiphertextFromFile(cipherPath);

        System.out.println(ciphertext.hashCode());

        String uid = "user2";
        User user = new User(uid, gp);



        String plaintextPath = base+"test-plain.txt";
        user.decrypt(cipherPath, uid);
        String s = new String(Utility.readFileIntoBytes(plaintextPath)).trim();
        try (FileWriter writer = new FileWriter(plaintextPath)) {
            writer.write(s);
        }
        System.out.println(s.trim());
        System.out.println(plaintextPath);

        String s2 = new String(Utility.readFileIntoBytes(base+"test.txt"));
        System.out.println(s2);


        System.out.println(Arrays.equals(Utility.readFileIntoBytes(plaintextPath), Utility.readFileIntoBytes(base + "test.txt")));
        System.out.println(s.equals(s2));
        System.out.println(s.length()+" "+s2.length());

    }



    @Test
    public void testMsg() throws IOException {
        String path = "/Users/daisy/Desktop/test.txt";
        byte[] fileBytes = Utility.readFileIntoBytes(path);
        System.out.println(new String(fileBytes));
        Message message = new Message(fileBytes);
    }

    public void testWrite() {

    }
}

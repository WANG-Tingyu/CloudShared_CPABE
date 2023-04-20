package priv.sr.crypto.utility;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import priv.sr.crypto.dcpabe.Ciphertext;
import priv.sr.crypto.dcpabe.GlobalParameters;
import priv.sr.crypto.dcpabe.PersonalKeys;
import priv.sr.crypto.dcpabe.key.PersonalKey;
import priv.sr.crypto.dcpabe.key.PublicKey;
import priv.sr.crypto.dcpabe.key.SecretKey;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Utility {
    public static GlobalParameters readGlobalParameters(String globalParametersPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputGlobalParameters = new ObjectInputStream(new FileInputStream(globalParametersPath))) {
            return (GlobalParameters) inputGlobalParameters.readObject();
        }
    }

    public static void writePublicKeys(String publicKeysPath, Map<String, PublicKey> publicKeys) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(publicKeysPath);
                ObjectOutputStream outputPublicKey = new ObjectOutputStream(fos)) {
            outputPublicKey.writeObject(publicKeys);
        }
    }

    public static void writeSecretKeys(String secretKeyPath, Map<String, SecretKey> secretKeys) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(secretKeyPath);
                ObjectOutputStream outputSecretKey = new ObjectOutputStream(fos)) {
            outputSecretKey.writeObject(secretKeys);
        }

    }

    public static Map<String, SecretKey> readSecretKeys(String secretKeysPath) throws IOException, ClassNotFoundException {
        try (
                FileInputStream fis = new FileInputStream(secretKeysPath);
                ObjectInputStream secretKeys = new ObjectInputStream(fis)) {
            return (Map<String, SecretKey>) secretKeys.readObject();
        }
    }

    public static void writePersonalKey(String personalKeyPath, PersonalKey personalKey) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(personalKeyPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(personalKey);
        }
    }


    public static void writeGlobalParameters(String globalParameterPath, GlobalParameters globalParameters) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(globalParameterPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(globalParameters);
        }
    }

    public static Map<String, PublicKey> readPublicKeys(String publicKeysPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream publicKeys = new ObjectInputStream(new FileInputStream(publicKeysPath))) {
            return (Map<String, PublicKey>) publicKeys.readObject();
        }
    }

    public static PersonalKey readPersonalKey(String personalKeyPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream personalKey = new ObjectInputStream(new FileInputStream(personalKeyPath))) {
            return (PersonalKey) personalKey.readObject();
        }
    }

    public static PersonalKeys readPersonalKeysFromFolder(String username, String folderPath) throws IOException, ClassNotFoundException {
        PersonalKeys personalKeys = new PersonalKeys(username);
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        Arrays.sort(files); // Sort the files by name, if desired

        for (File file : files) {
            if (file.isFile()) {
                PersonalKey personalKey = Utility.readPersonalKey(file.getPath());
                personalKeys.addKey(personalKey);
            }
        }
        return personalKeys;
    }
    public static PaddedBufferedBlockCipher initializeAES(byte[] key, boolean encrypt) {
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        int BLOCKSIZE = 16;
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(Arrays.copyOfRange(key, 0, 192 / 8)), new byte[BLOCKSIZE]);
        aes.init(encrypt, ivAndKey);
        return aes;
    }

    public static Ciphertext readCiphertextFromFile(String filePath) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                return readCiphertext(ois);
            }
        }
    }
    public static Ciphertext readCiphertext(ObjectInputStream input) throws IOException, ClassNotFoundException {
        return (Ciphertext) input.readObject();
    }
    public static Ciphertext bytesToCiphertext(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                return (Ciphertext) ois.readObject();
            }
        }
    }
    public static void writeCiphertextToFile(Ciphertext oct, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] ciphertextBytes = toBytes(oct);
            fos.write(ciphertextBytes);
        }
    }
    public static byte[] readCiphertextBytesFromFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] bytes = fis.readAllBytes();
            return bytes;
        }
    }
    public static byte[] toBytes(Ciphertext oct) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(oct);
            }
            return  baos.toByteArray();
        }
    }

    public static byte[] objectToBytes(Object oct) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(oct);
            }
            return  baos.toByteArray();
        }
    }

    public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                return ois.readObject();
            }
        }
    }
    public static void writeBytesToFile(byte[] bytes, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytes);
        }
    }

    public static byte[] readFileIntoBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    public static byte[] encryptAndDecrypt(byte[] key, boolean doEncrypt, InputStream message) {
        PaddedBufferedBlockCipher aes = initializeAES(key, doEncrypt);
        byte[] inBuff = new byte[aes.getBlockSize()];
        byte[] outBuff = new byte[aes.getOutputSize(inBuff.length)];
        int nbytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while (-1 != (nbytes = message.read(inBuff, 0, inBuff.length))) {
                int length1 = aes.processBytes(inBuff, 0, nbytes, outBuff, 0);
                outputStream.write(outBuff, 0, length1);
            }
            nbytes = aes.doFinal(outBuff, 0);
            outputStream.write(outBuff, 0, nbytes);

            return outputStream.toByteArray();
        } catch (InvalidCipherTextException | IOException e) {
            throw new RuntimeException("Error processing message");
        }
    }
}

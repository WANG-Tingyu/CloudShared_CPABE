package priv.sr.crypto;

import org.junit.Test;
import priv.sr.crypto.dcpabe.DCPABE;
import priv.sr.crypto.dcpabe.GlobalParameters;
import priv.sr.crypto.utility.Utility;

import java.io.IOException;

public class Tester {

    @Test
    public void start() throws IOException {
        GlobalParameters gp = DCPABE.globalSetup(160);
        String targetFolder = "src/main/resources/priv/sr/crypto/cloud/";
        Utility.writeGlobalParameters(targetFolder + "gp.txt", gp);
    }
}

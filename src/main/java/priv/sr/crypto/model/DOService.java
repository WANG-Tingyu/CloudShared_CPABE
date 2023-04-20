package priv.sr.crypto.model;

import priv.sr.crypto.dcpabe.GlobalParameters;
import priv.sr.crypto.utility.Utility;

import java.io.IOException;

public class DOService {

    public DO createDO(String name, String[] attrs) throws IOException, ClassNotFoundException {
        GlobalParameters gp = Utility.readGlobalParameters(DOService.class.getResource("../cloud/gp.txt").getPath());
        DO aDo = new DO(name, attrs, gp);
        return aDo;
    }
}

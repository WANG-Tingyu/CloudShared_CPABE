package priv.sr.crypto.model;

import priv.sr.crypto.dcpabe.GlobalParameters;
import priv.sr.crypto.utility.Utility;

import java.io.IOException;

public class UserService {
    public User createUser(String name) throws IOException, ClassNotFoundException {
        GlobalParameters gp = Utility.readGlobalParameters(DOService.class.getResource("../cloud/gp.txt").getPath());
        User user = new User(name, gp);
        return user;
    }


}

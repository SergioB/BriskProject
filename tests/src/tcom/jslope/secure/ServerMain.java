package tcom.jslope.secure;


/**
 * Date: 27.09.2005
 */
public class ServerMain {
    public static void main(String[] args) {
        PersistentServer server = PersistentServer.getInstance();
        server.start();
    }
}

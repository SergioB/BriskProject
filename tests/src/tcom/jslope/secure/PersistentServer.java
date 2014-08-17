/**
 * Date: 03.10.2005
 */
package tcom.jslope.secure;


import java.net.ServerSocket;
import java.io.IOException;

public class PersistentServer {
    private static PersistentServer ourInstance = new PersistentServer();
    private ServerSocket serverSocket;

    public static PersistentServer getInstance() {
        return ourInstance;
    }

    private PersistentServer() {
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(NetworkConfig.getPersistPort());
            boolean listening = true;
            while (listening) {
                new com.jslope.briskproject.server.ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not listen on port:" + NetworkConfig.getPersistPort());
            System.exit(-1);
        }
    }

}

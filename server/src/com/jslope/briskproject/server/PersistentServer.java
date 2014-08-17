/**
 * Date: 03.10.2005
 */
package com.jslope.briskproject.server;


import java.net.ServerSocket;
import java.io.IOException;
import com.jslope.briskproject.networking.*;
import com.jslope.persistence.sql.SqlUtil;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Options;

public class PersistentServer {
    private static PersistentServer ourInstance = new PersistentServer();
    private ServerSocket serverSocket;
    private volatile boolean listening; //volatile makes "listening" variable more thread safe

    public static PersistentServer getInstance() {
        return ourInstance;
    }

    private PersistentServer() {
    }
    public void shutdown() {
        listening = false;
    }

    public void start() {
        Options.setOnServer(true);
        try {
            serverSocket = new ServerSocket(NetworkConfig.getPersistPort());
            System.out.println("Server started, listening on port "+NetworkConfig.getPersistPort());
            listening = true;
            while (listening) {
                new ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not listen on port:" + NetworkConfig.getPersistPort());
            System.exit(-1);
        }
        User admin = ServerConfig.getAdmin();
        admin.save();
        admin.saveChildren();
        SqlUtil.closeDatabase();
        System.out.println("Good bye");
    }
}

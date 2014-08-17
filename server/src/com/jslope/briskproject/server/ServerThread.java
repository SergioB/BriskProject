package com.jslope.briskproject.server;

import com.jslope.briskproject.networking.secure.ServerSecureProtocol;
import com.jslope.briskproject.networking.NetworkConfig;
import com.jslope.toDoList.core.User;

import java.net.Socket;
import java.io.*;

/**
 * Date: 03.10.2005
 */
public class ServerThread extends Thread {
    private Socket socket = null;

    public ServerThread(Socket socket) {
        super("ServerThread");
        this.socket = socket;
    }

    public void run() {
//        DebugInputStream dis = new DebugInputStream(null);
//        DebugOutputStream dos = new DebugOutputStream(null);
        try {
            System.out.println("Thread started");
            ServerSecureProtocol protocol = new ServerSecureProtocol();
            protocol.setSocket(socket);
            protocol.handshake();
            ServerProtocol pr = new ServerProtocol(protocol);
            if (NetworkConfig.isConfigured()) {
                //send info that server is not new.
                pr.loginClient();
                pr.dataExchangeServer();
            } else {
                User login = pr.configureServer();
                setAdmin(login);
            }
            pr.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println("Server thread finished");
    }

    private void setAdmin(User login) {
        ServerConfig.setAdmin(login);
        NetworkConfig.setConfigured(true);
    }

}

package com.jslope.briskproject.server;

import com.jslope.briskproject.networking.PersistenceProtocol;
import com.jslope.briskproject.networking.secure.AbstractProtocol;
import com.jslope.toDoList.core.User;

import java.io.IOException;

/**
 * Date: 11.11.2005
 */
public class ServerProtocol extends PersistenceProtocol {
    public ServerProtocol(AbstractProtocol protocol) {
        super(protocol);
    }

    public void dataExchangeServer() throws IOException {
        byte requestType = in().readByte();
        if (requestType == FULL_REQUEST) {
            serverSendFullData();
        } else if (requestType == PARTIAL_REQUEST) {
            serverDataExchange();
        } else if (requestType == ADD_USER_REQUEST) {
            addUserOnServer();
        } else if (requestType == SHUTDOWN_SERVER) {
            shutdownServer();
        } else if (requestType == REQUEST_OBJECT) {
            sendRequestedObject();
        } else {
            throw new IOException("Wrong request from server, must be full or partial now is: " + requestType);
        }
    }


    private void shutdownServer() throws IOException {
        if (ServerConfig.isAdmin(loggedUser)) {
            System.out.println("Shutdown started...");
            PersistentServer.getInstance().shutdown();
            out().writeByte(OK);
        } else {
            System.out.println("Warning, shutdown was tried from a user without admin priveleges!");
        }
    }

    private void addUserOnServer() throws IOException {
        User user = new User();
        user.receive(in());
        if (User.loginExists(user.getLogin())) {
            out().writeByte(ERROR_LOGIN_EXISTS);
        } else { //todo: to verify that ID does not exists and that user is added to child of logged user
            user.save();
            user.initParent();
            out().writeByte(OK);
        }
        out().flush();
    }


    private void serverSendFullData() throws IOException {
        sendAll(loggedUser.getAccessibleRoot()); //after date is 0
    }


}

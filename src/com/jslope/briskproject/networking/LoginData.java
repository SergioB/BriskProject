package com.jslope.briskproject.networking;

import java.io.*;

/**
 * Date: 12.10.2005
 */
public class LoginData implements Streamable {
    String login = null, password = null;

    public LoginData() {
    }

    public LoginData(String login, char[] password) {
        this.login = login;
        this.password = new String(password);
    }

    public String toString() {
        return "login: " + login + " password: " + password;
    }

    public void send(DataOutput out) throws IOException {
        out.writeUTF(login);
        out.writeUTF(password);
    }

    public void receive(DataInput in) throws IOException {
        login = in.readUTF();
        password = in.readUTF();
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}

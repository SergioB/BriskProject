package com.jslope.briskproject.networking;

import com.jslope.briskproject.networking.ui.InputValueDialog;
import com.jslope.briskproject.networking.ui.ClientLoginDialog;
import com.jslope.briskproject.networking.ui.NewServerDialog;
import com.jslope.briskproject.networking.secure.ClientSecureProtocol;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.TreeElement;
import com.jslope.UI.TreePanel;
import com.jslope.UI.TaskTreeModel;
import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.LoadException;
import com.jslope.AutoUpdater;

import javax.swing.*;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.awt.*;

/**
 * Date: 30.10.2005
 */
public class ClientProtocol {
    private static PersistenceProtocol persistentProtocol;
    private static Socket socket;

    /**
     * adds a user from client to server
     *
     * @param user
     * @return true if user was added successfully and false if it wasn't possible to add this user
     */
    public static boolean addUser(User user) throws IOException {
        startConnect();
        boolean retValue = persistentProtocol.addUserOnClient(user);
        stopConnect();
        return retValue;
    }

    private static void stopConnect() throws IOException {
        persistentProtocol.flush();
        persistentProtocol.close();
        socket.close();
    }

    private static void startConnect() throws IOException {
        try {
            connectSocket(NetworkConfig.getHost());
            if (checkVersion()) {
                String serverType = persistentProtocol.getServerType();
                if (serverType.equals(PersistenceProtocol.PLEASE_LOGIN)) {
                    persistentProtocol.sendLogin(Options.getInstance().getUser().getLogin());
                    if (!persistentProtocol.wasLogged()) {
                        throw new IOException("Wrong login or password");
                    }
                } else {
                    throw new IOException("Server is not waiting for us, there must be some error");
                }
            } else {
                throw new IOException("Bad server protocol version");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkVersion() throws IOException {
        persistentProtocol.out().writeInt(NetworkConfig.ProtocolVersion);
        switch (in().readByte()) {
            case PersistenceProtocol.CORRECT_PROTOCOL_VERSION:
                return true;
            case PersistenceProtocol.UPDATE_IS_NEEDED:
                updateNewVersionFromServer();
                break;
            case PersistenceProtocol.WRONG_PROTOCOL_VERSION:
                throw new IOException("Server protocol version is lower than client version");
        }

        return true;
    }

    private static void updateNewVersionFromServer() throws IOException {
        int fileLenght = in().readInt();
        byte[] fileBytes = new byte[fileLenght];
        int offset = 0;
        int numRead = 0;
        while (offset < fileBytes.length
               && (numRead=in().read(fileBytes, offset, fileBytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < fileBytes.length) {
            throw new IOException("Could not completely read update file from stream ");
        }
        AutoUpdater.writeUpdateFile(fileBytes);
    }

    public static DataInputStream in() throws IOException {
        return persistentProtocol.in();
    }


    private static JFrame frame;

    public static void doInitialConfig() {
        frame = createInitFrame();
        boolean connectedToHost = false;
        String host = null;
        do {
            host = getHost();
            connectedToHost = connectToHost(host);
            if (!connectedToHost) {
                JOptionPane.showMessageDialog(frame,
                        "Unable to connect to server: " + host,
                        "Server is inaccessible",
                        JOptionPane.ERROR_MESSAGE);
            }

        } while (!connectedToHost);
        NetworkConfig.setHost(host);
        frame.setVisible(false);
    }

    private static String getHost() {
        String host = "";
        boolean validated = false;
        do {
            host = InputValueDialog.enterValue(frame, "Please enter server address", host);
            if (host == null) { //was clicked Cancel
                System.exit(0);
            }
            validated = validateHost(host);
            if (!validated) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter a correct server address",
                        "Server address is incorrect",
                        JOptionPane.ERROR_MESSAGE);
            }
        } while (!validated);
        return host;
    }

    private static boolean connectToHost(String host) {
        try {
            connectSocket(host);
            configureClient();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void connectSocket(String host) throws IOException, ClassNotFoundException {
        socket = new Socket(host, NetworkConfig.getPersistPort());
        ClientSecureProtocol protocol = new ClientSecureProtocol();
        protocol.setSocket(socket);
        protocol.handshake();
        persistentProtocol = new PersistenceProtocol(protocol);
    }

    private static void configureClient() throws IOException {
        if (checkVersion()) {
            String serverType = persistentProtocol.getServerType();
            System.out.println("serverType: " + serverType);
            LoginData login = null;
            if (serverType.equals(PersistenceProtocol.SERVER_NEW)) {
                login = getNewServerLoginData();
                User user = (User) Options.getInstance().getRootNode();
                user.setLogin(login);
                user.setIsAdmin(true);
                user.save();
                persistentProtocol.sendUser(user);
                persistentProtocol.flush();
            } else {
                boolean logged = false;
                do {
                    login = getClientLoginData();
                    persistentProtocol.sendLogin(login);
                    logged = persistentProtocol.wasLogged();
                    if (!logged) {
                        JOptionPane.showMessageDialog(frame,
                                "Wrong login/password",
                                "Wrong login",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } while (!logged);
                persistentProtocol.clientReceiveAll();
            }
            saveLogin(login);
            NetworkConfig.setConfigured(true);
        } else {
            System.exit(1);
        }
    }

    private static void saveLogin(LoginData login) {
        User user = Options.getInstance().getUser();
        user.setLogin(login);
        user.save();
    }

    private static LoginData getClientLoginData() {
        ClientLoginDialog cl = new ClientLoginDialog(frame);
        cl.pack();
        cl.focusComponents();
        cl.setVisible(true);
        if (!cl.wasClickedOk()) {
            System.exit(0);
        }
        return cl.getLoginData();
    }

    private static LoginData getNewServerLoginData() {
        NewServerDialog sd = new NewServerDialog(frame,
                "Remote server is not configured, now you should fill login information, " +
                "after which you'll become server administrator\n");
        sd.pack();
        sd.focusComponents();
        sd.setVisible(true);
        if (!sd.wasClickedOk()) {
            System.exit(0);
        }
        return sd.getLoginData();
    }

    private static JFrame createInitFrame() {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel("Initializing..."));
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSzie = frame.getSize();
        frame.setLocation((int) (screenSize.getWidth() / 2 - frameSzie.getWidth() / 2),
                (int) (screenSize.getHeight() / 2 - frameSzie.getHeight() / 2));
        frame.setVisible(true);
        return frame;
    }

    private static final int MINIMUM_HOST_LENGTH = 3;

    private static boolean validateHost(String host) {
        if (host.length() < MINIMUM_HOST_LENGTH) {
            return false;
        }
        return true;
    }

    public static void dataExchange() throws IOException {
        startConnect();
        persistentProtocol.clientDataExchange();
        stopConnect();
    }

    public static void fullDataRefresh() throws IOException {
        TreeElement selectedNode = TreePanel.getSelectedNode();
        String selectedNodeID;
        if (selectedNode != null) {
            selectedNodeID = selectedNode.getID();
        } else {
            selectedNodeID = null;
        }
        isntFullRequest = false;
        startConnect();
        persistentProtocol.clientReceiveAll();
        TaskTreeModel.getInstance().setUserRoot();
        TaskTreeModel.getInstance().loadTree();
        TaskTreeModel.getInstance().reload();
        stopConnect();
        isntFullRequest = true;
        if (selectedNodeID != null && DBPersistentObject.existsObject(selectedNodeID)) {
            selectedNode = (TreeElement) DBPersistentObject.loadObject(selectedNodeID);
            if (selectedNode.isStillInTree()) {
                TreePanel.selectNode(selectedNode);
            } else {
                TreePanel.selectRootNode();
            }
        } else {
            TreePanel.selectRootNode();
        }
    }

    public static void shutdownServer() throws IOException {
        startConnect();
        persistentProtocol.shutownServer();
        stopConnect();
    }

    private static boolean isntFullRequest = true;

    public static boolean isntFullRequest() {
        return isntFullRequest;
    }

    public static void requestObjectFromServer(String taskID) {
        try {
            startConnect();
            persistentProtocol.requestObjectFromServer(taskID);
            stopConnect();
        } catch (IOException e) {
            throw new LoadException(e, "Was unable to request object from server");
        }
    }
}

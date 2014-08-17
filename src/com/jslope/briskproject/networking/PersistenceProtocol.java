package com.jslope.briskproject.networking;

import com.jslope.briskproject.networking.secure.AbstractProtocol;
import com.jslope.toDoList.core.*;
import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.ObjectCache;
import com.jslope.utils.Log;
import com.jslope.AutoUpdater;

import java.io.*;
import java.util.*;

/**
 * Date: 12.10.2005
 */
public class PersistenceProtocol {
    private DataInputStream secureInput;
    private DataOutputStream secureOutput;
    public static final String SERVER_NEW = "SERVER_NEW", PLEASE_LOGIN = "login";
    private static final byte LOGGED = 1, NOT_LOGGED = 0;
    protected static final byte OK = 1;
    protected static final byte ERROR_LOGIN_EXISTS = 2;

    public static final byte CORRECT_PROTOCOL_VERSION = 1;
    public static final byte UPDATE_IS_NEEDED = 2;
    public static final byte WRONG_PROTOCOL_VERSION = 3;   //it's a responce when server protocol version is lower than client protocol version


    protected User loggedUser;

    //type of requests:
    protected static final byte FULL_REQUEST = 1;
    protected static final byte PARTIAL_REQUEST = 2;
    protected static final byte ADD_USER_REQUEST = 3;
    protected static final byte SHUTDOWN_SERVER = 4;
    protected static final byte REQUEST_OBJECT = 5;
    private long deltaTime;


    public PersistenceProtocol(AbstractProtocol protocol) {
        InputStream secin = protocol.getSecureInputStream();
        secureInput = new DataInputStream(secin);
        secureOutput = new DataOutputStream(protocol.getSecureOutputStream());
    }

    protected DataInputStream in() throws IOException {
        out().flush();
        return this.secureInput;
    }

    protected DataOutputStream out() {
        return this.secureOutput;
    }

    public User configureServer() throws IOException {
        checkProtocolVersionServer();
        out().writeUTF(SERVER_NEW);
        return readNewUser();
    }

    private User readNewUser() throws IOException {
        User user = new User();
        user.receive(in());
        return user;
    }

    private LoginData readLogin() throws IOException {
        LoginData login = new LoginData();
        login.receive(in());
        return login;
    }

    public void configureClient() throws IOException {
        String serverState = in().readUTF();
        if (serverState.equals(SERVER_NEW)) {
            initServerFromClient();
        } else {

        }

    }

    private void initServerFromClient() {

    }

    public String getServerType() throws IOException {
        return in().readUTF();
    }

    public void sendLogin(LoginData login) throws IOException {
        login.send(out());
    }

    public void flush() throws IOException {
        out().flush();
    }

    /**
     * it's a server side function which verifies client login
     *
     * @throws IOException
     */
    public void loginClient() throws IOException {
        checkProtocolVersionServer();
        out().writeUTF(PLEASE_LOGIN);
        boolean logged = false;
        LoginData loginData = null;
        do {
            loginData = readLogin();
            if (User.verifyLogin(loginData)) {
                logged = true;
                out().writeByte(LOGGED);
            } else {
                out().writeByte(NOT_LOGGED);
            }
        } while (!logged);
        loggedUser = User.loadUser(loginData);
    }

    private boolean checkProtocolVersionServer() throws IOException {
        int clientProtocolVersion = in().readInt();
        if (clientProtocolVersion == NetworkConfig.ProtocolVersion) {
            out().writeByte(CORRECT_PROTOCOL_VERSION);
        } else if (clientProtocolVersion < NetworkConfig.ProtocolVersion) {
            out().writeByte(UPDATE_IS_NEEDED);
            return sendNewVersionToClient();
        } else { // clientProtocolVersion > NetworkConfig.ProtocolVersion
            out().writeByte(WRONG_PROTOCOL_VERSION);
            return false;
        }
        return true;
    }

    private boolean sendNewVersionToClient() throws IOException {
        byte fileToUpload[] = AutoUpdater.getUpdateFile();
        out().writeInt(fileToUpload.length);
        out().write(fileToUpload);
        return true;
    }

    /**
     * waits for response to sent login
     *
     * @return true if was logged in otherwise false
     */
    public boolean wasLogged() throws IOException {
        return in().readByte() == LOGGED;
    }

    public void sendAll(TreeElement rootObject) throws IOException {
        serverSynchronizeTime();
        rootObject.showDebugInfo();
        serverSendPartial(rootObject, 0);
        out().writeUTF(rootObject.getID());
        out().writeLong(temporalTimePoint);
    }

    long temporalTimePoint = 0;

    public void serverSendPartial(TreeElement rootObject, long afterDate) throws IOException {
        Log.debug("send partial with root node: " + rootObject + " id:" + rootObject.getID() + " obj:" + rootObject.hashCode() + " after:" + afterDate);
        Collection<NetObject> objectsToSend = new HashSet<NetObject>();
        rootObject.addObjectsToSend(objectsToSend, afterDate, receivedIDs);
        sendObjects(objectsToSend);
    }

    /**
     * this method is called on client side
     *
     * @throws IOException
     */
    public void clientReceiveAll() throws IOException {
        deleteAll();
        Set<NetObject> receivedObjects = new HashSet<NetObject>();
        out().writeByte(FULL_REQUEST);
        clientSynchronizeTime();
        int howMany = in().readInt();
        System.out.println("In recieveAll, will receive " + howMany + " objects");
        for (int i = 0; i < howMany; i++) {
            String id = in().readUTF();
            receivedObjects.add(receiveNewObject(id));
        }
        String rootNodeID = in().readUTF();
        Options.setRootNode(rootNodeID);
        for (NetObject obj : receivedObjects) {
            obj.afterReceive();
        }
        NetworkConfig.setLastServerSend(in().readLong());   //here NetworkConfig is not saved, it is saved in next call to NetworkConfig
        NetworkConfig.setLastDataExchange(System.currentTimeMillis());
    }

    private void deleteAll() {
        for (NetObject obj : Transportables.objectsToSynchronize()) {
            obj.deleteAll();
        }
        ObjectCache.getInstance().clearCache();
    }

    Set<String> receivedIDs = new HashSet<String>();

    /**
     * when only new objects where sent
     *
     * @throws IOException
     */
    public void clientReceivePartial() throws IOException {
        receivedIDs = new HashSet<String>();
        int howMany = in().readInt();
        Log.debug("\n\n\n now will receive " + howMany + " objects");
        for (int i = 0; i < howMany; i++) {
            String id = in().readUTF();
            Log.debug("(partialReceive) now will receive object with id:" + id);
            receiveOneObject(id);
        }
        String debugInfo = "[";
        for (String id : receivedIDs) {
            NetObject obj = (NetObject) NetObject.loadObject(id);
            obj.afterReceive();
            debugInfo += obj + " ";
        }
        debugInfo += "]";
        Log.debug(" received obejcts: " + debugInfo);
    }

    private void receiveOneObject(String id) throws IOException {
        if (DBPersistentObject.existsObject(id)) {
            NetObject localObject = (NetObject) DBPersistentObject.loadObject(id);
            NetObject receivedObject = (NetObject) TreeElement.getNewObject(id);
            receivedObject.receive(in());
            System.out.print(", " + receivedObject);
            receivedObject.synchronizeTime(deltaTime);
            if (localObject.getModifiedTime() < receivedObject.getModifiedTime()) {
                localObject.copyFrom(receivedObject);
                localObject.save();
                receivedIDs.add(localObject.getID());
                Log.debug(" object " + localObject + " received successfully");
            } else {
                Log.debug("local time for object " + localObject + " is greater than new object time deltaTime=" + deltaTime);
            }
        } else {
            receiveNewObject(id);
            receivedIDs.add(id);
        }
    }

    private NetObject receiveNewObject(String id) throws IOException {
        NetObject obj = (NetObject) NetObject.getNewObject(id);
        obj.receive(in());
        obj.synchronizeTime(deltaTime);
        obj.save();
        return obj;
    }

    public boolean addUserOnClient(User user) throws IOException {
        out().writeByte(ADD_USER_REQUEST);
        user.getID();   //generating ID if it wasn't generated
        user.send(out());
        if (in().readByte() != OK) { //read status of the add operation
            return false;
        }
        return true;
    }

    public void close() throws IOException {
        out().close();
        in().close();
    }

    public void sendUser(User user) throws IOException {
        user.send(out());
    }

    public void sendObject(Streamable obj) throws IOException {
        obj.send(out());
    }

    public void clientDataExchange() throws IOException {
        out().writeByte(PARTIAL_REQUEST);
        clientSynchronizeTime();
        clientReceivePartial();
        clientSendPartial();
        sendPropagationNodes();
        if (in().readBoolean()) {    //check if we need a refresh
            System.out.println(" doing full data refresh ");
            ClientProtocol.fullDataRefresh();
        }
        PropagationList.getInstance().clear();
    }

    protected void clientSendPartial() throws IOException {
        Set<NetObject> objectsToSend = new HashSet<NetObject>();
        for (NetObject type : Transportables.objectsToSynchronize()) {
            type.addObjects(objectsToSend, receivedIDs);
        }
        sendObjects(objectsToSend);
    }

    private void sendObjects(Collection<NetObject> objectsToSend) throws IOException {
        temporalTimePoint = System.currentTimeMillis();
        System.out.println("objects to send: " + objectsToSend);
        out().writeInt(objectsToSend.size());
        for (NetObject obj : objectsToSend) {
            out().writeUTF(obj.getID());
            obj.send(out());
        }
    }

    protected void serverReceivePartial() throws IOException {
        receivedIDs = new HashSet<String>();
        int howMany = in().readInt();
        Set<NetObject> receivedObjects = new HashSet<NetObject>();
        NetObject localObject, receivedObject;
        for (int i = 0; i < howMany; i++) {
            String id = in().readUTF();
            if (DBPersistentObject.existsObject(id)) {
                localObject = (NetObject) DBPersistentObject.loadObject(id);
                receivedObject = (NetObject) DBPersistentObject.getNewObject(id);
                receivedObject.receive(in());
                receivedObject.synchronizeTime(deltaTime);
                if (localObject.getModifiedTime() < receivedObject.getModifiedTime()) {
                    localObject.copyFrom(receivedObject);
                    localObject.save();
                    receivedObjects.add(localObject);
                    receivedIDs.add(id);
                }
            } else {
                receivedObjects.add(receiveNewObject(id));
                receivedIDs.add(id);
            }
        }
        System.out.println(" received objects: " + receivedObjects);

        for (NetObject object : receivedObjects) {
            if (object instanceof TreeElement) {
                TreeElement element = (TreeElement) object;
                element.initParent();       // init parent is necessary so that propagate will work
            }
        }
        Tosser.addObjects(receivedObjects, loggedUser.getID());
    }

    private void serverSendPartial() throws IOException {
        ObjectsToSend objectsToSend = ObjectsToSend.forUser(loggedUser);
        List<NetObject> objectList = new Vector<NetObject>();
        while (objectsToSend.hasObjects()) {
            objectList.add(objectsToSend.next());
        }
        System.out.println("now will send: " + objectList);
        out().writeInt(objectList.size());
        for (NetObject obj : objectList) {
            System.out.println(" sending object " + obj);
            out().writeUTF(obj.getID());
            obj.send(out());
            System.out.println(" just sent object " + obj);
        }
    }

    protected void serverDataExchange() throws IOException {
        Log.debug(" data exchange with " + loggedUser);
        serverSynchronizeTime();
        serverSendPartial();
        serverReceivePartial();
        receivePropagationNodes();
        out().writeBoolean(RefreshMarker.hasMarker(loggedUser));
        Log.debug(" finished data exchange with " + loggedUser);
    }

    private void serverSynchronizeTime() throws IOException {
        long serverTime = System.currentTimeMillis();
        long clientTime = in().readLong();
        long transportTime = System.currentTimeMillis() - serverTime;
        Log.debug(" transportTime = " + transportTime);
        deltaTime = serverTime - clientTime;
        out().writeLong(deltaTime);
        Log.debug("deltaTime=" + deltaTime);
    }

    private void clientSynchronizeTime() throws IOException {
        out().writeLong(System.currentTimeMillis());
        out().flush();
        deltaTime = -in().readLong();   // we negate delta on client because it's oposite of server delta time
        System.out.println(" deltaTime=" + deltaTime);
    }

    /**
     * receives propagation nodes and propagate modified time up to the root node
     *
     * @throws IOException
     */
    private void receivePropagationNodes() throws IOException {
        PropagationList prObject = new PropagationList();
        prObject.receive(in());
        prObject.afterLoad();
        prObject.propagate();
    }

    private void sendPropagationNodes() throws IOException {
        PropagationList.getInstance().send(out());
    }

    public void shutownServer() throws IOException {
        out().writeByte(SHUTDOWN_SERVER);
        in().readByte();    //this byte should be OK, we wait for it so that when starting next shutdown trigger will be set
    }

    public void requestObjectFromServer(String objectID) throws IOException {
        out().writeByte(REQUEST_OBJECT);
        clientSynchronizeTime();
        out().writeUTF(objectID);
        if (in().readBoolean()) {
            receiveOneObject(objectID);
        } else {
            throw new IOException("There is no object on server with id " + objectID);
        }
    }

    protected void sendRequestedObject() throws IOException {
        serverSynchronizeTime();
        String objectID = in().readUTF();
        if (DBPersistentObject.existsObject(objectID)) {
            out().writeBoolean(true);
            NetObject obj = (NetObject) DBPersistentObject.loadObject(objectID);
            sendObject(obj);
        } else {
            out().writeBoolean(false);
        }
    }
}

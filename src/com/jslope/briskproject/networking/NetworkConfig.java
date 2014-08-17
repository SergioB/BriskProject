package com.jslope.briskproject.networking;

import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.fields.BooleanField;
import com.jslope.persistence.fields.VarcharField;
import com.jslope.persistence.fields.TimestampField;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Options;

/**
 * Date: 05.10.2005
 */
public class NetworkConfig extends DBPersistentObject {
    public static final int PersistPort = 1129;
    public static final int DebugPersistPort = 1128;
//    public static final int DebugPersistPort = 1129;
    public static final int ProtocolVersion = 1;
    private static final String IS_CONFIGURED = "IS_CONFIGURED";
    private static NetworkConfig ourInstance = null;
    private static final String SERVER_HOST = "ServerHost";
    private static final String LAST_DATE_EXCHANGE ="LastDateExchange";
    private static final String LAST_DATE_SERVER = "lastDateServer";
    private static final String TO_SEND_TABLE_EXISTS = "tosendTableExists";

    static {
        ourInstance = new NetworkConfig();
        ObjectList result = ourInstance.loadObjects(null);
        if (result.hasNext()) {
            ourInstance = (NetworkConfig) result.next();
        } else {
            ourInstance.save();
        }
        new User();//this is done so that these objects will be initialized before there will be
    }
    public void define() {
        this.addField(IS_CONFIGURED, new BooleanField(false));
        this.addField(SERVER_HOST, new VarcharField(128));
        this.addField(LAST_DATE_EXCHANGE, new TimestampField());
        this.addField(LAST_DATE_SERVER, new TimestampField());
        this.addField(TO_SEND_TABLE_EXISTS, new BooleanField(false));
    }

    public static NetworkConfig getInstance() {
        return ourInstance;
    }

    public static int getPersistPort() {
        if (Options.isDebugMode()) {
            return DebugPersistPort;
        }
        return PersistPort;
    }
    public static String getShortSimAlgorithm() {
        return "DESede";
//        return "AES";
    }
    public static String getSymetricAlgorithm() {
//        return "DESede";
//        return "AES/CBC/PKCS5Padding";
//        return "DESede/CBC/PKCS5Padding";
        return "DESede/CBC/ISO10126Padding";
    }
    public static String getAsymetricAlgorithm() {
        return "RSA";
    }

    public static boolean isConfigured() {
        return ((BooleanField)getInstance().getField(IS_CONFIGURED)).getBooleanValue();
    }

    public static void setConfigured(boolean newValue) {
        ((BooleanField)getInstance().getField(IS_CONFIGURED)).setValue(newValue);
        getInstance().save();
    }

    public static String getHost() {
        return getInstance().get(SERVER_HOST);
    }
    public static void setHost(String host) {
        getInstance().set(SERVER_HOST, host);
        getInstance().save();
    }

    public static long getLastDataExchange() {
        return ((TimestampField)(getInstance().getField(LAST_DATE_EXCHANGE))).getLongValue();
    }

    public static void setLastDataExchange(long timeForNextSend) {
        ((TimestampField)(getInstance().getField(LAST_DATE_EXCHANGE))).setLongValue(timeForNextSend);
        getInstance().save();
    }

    public static long getLastServerSend() {
        return ((TimestampField)(getInstance().getField(LAST_DATE_SERVER))).getLongValue();
    }

    public static void setLastServerSend(long newTime) {
        ((TimestampField)(getInstance().getField(LAST_DATE_SERVER))).setLongValue(newTime);
    }

    public static boolean toSendTableExists() {
        return ((BooleanField)(getInstance().getField(TO_SEND_TABLE_EXISTS))).getBooleanValue();
    }

    public static void setToSendTable(boolean newValue) {
        ((BooleanField)(getInstance().getField(TO_SEND_TABLE_EXISTS))).setValue(newValue);
        getInstance().save();
    }
}

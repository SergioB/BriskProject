package com.jslope.briskproject.networking.secure;

import com.jslope.briskproject.networking.secure.impl.EncryptedKey;
import com.jslope.briskproject.networking.NetworkConfig;
import com.jslope.utils.Utils;

import javax.crypto.*;
import java.io.*;
import java.security.*;

/**
 * Date: 05.10.2005
 */
public class ServerSecureProtocol extends AbstractProtocol {
    private static KeyPair keyPair;

    static {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(NetworkConfig.getAsymetricAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();    //todo: to handle this exception
        }
        kpg.initialize(1024, new SecureRandom());
        keyPair = kpg.generateKeyPair();
    }

    public void handshake() throws IOException, ClassNotFoundException {
        ObjectInputStream objIn = new ObjectInputStream(in);
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeObject(keyPair.getPublic());
        EncryptedKey encKey = (EncryptedKey) objIn.readObject();
        SecretKey key = encKey.getSecretKey(keyPair.getPrivate());
        objOut.flush();
        generateSecureStreams(key);

    }
       protected void generateSecureStreams(SecretKey key) {
        try {
            createOutputStream(key);
            createInputStream(key);
        } catch (Exception e) {
            e.printStackTrace(); //todo: to handle exception
        }
    }
}

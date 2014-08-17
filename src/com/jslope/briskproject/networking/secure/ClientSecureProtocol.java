package com.jslope.briskproject.networking.secure;

import com.jslope.briskproject.networking.secure.impl.EncryptedKey;
import com.jslope.briskproject.networking.NetworkConfig;

import javax.crypto.*;
import java.io.*;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;

/**
 * Date: 04.10.2005
 */
public class ClientSecureProtocol extends AbstractProtocol {
    private static SecretKey key;

    static {
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(NetworkConfig.getShortSimAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); //todo: to handle exception
        }
        kg.init(new SecureRandom());
        key = kg.generateKey();

    }

    public void handshake() throws IOException, ClassNotFoundException {
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        ObjectInputStream objIn = new ObjectInputStream(in);
        PublicKey publicKey = (PublicKey) objIn.readObject();
        EncryptedKey encKey = new EncryptedKey();
        encKey.store(publicKey, key);
        objOut.writeObject(encKey);
        objOut.flush();
        generateSecureStreams(key);
    }

    protected void generateSecureStreams(SecretKey key) {
        try {
            createInputStream(key);
            createOutputStream(key);
        } catch (Exception e) {
            e.printStackTrace(); //todo: to handle exception
        }
    }

}

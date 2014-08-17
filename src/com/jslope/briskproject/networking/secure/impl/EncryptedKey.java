package com.jslope.briskproject.networking.secure.impl;

import com.jslope.briskproject.networking.NetworkConfig;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.io.Serializable;

/**
 * Date: 05.10.2005
 */
public class EncryptedKey implements Serializable {
    byte[] encrypted = null;

    public void store(PublicKey publicKey, SecretKey key) {
        try {
            Cipher cif = Cipher.getInstance(NetworkConfig.getAsymetricAlgorithm());
            cif.init(Cipher.ENCRYPT_MODE, publicKey);
            encrypted = cif.doFinal(key.getEncoded());
        } catch (IllegalBlockSizeException e) { //todo: to handle these exceptions
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SecretKey getSecretKey(PrivateKey privateKey) {
        SecretKey key = null;
        try {
            Cipher cif = Cipher.getInstance(NetworkConfig.getAsymetricAlgorithm());
            cif.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decripted = cif.doFinal(encrypted);
            SecretKeyFactory kf = SecretKeyFactory.getInstance(NetworkConfig.getShortSimAlgorithm());

//            key = new SecretKeySpec(decripted, NetworkConfig.getSymetricAlgorithm());
//            SecretKeySpec secretKeySpec = new SecretKeySpec(decripted, NetworkConfig.getShortSimAlgorithm());
            DESedeKeySpec secretKeySpec = new DESedeKeySpec(decripted);
            key  = kf.generateSecret(secretKeySpec);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }
}

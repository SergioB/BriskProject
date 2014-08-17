package com.jslope.briskproject.networking.secure;

import com.jslope.briskproject.networking.secure.impl.MyCipherOutputStream;
import com.jslope.briskproject.networking.secure.impl.MyCipherInputStream;
import com.jslope.briskproject.networking.NetworkConfig;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.net.Socket;

/**
 * Date: 05.10.2005
 */
public abstract class AbstractProtocol {
    protected InputStream in, secIn;
    protected OutputStream out, secOut;


    public void setOutputStream(OutputStream outputStream) {
        out = outputStream;
    }

    public void setInputStream(InputStream inputStream) {
        in = inputStream;
    }

    public InputStream getSecureInputStream() {
        return secIn;
    }

    public OutputStream getSecureOutputStream() {
        return secOut;
    }
    protected void createInputStream(SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cif;
        cif = Cipher.getInstance(NetworkConfig.getSymetricAlgorithm());
        IvParameterSpec iv = generateIV(cif);
        cif.init(Cipher.DECRYPT_MODE, key, iv);
        secIn = new MyCipherInputStream(in, cif);
    }

    protected void createOutputStream(SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
        Cipher cif = Cipher.getInstance(NetworkConfig.getSymetricAlgorithm());
        IvParameterSpec iv = generateIV(cif);
        cif.init(Cipher.ENCRYPT_MODE, key, iv);
//        out.flush();
        secOut = new MyCipherOutputStream(out, cif);
//        secOut.flush();
    }

    private IvParameterSpec generateIV(Cipher cif) {
        int size = cif.getBlockSize();
        byte[] tmp = new byte[size];
        Arrays.fill(tmp, (byte)15);
        IvParameterSpec iv = new IvParameterSpec(tmp);
        return iv;
    }

    public void setSocket(Socket socket) throws IOException {
        setInputStream(socket.getInputStream());
        setOutputStream(socket.getOutputStream());
    }


    abstract public void handshake() throws IOException, ClassNotFoundException;
}

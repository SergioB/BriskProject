package com.jslope.briskproject.networking.secure.impl;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import java.io.*;

/**
 * Date: 08.10.2005
 * A class which works with MyCipherInputStream
 */
public class MyCipherInputStream extends InputStream {
    private DataInputStream parent;
    private Cipher cipher;
    private ByteArrayInputStream in;

    public MyCipherInputStream(InputStream parent, Cipher cif) {
        this.parent = new DataInputStream(parent);
        this.cipher = cif;
        in = new ByteArrayInputStream(new byte[0]);  // setting buffer to be empty
    }

    public int read() throws IOException {
        if (bufferIsEmpty()) {
            readData();
        }
        return in.read();
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (bufferIsEmpty()) {
            readData();
        }
        return in.read(b, off, len);
    }

    private void readData() throws IOException {
        int lenght = parent.readInt();
        int readData = 0, result;
        byte[] buffer = new byte[lenght];
        while (readData < lenght) {
            result = parent.read(buffer, readData, lenght - readData);
            if (result == -1) {
                throw new IOException("premature end of stream (read returned -1)");
            }
            readData += result;
        }
        try {
            byte[] decrypted = cipher.doFinal(buffer);
            in = new ByteArrayInputStream(decrypted);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    private boolean bufferIsEmpty() {
        return in.available() == 0;
    }
}

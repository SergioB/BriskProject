package com.jslope.briskproject.networking.secure.impl;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import java.io.*;

/**
 * Date: 08.10.2005
 * A class which accumulates data to be encrypted then when flush is called all data are written to the socket
 */
public class MyCipherOutputStream extends OutputStream {
    private ByteArrayOutputStream buffer;
    private DataOutputStream parent;
    private Cipher cipher;

    public MyCipherOutputStream(OutputStream parent, Cipher cif) {
        buffer = new ByteArrayOutputStream();
        this.parent = new DataOutputStream(parent);
        this.cipher = cif;
    }

    public void write(int b) throws IOException {
        buffer.write(b);
    }

    public void write(byte[] b) throws IOException {
        buffer.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        buffer.write(b, off, len);
    }

    public void flush() throws IOException {
        if (buffer.size() > 0) {
            try {
                byte[] encrypted = cipher.doFinal(buffer.toByteArray());
                parent.writeInt(encrypted.length);
                parent.write(encrypted);
                parent.flush();
                buffer.reset();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
                throw new IOException("was IllegalBlockSize");
            } catch (BadPaddingException e) {
                e.printStackTrace();
                throw new IOException("was BadPadding");
            }
        }
    }

    public void close() throws IOException {
        flush();
        parent.close();
    }
}

package com.jslope.briskproject.networking.secure.impl;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Date: 06.10.2005
 */
public class DebugInputStream extends InputStream {
    InputStream in;
    private ByteArrayOutputStream bi;

    public void setShow(boolean show) {
        this.show = show;
    }

    boolean show = false;

    public DebugInputStream(InputStream parent) {
        in = parent;
        bi = new ByteArrayOutputStream();
    }

    public int read() throws IOException {
        int byt = in.read();
        bi.write(byt);
        if (show) {
            System.out.print(" " + byt);
        }
        return byt;
    }

    public byte[] getBytes() {
        return bi.toByteArray();
    }

    public void close() throws IOException {
        in.close();
    }

    public int available() throws IOException {
        return in.available();
    }

    public void setParent(InputStream inputStream) {
        in = inputStream;
    }
}

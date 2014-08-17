package com.jslope.briskproject.networking.secure.impl;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
 * Date: 06.10.2005
 */
public class DebugOutputStream extends OutputStream {
    OutputStream out;
    private ByteArrayOutputStream bi;

    public void setShow(boolean show) {
        this.show = show;
    }

    boolean show = false;

    public DebugOutputStream(OutputStream parent) {
        out = parent;
        bi = new ByteArrayOutputStream();
    }

    public void write(int b) throws IOException {
        if (show) {
            System.out.print(" " + b);
        }
        bi.write(b);
        out.write(b);
    }

    public byte[] getBytes() {
        return bi.toByteArray();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }

    public void setParent(OutputStream outputStream) {
        out = outputStream;
    }
}

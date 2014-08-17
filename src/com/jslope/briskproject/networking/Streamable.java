package com.jslope.briskproject.networking;

import java.io.DataOutput;
import java.io.IOException;
import java.io.DataInput;

/**
 * Date: 19.10.2005
 */
public interface Streamable {
    public void send(DataOutput out) throws IOException;
    public void receive(DataInput in) throws IOException;
}

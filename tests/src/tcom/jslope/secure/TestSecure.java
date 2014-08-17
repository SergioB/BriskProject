package tcom.jslope.secure;

import junit.framework.TestCase;
import com.jslope.briskproject.networking.secure.ClientSecureProtocol;

import java.io.*;
import java.net.Socket;

/**
 * This class tests secure protocol, for it to work ServerMain should be launched
 * Date: 05.10.2005
 */
public class TestSecure extends TestCase {
    public void testSecureProtocol() throws Exception {
        Socket socket = new Socket("localhost", NetworkConfig.getPersistPort());
        ClientSecureProtocol protocol = new ClientSecureProtocol();
//        DebugOutputStream dos = new DebugOutputStream(socket.getOutputStream());
//        dos.setShow(true);
        protocol.setOutputStream(socket.getOutputStream());
//        DebugInputStream dis = new DebugInputStream(socket.getInputStream());
        protocol.setInputStream(socket.getInputStream());
        protocol.handshake();
        System.out.println("After handshake");
        OutputStream sout = protocol.getSecureOutputStream();
        for (int i = 0; i < 5; i++) {
            sout.write(i + 10);
        }
        sout.flush();
        System.out.println("After flush");
        InputStream sin = protocol.getSecureInputStream();
        for (int i = 0; i < 5; i++) {
            System.out.println("i=" + i + " read:" + sin.read());
        }
        DataOutputStream out = new DataOutputStream(protocol.getSecureOutputStream());
        System.out.println("After out ");
        out.writeUTF("Hallo World");
        out.flush();
        System.out.println("\nAftert write");
        InputStream ins = protocol.getSecureInputStream();
        DataInputStream in = new DataInputStream(ins);
        System.out.println("\nAfter in there are "+in.available());
        System.out.println("reply:" + in.readUTF());
//        System.out.println(" os:"+Utils.showBytes(dos.getBytes()));
//        System.out.println(" in:"+Utils.showBytes(dis.getBytes()));
        out.close();
        in.close();
        assertTrue(true);
        socket.close();
    }
}

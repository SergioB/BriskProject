package tcom.jslope.secure;

import com.jslope.briskproject.networking.secure.ServerSecureProtocol;
import com.jslope.briskproject.networking.secure.impl.DebugInputStream;
import com.jslope.briskproject.networking.secure.impl.DebugOutputStream;
import com.jslope.utils.Utils;

import java.net.Socket;
import java.io.*;

/**
 * Date: 03.10.2005
 */
public class ServerThread extends Thread {
    private Socket socket = null;

    public ServerThread(Socket socket) {
        super("ServerThread");
        this.socket = socket;
    }

    public void run() {
//        DebugInputStream dis = new DebugInputStream(null);
//        DebugOutputStream dos = new DebugOutputStream(null);
        try {
            System.out.println("Thread started");
            ServerSecureProtocol protocol = new ServerSecureProtocol();
//            dis.setParent(socket.getInputStream());
//            dis.setShow(true);
//            dos.setParent(socket.getOutputStream());
            protocol.setInputStream(socket.getInputStream());
            protocol.setOutputStream(socket.getOutputStream());
            protocol.handshake();
            System.out.println("after handshake");
            InputStream sin = protocol.getSecureInputStream();
            for (int i=0;i<5;i++) {
                System.out.println("i="+i+" read:"+sin.read());
            }
            OutputStream sout = protocol.getSecureOutputStream();
            for (int i = 0;i<5;i++) {
                sout.write(i);
            }
            sout.flush();
            DataInputStream in = new DataInputStream(protocol.getSecureInputStream());
            System.out.println("before reading object");
            String obj = in.readUTF();
            System.out.println("read " + obj);
            System.out.println("available: "+in.available());
            DataOutputStream out = new DataOutputStream(protocol.getSecureOutputStream());
            out.flush();
            out.writeUTF("Hallo, hallo!");
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
//        System.out.println(" os:"+Utils.showBytes(dos.getBytes()));
//        System.out.println(" in:"+Utils.showBytes(dis.getBytes()));
        System.out.println("Server thread finished");
    }
}

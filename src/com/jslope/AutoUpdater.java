package com.jslope;

import java.io.*;

/**
 * Date: Jun 19, 2006
 * Time: 3:53:44 PM
 */
public class AutoUpdater {
    private static final String BRISK_PROJECT_JAR_FILE = "../lib/BriskProject.jar";
    private static final String BRISK_PROJECT_UPDATE = "../lib/BriskProject.update";

    /**
     * loads the BriskProject.jar into a byte array and returns it to be sent to the client as update
     * @return  byte array containing BriskProject.jar
     * @throws IOException
     */
    public static byte[] getUpdateFile() throws IOException {
        File briskProjectJar = new File(BRISK_PROJECT_JAR_FILE);
        InputStream is = new FileInputStream(briskProjectJar);
        long lenght = briskProjectJar.length();
        if (lenght > Integer.MAX_VALUE) {
            throw new IOException("File is too large");
        }
        byte[] bytes = new byte[(int)lenght];
// Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+briskProjectJar.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
        //        return new byte[0];
    }

    public static void writeUpdateFile(byte[] fileBytes) throws IOException {
        File updateFile = new File(BRISK_PROJECT_UPDATE);
        if (updateFile.exists()) {
            updateFile.delete();
        }
        OutputStream os = new FileOutputStream(updateFile);
        os.write(fileBytes);
        os.close();
    }
}

package tcom.jslope.secure;

/**
 * Date: 05.10.2005
 */
public class NetworkConfig {
    public static final int PersistPort = 1129;

    public static int getPersistPort() {
        return PersistPort;
    }
    public static String getShortSimAlgorithm() {
        return "DESede";
//        return "AES";
    }
    public static String getSymetricAlgorithm() {
//        return "DESede";
//        return "AES/CBC/PKCS5Padding";
//        return "DESede/CBC/PKCS5Padding";
        return "DESede/CBC/ISO10126Padding";
    }
    public static String getAsymetricAlgorithm() {
        return "RSA";
    }
}

package com.jslope.utils;

/**
 * Date: 16.06.2005
 */
public class Utils {

    public static String formatTime(int interval) {
        int hours = interval / 3600;
        int seconds = interval % 3600;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        String secStr = "" + seconds, minStr = "" + minutes;

        if (seconds < 10) {
            secStr = "0" + secStr;
        }
        if (minutes < 10) {
            minStr = "0" + minStr;
        }

        return "" + hours + ":" + minStr + ":" + secStr;
    }

    /**
     * cutting a string to limited lenght (or enlarging it)
     *
     * @param string string to be formated
     * @param length length to which to be formmated (can not be less than 3)
     * @return formatted string
     */
    public static String formatString(String string, int length) {
        if (string.length() > length) {
            string = string.substring(0, length - 3) + "...";
        } else {
            int len = length - string.length();
            for (int i = 0; i < len; i++) {
                string += " ";
            }
        }
        return string;
    }

    public static String indentFormat(String indent, String text) {
        String newText = "";
        for (String str : text.split("\n")) {
            newText += indent + str + "\n";
        }
        return newText;
    }

    public static String showBytes(byte[] bytes) {
        String retValue = "";
        for (byte b : bytes) {
            retValue += " " + b;
        }
        return retValue;
    }
}

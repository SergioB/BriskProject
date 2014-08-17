package com.jslope.utils;

import com.jslope.UI.MainWindow;

import javax.swing.*;

/**
 * Date: 14.11.2005
 */
public class Dialogs {
    public static void unableToConnectMessage() {
        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                "Unable to connect to server, please verify your network connection",
                "Unable to connect to server",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void errorDuringDataExcange() {
        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                "Error during data exchange, please send exception to jslope, doing full data refresh",
                "Error during data exchange",
                JOptionPane.ERROR_MESSAGE);
    }
}

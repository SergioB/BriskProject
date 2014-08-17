package com.jslope.utils;

import javax.swing.*;

/**
 * Date: 24.12.2005
 */
public class UIUtils {
    public static JPanel linePanel(JComponent comp1, JComponent comp2) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(comp1);
        panel.add(comp2);
        return panel;
    }
}

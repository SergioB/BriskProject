package com.jslope.UI.components;

import javax.swing.*;
import java.awt.*;

/**
 * Date: 31.07.2005
 */
public class NumberEditor {
    private JSpinner spinnerEditor;

    public NumberEditor(int defaultValue, int minValue, int maxValue) {
        SpinnerModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, 1);
        spinnerEditor = new JSpinner(model);
//        spinnerEditor.addMouseListener(PopupMouseListener.getInstance()); it doesn't work, possible because of madal mode
    }

    public void showTo(Container c, String prefix, String sufix) {
        JPanel spinnerPanel;
        spinnerPanel = new JPanel();
        spinnerPanel.add(new JLabel(prefix));
        spinnerPanel.add(spinnerEditor);
        if (sufix != null) {
            spinnerPanel.add(new JLabel(sufix));
        }
        c.add(spinnerPanel);
    }

    public int getValue() {
        return ((Integer)spinnerEditor.getValue()).intValue();
    }
    public void setValue(int newValue) {
        spinnerEditor.setValue(newValue);
    }

    public void setToolTip(String toolTip) {
        spinnerEditor.setToolTipText(toolTip);
    }

    public void setEnabled(boolean enabled) {
        spinnerEditor.setEnabled(enabled);
    }
}

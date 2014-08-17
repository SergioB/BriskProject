package com.jslope.UI.components;

import javax.swing.*;
import java.awt.*;

/**
 * Date: 08.08.2005
 */
public class LabeledTextField {
    JTextField textField;
    protected String fieldTooltip;
    public LabeledTextField() {
        textField = new JTextField();
    }
    public LabeledTextField(String tooltip) {
        this();
        fieldTooltip = tooltip;
    }


    public void showTo(Container c, String label) {
        textField.setToolTipText(fieldTooltip);
        JLabel etaLabel = new JLabel(label);
        etaLabel.setToolTipText(fieldTooltip);
        c.add(etaLabel);
        c.add(textField);
    }

    public String getValue() {
        return textField.getText();
    }

    public void setValue(String newValue) {
            textField.setText(newValue);
    }
}

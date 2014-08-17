package com.jslope.UI.components;

import javax.swing.event.ChangeListener;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Date: 08.08.2005
 */
public class DateEditor {
    JSpinner dateField;
    protected String fieldTooltip;
    protected static final String DATE_FORMAT = "dd/MM/yyyy";
    final static SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);
    public DateEditor() {
        SpinnerModel model = new SpinnerDateModel();
        dateField = new JSpinner(model);
        dateField.setEditor(new JSpinner.DateEditor(dateField, DATE_FORMAT));
    }
    public DateEditor(String tooltip) {
        this();
        fieldTooltip = tooltip;
    }

    public void addChangeListener(ChangeListener listener) {
        dateField.addChangeListener(listener);
    }


    public void showTo(Container c, String label) {
        dateField.setToolTipText(fieldTooltip + DATE_FORMAT);
        JLabel etaLabel = new JLabel(label);
        etaLabel.setToolTipText(fieldTooltip);
        c.add(etaLabel);
        c.add(dateField);
    }

    public Date getValue() {
        Date date = (Date)dateField.getValue();
        return date;
    }

    public void setValue(Date newValue) {
            dateField.setValue(newValue);
    }
}

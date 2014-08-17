package com.jslope.UI.components;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * Date: 28.12.2005
 */
public class FixedSizePlainDocument extends PlainDocument {
    int maxSize;

    public FixedSizePlainDocument(int limit) {
        maxSize = limit;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if ((getLength() + str.length()) <= maxSize) {
            super.insertString(offs, str, a);
        } else {
            throw new BadLocationException("Insertion exceeds max size of document", offs);
        }
    }
}

package com.jslope.UI.components;

import com.jslope.UI.Menu;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * Date: 14.09.2005
 */
class PopupMouseListener extends MouseAdapter {
    static private PopupMouseListener ourInstance = new PopupMouseListener();
    static private JPopupMenu popup = Menu.getEeditPopupMenu();
    static public PopupMouseListener getInstance() {
        return ourInstance;
    }

    private void checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show((Component)e.getSource(), e.getX(), e.getY());
        }

    }
    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
    }


    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

}

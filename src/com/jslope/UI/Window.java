package com.jslope.UI;

import com.jslope.UI.interfaces.WindowInterface;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 1, 2004
 * Time: 2:38:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Window implements WindowInterface {
    JFrame window;
    public Window() {
        window = new JFrame();
    }
    public void setTitle(String title) {
        window.setTitle(title);
    }

    public void setVisible(boolean b) {
        window.setVisible(b);
    }

}

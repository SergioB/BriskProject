package com.jslope.toDoList;

import com.jslope.UI.MainWindow;
import com.jslope.toDoList.core.Options;

import java.util.concurrent.CountDownLatch;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 1, 2004
 * Time: 12:18:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToDoList {
    public static void main(String[] args) throws InterruptedException {
        Options.setOnServer(false);
        CountDownLatch finishFlag = new CountDownLatch(1);
        MainWindow window = MainWindow.getInstance(finishFlag);
        window.setVisible(true);
        finishFlag.await();
        System.exit(0);
    }
}

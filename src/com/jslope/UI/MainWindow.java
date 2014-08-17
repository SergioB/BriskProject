package com.jslope.UI;

import com.jslope.UI.interfaces.MainWindowInterface;
import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.TreeElement;
import com.jslope.toDoList.core.Task;
import com.jslope.persistence.sql.SqlUtil;
import com.jslope.persistence.DatabaseInUseException;
import com.jslope.briskproject.networking.PropagationList;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


public class MainWindow extends JFrame implements MainWindowInterface {
    static MainWindow instance = null;
    CountDownLatch finishFlag = null;

    private MainWindow(CountDownLatch flag) {
        this();
        finishFlag = flag;
    }

    private MainWindow() {
        super();
        try {
            this.init();
        } catch (DatabaseInUseException e) {
            databaseInUse();
        } catch (ExceptionInInitializerError e) {
            if (e.getException() instanceof DatabaseInUseException) {
                databaseInUse();
            } else {
                e.printStackTrace();
                throw new RuntimeException(e.getException());
            }
        }
    }
    public static void databaseInUse() {
        JOptionPane.showMessageDialog(null,
                "<html>Another instance is running, only one instance can be running at a time</html>",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private void init() {
        this.setJMenuBar(Menu.getMenuBar());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(new MainPanel());
        this.setIconImage(Buttons.applicationIcon.getImage());

        Options options = Options.getInstance();
        this.setLocation(options.getWindowLocation());
        this.setSize(options.getWindowSize());
        this.setTitle("BriskProject "+Options.getLoggedUser());

//        Runtime.getRuntime().addShutdownHook(new Thread() {
//        this.addWindowListener(new WindowListener() {
        this.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                System.out.println("Window is closing, saving data...");
                Options options = Options.getInstance();
                options.setWindowLocation(getLocation());
                options.setWindowSize(getSize());
                options.setSplitPane(TreePanel.getSplitPaneLocation());
                TreeElement selectedNode = TreePanel.getSelectedNode() ;
                options.setSelectedNode(selectedNode);
                options.save();
                PropagationList.getInstance().save();
                TreePanel.saveTree();
                Task.pauseActiveTask();
//                TodayTaskList.getInstance().save();
                SqlUtil.closeDatabase();
                instance = null;
                finishFlag.countDown();
            }
        });
    }

    public static MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }
    public static MainWindow getInstance(CountDownLatch flag) {
        instance = new MainWindow(flag);  //todo: to fix it
        return getInstance();
    }
}

/**
 * Date: 14.08.2005
 */
package com.jslope.UI;

import com.jslope.toDoList.core.Options;
import com.jslope.briskproject.networking.ClientProtocol;
import com.jslope.utils.Dialogs;
import com.jslope.utils.Log;
import com.jslope.persistence.LoadException;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.EOFException;
import java.net.SocketException;

public class Menu {
    public AbstractAction dataExchange;

    private static Menu ourInstance = new Menu();
    public static Menu getInstance() {
        return ourInstance;
    }

    private JMenuBar menuBar;

    private Menu() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem save = new JMenuItem("Save", KeyEvent.VK_S);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TaskTreeModel.getInstance().saveRootNode();
            }
        });
        fileMenu.add(save);
        menuBar.add(fileMenu);
        menuBar.add(getEditMenu());

        menuBar.add(ActionMenu.getMenu());

        JMenu reportMenu = new JMenu("Reports");
        reportMenu.setMnemonic('R');
        JMenuItem monthlyReport = new JMenuItem("Monthly Report", KeyEvent.VK_M);
        monthlyReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MonthlyReport.showHistory();
            }
        });
       reportMenu.add(monthlyReport);
        reportMenu.setMnemonic('R');
        JMenuItem dailyReport = new JMenuItem("Daily Report", KeyEvent.VK_D);
        dailyReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DailyReport.showHistory();
            }
        });
       reportMenu.add(dailyReport);

//        JMenuItem generateReport = new JMenuItem("Generate Text Report", KeyEvent.VK_G);
//        reportMenu.add(generateReport);

        menuBar.add(reportMenu);

        if (Options.isNetworkClient()) {
            menuBar.add(getConnectMenu());
        }

        JMenu options = new JMenu("Options");
        options.setMnemonic('O');
        JMenuItem optionsItem = new JMenuItem("Options");
        optionsItem.setMnemonic('O');
        options.add(optionsItem);

        optionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OptionsWindow window = OptionsWindow.getInstance();
                window.pack();
                window.setVisible(true);
            }
        });

        menuBar.add(options);

        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        JMenuItem about = new JMenuItem("About", KeyEvent.VK_A);
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        help.add(about);
        menuBar.add(help);
    }

    private static JMenu getEditMenu() {
        JMenuItem menuItem = null;
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T);
        editMenu.add(menuItem);
        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(menuItem);
        menuItem = new JMenuItem(new PasteAction());
        menuItem.setText("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(menuItem);
        return editMenu;
    }

    public static JPopupMenu getEeditPopupMenu() {
        System.out.println(" getInstance = " + getInstance());
        JMenu editMenu = getEditMenu();
        System.out.println(" editMenu = " + editMenu);
        return editMenu.getPopupMenu();
    }

    private JMenu getConnectMenu() {
        JMenu connect = new JMenu("Connect");
        connect.setMnemonic('C');
        JMenuItem menuItem = null;
        menuItem = new JMenuItem("Request full refresh from server");
        menuItem.setMnemonic(KeyEvent.VK_F);
//        menuItem.setTooltip("Renew all the data from the server (local changes might be lost)");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ClientProtocol.fullDataRefresh();
                } catch (IOException e1) {
                    Dialogs.unableToConnectMessage();
                    e1.printStackTrace();
                }
            }
        });
        connect.add(menuItem);
        dataExchange = new AbstractAction("Data exchange") {
            public void actionPerformed(ActionEvent e) {
                dataExchange();
            }
        };
        menuItem = new JMenuItem(dataExchange);
        connect.add(menuItem);
        if (Options.rootIsAdmin()) {
            menuItem = new JMenuItem("Shutodown server");
            menuItem.setMnemonic(KeyEvent.VK_S);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int answer = JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                            "Are you sure you would like to shutdown server?",
                            "Confirm server shutdown",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        try {
                            ClientProtocol.shutdownServer();    //it shoudl be 2 times because second time server might
                            ClientProtocol.shutdownServer();    // wait for one more connect
                            //todo: seccond connect can be replaced with an empty request.
                        } catch (IOException e1) {
                            if (e1 instanceof SocketException || e1 instanceof EOFException) {
                                System.out.println("shutdown in progress");
                            } else {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
            connect.add(menuItem);
        }
        return connect;
    }

    public static void dataExchange() {
        try {
            Options.saveRootNode(); //you can't use save root node from TreeModel, because there Task might be a root node.
            ClientProtocol.dataExchange();
            TreePanel.updateCurrentNode();
            TreePanel.reloadTree();
        } catch (IOException e1) {
            Dialogs.unableToConnectMessage();
            e1.printStackTrace();
        } catch (LoadException e1) {
            Log.error("Error: ", e1);
            Dialogs.errorDuringDataExcange();
            try {
                ClientProtocol.fullDataRefresh();
            } catch (IOException el) {
                Dialogs.unableToConnectMessage();
                e1.printStackTrace();
            }
        }
    }


    private void showAboutDialog() {
        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                "<html>BriskProject v0.89<br>" +
                "Project Management Solution<br>" +
                "Developed by <a href=\"http://jslope.com\">JSlope</a>.<br>" +
                "For latest version please visit our site <a href=\"http://jslope.com\">http://jslope.com</a></html>",
                "About",
                JOptionPane.PLAIN_MESSAGE);
    }

    public static JMenuBar getMenuBar() {
        return getInstance().menuBar;
    }
}

class PasteAction extends TextAction {
    /**
     * Create this object with the appropriate identifier.
     */
    public PasteAction() {
        super(DefaultEditorKit.pasteAction);
    }

    /**
     * The operation to perform when this action is triggered.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
            if (target.isShowing()) {
                target.paste();
            }
        }
    }
}

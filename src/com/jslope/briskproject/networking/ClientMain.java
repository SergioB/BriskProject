package com.jslope.briskproject.networking;

import static com.jslope.UI.MainWindow.databaseInUse;
import com.jslope.toDoList.core.Options;
import com.jslope.UI.MainWindow;
import com.jslope.UI.Menu;
import com.jslope.persistence.DatabaseInUseException;

import java.util.concurrent.CountDownLatch;


/**
 * Date: 09.10.2005
 */
public class ClientMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start");
        launchBriskProject();
        System.out.println("Finish");
        System.exit(0);
    }

    public static void launchBriskProject() throws InterruptedException {
        try {
            Options.setNetworkClient(true);
            Options.setOnServer(false);
            if (NetworkConfig.isConfigured()) {
                startApplication();
            } else {
                ClientProtocol.doInitialConfig();
                startApplication();
            }
        } catch (DatabaseInUseException e) {    // we copied catching database in use exception here because database is checked before MainWindow is instantiated in NetworkConfig
            databaseInUse();
        } catch (ExceptionInInitializerError e) {
            if (e.getException() instanceof DatabaseInUseException) {
                databaseInUse();
            } else {
                throw new RuntimeException(e.getException());
            }
        }
    }

    private static void startApplication() throws InterruptedException {
        System.out.println("Starting application...");
        CountDownLatch finishFlag = new CountDownLatch(1);
        System.out.println("before main window");
        MainWindow window = MainWindow.getInstance(finishFlag);
        System.out.println("after main window");
        window.setVisible(true);
        System.out.println("after set visible window");
        Menu.dataExchange();
        finishFlag.await();
        System.out.println("after await");
    }
}

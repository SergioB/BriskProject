package com.jslope.utils;

/**
 * Date: 16.07.2005
 * A class designed to measure time
 */
public class StopWatch {
    long startTime, endTime;
    boolean isRuning;
    public StopWatch() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        isRuning = true;
    }


    /**
     * Now it does the same as start but in future it may change
     */
    public void restart() {
        start();
    }

    public void stop() {
        copyTime();
        isRuning = false;
    }

    public long getMillis() {
        checkTime();
        return endTime - startTime;
    }

    public long getSeconds() {
        return getMillis() / 1000;
    }

    public long getMinutes() {
        return getSeconds() / 60;
    }

    private void checkTime() {
        if (isRuning) {
            copyTime();
        }
    }

    public void copyTime() {
        endTime = System.currentTimeMillis();
    }
}

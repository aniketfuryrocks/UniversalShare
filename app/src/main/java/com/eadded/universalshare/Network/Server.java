package com.eadded.universalshare.Network;

public interface Server {

    boolean hasPending();

    void showNoti();

    void showPopup();

    void closePupup();

    void closeNoti();

    void exit() throws Throwable;

    boolean isWorking();

}
package com.example.jayden.servicedemo;

/**
 * Created by jayden on 7/21/15.
 */
public interface ViewPresenter {
    void wifiConnecting();
    void wifiConnected();
    void wifiDisconnected();
    void wifiConnectFailed();
}

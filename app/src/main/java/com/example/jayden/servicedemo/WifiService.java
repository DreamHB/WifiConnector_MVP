package com.example.jayden.servicedemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

/**
 * notify presenter through local broadcast receiver
 */
public class WifiService extends Service {
    private static final String LOG_TAG = "WifiServiceBingo";

    public static final String ACTION_SCAN_RESULT_GOT = "action_scan_result_got";
    public static final String ACTION_WIFI_CONNECTED = "action_wifi_connected";
    public static final String ACTION_WIFI_DISCONNECTED = "action_wifi_disconnected";
    public static final int WIFI_CLIENT_BIND = 0;
    public static final int WIFI_CLIENT_CONNECT = 1;
    public static final int WIFI_CLIENT_DISCONNECT = 2;

    private LocalBinder binder;
    private WifiManager wifiManager;
    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEvent(intent);
        }
    };

    protected class LocalBinder extends Binder{
        public WifiService getService(){
            return WifiService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        binder = new LocalBinder();
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        registerWifiReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }

    private void registerWifiReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiReceiver, filter);
    }

    /**
     * handle broadcast event
     * @param intent
     */
    private void handleEvent(Intent intent){
        switch (intent.getAction()){
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                wifiStateChanged(intent);
                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                scanResultsAvail(intent);
                break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                networkStateChanged(intent);
                break;
            case WifiManager.NETWORK_IDS_CHANGED_ACTION:
                networkIdsChanged(intent);
                break;
            case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                supplicantStateChanged(intent);
                break;
            case WifiManager.RSSI_CHANGED_ACTION:
                rssiChanged(intent);
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                connectChanged(intent);
                break;
        }
    }

    private void wifiStateChanged(Intent intent){
        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
        int preState = intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, -1);
        Log.d(LOG_TAG, " wifi state = " + state + " previous state = " + preState);
    }

    /**
     * when scan results available, we broadcast it and
     * client can get from here
     * @param intent
     */
    private void scanResultsAvail(Intent intent){
        //broadcast scan results
        Intent intentNew = new Intent(WifiService.ACTION_SCAN_RESULT_GOT);
        boolean broadcastStatus = localBroadcastManager.sendBroadcast(intentNew);
        Log.d(LOG_TAG, " scan results available and we broadcast intent -> " + broadcastStatus);
    }

    /**
     * @return scan result list
     */
    public List<ScanResult> getScanResults(){
        if(wifiManager != null) {
            return wifiManager.getScanResults();
        }
        return null;
    }

    private void networkStateChanged(Intent intent){
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        NetworkInfo.State state = networkInfo.getState();
        NetworkInfo.DetailedState detailedState = networkInfo.getDetailedState();
        boolean isConnected = networkInfo.isConnected();
        Log.d(LOG_TAG, " networkStateChanged state = " + state + " detailedState = " + detailedState + " connected = " + isConnected);
        if(isConnected){
            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            String rssi = intent.getStringExtra(WifiManager.EXTRA_BSSID);
            Log.d(LOG_TAG, " networkStateChanged rssi = " + rssi);
            wifiConnected();
        }


    }

    private void networkIdsChanged(Intent intent){
        Log.d(LOG_TAG, " networkIdsChanged ");
    }

    /**
     * when state is DISCONNECTED, and password is wrong, then error code maybe 1
     * @param intent
     */
    private void supplicantStateChanged(Intent intent){
        SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        Log.d(LOG_TAG, " supplicant new state = " + state);
        int errorReason = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
        Log.d(LOG_TAG, " supplicant error = " + errorReason);
        if(state == SupplicantState.DISCONNECTED && errorReason == 1){
            //password error, notify ui that should reinput password
        }

    }

    private void rssiChanged(Intent intent){
        //Log.d(LOG_TAG, " rssiChanged new rssi = " + intent.getStringExtra(WifiManager.EXTRA_NEW_RSSI));
    }

    private void connectChanged(Intent intent){
        ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
        Log.d(LOG_TAG, " connect failed reason = " + reason);
    }


    //receiver wifi broadcast and send message to client
    private void wifiConnected(){
        boolean state = localBroadcastManager.sendBroadcast(new Intent(WifiService.ACTION_WIFI_CONNECTED));
        Log.d(LOG_TAG, " send connected broadcast = " + state);
    }

    /**
     * method for wifi operating
     */
    public boolean connect(){
        return false;
    }

    /**
     * disconnect
     * @return true is success or false for failed
     */
    public boolean disconnect(){
        return false;
    }
}

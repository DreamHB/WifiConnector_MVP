package com.example.jayden.servicedemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class WifiService extends Service {
    private static final String LOG_TAG = "WifiServiceBingo";

    public static final int WIFI_CLIENT_BIND = 0;
    public static final int WIFI_CLIENT_CONNECT = 1;
    public static final int WIFI_CLIENT_DISCONNECT = 2;

    private Messenger client = null;
    private Messenger service = new Messenger(new InComingHandler(this));
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEvent(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerWifiReceiver();
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

    private void scanResultsAvail(Intent intent){
        Log.d(LOG_TAG, " scan results available ");
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
        Log.d(LOG_TAG, " rssiChanged new rssi = " + intent.getStringExtra(WifiManager.EXTRA_NEW_RSSI));
    }

    private void connectChanged(Intent intent){
        ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
        Log.d(LOG_TAG, " connect failed reason = " + reason);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return service.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
    }

    static class InComingHandler extends Handler{
        private WifiService wifiService;

        public InComingHandler(WifiService wifiService){
            this.wifiService = wifiService;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WIFI_CLIENT_BIND:
                    wifiService.client = msg.replyTo;
                    Log.d(LOG_TAG, " client bind == " + wifiService.client);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //receiver wifi broadcast and send message to client
    private void wifiConnected(){
        Message msg = Message.obtain(null, WIFI_CLIENT_CONNECT);
        try {
            if(client != null) {
                client.send(msg);
            }
            Log.d(LOG_TAG, " wifiConnected wifi connected send msg");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

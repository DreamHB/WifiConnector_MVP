package com.example.jayden.servicedemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.ScanResult;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;


/**
 * Created by jayden on 7/21/15.
 * This class should be Singleton.
 *
 * But there are two ViewPresenter, and ViewPresenter
 * is in Activity, when Activity is not visible or stopped, Do we need to remove
 * this ViewPresenter in PresenterImpl ?
 * If we keep all ViewPresenter instances in PresenterImpl, all the resources of Activity
 * will not be released.
 * If we keep all ViewPresenter, can I refresh it anywhere ?
 */
public class PresenterImpl implements WifiPresenter{
    private static final String LOG_TAG = PresenterImpl.class.getSimpleName();

    private Context context;
    private LocalBroadcastManager localBroadcastManager;
    /**
     * array of viewPresenter
     */
    private SparseArray<ViewPresenter> viewPresenterSparseArray;
    private static PresenterImpl INSTANCE;
    private WifiService wifiService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            wifiService = ((WifiService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            wifiService = null;// let gc collect it
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, " get action = " + intent.getAction());
            wifiConnected();
        }
    };

    private PresenterImpl(Context context){
        this.context = context;
        viewPresenterSparseArray = new SparseArray<>(2);
        context.startService(new Intent(context, WifiService.class));
        //bind service
        context.bindService(new Intent(context, WifiService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter filter = new IntentFilter(WifiService.ACTION_WIFI_CONNECTED);
        filter.addAction(WifiService.ACTION_WIFI_DISCONNECTED);
        localBroadcastManager.registerReceiver(broadcastReceiver, filter);
    }

    public static PresenterImpl getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new PresenterImpl(context);
        }
        return INSTANCE;
    }

    /**
     * at most two
     * @param presenter
     */
    public void setViewPresenter(ViewPresenter presenter){
        if(viewPresenterSparseArray == null){
            throw new NullPointerException(" please call getInstance first !");
        }

        if(viewPresenterSparseArray.size() == 0){
            viewPresenterSparseArray.put(0, presenter);
        }else if(viewPresenterSparseArray.size() == 1){
            viewPresenterSparseArray.put(1, presenter);
        }else if(viewPresenterSparseArray.size() == 2){
            viewPresenterSparseArray.remove(0);
            viewPresenterSparseArray.put(0, presenter);
        }
    }

    public void unBindService(){
        context.unbindService(serviceConnection);
    }

    public List<ScanResult> getScanResults(){
        return wifiService.getScanResults();
    }

    @Override
    public void connect() {
        wifiService.connect();
    }

    @Override
    public void disconnect() {
        wifiService.disconnect();
    }

    private void wifiConnected(){
        for(int i = 0, len = viewPresenterSparseArray.size(); i < len; i++){
            viewPresenterSparseArray.get(i).wifiConnected();
        }
    }

    /**
     * test code for unregister receiver
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
}

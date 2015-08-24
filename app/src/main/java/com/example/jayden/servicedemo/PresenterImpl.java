package com.example.jayden.servicedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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
    /**
     * array of viewPresenter
     */
    private SparseArray<ViewPresenter> viewPresenterSparseArray;
    private static PresenterImpl INSTANCE;
    private Messenger messenger = new Messenger(new InComingHandler(this));
    private Messenger mService = null;
    private WifiService wifiService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            mService = new Messenger(service);
//            Message msg = Message.obtain(null, WifiService.WIFI_CLIENT_BIND);
//            msg.replyTo = messenger;
//            try {
//                mService.send(msg);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            wifiService = ((WifiService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            wifiService = null;// let gc collect it
        }
    };

    private PresenterImpl(Context context){
        this.context = context;
        viewPresenterSparseArray = new SparseArray<>(2);
        context.startService(new Intent(context, WifiService.class));
        //bind service
        context.bindService(new Intent(context, WifiService.class), serviceConnection, Context.BIND_AUTO_CREATE);
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
//        Message msg = Message.obtain(null, WifiService.WIFI_CLIENT_CONNECT, "ssid");
//        try {
//            mService.send(msg);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        wifiService.connect();
    }

    @Override
    public void disconnect() {
//        Message msg = Message.obtain(null, WifiService.WIFI_CLIENT_DISCONNECT, "ssid");
//        try {
//            mService.send(msg);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        wifiService.disconnect();
    }

    static class InComingHandler extends Handler{
        private PresenterImpl presenter;
        public InComingHandler(PresenterImpl presenter){
            this.presenter = presenter;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WifiService.WIFI_CLIENT_CONNECT:
                    Log.d(LOG_TAG, " wifi connected receive msg");
                    for(int i = 0, size = presenter.viewPresenterSparseArray.size(); i < size; i++){
                        ViewPresenter vp = presenter.viewPresenterSparseArray.get(i);
                        vp.wifiConnecting();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}

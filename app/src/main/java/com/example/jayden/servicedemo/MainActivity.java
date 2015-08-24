package com.example.jayden.servicedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements ViewPresenter{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private PresenterImpl presenter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, " onReceiver == " + intent.getAction());
            if(intent.getAction().equals(WifiService.ACTION_SCAN_RESULT_GOT)){
                //handle and show scan results
                presenter.getScanResults();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }


    private void init(){
        presenter = PresenterImpl.getInstance(getApplicationContext());
        presenter.setViewPresenter(this);
        //mock connect
        //presenter.connect();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter(WifiService.ACTION_SCAN_RESULT_GOT);
        lbm.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unBindService();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void wifiConnecting() {

    }

    @Override
    public void wifiConnected() {
        Log.d(LOG_TAG, " wifi connected ");
        Toast.makeText(this, " wifi connected !!!! ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void wifiDisconnected() {

    }

    @Override
    public void wifiConnectFailed() {

    }
}

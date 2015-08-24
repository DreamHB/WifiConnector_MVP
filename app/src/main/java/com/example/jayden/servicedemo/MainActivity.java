package com.example.jayden.servicedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements ViewPresenter{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }


    private void init(){
        PresenterImpl presenter = PresenterImpl.getInstance(this);
        presenter.setViewPresenter(this);
        //mock connect
        //presenter.connect();
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

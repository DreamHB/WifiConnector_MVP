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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

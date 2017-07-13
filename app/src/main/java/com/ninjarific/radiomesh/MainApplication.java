package com.ninjarific.radiomesh;

import android.app.Application;
import android.widget.Toast;

import com.ninjarific.radiomesh.database.RadioPointDatabase;

import io.realm.Realm;
import timber.log.Timber;


public class MainApplication extends Application implements IMessageHandler {

    private static WifiScanner wifiScanner;
    private static RadioPointDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Realm.init(this);
        database = new RadioPointDatabase();
        wifiScanner = new WifiScanner(this, database, message -> Toast.makeText(this, message, Toast.LENGTH_SHORT)
                .show());
    }

    public static WifiScanner getWifiScanner() {
        return wifiScanner;
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static RadioPointDatabase getDatabase() {
        return database;
    }
}

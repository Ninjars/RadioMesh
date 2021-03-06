package com.ninjarific.radiomesh;

import android.app.Application;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.ninjarific.radiomesh.database.realm.RealmDatabase;
import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.utils.ScanSchedulerUtil;

import io.realm.Realm;
import timber.log.Timber;


public class MainApplication extends Application implements IMessageHandler {
    public static final String PREF_BACKGROUND_SCAN = "background_scans";

    private static WifiScanner wifiScanner;
    private static RealmDatabase realmDatabase;
    private static DatabaseHelper roomDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Realm.init(this);
        realmDatabase = new RealmDatabase();
        roomDatabaseHelper = DatabaseHelper.init(this);

        wifiScanner = new WifiScanner(this, roomDatabaseHelper, message
                -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
        boolean backgroundScan = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(PREF_BACKGROUND_SCAN, false);
        if (backgroundScan) {
            boolean scanJobStarted = ScanSchedulerUtil.scheduleScanJob(this);
            Timber.d("scan job started? " + scanJobStarted);
        }
    }

    public static WifiScanner getWifiScanner() {
        return wifiScanner;
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static RealmDatabase getDatabase() {
        return realmDatabase;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return roomDatabaseHelper;
    }
}

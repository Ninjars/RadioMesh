package com.ninjarific.radiomesh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.ninjarific.radiomesh.database.IDatabase;

import java.util.List;

import timber.log.Timber;

public class WifiScanner {

    private final WifiManager wifiManager;
    private final BroadcastReceiver broadCastReceiver;
    private final IMessageHandler messageReceiver;
    private final IDatabase database;

    private ScanState scanState = ScanState.IDLE;

    private enum ScanState {
        IDLE,
        PENDING,
        SCANNING
    }

    public WifiScanner(Context context, IDatabase database, IMessageHandler messageReceiver) {
        this.messageReceiver = messageReceiver;
        this.database = database;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        broadCastReceiver = new WifiReceiver(this);
    }

    public void onScanResultsAvailable() {
        onScanResults(wifiManager.getScanResults());
    }

    public void register(Context context) {
        context.registerReceiver(broadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        context.registerReceiver(broadCastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    public void unregister(Context context) {
        context.unregisterReceiver(broadCastReceiver);
    }

    public void triggerScan(Context context) {
        Timber.i("startScan()");
        switch (scanState) {
            case SCANNING:
                sendMessage("Scan already running");
                break;

            case PENDING:
                sendMessage("Waiting for wifi adapter");
                break;

            case IDLE:
                scanState = ScanState.PENDING;
                enableWifi(context);
                break;
        }
    }

    private void enableWifi(Context context) {
        if (!wifiManager.isWifiEnabled()) {
            Timber.i("Enabling WiFi");
            wifiManager.setWifiEnabled(true);
            context.registerReceiver(broadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        } else {
            Timber.d("Wifi enabled already");
            onWifiReadyForScan();
        }
    }

    private void onWifiReadyForScan() {
        if (scanState == ScanState.PENDING) {
            Timber.d("\t start scan");
            sendMessage("Scanning");
            wifiManager.startScan();
            scanState = ScanState.SCANNING;
        }
    }

    public void onReceiveStateChange(int intExtra) {
        Timber.i("onReceiveStateChange() " + intExtra);
        switch (intExtra) {
            case WifiManager.WIFI_STATE_ENABLED:
                Timber.d("\t WIFI_STATE_ENABLED");
                onWifiReadyForScan();
                break;
        }

    }

    private void onScanResults(List<ScanResult> scanResults) {
        if (scanState == ScanState.SCANNING) {
            Timber.i("onScanResults() count " + scanResults.size());
            scanState = ScanState.IDLE;
            database.registerScanResults(scanResults);

        } else {
            Timber.i("ignoring system scan");
        }
    }

    private void sendMessage(String string) {
        messageReceiver.onMessage(string);
    }

}

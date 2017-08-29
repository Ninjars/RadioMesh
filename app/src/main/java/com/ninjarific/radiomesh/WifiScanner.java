package com.ninjarific.radiomesh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;

import com.ninjarific.radiomesh.database.IDatabase;

import java.util.List;

import timber.log.Timber;

public class WifiScanner {

    private final WifiManager wifiManager;
    private final BroadcastReceiver broadCastReceiver;
    private final IMessageHandler messageReceiver;
    private final IDatabase database;

    private ScanState scanState = ScanState.IDLE;
    @Nullable
    private Runnable scanFinishedCallback;

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
        try {
            context.unregisterReceiver(broadCastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void triggerScan(@Nullable Runnable onFinishedCallback) {
        this.scanFinishedCallback = onFinishedCallback;
        Timber.i("startScan()");
        if (!wifiManager.isWifiEnabled()) {
            Timber.w(">> aborting scan; wifi not enabled");
            return;
        }
        switch (scanState) {
            case SCANNING:
                sendMessage("Scan already running");
                break;

            case PENDING:
                sendMessage("Waiting for wifi adapter");
                break;

            case IDLE:
                if (ableToScan()) {
                    scanState = ScanState.PENDING;
                    startScan();
                }
                break;
        }
    }

    public void clearBackgroundScan() {
        scanFinishedCallback = null;
    }

    private boolean ableToScan() {
        if (!wifiManager.isWifiEnabled()) {
            return false;
        } else {
            Timber.d("Wifi enabled already");
            return true;
        }
    }

    private void startScan() {
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
                startScan();
                break;
        }
    }

    private void onScanResults(List<ScanResult> scanResults) {
        if (scanState == ScanState.SCANNING) {
            Timber.i("onScanResults() count " + scanResults.size());
            scanState = ScanState.IDLE;
            Runnable callback = scanFinishedCallback;
            scanFinishedCallback = null;
            database.registerScanResults(scanResults, callback);

        } else {
            Timber.i("ignoring system scan");
        }
    }

    private void sendMessage(String string) {
        messageReceiver.onMessage(string);
    }

}

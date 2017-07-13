package com.ninjarific.radiomesh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;


public class WifiReceiver extends BroadcastReceiver {
    private WifiScanner wifiScanner;

    public WifiReceiver(WifiScanner wifiScanner) {
        this.wifiScanner = wifiScanner;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            wifiScanner.onScanResultsAvailable();
        }
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            wifiScanner.onReceiveStateChange
                    (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
        }
    }
}

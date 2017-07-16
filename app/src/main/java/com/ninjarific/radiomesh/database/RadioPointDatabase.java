package com.ninjarific.radiomesh.database;

import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class RadioPointDatabase implements IDatabase {
    @Override
    public void registerScanResults(List<ScanResult> scanResults, @Nullable Runnable scanFinishedCallback) {
        Timber.d("registerScanResults: " + scanResults.size());
        Realm realmInstance = Realm.getDefaultInstance();
        realmInstance.executeTransactionAsync(realm -> {
            Timber.d("> registerScanResults: async transaction executing");
            List<RadioPoint> radioPoints = new ArrayList<>(scanResults.size());
            for (ScanResult result : scanResults) {
                RadioPoint scannedRadioPoint = realm.where(RadioPoint.class)
                        .equalTo(RadioPoint.KEY_BSSID, result.BSSID)
                        .findFirst();
                if (scannedRadioPoint == null) {
                    Timber.v(">>>> new point found " + result.BSSID);
                    scannedRadioPoint = new RadioPoint(result.BSSID, result.SSID);
                    scannedRadioPoint = realm.copyToRealm(scannedRadioPoint);
                }
                radioPoints.add(scannedRadioPoint);
            }
            for (RadioPoint point : radioPoints) {
                radioPoints.forEach(point::addConnection);
            }
            Timber.d("> registerScanResults: async transaction completed");
            if (scanFinishedCallback != null) {
                Timber.d("> job completed callback");
                scanFinishedCallback.run();
            }
        });
    }

    public RealmResults<RadioPoint> getRadioPoints() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(RadioPoint.class).findAllSorted(RadioPoint.KEY_BSSID);
    }
}

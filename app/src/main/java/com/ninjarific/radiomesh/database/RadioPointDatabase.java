package com.ninjarific.radiomesh.database;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class RadioPointDatabase implements IDatabase {
    @Override
    public void registerScanResults(List<ScanResult> scanResults) {
        Timber.d("registerScanResults: " + scanResults.size());
        Realm realm = Realm.getDefaultInstance();
        List<RadioPoint> radioPoints = new ArrayList<>(scanResults.size());
        realm.beginTransaction();
        for (ScanResult result : scanResults) {
            RadioPoint scannedRadioPoint = realm.where(RadioPoint.class).equalTo(RadioPoint.KEY_BSSID, result.BSSID).findFirst();
            if (scannedRadioPoint == null) {
                Timber.d(">>>> new point found " + result.BSSID);
                scannedRadioPoint = new RadioPoint(result.BSSID, result.SSID);
                scannedRadioPoint = realm.copyToRealm(scannedRadioPoint);
            }
            radioPoints.add(scannedRadioPoint);
        }
        for (RadioPoint point : radioPoints) {
            radioPoints.forEach(point::addConnection);
        }
        realm.commitTransaction();
        Timber.d(">> committed scan results");
    }

    public RealmResults<RadioPoint> getRadioPoints() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(RadioPoint.class).findAllSorted(RadioPoint.KEY_BSSID);
    }
}

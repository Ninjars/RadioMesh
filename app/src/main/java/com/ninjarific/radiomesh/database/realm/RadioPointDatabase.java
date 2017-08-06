package com.ninjarific.radiomesh.database.realm;

import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class RadioPointDatabase implements IDatabase {

    private final List<RadioPointsUpdateListener> listeners = new ArrayList<>();
    private List<List<RadioPoint>> groupedRadioPoints;

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

    public interface RadioPointsUpdateListener {
        void onDataSetUpdate(List<List<RadioPoint>> newDataset);
    }

    public void registerGroupedRadioPointsListener(RadioPointsUpdateListener listener) {
        listeners.add(listener);
        if (groupedRadioPoints == null && listeners.size() == 1) {
            updateGroupedValues.execute();
        }
        if (groupedRadioPoints != null) {
            listener.onDataSetUpdate(groupedRadioPoints);
        }
    }

    public void unregisterGroupedRadioPointsListener(RadioPointsUpdateListener listener) {
        listeners.remove(listener);
    }

    private AsyncTask<Void, Void, List<List<RadioPoint>>> updateGroupedValues
            = new AsyncTask<Void, Void, List<List<RadioPoint>>>() {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Timber.i("Start updating grouped data");
        }

        @Override
        protected List<List<RadioPoint>> doInBackground(Void... voids) {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<RadioPoint> query = realm.where(RadioPoint.class).findAll();
            return groupData(realm.copyFromRealm(query));
        }

        @Override
        protected void onPostExecute(List<List<RadioPoint>> lists) {
            Timber.i("Finished updating grouped data");
            onGroupedDataUpdated(lists);
        }
    };

    private void onGroupedDataUpdated(List<List<RadioPoint>> lists) {
        groupedRadioPoints = lists;
        for (RadioPointsUpdateListener listener : listeners) {
            listener.onDataSetUpdate(groupedRadioPoints);
        }
    }

    private List<List<RadioPoint>> groupData(List<RadioPoint> dataset) {
        List<RadioPoint> parsedPoints = new ArrayList<>(dataset.size());
        List<List<RadioPoint>> datapoints = new ArrayList<>();

        for (RadioPoint point : dataset) {
            if (parsedPoints.contains(point)) continue;
            HashSet<RadioPoint> currentSet = new HashSet<>(dataset.size()/4);
            addConnectedNodesRecursive(point, currentSet);
            parsedPoints.addAll(currentSet);
            datapoints.add(new ArrayList<>(currentSet));
        }
        Collections.sort(datapoints, (lhs, rhs) -> Integer.compare(lhs.size(), rhs.size()));
        return datapoints;
    }

    private void addConnectedNodesRecursive(RadioPoint point, HashSet<RadioPoint> currentSet) {
        currentSet.add(point);
        for (RadioPoint connected : point.getConnectedPoints()) {
            if (!currentSet.contains(connected)) {
                addConnectedNodesRecursive(connected, currentSet);
            }
        }
    }
}

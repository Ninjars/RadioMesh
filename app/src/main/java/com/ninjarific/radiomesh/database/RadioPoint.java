package com.ninjarific.radiomesh.database;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RadioPoint extends RealmObject {
    @PrimaryKey
    private String bssid;
    private String ssid;
    private RealmList<RadioPoint> connectedPoints;

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public RealmList<RadioPoint> getConnectedPoints() {
        return connectedPoints;
    }

    public void addConnection(RadioPoint point) {
        if (doesNotHaveConnection(point)) {
            connectedPoints.add(point);
        }
    }

    private boolean doesNotHaveConnection(RadioPoint point) {
        return connectedPoints.where().equalTo("bssid", point.bssid).findFirst() == null;
    }
}

package com.ninjarific.radiomesh.database;

import java.util.Objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RadioPoint extends RealmObject {
    public static final String KEY_BSSID = "bssid";

    @PrimaryKey
    private String bssid;
    private String ssid;
    private RealmList<RadioPoint> connectedPoints;

    public RadioPoint() {}

    public RadioPoint(String bssid, String ssid) {
        this.bssid = bssid;
        this.ssid = ssid;
    }

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
        if (!Objects.equals(point.bssid, bssid) && doesNotHaveConnection(point)) {
            connectedPoints.add(point);
        }
    }

    private boolean doesNotHaveConnection(RadioPoint point) {
        return connectedPoints.where().equalTo("bssid", point.bssid).findFirst() == null;
    }
}

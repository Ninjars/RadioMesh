package com.ninjarific.radiomesh.database;

import android.net.wifi.ScanResult;

import java.util.List;

public interface IDatabase {
    void registerScanResults(List<ScanResult> scanResults);
}

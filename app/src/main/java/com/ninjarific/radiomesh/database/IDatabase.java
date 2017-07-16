package com.ninjarific.radiomesh.database;

import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import java.util.List;

public interface IDatabase {
    void registerScanResults(List<ScanResult> scanResults, @Nullable Runnable scanFinishedCallback);
}

package com.ninjarific.radiomesh;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;

import timber.log.Timber;


public class ScanService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Timber.d("onStartJob");
        Context context = getApplicationContext();
        WifiScanner wifiScanner = MainApplication.getWifiScanner();
        wifiScanner.register(context);
        wifiScanner.triggerScan(() -> {
            Timber.d("job finished callback");
            wifiScanner.unregister(context);
            jobFinished (jobParameters, false);
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Timber.d("onStopJob");
        MainApplication.getWifiScanner().unregister(getApplicationContext());
        return true;
    }
}

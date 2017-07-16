package com.ninjarific.radiomesh.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.ninjarific.radiomesh.ScanService;

import timber.log.Timber;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class ScanSchedulerUtil {

    private static final long SCAN_INTERVAL = 1000 * 60 * 30;

    public static boolean scheduleScanJob(Context context) {
        Timber.d("scheduleScanJob()");
        ComponentName serviceComponent = new ComponentName(context, ScanService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPeriodic(SCAN_INTERVAL);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        int taskCode = jobScheduler.schedule(builder.build());
        return taskCode > 0; // -1 indicates a failed attempt to start
    }
}

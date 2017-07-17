package com.ninjarific.radiomesh.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.ninjarific.radiomesh.ScanService;

import timber.log.Timber;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class ScanSchedulerUtil {
    private static final int JOB_ID = 0;
    private static final long SCAN_INTERVAL = 1000 * 60 * 30;

    public static boolean scheduleScanJob(Context context) {
        Timber.d("scheduleScanJob()");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (!jobScheduler.getAllPendingJobs().isEmpty()) {
            Timber.d("already got a job scheduled"); // assumes this app doesn't have other jobs scheduled...
            return true;
        }
        ComponentName serviceComponent = new ComponentName(context, ScanService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
        builder.setPeriodic(SCAN_INTERVAL);
        int taskCode = jobScheduler.schedule(builder.build());
        return taskCode > 0; // -1 indicates a failed attempt to start
    }

    public static void cancelScanJob(Context context) {
        Timber.d("cancelScanJob()");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }
}

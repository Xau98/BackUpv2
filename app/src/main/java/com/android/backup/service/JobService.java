package com.android.backup.service;


import android.app.job.JobParameters;
import android.util.Log;

public class JobService extends android.app.job.JobService {
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        doBackground(params);
        return true;
    }

    private void doBackground(final JobParameters jobParameters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    if (jobCancelled)
                        return;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("Tiennvh", "run: Oke");
                jobFinished(jobParameters, false);
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("Tiennvh", "onStopJob: ");

        jobCancelled = true;
        return false;
    }
}
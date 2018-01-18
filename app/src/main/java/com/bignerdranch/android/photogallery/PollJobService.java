package com.bignerdranch.android.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by Garry on 13/11/2017.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService
{
    private static final String TAG = PollJobService.class.getSimpleName();

    public static final int JOB_ID = 9999;

    private PollTask mCurrentTask;
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("TESTPOLL", "Job Starting");
        mCurrentTask = new PollTask();
        mCurrentTask.execute(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if(mCurrentTask != null) {
            Log.d("TESTPOLL", "Stopping Job");
            mCurrentTask.cancel(true);
        }
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters, Void , Void> {
        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            JobParameters params = jobParameters[0];

            if(!isNetworkAvailableAndConnected()) {
                return null;
            }

            String query = QueryPreferences.getStoredQuery(PollJobService.this);
            String lastResultId = QueryPreferences.getLastResultId(PollJobService.this);
            List<GalleryItem> items;

            if(query == null) {
                items = new FlikrFetcher().fetchRecentPhotos();
            } else {
                items = new FlikrFetcher().searchPhotos(query);
            }

            if(items.size() == 0) {
                return null;
            }

            String resultId = items.get(0).getId();
            if(resultId.equals(lastResultId)) {
                Log.i(TAG, "Got an old result: " + resultId);
            } else {
                Log.i(TAG, "Got a new result: " + resultId);

                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i, 0);

                Notification notification = new NotificationCompat.Builder(PollJobService.this)
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(PollJobService.this);
                notificationManager.notify(0, notification);
            }

            QueryPreferences.setLastResultId(PollJobService.this, resultId);

            jobFinished(params, false);
            Log.d("TESTPOLL", "Job Finished");
            return null;
        }
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}

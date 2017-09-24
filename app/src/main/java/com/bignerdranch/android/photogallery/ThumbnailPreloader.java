package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Garry on 24/09/2017.
 */

public class ThumbnailPreloader extends HandlerThread{
    private static final String TAG = ThumbnailPreloader.class.getSimpleName();
    private static final int MESSAGE_PRELOAD = 1;

    private Handler requestHandler;
    private ConcurrentLinkedQueue<String> requestQueue = new ConcurrentLinkedQueue();

    public ThumbnailPreloader(String name) {
        super(name);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        requestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_PRELOAD) {
                    Log.i(TAG, "Preload request for URL: " + msg.obj);

                    if(BitmapCache.getInstance().getBitmap((String)msg.obj) == null) {
                        handleRequest((String) msg.obj);
                    } else {
                        Log.i(TAG, "Already preloaded this bitmap");
                    }
                }
            }
        };
    }

    public void clearQueue() {
        requestQueue.clear();
    }

    private void handleRequest(String url) {
        try {
            byte[] bitmapBytes = new FlikrFetcher().getUrlBytes(url);

            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0 , bitmapBytes.length);

            if(BitmapCache.getInstance().getBitmap(url) == null) {
                BitmapCache.getInstance().addBitmap(url, bitmap);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void preLoadThumbnails(String url) {
        if(requestQueue.contains(url)) {
            return;
        } else {
            requestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
        }

    }
}

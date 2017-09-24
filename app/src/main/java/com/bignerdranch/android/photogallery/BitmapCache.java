package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Garry on 24/09/2017.
 */

public class BitmapCache {

    private static BitmapCache instance;
    private static final int CACHE_SIZE = 50;

    private LruCache<String, Bitmap> mBitmapCache;

    public static BitmapCache getInstance() {
        if(instance != null) {
            return instance;
        } else {
            instance = new BitmapCache();
            return instance;
        }
    }

    private BitmapCache() {
        mBitmapCache = new LruCache<>(CACHE_SIZE);
    }

    public void addBitmap(String key, Bitmap bitmap) {
        mBitmapCache.put(key, bitmap);
    }

    public Bitmap getBitmap(String key) {
        return mBitmapCache.get(key);
    }

}

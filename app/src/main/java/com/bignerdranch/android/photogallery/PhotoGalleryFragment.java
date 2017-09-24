package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Garry on 20/09/2017.
 */

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();
    private static final int PRELOAD_PREVIOUS_LIMIT = 10;
    private static final int PRELOAD_NEXT_LIMIT = 10;

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private ThumbnailPreloader mThumbnailPreloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadedListener(new ThumbnailDownloader.ThumbnailDownloadedListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();

        mThumbnailPreloader = new ThumbnailPreloader("Preload");
        mThumbnailPreloader.start();
        mThumbnailPreloader.getLooper();

        Log.d(TAG, "Background Threads started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
        mThumbnailPreloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        mThumbnailPreloader.quit();
        Log.i(TAG, "Background Thread destroyed");
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            return new FlikrFetcher().fetchitems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            setupAdapter();
        }
    }

    private class PreloadItemsTask extends AsyncTask<List<GalleryItem>, Void, Void> {

        @Override
        protected Void doInBackground(List<GalleryItem>... lists) {
            List<GalleryItem> preLoaditems = lists[0];
            for(GalleryItem item : preLoaditems) {
                mThumbnailPreloader.preLoadThumbnails(item.getUrl());
            }
            return null;
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            preLoadBitMaps(position);
            if(BitmapCache.getInstance().getBitmap(item.getUrl()) != null) {
                Log.i(TAG, "Bitmap in cache");
                Bitmap bitmap = BitmapCache.getInstance().getBitmap(item.getUrl());
                Drawable thumbnail = new BitmapDrawable(getResources(), bitmap);
                holder.bindDrawable(thumbnail);
            } else {
                holder.bindDrawable(placeholder);
                mThumbnailDownloader.queueThumbnail(holder, item.getUrl());
            }

        }

        private void preLoadBitMaps(int currentItem) {
            int preloadTo;
            int preLoadFrom;
            if(currentItem + PRELOAD_NEXT_LIMIT >= mGalleryItems.size()-1) {
                preloadTo = mGalleryItems.size()-1;
            } else {
                preloadTo = currentItem + PRELOAD_NEXT_LIMIT;
            }

            if( currentItem - PRELOAD_PREVIOUS_LIMIT <= 0) {
                preLoadFrom = 0;
            } else {
                preLoadFrom = currentItem - PRELOAD_PREVIOUS_LIMIT;
            }

            Log.i(TAG, "Preloading bitmaps from : " + preLoadFrom + " to: " + preloadTo);

            List<GalleryItem> items = mGalleryItems.subList(preLoadFrom, preloadTo);
            new PreloadItemsTask().execute(items);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}

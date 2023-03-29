package com.adgrowth.adserver.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.adgrowth.adserver.views.AdImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {
    private Map<String, Bitmap> cache;
    private Context context;
    private AdImage.Listener listener = null;

    public ImageLoader(Context context) {
        this.context = context;
        cache = new HashMap<>();
    }

    public void loadImage(String url, ImageView imageView) {
        this.listener = listener != null ? listener : new AdImage.Listener();
        if (cache.containsKey(url)) {
            imageView.setImageBitmap(cache.get(url));

            assert this.listener != null;
            this.listener.onLoad();
            return;
        }

        Bitmap cachedBitmap = loadImageFromCache(url);

        if (cachedBitmap != null)
            imageView.setImageBitmap(cachedBitmap);
        else new LoadImageTask(url, imageView).execute();

        assert listener != null;
        listener.onLoad();

    }

    public void setListener(@Nullable AdImage.Listener imageListener) {
        listener = imageListener;
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private String url;
        private ImageView imageView;

        public LoadImageTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream inputStream = conn.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                cache.put(url, bitmap);
                saveImageToCache(url, bitmap);

                inputStream.close();

                return bitmap;
            } catch (IOException e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                assert listener != null;
                listener.onLoad();
            }
        }
    }

    private void saveImageToCache(String url, Bitmap bitmap) {
        Uri uri = Uri.parse(url);
        String filename = String.format("%s?%s", Uri.parse(url).getLastPathSegment(), uri.getQuery());
        File cacheDir = context.getCacheDir();
        File cacheFile = new File(cacheDir, filename);

        try {
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadImageFromCache(String url) {
        Uri uri = Uri.parse(url);
        String filename = String.format("%s?%s", Uri.parse(url).getLastPathSegment(), uri.getQuery());

        File cacheDir = context.getCacheDir();
        File cacheFile = new File(cacheDir, filename);

        if (cacheFile.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(cacheFile);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
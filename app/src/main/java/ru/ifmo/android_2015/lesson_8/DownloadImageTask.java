package ru.ifmo.android_2015.lesson_8;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.stetho.urlconnection.StethoURLConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dmitry.trunin on 30.11.2015.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    public interface ImageResultListener {
        void onImageReceived(Bitmap bitmap);
        void onImageError();
    }

    private ImageResultListener listener;
    private AsyncTaskState state = AsyncTaskState.IDLE;
    private Bitmap bitmap;

    public DownloadImageTask(ImageResultListener listener) {
        this.listener = listener;
    }

    public void attach(ImageResultListener listener) {
        this.listener = listener;
    }

    public @Nullable Bitmap getBitmap() {
        return bitmap;
    }

    public AsyncTaskState getState() {
        return state;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        state = AsyncTaskState.EXECUTING;

        final String url = params != null && params.length > 0 ? params[0] : null;
        if (url == null) {
            return null;
        }

        // Для отладки. См. http://facebook.github.io/stetho/
        StethoURLConnectionManager stethoManager = new StethoURLConnectionManager("OkApi");

        HttpURLConnection conn = null;
        InputStream in = null;

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            stethoManager.preConnect(conn, null);

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            stethoManager.postConnect();

            in = conn.getInputStream();
            in = stethoManager.interpretResponseStream(in);

            return BitmapFactory.decodeStream(in);

        } catch (IOException e) {
            Log.e(TAG, "Failed to download image: " + e, e);
            stethoManager.httpExchangeFailed(e);
            return null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            return;
        }
        state = bitmap == null ? AsyncTaskState.ERROR : AsyncTaskState.DONE;
        this.bitmap = bitmap;

        if (listener != null) {
            if (bitmap != null) {
                listener.onImageReceived(bitmap);
            } else {
                listener.onImageError();
            }
        }
    }

    private static final String TAG = "DownloadImageTask";
}

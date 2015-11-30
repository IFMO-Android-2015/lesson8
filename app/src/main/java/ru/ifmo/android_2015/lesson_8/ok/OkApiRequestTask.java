package ru.ifmo.android_2015.lesson_8.ok;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.facebook.stetho.urlconnection.StethoURLConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.ifmo.android_2015.lesson_8.ApiException;
import ru.ifmo.android_2015.lesson_8.AsyncTaskState;
import ru.ifmo.android_2015.lesson_8.ok.api.OkApiRequest;
import ru.ifmo.android_2015.lesson_8.ok.api.Session;
import ru.ifmo.android_2015.lesson_8.ok.api.SessionStore;

/**
 * Базовый класс для AsyncTask, выполняющего запросы к API
 */
public class OkApiRequestTask<TResult> extends AsyncTask<OkApiRequest, Void, TResult> {

    public interface ResultListener<TResult> {
        void onResultReceived(TResult result);
        void onError(ApiException apiException);
    }

    private Context context;
    private ResultListener<TResult> resultListener;
    private OkApiResultParser<TResult> parser;
    private TResult result;
    private ApiException apiException;
    private AsyncTaskState state = AsyncTaskState.IDLE;

    public OkApiRequestTask(Context context,
                            ResultListener<TResult> listener,
                            OkApiResultParser<TResult> parser) {
        this.context = context.getApplicationContext();
        this.resultListener = listener;
        this.parser = parser;
    }

    public void attach(Context context, ResultListener<TResult> listener) {
        this.context = context.getApplicationContext();
        this.resultListener = listener;
    }

    public AsyncTaskState getState() {
        return state;
    }

    public @Nullable TResult getResult() {
        return result;
    }

    public @Nullable ApiException getApiException() {
        return apiException;
    }

    private void setResult(TResult result) {
        this.result = result;
        state = AsyncTaskState.DONE;
    }

    private void setApiException(ApiException apiException) {
        this.apiException = apiException;
        state = AsyncTaskState.ERROR;
    }

    @Override
    protected TResult doInBackground(OkApiRequest... params) {
        state = AsyncTaskState.EXECUTING;

        final OkApiRequest request = params.length == 0 ? null : params[0];
        if (request == null) {
            setApiException(new ApiException("null request"));
            return null;
        }
        Log.d(TAG, "Start performing Ok API request: " + request);

        // Для отладки. См. http://facebook.github.io/stetho/
        StethoURLConnectionManager stethoManager = new StethoURLConnectionManager("OkApi");

        final Session session = SessionStore.getInstance().getSession(context);
        final String url;

        try {
            url = session.getGetUrl(request);
        } catch (ApiException e) {
            Log.e(TAG, "Failed to obtain API request URL: " + e, e);
            setApiException(e);
            return null;
        }

        HttpURLConnection conn = null;
        InputStream in = null;

        try {

            conn = (HttpURLConnection) new URL(url).openConnection();
            stethoManager.preConnect(conn, null);

            // Проверяем HTTP код ответа. Ожидаем только ответ 200 (ОК).
            // Остальные коды считаем ошибкой.
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            stethoManager.postConnect();

            in = conn.getInputStream();
            in = stethoManager.interpretResponseStream(in);

            final JsonReader reader = new JsonReader(new InputStreamReader(in));
            TResult result = parser.parse(reader);
            setResult(result);
            return result;

        } catch (IOException e) {
            Log.e(TAG, "Failed to receive response: " + e, e);
            stethoManager.httpExchangeFailed(e);
            setApiException(new ApiException("Failed to execute API request: " + e, e));
            return null;

        } catch (ApiException e) {
            Log.e(TAG, "Failed to execute response: " + e, e);
            setApiException(e);
            return null;

        } finally {
            // Закрываем все потоки и соедиениние
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close HTTP input stream: " + e, e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
            request.recycle();
        }
    }

    @Override
    protected void onPostExecute(TResult result) {
        if (isCancelled()) {

        }
        if (state != AsyncTaskState.DONE && state != AsyncTaskState.ERROR) {
            // doInBackground должен обязательно завершиться DONE или ERROR
            // Если этого не произошло, значит случилось что-то непредвиденное
            setApiException(new ApiException("Internal error"));
        }
        if (resultListener != null) {
            if (state == AsyncTaskState.DONE) {
                resultListener.onResultReceived(result);
            } else if (state == AsyncTaskState.ERROR) {
                resultListener.onError(apiException);
            }
        }
    }

    protected final String TAG = getClass().getSimpleName();
}

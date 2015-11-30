package ru.ifmo.android_2015.lesson_8.vk;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ifmo.android_2015.lesson_8.AsyncTaskState;
import ru.ifmo.android_2015.lesson_8.ok.OkApiRequestTask;

/**
 * Базовый класс для AsyncTask, выполняющего запросы к API
 */
public class VkApiRequestTask<TResult> extends AsyncTask<VKRequest, Void, TResult> {

    public interface ResultListener<TResult> {
        void onResultReceived(TResult result);
        void onError(VKError error);
    }

    private Context context;
    private VkApiRequestTask.ResultListener<TResult> resultListener;
    private final VkApiResultParser<TResult> parser;
    private TResult result;
    private VKError error;
    private AsyncTaskState state = AsyncTaskState.IDLE;

    public VkApiRequestTask(Context context,
                            VkApiRequestTask.ResultListener<TResult> listener,
                            VkApiResultParser<TResult> parser) {
        this.context = context.getApplicationContext();
        this.resultListener = listener;
        this.parser = parser;
    }

    public void attach(Context context, VkApiRequestTask.ResultListener<TResult> listener) {
        this.context = context.getApplicationContext();
        this.resultListener = listener;
    }

    public AsyncTaskState getState() {
        return state;
    }

    public @Nullable TResult getResult() {
        return result;
    }

    public @Nullable VKError getError() {
        return error;
    }

    private void setResponse(TResult response) {
        this.result = response;
        state = AsyncTaskState.DONE;
    }

    private void setError(VKError error) {
        this.error = error;
        state = AsyncTaskState.ERROR;
    }

    @Override
    protected TResult doInBackground(final VKRequest... params) {
        state = AsyncTaskState.EXECUTING;

        final VKRequest request = params.length == 0 ? null : params[0];
        if (request == null) {
            setError(new VKError(-1));
            return null;
        }
        Log.d(TAG, "Start performing Vk API request: " + request);

        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    setResponse(parser.parse(response.json));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse response: " + e);
                    setError(new VKError(-3));
                }
            }

            @Override
            public void onError(VKError error) {
                setError(error);
            }
        });
        return result;
    }

    @Override
    protected void onPostExecute(TResult result) {
        if (isCancelled()) {
            return;
        }
        if (state != AsyncTaskState.DONE && state != AsyncTaskState.ERROR) {
            // doInBackground должен обязательно завершиться DONE или ERROR
            // Если этого не произошло, значит случилось что-то непредвиденное
            setError(new VKError(-2));
        }
        if (resultListener != null) {
            if (state == AsyncTaskState.DONE) {
                resultListener.onResultReceived(result);
            } else if (state == AsyncTaskState.ERROR) {
                resultListener.onError(error);
            }
        }
    }

    protected final String TAG = getClass().getSimpleName();
}

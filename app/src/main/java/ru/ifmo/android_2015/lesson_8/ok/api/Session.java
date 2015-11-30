package ru.ifmo.android_2015.lesson_8.ok.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import ru.ifmo.android_2015.lesson_8.ApiUtils;

import static ru.ifmo.android_2015.lesson_8.Constants.LOG;

/**
 * Created by dtrunin on 05.04.2015.
 */
public class Session {

    private String accessToken;
    private String sessionSecretKey;
    private byte[] sessionSecretKeyBytes;

    public synchronized boolean hasKeys() {
        return !TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(sessionSecretKey);
    }

    synchronized void setKeys(@Nullable String accessToken, @Nullable String sessionSecretKey) {
        if (accessToken == null) {
            sessionSecretKey = null;
        } else if (sessionSecretKey == null) {
            accessToken = null;
        }
        if (LOG) Log.d(LOG_TAG, "setKeys: accessToken=" + accessToken
                + " sessionSecretKey=" + sessionSecretKey);
        this.accessToken = accessToken;
        this.sessionSecretKey = sessionSecretKey;
        if (sessionSecretKey == null) {
            sessionSecretKeyBytes = null;
        } else {
            sessionSecretKeyBytes = sessionSecretKey.getBytes();
        }
    }

    public synchronized String getGetUrl(@NonNull OkApiRequest request) throws OkApiNoSessionException {
        if (!hasKeys()) {
            throw new OkApiNoSessionException();
        }
        final String signature = request.calculateSignature(sessionSecretKeyBytes);
        final StringBuilder sb = ApiUtils.obtainStringBuilder(200);
        sb.append(OkApi.DEFAULT_SCHEMA).append("://").append(OkApi.BASE_HOST_NAME).append("/fb.do?");
        if (request.appendQueryParams(sb)) {
            sb.append('&');
        }
        sb.append("access_token=").append(accessToken).append('&').append("sig=").append(signature);
        final String url = sb.toString();
        ApiUtils.releaseStringBuilder(sb);
        if (LOG) Log.d(LOG_TAG, "getGetUrl: " + url);
        return url;
    }

    @Override
    public synchronized String toString() {
        return "Session[accessToken=" + accessToken + " sessionSecretKey=" + sessionSecretKey + "]";
    }

    private static final String LOG_TAG = "Session";
}

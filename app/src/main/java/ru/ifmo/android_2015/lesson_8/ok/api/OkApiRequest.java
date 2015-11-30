package ru.ifmo.android_2015.lesson_8.ok.api;

import android.support.v4.util.Pools;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;

import ru.ifmo.android_2015.lesson_8.ApiUtils;

/**
 * Created by dtrunin on 11.04.2015.
 */
public class OkApiRequest {

    String method;
    private OkApiParam[] params = new OkApiParam[20];
    private int paramCount;
    boolean useSessionKey;

    private OkApiRequest() {}

    void setMethod(String method) {
        if (this.method != null) {
            throw new IllegalStateException("Method already set: " + method);
        }
        this.method = method;
        addParam("method", method);
        addParam("format", "json");
    }

    void addParam(String name, String value) {
        if ("format".equals(name)) {
            if (!"json".equals(value)) {
                throw new IllegalArgumentException("Unsupported format: " + value);
            }
            // format=json parameter is added in setMethod()
            return;
        }
        final OkApiParam param = OkApiParam.obtain();
        param.name = name;
        param.value = value;
        if (paramCount == params.length) {
            final OkApiParam[] p = new OkApiParam[paramCount * 2];
            System.arraycopy(param, 0, p, 0, paramCount);
            params = p;
        }
        params[paramCount++] = param;
    }

    void setUseSessionKey(boolean useSessionKey) {
        this.useSessionKey = useSessionKey;
    }

    /**
     * Returns true if parameters were appended, or false if nothing was appended.
     */
    boolean appendQueryParams(StringBuilder out) {
        for (int i = 0; i < paramCount; i++) {
            final OkApiParam param = params[i];
            if (i > 0) {
                out.append('&');
            }
            // TODO: do we have to URL encode the value?
            out.append(param.name).append('=').append(ApiUtils.safeUrlEncode(param.value));
        }
        return paramCount > 0;
    }

    String calculateSignature(byte[] sessionSecretKey) {
        Arrays.sort(params, 0, paramCount, signatureOrderComparator);
        final MessageDigest md5 = ApiUtils.obtainMd5();
        for (int i = 0; i < paramCount; i++) {
            final OkApiParam param = params[i];
            // Note: UTF-8 is default charset on Android
            md5.update(param.name.getBytes());
            md5.update(EQUAL_SIGN_BYTES);
            md5.update(param.value.getBytes());
        }
        final byte[] sig = md5.digest(sessionSecretKey);
        ApiUtils.releaseMd5(md5);

        final StringBuilder sb = ApiUtils.obtainStringBuilder(sig.length * 2);
        for (int i = 0; i < sig.length; i++) {
            final int d = sig[i];
            char low = (char) ('0' + (d & 0x0f));
            if (low > '9') {
                low += 'a' - '9' - 1;
            }
            char high = (char) ('0' + ((d >> 4) & 0x0f));
            if (high > '9') {
                high += 'a' - '9' - 1;
            }
            sb.append(high).append(low);
        }
        final String sigStr = sb.toString();
        ApiUtils.releaseStringBuilder(sb);
        return sigStr;
    }

    // ApiRequest object pool implementation

    protected void reset() {
        method = null;
        for (int i = 0; i < paramCount; i++) {
            params[i].recycle();
        }
        Arrays.fill(params, 0, paramCount, null);
        paramCount = 0;
        useSessionKey = false;
    }

    private static final Pools.Pool<OkApiRequest> pool = new Pools.SynchronizedPool<>(20);

    public static OkApiRequest obtain() {
        OkApiRequest obj = pool.acquire();
        if (obj == null) {
            obj = new OkApiRequest();
        }
        return obj;
    }

    public void recycle() {
        reset();
        pool.release(this);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ApiRequest[").append(method).append(" ?");
        for (int i = 0; i < paramCount; i++) {
            final OkApiParam param = params[i];
            sb.append(" ").append(param.name).append('=').append(param.value);
            if (i + 1 < paramCount) {
                sb.append(" &");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static final Comparator<OkApiParam> signatureOrderComparator =
            new Comparator<OkApiParam>() {
        @Override
        public int compare(OkApiParam lhs, OkApiParam rhs) {
            return lhs.name.compareTo(rhs.name);
        }
    };


    private static final byte[] EQUAL_SIGN_BYTES = "=".getBytes();

    private static final String LOG_TAG = "ApiRequest";
}

package ru.ifmo.android_2015.lesson_8;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

import static ru.ifmo.android_2015.lesson_8.Constants.LOG;


/**
 * Вспомогательные методы для работы с API.
 * - эффективное переиспользование объекта MessageDigest
 * - эффективное переиспользование объекта StringBuilder
 */
public final class ApiUtils {

    private static AtomicReference<MessageDigest> md5Ref = new AtomicReference<>();
    private static AtomicReference<StringBuilder> sbRef = new AtomicReference<>();

    public static MessageDigest obtainMd5() {
        MessageDigest md5 = md5Ref.getAndSet(null);
        if (md5 == null) {
            try {
                md5 = MessageDigest.getInstance("MD5");
                if (LOG) Log.d(LOG_TAG, "Created instance of MD5 message digest");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 message digest algorithm not found", e);
            }
        }
        return md5;
    }

    public static void releaseMd5(MessageDigest md5) {
        md5.reset();
        md5Ref.set(md5);
    }

    public static StringBuilder obtainStringBuilder(int length) {
        StringBuilder sb = sbRef.get();
        if (sb == null) {
            sb = new StringBuilder(length);
            if (LOG) Log.d(LOG_TAG, "Created instance of StringBuilder");
        }
        return sb;
    }

    public static void releaseStringBuilder(StringBuilder sb) {
        sb.setLength(0);
        sbRef.set(sb);
    }

    public static String safeUrlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    private ApiUtils() {}

    private static final String LOG_TAG = "ApiUtils";
}

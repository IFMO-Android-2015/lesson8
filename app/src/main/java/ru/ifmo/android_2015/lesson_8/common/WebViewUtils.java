package ru.ifmo.android_2015.lesson_8.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Created by dmitry.trunin on 01.12.2015.
 */
public final class WebViewUtils {

    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clearCookiesLollipop();
        } else {
            clearCookiesPreLollopop(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void clearCookiesLollipop() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
    }

    private static void clearCookiesPreLollopop(final Context context) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                CookieSyncManager.createInstance(context).sync();
            }
        });
    }


    private WebViewUtils() {}
}

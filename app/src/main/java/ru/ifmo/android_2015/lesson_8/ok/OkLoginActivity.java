package ru.ifmo.android_2015.lesson_8.ok;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.ifmo.android_2015.lesson_8.R;
import ru.ifmo.android_2015.lesson_8.ok.api.OkConstants;
import ru.ifmo.android_2015.lesson_8.ok.api.OkOAuth;
import ru.ifmo.android_2015.lesson_8.ok.api.SessionStore;

import static ru.ifmo.android_2015.lesson_8.Constants.LOG;

public class OkLoginActivity extends Activity {

    public static final String EXTRA_RESULT_ERROR = "error";

    private final OkOAuth oauth = OkOAuth.getInstance(OkConstants.APP_ID);

    private WebView webView;
    private String redirectUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOG) Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_login);
        webView = (WebView) findViewById(R.id.web_view_login);
        webView.getSettings().setSavePassword(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (LOG) Log.d(LOG_TAG, "shouldOverrideUrlLoading: " + url);
                if (url.startsWith(redirectUrl)) {
                    parseResultAndFinish(url);
                    // Не загружать страницу
                    return true;
                }
                // Загрузить страницу как обычно
                return false;
            }
        });
        redirectUrl = oauth.getRedirectUri();
        if (savedInstanceState == null) {
            startOauth();
        }
    }

    private void startOauth() {
        final String oauthUrl = oauth.createOauthUrl(
                new String[] {
                        OkOAuth.SCOPE_VALUABLE_ACCESS,
                        OkOAuth.SCOPE_VIDEO_CONTENT,
                        OkOAuth.SCOPE_LIKE
                },
                OkOAuth.LAYOUT_MOBILE_NO_ACTION_BAR, null);
        if (LOG) Log.d(LOG_TAG, "startOauth: oauthUrl=" + oauthUrl);
        webView.loadUrl(oauthUrl);
    }

    @Override
    public void onBackPressed() {
        if (LOG) Log.d(LOG_TAG, "onBackPressed");
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    private void parseResultAndFinish(String url) {
        final Uri uri = Uri.parse(url);
        final String fragment = uri.getFragment();

        if (fragment == null) {
            finish();
            return;
        }

        String error = null;
        String accessToken = null;
        String sessionSecretKey = null;

        int off = 0;
        int equalSignPosition;
        int length = fragment.length();

        while (off < length && (equalSignPosition = fragment.indexOf('=', off)) != -1) {
            final String key = fragment.substring(off, equalSignPosition);
            final int andSignPosition = fragment.indexOf('&', equalSignPosition + 1);
            final int valueEnd = andSignPosition > equalSignPosition ? andSignPosition : length;
            final String value = fragment.substring(equalSignPosition + 1, valueEnd);
            switch (key) {
                case "access_token":        accessToken = value; break;
                case "session_secret_key":  sessionSecretKey = value; break;
                case "error":               error = value; break;
            }
            off = valueEnd + 1;
        }

        if (LOG) Log.d(LOG_TAG, "parseResultAndFinish: accessToken=" + accessToken
                + " sessionSecretKey=" + sessionSecretKey + " error=" + error);

        final Intent data = new Intent();
        if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(sessionSecretKey)) {
            SessionStore.getInstance().updateKeys(this, accessToken, sessionSecretKey);
            setResult(Activity.RESULT_OK);
        }
        if (!TextUtils.isEmpty(error)) {
            data.putExtra(EXTRA_RESULT_ERROR, error);
        }
        finish();
    }

    private static final String LOG_TAG = "LoginActivity";
}

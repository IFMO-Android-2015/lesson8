package ru.ifmo.android_2015.lesson_8.ok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import ru.ifmo.android_2015.lesson_8.ok.api.Session;
import ru.ifmo.android_2015.lesson_8.ok.api.SessionStore;

import static ru.ifmo.android_2015.lesson_8.Constants.LOG;

/**
 * Created by dtrunin on 06.04.2015.
 */
public abstract class OkBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (LOG) Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        initContentView();

        final Session session = SessionStore.getInstance().getSession(this);
        if (LOG) Log.d(LOG_TAG, "onCreate: " + session);
        if (session.hasKeys()) {
            onLoggedIn(session, false/*allowStateLoss*/);
        } else if (savedInstanceState == null) {
            startLogin();
        }
    }

    /**
     * Вызывается из onCreate(), должен вызвать setContentView и инициализировать view
     */
    protected abstract void initContentView();

    protected void startLogin() {
        if (LOG) Log.d(LOG_TAG, "startLogin");
        final Intent intent = new Intent(this, OkLoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (LOG) Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode
                + " resultCode=" + resultCode + " data=" + data);

        if (requestCode == REQUEST_CODE_LOGIN) {

            if (resultCode == Activity.RESULT_OK) {
                final Session session = SessionStore.getInstance().getSession(this);
                if (LOG) Log.d(LOG_TAG, "onActivityResult: " + session);
                onLoggedIn(session, true /*allowStateLoss*/);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (LOG) Log.w(LOG_TAG, "onActivityResult: login cancelled, finishing.");
                finish();

            } else {
                final String error = data == null
                                   ? null
                                   : data.getStringExtra(OkLoginActivity.EXTRA_RESULT_ERROR);
                if (LOG) Log.e(LOG_TAG, "onActivityResult: error=" + error);
            }
        }
    }

    protected abstract void onLoggedIn(final Session session, boolean allowStateLoss);

    private final int REQUEST_CODE_LOGIN = 1;

    private final String LOG_TAG = getClass().getSimpleName();
}

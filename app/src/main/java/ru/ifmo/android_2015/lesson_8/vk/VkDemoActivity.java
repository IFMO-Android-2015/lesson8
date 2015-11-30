package ru.ifmo.android_2015.lesson_8.vk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKScopes;

import ru.ifmo.android_2015.lesson_8.AsyncTaskState;
import ru.ifmo.android_2015.lesson_8.DownloadImageTask;
import ru.ifmo.android_2015.lesson_8.R;
import ru.ifmo.android_2015.lesson_8.common.CurrentUser;
import ru.ifmo.android_2015.lesson_8.common.WebViewUtils;
import ru.ifmo.android_2015.lesson_8.ok.api.SessionStore;

/**
 * Created by dmitry.trunin on 01.12.2015.
 */
public class VkDemoActivity extends Activity {

    static class NonConfigurationState {
        final VkApiRequestTask<CurrentUser> currentUserTask;
        final DownloadImageTask downloadImageTask;

        public NonConfigurationState(VkApiRequestTask<CurrentUser> currentUserTask,
                                     DownloadImageTask downloadImageTask) {
            this.currentUserTask = currentUserTask;
            this.downloadImageTask = downloadImageTask;
        }
    }

    private TextView nameView;
    private ImageView imageView;
    private ProgressBar progressView;
    private Button logoutButton;

    VkApiRequestTask<CurrentUser> currentUserTask;
    DownloadImageTask downloadImageTask;

    private static final String KEY_TOKEN = "vk_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentView();

        VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(this, KEY_TOKEN);
        if (token != null) {
            Log.d(TAG, "onCreate: using saved token");
            onLoggedIn(token);
        } else if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: token is missing, performing login...");
            VKSdk.login(this, VKScopes.STATS);
        }
    }

    protected void initContentView() {
        setContentView(R.layout.activity_ok_demo);
        nameView = (TextView) findViewById(R.id.user_name);
        imageView = (ImageView) findViewById(R.id.user_photo);
        progressView = (ProgressBar) findViewById(R.id.progress);
        logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(logoutClickListener);
    }

    void resetView() {
        logoutButton.setEnabled(false);
        progressView.setVisibility(View.VISIBLE);
        nameView.setText(null);
        imageView.setImageBitmap(null);
    }


    protected void onLoggedIn(VKAccessToken token) {
        Log.d(TAG, "onLoggedIn: " + token);
        startCurrentUserRequest();
    }

    protected void onLoginFailed(VKError error) {
        Log.w(TAG, "onLoginFailed: " + error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken token) {
                token.saveTokenToSharedPreferences(VkDemoActivity.this, KEY_TOKEN);
                onLoggedIn(token);
            }

            @Override
            public void onError(VKError error) {
                onLoginFailed(error);
            }
        });
    }

    void onCurrentUser(CurrentUser currentUser) {
        Log.d(TAG, "onCurrentUser: " + currentUser);
        nameView.setText(currentUser.name);
        if (!TextUtils.isEmpty(currentUser.picUrl)) {
            startDownloadImage(currentUser.picUrl);
        } else {
            progressView.setVisibility(View.GONE);
        }
    }

    void onCurrentUserError(VKError error) {
        Log.w(TAG, "onCurrentUserError: " + error);
        String errorMessage = error == null ? getString(R.string.error) : error.toString();
        nameView.setText(errorMessage);
    }

    void onUserImage(Bitmap bitmap) {
        Log.d(TAG, "onUserImage");
        imageView.setImageBitmap(bitmap);
        progressView.setVisibility(View.GONE);
        logoutButton.setEnabled(true);
    }

    void onUserImageError() {
        Log.w(TAG, "onUserImageError");
        imageView.setImageDrawable(null);
        progressView.setVisibility(View.GONE);
        logoutButton.setEnabled(true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object onRetainNonConfigurationInstance() {
        return new NonConfigurationState(currentUserTask, downloadImageTask);
    }

    @SuppressWarnings("unchecked,deprecation")
    void startCurrentUserRequest() {
        if (currentUserTask == null) {
            NonConfigurationState state = (NonConfigurationState) getLastNonConfigurationInstance();
            currentUserTask = state == null ? null : state.currentUserTask;
        }
        if (currentUserTask == null) {
            final VKRequest request = new VKRequest("users.get", VKParameters.from(
                    "fields", "photo_max,first_name,last_name"
            ));
            currentUserTask = new VkApiRequestTask<>(this, currentUserListener,
                    new VkCurrentUserParser());
            currentUserTask.execute(request);
        } else {
            currentUserTask.attach(this, currentUserListener);
            if (currentUserTask.getState() == AsyncTaskState.DONE) {
                final CurrentUser currentUser = currentUserTask.getResult();
                if (currentUser != null) {
                    onCurrentUser(currentUser);
                }
            } else if (currentUserTask.getState() == AsyncTaskState.ERROR) {
                onCurrentUserError(currentUserTask.getError());
            }
        }
    }

    void startDownloadImage(String imageUrl) {
        if (downloadImageTask == null) {
            NonConfigurationState state = (NonConfigurationState) getLastNonConfigurationInstance();
            downloadImageTask = state == null ? null : state.downloadImageTask;
        }
        if (downloadImageTask == null) {
            downloadImageTask = new DownloadImageTask(imageResultListener);
            downloadImageTask.execute(imageUrl);
        } else {
            downloadImageTask.attach(imageResultListener);
            if (downloadImageTask.getState() == AsyncTaskState.DONE) {
                onUserImage(downloadImageTask.getBitmap());
            } else if (downloadImageTask.getState() == AsyncTaskState.ERROR) {
                onUserImageError();
            }
        }
    }

    private final VkApiRequestTask.ResultListener<CurrentUser> currentUserListener =
            new VkApiRequestTask.ResultListener<CurrentUser>() {
        @Override
        public void onResultReceived(CurrentUser currentUser) {
            onCurrentUser(currentUser);
        }

        @Override
        public void onError(VKError error) {
            onCurrentUserError(error);
        }
    };

    private DownloadImageTask.ImageResultListener imageResultListener =
            new DownloadImageTask.ImageResultListener() {
        @Override
        public void onImageReceived(Bitmap bitmap) {
            onUserImage(bitmap);
        }

        @Override
        public void onImageError() {
            onUserImageError();
        }
    };

    private View.OnClickListener logoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Context context = VkDemoActivity.this;

            // Выполняем логаут в Vk SDK
            VKSdk.logout();

            // Удаляем сохраненный токен
            VKAccessToken.removeTokenAtKey(context, KEY_TOKEN);

            // Очищаем вьюшки
            resetView();

            // Отменяем загрузки для текущего пользователя
            if (currentUserTask != null) {
                currentUserTask.cancel(false);
                currentUserTask = null;
            }
            if (downloadImageTask != null) {
                downloadImageTask.cancel(false);
                downloadImageTask = null;
            }

            // Выкидываем пользователя на логин
            VKSdk.login(VkDemoActivity.this);
        }
    };


    protected final String TAG = getClass().getSimpleName();
}

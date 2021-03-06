package ru.ifmo.android_2015.lesson_8.ok;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.ifmo.android_2015.lesson_8.ApiException;
import ru.ifmo.android_2015.lesson_8.DownloadImageTask;
import ru.ifmo.android_2015.lesson_8.R;
import ru.ifmo.android_2015.lesson_8.AsyncTaskState;
import ru.ifmo.android_2015.lesson_8.common.WebViewUtils;
import ru.ifmo.android_2015.lesson_8.ok.api.OkApi;
import ru.ifmo.android_2015.lesson_8.ok.api.OkConstants;
import ru.ifmo.android_2015.lesson_8.ok.api.Session;
import ru.ifmo.android_2015.lesson_8.ok.api.SessionStore;
import ru.ifmo.android_2015.lesson_8.common.CurrentUser;
import ru.ifmo.android_2015.lesson_8.ok.api.user.OkCurrentUserParser;

/**
 * Created by dmitry.trunin on 30.11.2015.
 */
public class OkDemoActivity extends OkBaseActivity {

    static class NonConfigurationState {
        final OkApiRequestTask<CurrentUser> currentUserTask;
        final DownloadImageTask downloadImageTask;

        public NonConfigurationState(OkApiRequestTask<CurrentUser> currentUserTask,
                                     DownloadImageTask downloadImageTask) {
            this.currentUserTask = currentUserTask;
            this.downloadImageTask = downloadImageTask;
        }
    }

    private TextView nameView;
    private ImageView imageView;
    private ProgressBar progressView;
    private Button logoutButton;


    OkApiRequestTask<CurrentUser> currentUserTask;
    DownloadImageTask downloadImageTask;

    @Override
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

    @Override
    protected void onLoggedIn(Session session, boolean allowStateLoss) {
        startCurrentUserRequest();
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

    void onCurrentUserError(ApiException error) {
        Log.w(TAG, "onCurrentUserError: " + error);
        String errorMessage = error == null ? getString(R.string.error) : error.getMessage();
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
            currentUserTask = new OkApiRequestTask<>(this, currentUserListner,
                    new OkCurrentUserParser());
            currentUserTask.execute(
                    OkApi.User.CurrentUser.createRequest(OkConstants.APP_PUBLIC_KEY));
        } else {
            currentUserTask.attach(this, currentUserListner);
            if (currentUserTask.getState() == AsyncTaskState.DONE) {
                final CurrentUser currentUser = currentUserTask.getResult();
                if (currentUser != null) {
                    onCurrentUser(currentUser);
                }
            } else if (currentUserTask.getState() == AsyncTaskState.ERROR) {
                onCurrentUserError(currentUserTask.getApiException());
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

    private OkApiRequestTask.ResultListener<CurrentUser> currentUserListner =
            new OkApiRequestTask.ResultListener<CurrentUser>() {
        @Override
        public void onResultReceived(CurrentUser okCurrentUser) {
            onCurrentUser(okCurrentUser);
        }

        @Override
        public void onError(ApiException apiException) {
            onCurrentUserError(apiException);
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
            // Удаляем сесиию из нашего хранилища
            SessionStore.getInstance().updateKeys(OkDemoActivity.this, null, null);
            // Очищаем вьюшки
            resetView();

            // Чистим куки в WebView, чтобы OAuth не подумал, что мы уже залогинились
            WebViewUtils.clearCookies(OkDemoActivity.this);

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
            startLogin();

        }
    };

    private static final String TAG = "OkDemoActivity";
}

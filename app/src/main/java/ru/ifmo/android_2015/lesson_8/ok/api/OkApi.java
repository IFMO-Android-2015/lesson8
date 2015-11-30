package ru.ifmo.android_2015.lesson_8.ok.api;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dtrunin on 05.04.2015.
 */
public final class OkApi {

    public static final String BASE_HOST_NAME = "api.ok.ru";
    public static final String DEFAULT_SCHEMA = "http";

    public final static class Stream {

        public static final String PATTERN_CONTENT = "CONTENT";
        public static final String PATTERN_PHOTO = "PHOTO";

        public static final String DEFAULT_FIELDS =
                  "feed.*" + ","
                + "feed.title_tokens" + ","
                + "photo.id" + ","
                        + "photo.standard_width" + ","
                        + "photo.standard_height" + ","
                        + "photo.created_ms" + ","
                        + "photo.pic240min" + ","
                        + "photo.like_summary" + ","
                + "group_photo.id" + ","
                        + "group_photo.standard_width" + ","
                        + "group_photo.standard_height" + ","
                        + "group_photo.created_ms" + ","
                        + "group_photo.pic240min" + ","
                        + "group_photo.like_summary" + ","
                + "media_topic.*" + ","
                + "user.name" + "," + "user.pic128x128" + ","
                + "group.*" + ","
                + "video.id" + "," + "video.title" +"," + "video.thumbnail_url" + ","
                + "video.big_thumbnail_url" + "," + "video.like_summary" + ","
                + "video.discussion_summary" + "," + "video.duration" + ","
                + "video.url_mp4" + "," + "video.created_ms" + ","
                + "music_track.*";


        public final static class Get {

            @IntDef( value = { DIRECTION_FORWARD, DIRECTION_BACKWARD } )
            public @interface Direction {}

            public static final int DIRECTION_FORWARD = 1;
            public static final int DIRECTION_BACKWARD = 2;

            public static OkApiRequest createRequest(@NonNull String appPublicKey,
                                                   @NonNull String patterns,
                                                   @Nullable String anchor,
                                                   @Direction int direction,
                                                   int count) {
                final OkApiRequest request = OkApiRequest.obtain();
                request.setMethod("stream.get");
                request.addParam("application_key", appPublicKey);
                request.addParam("patterns", patterns);
                if (anchor != null) {
                    request.addParam("anchor", anchor);
                }
                request.addParam("count", Integer.toString(count));
                request.addParam("fields", DEFAULT_FIELDS);
                request.setUseSessionKey(true);
                return request;
            }
        }
    }

    public static final class Video {

        public static final class Get {

            public static OkApiRequest createRequest(@NonNull String appPublicKey,
                                                   @NonNull String videoId) {
                final OkApiRequest request = OkApiRequest.obtain();
                request.setMethod("video.get");
                request.addParam("application_key", appPublicKey);
                request.addParam("vids", videoId);
                request.addParam("fields", "video.*");
                request.setUseSessionKey(true);
                return request;
            }

            private Get() {}
        }

        private Video() {}
    }

    public static final class Like {

        public static OkApiRequest getSummary(@NonNull String appPublicKey,
                                            @NonNull String likeId) {
            final OkApiRequest request = OkApiRequest.obtain();
            request.setMethod("like.getSummary");
            request.addParam("application_key", appPublicKey);
            request.addParam("like_id", likeId);
            request.setUseSessionKey(true);
            return request;
        }

        public static OkApiRequest like(@NonNull String appPublicKey,
                                      @NonNull String likeId) {
            final OkApiRequest request = OkApiRequest.obtain();
            request.setMethod("like.like");
            request.addParam("application_key", appPublicKey);
            request.addParam("like_id", likeId);
            request.setUseSessionKey(true);
            return request;
        }

        public static OkApiRequest unlike(@NonNull String appPublicKey,
                                        @NonNull String likeId) {
            final OkApiRequest request = OkApiRequest.obtain();
            request.setMethod("like.unlike");
            request.addParam("application_key", appPublicKey);
            request.addParam("like_id", likeId);
            request.setUseSessionKey(true);
            return request;
        }

        private Like() {}
    }

    public static final class User {

        // Поле с именем пользователя
        public static final String NAME = "name";

        // Поле с урлом большой картинки пользователя
        public static final String PIC_FULL = "pic_full";

        private static final String PREFIX = "user.";

        public static final class CurrentUser {

            private static final String DEFAULT_FIELDS = PREFIX + NAME + "," + PREFIX + PIC_FULL;

            public static OkApiRequest createRequest(@NonNull String appPublicKey) {
                OkApiRequest request = OkApiRequest.obtain();
                request.setMethod("users.getCurrentUser");
                request.addParam("application_key", appPublicKey);
                request.addParam("fields", DEFAULT_FIELDS);
                request.setUseSessionKey(true);
                return request;
            }
        }
    }



    public static String getGetUrl(@NonNull Context context, @NonNull OkApiRequest request)
            throws OkApiNoSessionException {
        if (request.useSessionKey) {
            final Session session =  SessionStore.getInstance().getSession(context);
            return session.getGetUrl(request);
        }
        throw new UnsupportedOperationException("No-session requests not supported");
    }

    private static AtomicInteger requestCount = new AtomicInteger(0);

    private OkApi() {}

    private static final String LOG_TAG = "Api";
}

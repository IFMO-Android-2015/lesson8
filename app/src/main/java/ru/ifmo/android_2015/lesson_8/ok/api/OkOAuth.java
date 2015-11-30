package ru.ifmo.android_2015.lesson_8.ok.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dtrunin on 05.04.2015.
 */
public final class OkOAuth {

    public static final String SCOPE_VALUABLE_ACCESS = "VALUABLE_ACCESS";
    public static final String SCOPE_MESSAGING = "MESSAGING";
    public static final String SCOPE_VIDEO_CONTENT = "VIDEO_CONTENT";
    public static final String SCOPE_LIKE = "LIKE";

    public static final String LAYOUT_MOBILE = "m";
    public static final String LAYOUT_MOBILE_NO_ACTION_BAR = "a";

    private static final String BASE_OAUTH_URL = "http://www.ok.ru/oauth/authorize";

    private static OkOAuth instance;

    public static OkOAuth getInstance(String appId) {
        if (instance == null) {
            instance = new OkOAuth(appId);
        }
        return instance;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String createOauthUrl(String[] scopes, String layout, String state) {
        final StringBuilder sb = new StringBuilder(200);
        sb.append(BASE_OAUTH_URL);
        sb.append("?client_id=").append(appId);
        if (scopes != null) {
            sb.append("&scope=");
            for (int i = 0; i < scopes.length; i++) {
                if (i > 0) {
                    sb.append(';');
                }
                sb.append(scopes[i]);
            }
        }
        sb.append("&response_type=token");
        sb.append("&redirect_uri=").append(safeUrlEncode(redirectUri));
        if (layout != null) {
            sb.append("&layout=").append(layout);
        }
        if (state != null) {
            sb.append("&state=").append(state);
        }
        return sb.toString();
    }

    private static String safeUrlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    private final String appId;
    private final String redirectUri;

    private OkOAuth(String appId) {
        this.appId = appId;
        this.redirectUri = "okauth://ok" + appId;
    }
}

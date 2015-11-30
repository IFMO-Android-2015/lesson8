package ru.ifmo.android_2015.lesson_8.ok.api.user;

/**
 * Created by dmitry.trunin on 30.11.2015.
 */
public final class OkCurrentUser {

    /**
     * URL большой фотографии пользователя.
     */
    public final String bigPicUrl;

    /**
     * Имя пользователя.
     */
    public final String name;

    public OkCurrentUser(String bigPicUrl, String name) {
        this.bigPicUrl = bigPicUrl;
        this.name = name;
    }

    @Override
    public String toString() {
        return "OkCurrentUserResponse[name=\"" + name + "\" bigPicUrl=" + bigPicUrl + "]";
    }
}

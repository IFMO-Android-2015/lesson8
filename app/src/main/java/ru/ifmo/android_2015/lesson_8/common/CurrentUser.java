package ru.ifmo.android_2015.lesson_8.common;

/**
 * Created by dmitry.trunin on 30.11.2015.
 */
public final class CurrentUser {

    /**
     * URL фотографии пользователя.
     */
    public final String picUrl;

    /**
     * Имя пользователя.
     */
    public final String name;

    public CurrentUser(String bigPicUrl, String name) {
        this.picUrl = bigPicUrl;
        this.name = name;
    }

    @Override
    public String toString() {
        return "CurrentUser[name=\"" + name + "\" picUrl=" + picUrl + "]";
    }
}

package ru.ifmo.android_2015.lesson_8.ok.api;

import android.support.annotation.NonNull;

import ru.ifmo.android_2015.lesson_8.ApiException;

/**
 * Исключение, возникающее, если на запрос API ответило ошибкой.
 */
public class OkApiResponseException extends ApiException {

    private static final long serialVersionUID = 1L;

    private final @NonNull OkApiErrorInfo info;

    public OkApiResponseException(@NonNull OkApiErrorInfo info) {
        super(info.toString());
        this.info = info;
    }

    public @NonNull OkApiErrorInfo getInfo() {
        return info;
    }
}

package ru.ifmo.android_2015.lesson_8;

/**
 * Базовый класс для исключений, возникающих в процессе взаимодействия с API
 */
public class ApiException extends Exception {

    private static final long serialVersionUID = 1L;

    public ApiException() {
        super();
    }

    public ApiException(String detailMessage) {
        super(detailMessage);
    }

    public ApiException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ApiException(Throwable throwable) {
        super(throwable);
    }
}

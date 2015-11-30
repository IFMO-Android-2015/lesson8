package ru.ifmo.android_2015.lesson_8.ok.api;

import java.io.Serializable;

/**
 * Информация об ошибке, полученной от API при выполнении запроса.
 */
public class OkApiErrorInfo implements Serializable {

    public static final int ERROR_CODE_SESSION_EXPIRED = 102;
    public static final int ERROR_CODE_SESSION_KEY = 103;

    public int errorCode;
    public String errorMessage;
    public String errorData;

    public OkApiErrorInfo() {}

    public OkApiErrorInfo(int errorCode, String errorMessage, String errorData) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorData = errorData;
    }

    @Override
    public String toString() {
        return "ApiError(" + errorCode + "," + errorMessage + "," + errorData + ")";
    }

    private static final long serialVersionUID = 1L;
}

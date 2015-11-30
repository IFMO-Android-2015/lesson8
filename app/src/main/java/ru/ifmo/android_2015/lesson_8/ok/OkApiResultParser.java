package ru.ifmo.android_2015.lesson_8.ok;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;

import ru.ifmo.android_2015.lesson_8.ok.api.OkApiErrorInfo;
import ru.ifmo.android_2015.lesson_8.ok.api.OkApiResponseException;

import static ru.ifmo.android_2015.lesson_8.Constants.LOG;

/**
 * Базовый класс для реализации парсера ответов от Ok API в формате JSON.
 *
 */
public abstract class OkApiResultParser<TResult> {

    public final TResult parse(JsonReader reader) throws IOException, OkApiResponseException {
        OkApiErrorInfo errorInfo = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final String name = reader.nextName();
            if (name == null) {
                reader.skipValue();
                continue;
            }

            final boolean handled = parseRootField(name, reader);
            if (!handled) {
                errorInfo = parseErrorInfo(errorInfo, name, reader);
            }
        }
        reader.endObject();

        if (errorInfo != null) {
            throw new OkApiResponseException(errorInfo);
        }

        return createResult();
    }

    /**
     * Парсит очередное поле, найденное на самом верхнем уровне JSON-ответа. Реализация должна
     * вернуть true, если это поле было обработано, или false, если нет -- тогда поле будет
     * обработано базовым классом (например, так обрабатываются общие для всех запросов поля
     * ошибок).
     *
     * @param name      название поля
     * @param reader    JsonReader с текущей позицией на значении поля
     *
     * @return  true, если поле обработано, иначе false.
     */
    protected abstract boolean parseRootField(String name, JsonReader reader) throws IOException;

    /**
     * Этот метод вызывается в конце, когда весь JSON обработан. Реализация должна вернуть
     * полученные при разборе ответа данные.
     */
    protected abstract @NonNull TResult createResult() throws IOException;

    private OkApiErrorInfo parseErrorInfo(OkApiErrorInfo errorInfo,
                                          String name,
                                          JsonReader reader) throws IOException {
        if (name == null) {
            reader.skipValue();
            return errorInfo;
        }
        switch (name) {
            case "error_code":
            {
                if (errorInfo == null) {
                    errorInfo = new OkApiErrorInfo();
                }
                errorInfo.errorCode = reader.nextInt();
                break;
            }
            case "error_msg":
            {
                if (errorInfo == null) {
                    errorInfo = new OkApiErrorInfo();
                }
                errorInfo.errorMessage = reader.nextString();
                break;
            }
            case "error_data":
            {
                if (errorInfo == null) {
                    errorInfo = new OkApiErrorInfo();
                }
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                } else {
                    errorInfo.errorData = reader.nextString();
                }
                break;
            }
            default:
            {
                if (LOG) Log.w(LOG_TAG, "Unsupported field " + name);
                reader.skipValue();
                break;
            }
        }
        return errorInfo;
    }

    protected final String LOG_TAG = getClass().getSimpleName();

}

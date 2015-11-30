package ru.ifmo.android_2015.lesson_8.ok.api.user;

import android.support.annotation.NonNull;
import android.util.JsonReader;

import java.io.IOException;

import ru.ifmo.android_2015.lesson_8.common.CurrentUser;
import ru.ifmo.android_2015.lesson_8.ok.OkApiResultParser;
import ru.ifmo.android_2015.lesson_8.ok.api.OkApi;

/**
 * Created by dmitry.trunin on 30.11.2015.
 */
public class OkCurrentUserParser extends OkApiResultParser<CurrentUser> {

    private String bigPicUrl;
    private String userName;

    @Override
    protected boolean parseRootField(String key, JsonReader reader) throws IOException {
        switch (key) {
            case OkApi.User.NAME:       userName = reader.nextString(); return true;
            case OkApi.User.PIC_FULL:   bigPicUrl = reader.nextString(); return true;
        }
        return false;
    }

    @NonNull
    @Override
    protected CurrentUser createResult() throws IOException {
        return new CurrentUser(bigPicUrl, userName);
    }
}

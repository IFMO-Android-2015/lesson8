package ru.ifmo.android_2015.lesson_8.vk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.ifmo.android_2015.lesson_8.common.CurrentUser;

/**
 * Created by dmitry.trunin on 01.12.2015.
 */
public class VkCurrentUserParser implements VkApiResultParser<CurrentUser> {

    @Override
    public CurrentUser parse(JSONObject json) throws JSONException {
        final JSONArray data = json.getJSONArray("response");
        if (data.length() != 1) {
            throw new JSONException("Unexpected response size=" + data.length());
        }
        final JSONObject user = data.getJSONObject(0);
        final String firstName = user.optString("first_name", "");
        final String lastName = user.optString("last_name", "");
        final String picUrl = user.optString("photo_max");
        return new CurrentUser(picUrl, firstName + " " + lastName);
    }
}

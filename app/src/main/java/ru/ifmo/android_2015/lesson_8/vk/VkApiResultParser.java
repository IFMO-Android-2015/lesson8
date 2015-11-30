package ru.ifmo.android_2015.lesson_8.vk;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Базовый класс для реализации парсера ответов от Vk API в формате JSON.
 *
 */
public interface VkApiResultParser<TResult> {

    TResult parse(JSONObject json) throws JSONException;
}

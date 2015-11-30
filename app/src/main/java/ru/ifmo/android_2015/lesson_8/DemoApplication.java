package ru.ifmo.android_2015.lesson_8;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.vk.sdk.VKSdk;

/**
 * Используем этот специальны Application для отладки.
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Включаем инструмент отладки Stetho: http://facebook.github.io/stetho/
        Stetho.initializeWithDefaults(this);

        // Инициализируем SDK Вконтакте
        VKSdk.initialize(this);
    }
}

package com.example.module_route.lib.core;

import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

// RouterActivity.java
public abstract class RouterActivity extends AppCompatActivity {

    protected <T> T getPathParam(String key, Class<T> type) {
        String value = getIntent().getData().getQueryParameter(key);
        if (value == null) return null;

        if (type == String.class) {
            return (T) value;
        } else if (type == Integer.class || type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else {
            return new Gson().fromJson(value, type);
        }
    }

    protected <T> T getBodyParam(Class<T> type) {
        String encoded = getIntent().getData().getQueryParameter("_data");
        if (encoded == null) return null;

        String json = new String(
                Base64.decode(encoded, Base64.URL_SAFE),
                StandardCharsets.UTF_8
        );
        return new Gson().fromJson(json, type);
    }
}
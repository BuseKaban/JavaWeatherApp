package com.example.javaweatherapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.chromium.net.CronetEngine;
import org.chromium.net.UrlRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WeatherApi {
    private static WeatherApi instance;

    public static WeatherApi GetInstance() {
        if(instance == null) {
            instance = new WeatherApi();
        }

        return instance;
    }

    private static CronetEngine engine;

    public static void SetEngine(CronetEngine cronetEngine) {
        engine = cronetEngine;
    }

    private WeatherApi() {

    }

    public void GetCityFiveDayForecast(String cityName, UrlRequest.Callback callback) {
        Uri.Builder uriBuilder = Uri.parse("https://api.openweathermap.org/geo/1.0/direct").buildUpon();
        uriBuilder.appendQueryParameter("q", cityName);
        uriBuilder.appendQueryParameter("limit", "1");
        uriBuilder.appendQueryParameter("appid", "83299305a9929e9e6887defab131cda8");
        String locationApiUrl = uriBuilder.build().toString();

        SendRequest(locationApiUrl, new MyUrlRequestCallback(data -> {
            try {
                JSONObject geoObject = new JSONArray(data).getJSONObject(0);
                String lat = geoObject.getString("lat");
                String lon = geoObject.getString("lon");

                Uri.Builder uriBuilder1 = Uri.parse("https://api.openweathermap.org/data/2.5/forecast").buildUpon();
                uriBuilder1.appendQueryParameter("lat", lat);
                uriBuilder1.appendQueryParameter("lon", lon);
                uriBuilder1.appendQueryParameter("appid", "83299305a9929e9e6887defab131cda8");
                String weatherApiUrl = uriBuilder1.build().toString();

                SendRequest(weatherApiUrl, callback);
            } catch (JSONException e) {
                Log.e("JSON EXCEPTION", e.getMessage());
            }
        }));
    }
    public void SendRequest(String url, UrlRequest.Callback callback) {
        Executor executor = Executors.newSingleThreadExecutor();

        UrlRequest.Builder requestBuilder = engine.newUrlRequestBuilder(
                url,
                callback,
                executor);

        UrlRequest request = requestBuilder.build();

        request.start();
    }

    public void GetCityWeather(String cityName, UrlRequest.Callback callback) {
        Uri.Builder uriBuilder = Uri.parse("https://api.openweathermap.org/geo/1.0/direct").buildUpon();
        uriBuilder.appendQueryParameter("q", cityName);
        uriBuilder.appendQueryParameter("limit", "1");
        uriBuilder.appendQueryParameter("appid", "83299305a9929e9e6887defab131cda8");
        String locationApiUrl = uriBuilder.build().toString();

        SendRequest(locationApiUrl, new MyUrlRequestCallback(data -> {
            try {
                JSONObject geoObject = new JSONArray(data).getJSONObject(0);
                String lat = geoObject.getString("lat");
                String lon = geoObject.getString("lon");

                Uri.Builder uriBuilder1 = Uri.parse("https://api.openweathermap.org/data/2.5/weather").buildUpon();
                uriBuilder1.appendQueryParameter("lat", lat);
                uriBuilder1.appendQueryParameter("lon", lon);
                uriBuilder1.appendQueryParameter("appid", "83299305a9929e9e6887defab131cda8");
                String weatherApiUrl = uriBuilder1.build().toString();

                SendRequest(weatherApiUrl, callback);
            } catch (JSONException e) {
                Log.e("JSON EXCEPTION", e.getMessage());
            }
        }));
    }


}

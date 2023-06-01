package com.example.javaweatherapp;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ThemeUtils;
import androidx.fragment.app.Fragment;

import com.example.javaweatherapp.databinding.FragmentSecondBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String cityName =  SecondFragmentArgs.fromBundle(getArguments()).getCityName();
        binding.cityNameTextView.setText(cityName);
        WeatherApi.GetInstance().GetCityFiveDayForecast(cityName, new MyUrlRequestCallback(data -> {
            try {
                JSONArray weatherList = new JSONObject(data).getJSONArray("list");

                JSONObject weatherData = weatherList.getJSONObject(0);
                double humidity = weatherData.getJSONObject("main").getInt("humidity");
                double wind = weatherData.getJSONObject("wind").getDouble("speed");
                double todayTemp = weatherData.getJSONObject("main").getDouble("temp");
                String condition = weatherData.getJSONArray("weather").getJSONObject(0).getString("main");


                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                Map<String, WeatherRange> temperatureMap = new LinkedHashMap<>();
                for (int i = 0; i < weatherList.length(); i++) {

                    weatherData = weatherList.getJSONObject(i);
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    String dayName = sdf.format(inputDateFormat.parse(weatherData.getString("dt_txt")));

                    if(sdf.format(new Date()).equals(dayName)){
                        continue;
                    }

                    int temp = weatherData.getJSONObject("main").getInt("temp");

                    if (temperatureMap.containsKey(dayName))
                    {
                        WeatherRange range = temperatureMap.get(dayName);
                        if(temp > range.maxTemp)
                        {
                            range.maxTemp = temp;
                        }
                        if(temp < range.minTemp)
                        {
                            range.minTemp = temp;
                        }

                        temperatureMap.put(dayName, range);
                    }
                    else
                    {
                        temperatureMap.put(dayName, new WeatherRange(temp, temp));
                    }
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //do stuff for ui
                        binding.humidityValue.setText(humidity+ " %");
                        binding.windValue.setText(wind + " km/h");
                        binding.conditionValue.setText(condition);
                        binding.tempTextView.setText(String.format("%.0f",todayTemp - 273.15)+ " °C");

                        temperatureMap.forEach((key, value)-> {

                            LinearLayout verticalLayout = new LinearLayout(getContext());
                            verticalLayout.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.weight = 1; // Set weight to distribute space evenly
                            layoutParams.setMargins(12,12,12,12);
                            verticalLayout.setLayoutParams(layoutParams);
                            verticalLayout.setBackground(getResources().getDrawable(R.drawable.rounded_items, getContext().getTheme()));
                            verticalLayout.setPadding(32,32, 32, 32);

                            TextView dayTextView = new TextView(getContext());
                            dayTextView.setText(key);
                            dayTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                            verticalLayout.addView(dayTextView);

                            TextView maxTextView = new TextView(getContext());
                            maxTextView.setText((value.maxTemp - 273) + " °C");
                            maxTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                            maxTextView.setBackgroundColor(Color.WHITE);
                            verticalLayout.addView(maxTextView);

                            TextView minTextView = new TextView(getContext());
                            minTextView.setText((value.minTemp - 273) + " °C");
                            minTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                            minTextView.setBackgroundColor(Color.WHITE);
                            verticalLayout.addView(minTextView);

                            binding.fiveDayForecastHorizontal.addView(verticalLayout);
                            binding.fiveDayForecastHorizontal.setGravity(Gravity.CENTER_HORIZONTAL);
                        });
                    }
                });
            } catch (JSONException e) {
                Log.e("API EXCEPTION", e.getMessage());
            } catch (ParseException e) {
                Log.e("DATE EXCEPTION", e.getMessage());
            }
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
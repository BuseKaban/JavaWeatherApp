package com.example.javaweatherapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.javaweatherapp.databinding.FragmentFirstBinding;

import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    String selectedCityName;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = binding.spinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.city_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCityName = parent.getSelectedItem().toString();

                WeatherApi.GetInstance().GetCityWeather(selectedCityName, new MyUrlRequestCallback(data -> {
                    try {
                        JSONObject weatherObject = new JSONObject(data);
                        double temp = weatherObject.getJSONObject("main").getDouble("temp") - 273.15;
                        double tempMin = weatherObject.getJSONObject("main").getDouble("temp_min") - 273.15;
                        double tempMax = weatherObject.getJSONObject("main").getDouble("temp_max") - 273.15;
                        binding.textView2.setText(String.format("%.0f",temp)+ " °C");
                        binding.minMaxTempTextView.setText(String.format("%.0f °C - %.0f °C",tempMin, tempMax));
                    } catch (JSONException e) {
                        Log.e("JSON EXCEPTION", e.getMessage());
                    }
                }));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "nothing selected", Toast.LENGTH_SHORT).show();
            }
        });

        binding.detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirstFragmentDirections.ActionFirstFragmentToSecondFragment action = FirstFragmentDirections.actionFirstFragmentToSecondFragment(selectedCityName);
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(action);
            }
        });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
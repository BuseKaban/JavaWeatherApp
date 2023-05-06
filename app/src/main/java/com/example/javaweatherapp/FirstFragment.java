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
    private CronetEngine engine;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        CronetEngine.Builder myBuilder = new CronetEngine.Builder(requireContext());
        engine = myBuilder.build();
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UrlRequest.Callback callback = new UrlRequest.Callback() {
            private ByteArrayOutputStream mBytesReceived = new ByteArrayOutputStream();
            private WritableByteChannel mReceiveChannel = Channels.newChannel(mBytesReceived);

            @Override
            public void onRedirectReceived(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, String s) throws Exception {
                Log.i("TAG", "onRedirectReceived");
                urlRequest.followRedirect();
            }

            @Override
            public void onResponseStarted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) throws Exception {
                Log.i("TAG", "onResponseStarted");
                urlRequest.read(ByteBuffer.allocateDirect(32 * 1024));
                mBytesReceived.reset();
            }

            @Override
            public void onReadCompleted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, ByteBuffer byteBuffer) throws Exception {
                Log.i("TAG", "onReadCompleted");
                byteBuffer.flip();

                try {
                    mReceiveChannel.write(byteBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byteBuffer.clear();
                urlRequest.read(byteBuffer);
            }

            @Override
            public void onSucceeded(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) {
                final String receivedData = mBytesReceived.toString();

                try {
                    JSONObject geoObject = new JSONArray(receivedData).getJSONObject(0);
                    String lat = geoObject.getString("lat");
                    String lon = geoObject.getString("lon");

                    Executor executor = Executors.newSingleThreadExecutor();
                    Uri.Builder uriBuilder = Uri.parse("https://api.openweathermap.org/data/2.5/weather").buildUpon();
                    uriBuilder.appendQueryParameter("lat", lat);
                    uriBuilder.appendQueryParameter("lon", lon);
                    uriBuilder.appendQueryParameter("appid", "83299305a9929e9e6887defab131cda8");
                    String fullUrl = uriBuilder.build().toString();

                    Log.i("URL", fullUrl);
                    UrlRequest.Builder requestBuilder = engine.newUrlRequestBuilder(
                            fullUrl,
                            new MyUrlRequestCallback(new WeatherDataCallback() {
                                @Override
                                public void onWeatherDataReceived(String data) {
                                    try {
                                        JSONObject weatherObject = new JSONObject(data);
                                        double temp = weatherObject.getJSONObject("main").getDouble("temp") - 273.15;
                                        double humidity = weatherObject.getJSONObject("main").getInt("humidity");
                                        double wind = weatherObject.getJSONObject("wind").getDouble("speed");
                                        String condition = weatherObject.getJSONArray("weather").getJSONObject(0).getString("main");

                                        binding.textView2.setText(String.format("%.0f",temp)+ " °C");

                                        binding.humidityValue.setText(String.format("%.0f",humidity)+ "%");
                                        binding.windValue.setText(String.format("%.2f",wind)+ " km/h");
                                        binding.conditionValue.setText(condition);


                                    } catch (JSONException e) {
                                        Log.e("JSON EXCEPTION", e.getMessage());
                                    }

                                }
                            }),
                            executor);
                    UrlRequest request = requestBuilder.build();
                    request.start();
                } catch (JSONException e) {
                    Log.e("JSON EXCEPTION", e.getMessage());
                }
            }

            @Override
            public void onFailed(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, CronetException e) {
                Log.i("TAG", "onFailed");
                Log.i("TAG", "error is: %s" + e.getMessage());

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "onFailed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        Spinner spinner = binding.spinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.city_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String element = parent.getSelectedItem().toString();

                Executor executor = Executors.newSingleThreadExecutor();

                Uri.Builder uriBuilder = Uri.parse("https://api.openweathermap.org/geo/1.0/direct").buildUpon();
                uriBuilder.appendQueryParameter("q", element);
                uriBuilder.appendQueryParameter("limit", "1");
                uriBuilder.appendQueryParameter("appid", "83299305a9929e9e6887defab131cda8");
                String fullUrl = uriBuilder.build().toString();

                UrlRequest.Builder requestBuilder = engine.newUrlRequestBuilder(
                        fullUrl,
                        callback,
                        executor);

                UrlRequest request = requestBuilder.build();

                request.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "nothing selected", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
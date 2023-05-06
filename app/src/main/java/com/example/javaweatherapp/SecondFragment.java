package com.example.javaweatherapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.javaweatherapp.databinding.FragmentSecondBinding;

import org.chromium.net.CronetEngine;
import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private CronetEngine engine;

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



        Integer count = SecondFragmentArgs.fromBundle(getArguments()).getMyArg();
        String countText = getString(R.string.random_heading, count);
        binding.textviewHeader.setText(countText);

        Random random = new java.util.Random();
        Integer randomNumber = 0;
        if (count > 0) {
            randomNumber = random.nextInt(count + 1);
        }
        binding.textviewRandom.setText(randomNumber.toString());
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

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
                Log.i("TAG", "onSucceeded");
                Log.i("TAG", String.format("Request Completed, status code is %d, total received bytes is %d",
                        urlResponseInfo.getHttpStatusCode(), urlResponseInfo.getReceivedByteCount()));

                final String receivedData = mBytesReceived.toString();
                final String url = urlResponseInfo.getUrl();
                final String text = "Completed " + url + " (" + urlResponseInfo.getHttpStatusCode() + ")";

                Log.i("TAG", "text:" + text);
                Log.i("TAG", "receivedData:" + receivedData);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "onSucceeded", Toast.LENGTH_SHORT).show();
                        binding.textviewHeader.setText(receivedData);
                    }
                });
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


        binding.buttonSecond2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CronetEngine.Builder myBuilder = new CronetEngine.Builder(requireContext());
                engine = myBuilder.build();
                Executor executor = Executors.newSingleThreadExecutor();
                UrlRequest.Builder requestBuilder = engine.newUrlRequestBuilder(
                        "https://api.openweathermap.org/data/2.5/weather?lat=51.5073219&lon=-0.1276474&appid=83299305a9929e9e6887defab131cda8",
                        callback,
                        executor);
                UrlRequest request = requestBuilder.build();
                request.start();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
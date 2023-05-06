package com.example.javaweatherapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.javaweatherapp.WeatherDataCallback;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;


public class MyUrlRequestCallback extends UrlRequest.Callback {
    private WeatherDataCallback callback;
    private static final String TAG = "MyUrlRequestCallback";
    private ByteArrayOutputStream mBytesReceived = new ByteArrayOutputStream();
    private WritableByteChannel mReceiveChannel = Channels.newChannel(mBytesReceived);
    public MyUrlRequestCallback(WeatherDataCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) throws Exception {
        Log.i(TAG, "onRedirectReceived method called.");
        // You should call the request.followRedirect() method to continue
        // processing the request.
        request.followRedirect();
    }

    @Override
    public void onResponseStarted(UrlRequest request, UrlResponseInfo info) throws Exception {
        Log.i(TAG, "onResponseStarted method called.");
        request.read(ByteBuffer.allocateDirect(102400));
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
    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
        final String receivedData = mBytesReceived.toString();
        callback.onWeatherDataReceived(receivedData);
    }

    @Override
    public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {

    }


}
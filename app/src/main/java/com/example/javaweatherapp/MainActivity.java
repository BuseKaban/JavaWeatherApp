package com.example.javaweatherapp;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.javaweatherapp.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import org.chromium.net.CronetEngine;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CronetEngine.Builder myBuilder = new CronetEngine.Builder(getBaseContext());
        CronetEngine engine = myBuilder.build();
        WeatherApi.SetEngine(engine);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }


}
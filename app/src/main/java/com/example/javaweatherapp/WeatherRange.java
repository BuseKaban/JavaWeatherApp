package com.example.javaweatherapp;

public class WeatherRange {
    public int minTemp = Integer.MAX_VALUE;
    public int maxTemp = Integer.MIN_VALUE;

    public WeatherRange (int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public WeatherRange () {
    }
}

package com.github.stkent.ohnosnow.weatherapi;

import com.google.gson.JsonElement;

import retrofit.http.GET;

public interface WundergroundApi {

    @GET("/forecast/q/MI/Ferndale.json")
    JsonElement getWeatherData();

}

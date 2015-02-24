package com.github.stkent.ohnosnow.weatherapi;

import com.google.gson.JsonElement;

import retrofit.http.GET;
import retrofit.http.Path;

public interface WundergroundApi {

    @GET("/geolookup/q/{latitude},{longitude}.json")
    JsonElement getCityName(@Path("latitude") final double latitude, @Path("longitude") final double longitude);

    @GET("/forecast/q/{cityLocation}")
    JsonElement getWeatherData(@Path(value = "cityLocation", encode = false) final String cityLocation);

}

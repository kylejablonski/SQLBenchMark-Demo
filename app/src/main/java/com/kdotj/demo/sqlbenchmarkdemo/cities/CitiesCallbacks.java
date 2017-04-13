package com.kdotj.demo.sqlbenchmarkdemo.cities;

import com.kdotj.demo.sqlbenchmarkdemo.data.CityResponse;

import java.util.List;

/**
 * Created by kyle.jablonski on 4/13/17.
 */

public interface CitiesCallbacks {


    void onStartLoading();

    void onLoadCities(int groupOption, long timePassed, String procedure, List<CityResponse.City> cityList);
}

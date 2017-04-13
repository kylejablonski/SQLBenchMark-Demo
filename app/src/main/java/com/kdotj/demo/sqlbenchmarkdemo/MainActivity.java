package com.kdotj.demo.sqlbenchmarkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kdotj.demo.sqlbenchmarkdemo.cities.CitiesFragment;

public class MainActivity extends AppCompatActivity {

    private CitiesFragment mCitiesFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mCitiesFragment = (CitiesFragment) getSupportFragmentManager().findFragmentByTag(CitiesFragment.class.getSimpleName());
        if(mCitiesFragment == null){
            mCitiesFragment = CitiesFragment.newInstance(null);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mCitiesFragment, CitiesFragment.class.getSimpleName())
                .commit();


    }
}

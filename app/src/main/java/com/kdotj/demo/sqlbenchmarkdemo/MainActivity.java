package com.kdotj.demo.sqlbenchmarkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kdotj.demo.sqlbenchmarkdemo.cities.CitiesFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, CitiesFragment.newInstance(null), CitiesFragment.class.getSimpleName())
                .commit();
    }
}

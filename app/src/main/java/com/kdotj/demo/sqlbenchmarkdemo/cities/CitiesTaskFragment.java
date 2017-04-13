package com.kdotj.demo.sqlbenchmarkdemo.cities;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.kdotj.demo.sqlbenchmarkdemo.R;
import com.kdotj.demo.sqlbenchmarkdemo.data.CityResponse;
import com.kdotj.demo.sqlbenchmarkdemo.data.FileIO;
import com.kdotj.demo.sqlbenchmarkdemo.db.DatabaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless fragment to run DB operations on a background thread
 * that maintains state across orientation changes.
 *
 * Created by kyle.jablonski on 4/13/17.
 */

public class CitiesTaskFragment extends Fragment {

    private static final String TAG = CitiesTaskFragment.class.getSimpleName();
    private List<CityResponse.City> mCityList;
    private int mGroupOption;
    private long mTimePassed;
    private String mProcedure;
    private CitiesCallbacks mCallback;
    private CityTask mCityTask;

    public static CitiesTaskFragment newInstance(Bundle args){
        CitiesTaskFragment fragment = new CitiesTaskFragment();
        if(args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        setRetainInstance(true);
        mCityList = new ArrayList<>();

        try {
            mCallback = (CitiesCallbacks) getParentFragment();
        }catch(ClassCastException ex){
            throw new RuntimeException("Must implement the CitiesCallbacks interface in parent fragment!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    public List<CityResponse.City> getCityList(){
        return mCityList;
    }

    public void startFetch(int groupOption){
        Log.d(TAG, "startFetch() called with: groupOption = [" + groupOption + "]");
        mGroupOption = groupOption;
        // check if the task is running, if so return
        if(mCityTask != null) {
            if (mCityTask.getStatus() == AsyncTask.Status.RUNNING){
                return;
            }else{
                mCityTask = new CityTask();
                mCityTask.execute();
            }
        }else {
            mCityTask = new CityTask();
            mCityTask.execute();
        }
    }

    public void stopFetch(){
        if(mCityTask != null && mCityTask.getStatus() != AsyncTask.Status.RUNNING) {
            mCityTask = new CityTask();
            return;
        }
    }

    public void deleteCities(){
        Log.d(TAG, "deleteCities() called");
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
        databaseHelper.deleteCities();
    }

    private void readCityFile(){
        Log.d(TAG, "readCityFile() called");
        List<CityResponse.City> cityList = new ArrayList<>();
        FileIO fileIo = new FileIO(getContext());
        try {
            String citiesRaw = fileIo.readCities(R.raw.cities);
            cityList.addAll(CityResponse.fromJson(citiesRaw));
        }catch(IOException ex){
            throw new IllegalStateException("Unable to read the city file.");
        }

        String [] options = getResources().getStringArray(R.array.options_items);
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
        if(mGroupOption == 0){
            mTimePassed = databaseHelper.storeCitiesInDb(cityList);
        }else if(mGroupOption == 1){
            mTimePassed = databaseHelper.storeCitiesInDbTransactions(cityList);
        }else if(mGroupOption == 2){
            mTimePassed = databaseHelper.storeCitiesInDbPrepared(cityList);
        }else if(mGroupOption == 3){
            mTimePassed = databaseHelper.storeCitiesInDbPreparedTransaction(cityList);
        }else if(mGroupOption == 4){
            mTimePassed = databaseHelper.storeCitiesInDbWithOnConflict(cityList, SQLiteDatabase.CONFLICT_IGNORE);
        }else if(mGroupOption == 5){
            mTimePassed = databaseHelper.storeCitiesInDbTransactionsWithOnConflict(cityList, SQLiteDatabase.CONFLICT_IGNORE);
        }else if(mGroupOption == 6){
            mTimePassed = databaseHelper.storeCitiesInDbWithOnConflict(cityList, SQLiteDatabase.CONFLICT_REPLACE);
        }else if(mGroupOption == 7){
            mTimePassed = databaseHelper.storeCitiesInDbTransactionsWithOnConflict(cityList, SQLiteDatabase.CONFLICT_REPLACE);
        }
        mProcedure = options[mGroupOption];
    }

    private class CityTask extends AsyncTask<Void, Integer, List<CityResponse.City>> {

        CityTask(){
            Log.d(TAG, "CityTask() called");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute() called");
            mCallback.onStartLoading();
        }

        @Override
        protected List<CityResponse.City> doInBackground(Void... params) {
            Log.d(TAG, "doInBackground() called with: params = [" + params + "]");
            readCityFile();
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
            mCityList.clear();
            mCityList.addAll(databaseHelper.readCities());
            return mCityList;
        }

        @Override
        protected void onPostExecute(List<CityResponse.City> cities) {
            super.onPostExecute(cities);
            Log.d(TAG, "onPostExecute() called with: cities = [" + cities + "]");

            mCallback.onLoadCities(mGroupOption, mTimePassed, mProcedure, cities);

        }
    }
}

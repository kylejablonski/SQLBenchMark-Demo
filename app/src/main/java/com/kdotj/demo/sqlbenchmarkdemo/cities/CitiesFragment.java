package com.kdotj.demo.sqlbenchmarkdemo.cities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.kdotj.demo.sqlbenchmarkdemo.R;
import com.kdotj.demo.sqlbenchmarkdemo.data.CityResponse;
import com.kdotj.demo.sqlbenchmarkdemo.data.FileIO;
import com.kdotj.demo.sqlbenchmarkdemo.db.DatabaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by kyle.jablonski on 4/11/17.
 */

public class CitiesFragment extends Fragment {

    private List<CityResponse.City> mCityList;
    private CityAdapter mCityAdapter;
    private RecyclerView mCityRecyclerView;
    private AppCompatTextView mCityProcessingTime;
    private ProgressBar mPbLoading;
    private AppCompatTextView mTvEmpty;
    private FloatingActionButton mBtnRefresh;
    private RadioGroup mGroupOptions;

    private int mGroupOption;
    private long mTimePassed;
    private String mProcedure;

    public static CitiesFragment newInstance(Bundle args){
        CitiesFragment fragment = new CitiesFragment();
        fragment.setRetainInstance(true);
        fragment.setHasOptionsMenu(true);
        if(args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCityList = new ArrayList<>();
        mCityAdapter = new CityAdapter(getContext(), mCityList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cities, container, false);
        mBtnRefresh = (FloatingActionButton) view.findViewById(R.id.btn_refresh);
        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CityTask().execute();
            }
        });
        mGroupOptions = (RadioGroup) view.findViewById(R.id.group_options);
        mGroupOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                mGroupOption = checkedId;
            }
        });
        mTvEmpty = (AppCompatTextView) view.findViewById(R.id.tv_empty);
        mPbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
        mCityProcessingTime = (AppCompatTextView) view.findViewById(R.id.tv_time_spent);
        mCityRecyclerView = (RecyclerView) view.findViewById(R.id.rv_cities);
        mCityRecyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        mCityRecyclerView.setAdapter(mCityAdapter);
        mCityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPbLoading.setVisibility(View.GONE);
        mTvEmpty.setVisibility(View.VISIBLE);
        mCityProcessingTime.setText("Select an option and click refresh");
        mGroupOption = R.id.option_content_values;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cities, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_delete){
            deleteCities();
            mCityList.clear();
            mCityAdapter.notifyDataSetChanged();
            mPbLoading.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
            mCityProcessingTime.setText("Select an option and click refresh");
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private List<CityResponse.City> readCityFile(){

        FileIO fileIo = new FileIO(getContext());
        try {
            String citiesRaw = fileIo.readCities(R.raw.cities);
            mCityList.clear();
            mCityList.addAll(CityResponse.fromJson(citiesRaw));
        }catch(IOException ex){
            throw new IllegalStateException("Unable to read the city file.");
        }

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
        if(mGroupOption == R.id.option_content_values){
            mTimePassed = databaseHelper.storeCitiesInDb(mCityList);
            mProcedure = "ContentValues";
        }else if(mGroupOption == R.id.option_raw_query){
            mTimePassed = databaseHelper.storeCitiesRawInsert(mCityList);
            mProcedure = "Raw Query";
        }else{
            mTimePassed = databaseHelper.storeCitiesInDbPrepared(mCityList);
            mProcedure = "Prepared statement";
        }
        return mCityList;
    }

    private void deleteCities(){
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
        databaseHelper.deleteCities();
    }

    class CityTask extends AsyncTask<Void, Integer, List<CityResponse.City>>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPbLoading.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);
            mCityProcessingTime.setText("Calculating processing time");

        }

        @Override
        protected List<CityResponse.City> doInBackground(Void... params) {
            deleteCities();
            return readCityFile();
        }

        @Override
        protected void onPostExecute(List<CityResponse.City> cities) {
            super.onPostExecute(cities);

            mPbLoading.setVisibility(View.GONE);

            if(mCityList == null || mCityList.size() < 1){
                mTvEmpty.setVisibility(View.VISIBLE);
            }

            mCityProcessingTime.setText(String.format(Locale.getDefault(), "Storing %s cities took %sms, using %s", String.valueOf(mCityList.size()), Long.toString(mTimePassed), mProcedure));

            mCityAdapter.notifyDataSetChanged();
        }
    }


}

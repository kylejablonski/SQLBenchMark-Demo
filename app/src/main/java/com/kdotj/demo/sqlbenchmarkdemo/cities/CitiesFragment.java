package com.kdotj.demo.sqlbenchmarkdemo.cities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
                v.setEnabled(false);
                new CityTask().execute();
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
        resetUI();
        mGroupOption = 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cities, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_delete){
            deleteCities();
            resetUI();
            return true;
        }else if(item.getItemId() == R.id.action_option){
            showOptionsDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showOptionsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
            builder.setSingleChoiceItems(R.array.options_items, mGroupOption, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mGroupOption != which) {
                        mGroupOption = which;
                        resetUI();
                    }
                    dialog.dismiss();
                }
            });
        builder.setTitle("Select an option");
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void readCityFile(){

        List<CityResponse.City> cityList = new ArrayList<>();
        FileIO fileIo = new FileIO(getContext());
        try {
            String citiesRaw = fileIo.readCities(R.raw.cities);
            cityList.addAll(CityResponse.fromJson(citiesRaw));
        }catch(IOException ex){
            throw new IllegalStateException("Unable to read the city file.");
        }

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
        if(mGroupOption == 0){
            mTimePassed = databaseHelper.storeCitiesInDb(cityList);
            mProcedure = "ContentValues";
        }else if(mGroupOption == 1){
            mTimePassed = databaseHelper.storeCitiesInDbTransactions(cityList);
            mProcedure = "ContentValues(transaction)";
        }else if(mGroupOption == 2){
            mTimePassed = databaseHelper.storeCitiesInDbPrepared(cityList);
            mProcedure = "Prepared statement";
        }else if(mGroupOption == 3){
            mTimePassed = databaseHelper.storeCitiesInDbPreparedTransaction(cityList);
            mProcedure = "Prepared statement(transaction)";
        }

    }

    private void deleteCities(){
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
        databaseHelper.deleteCities();

    }

    private void resetUI(){
        mCityList.clear();
        mCityAdapter.notifyDataSetChanged();
        mPbLoading.setVisibility(View.GONE);
        mTvEmpty.setVisibility(View.VISIBLE);
        mCityProcessingTime.setText("Select an option and click refresh");
        mBtnRefresh.setEnabled(true);

    }

    class CityTask extends AsyncTask<Void, Integer, List<CityResponse.City>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCityProcessingTime.setText("Calculating processing time");
            mPbLoading.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);
        }

        @Override
        protected List<CityResponse.City> doInBackground(Void... params) {
            deleteCities();
            readCityFile();
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getContext().getApplicationContext());
            mCityList.clear();
            mCityList.addAll(databaseHelper.readCities());
            return mCityList;
        }

        @Override
        protected void onPostExecute(List<CityResponse.City> cities) {
            super.onPostExecute(cities);

            mPbLoading.setVisibility(View.GONE);

            if(mCityList == null || mCityList.size() < 1){
                mTvEmpty.setVisibility(View.VISIBLE);
            }

            mBtnRefresh.setEnabled(true);
            mCityProcessingTime.setText(String.format(Locale.getDefault(), "Storing %s cities took %sms, using %s", String.valueOf(mCityList.size()), Long.toString(mTimePassed), mProcedure));

            mCityAdapter.notifyDataSetChanged();
        }
    }


}

package com.kdotj.demo.sqlbenchmarkdemo.cities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.kdotj.demo.sqlbenchmarkdemo.R;
import com.kdotj.demo.sqlbenchmarkdemo.data.CityResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by kyle.jablonski on 4/11/17.
 */

public class CitiesFragment extends Fragment implements CitiesCallbacks {

    private static final String TAG = CitiesFragment.class.getSimpleName();
    private static final String GROUP_OPTION = "group_option";
    private static final String ACTIVE_TASK = "active_task";

    private List<CityResponse.City> mCityList;
    private CityAdapter mCityAdapter;
    private RecyclerView mCityRecyclerView;
    private AppCompatTextView mCityProcessingTime;
    private ProgressBar mPbLoading;
    private AppCompatTextView mTvEmpty;
    private FloatingActionButton mBtnRefresh;


    private int mGroupOption;
    private boolean mActiveTask;
    private CitiesTaskFragment mCitiesTaskFragment;

    public static CitiesFragment newInstance(Bundle args) {
        Log.d(TAG, "newInstance() called with: args = [" + args + "]");
        CitiesFragment fragment = new CitiesFragment();
        fragment.setRetainInstance(true);
        fragment.setHasOptionsMenu(true);
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        mCityList = new ArrayList<>();
        mCityAdapter = new CityAdapter(getContext(), mCityList);

        mCitiesTaskFragment = (CitiesTaskFragment) getChildFragmentManager().findFragmentByTag(CitiesTaskFragment.class.getSimpleName());
        if(mCitiesTaskFragment == null) {
            // null bundle passed for now
            mCitiesTaskFragment = CitiesTaskFragment.newInstance(null);

            // add the fragment right away
            getChildFragmentManager()
                    .beginTransaction()
                    .add(mCitiesTaskFragment, CitiesTaskFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        View view = inflater.inflate(R.layout.fragment_cities, container, false);
        mTvEmpty = (AppCompatTextView) view.findViewById(R.id.tv_empty);
        mPbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);
        mCityProcessingTime = (AppCompatTextView) view.findViewById(R.id.tv_time_spent);
        mCityRecyclerView = (RecyclerView) view.findViewById(R.id.rv_cities);
        mCityRecyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        mCityRecyclerView.setAdapter(mCityAdapter);
        mCityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBtnRefresh = (FloatingActionButton) view.findViewById(R.id.btn_refresh);
        mBtnRefresh.setOnClickListener(RefreshListener);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called with: view = [" + view + "], savedInstanceState = [" + savedInstanceState + "]");
        if(savedInstanceState == null) {
            resetUI();
            mGroupOption = 0;
        }else{
            mGroupOption = savedInstanceState.getInt(GROUP_OPTION, 0);
            mActiveTask = savedInstanceState.getBoolean(ACTIVE_TASK, false);
        }

        if(mActiveTask){
            mCityProcessingTime.setText("Calculating processing time");
            mPbLoading.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.GONE);
            mBtnRefresh.setEnabled(false);
        }else{
            mCityProcessingTime.setText("Select an option and click refresh");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");
        inflater.inflate(R.menu.menu_cities, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        mCitiesTaskFragment = (CitiesTaskFragment) getChildFragmentManager().findFragmentByTag(CitiesTaskFragment.class.getSimpleName());
        if (item.getItemId() == R.id.action_delete) {
            if (mCitiesTaskFragment != null) {
                mCitiesTaskFragment.deleteCities();
                if(mActiveTask) {
                    mCitiesTaskFragment.stopFetch();
                }
                resetUI();
                return true;
            } else {
                return false;
            }
        } else if (item.getItemId() == R.id.action_option) {
            showOptionsDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(GROUP_OPTION, mGroupOption);
        outState.putBoolean(ACTIVE_TASK, mActiveTask);

        Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
    }

    private void showOptionsDialog() {
        Log.d(TAG, "showOptionsDialog() called");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setSingleChoiceItems(R.array.options_items, mGroupOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mGroupOption != which) {
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

    private void resetUI() {
        Log.d(TAG, "resetUI() called");
        mCityList.clear();
        mCityAdapter.notifyDataSetChanged();
        mPbLoading.setVisibility(View.GONE);
        mTvEmpty.setVisibility(View.VISIBLE);
        mCityProcessingTime.setText("Select an option and click refresh");
        mBtnRefresh.setEnabled(true);

    }

    @Override
    public void onStartLoading() {
        Log.d(TAG, "onStartLoading() called");
        mActiveTask = true;
        mCityList.clear();
        mCityProcessingTime.setText("Calculating processing time");
        mPbLoading.setVisibility(View.VISIBLE);
        mTvEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onLoadCities(int groupOption, long timePassed, String procedure, List<CityResponse.City> cityList) {
        Log.d(TAG, "onLoadCities() called with: groupOption = [" + groupOption + "], timePassed = [" + timePassed + "], procedure = [" + procedure + "], cityList = [" + cityList + "]");
        mGroupOption = groupOption;
        mActiveTask = false;

        mPbLoading.setVisibility(View.GONE);

        mCityList.addAll(cityList);

        if (mCityList == null || mCityList.size() < 1) {
            mTvEmpty.setVisibility(View.VISIBLE);
        }

        mBtnRefresh.setEnabled(true);
        mCityProcessingTime.setText(String.format(Locale.getDefault(), "Storing %s cities took %sms, using %s", String.valueOf(mCityList.size()), Long.toString(timePassed), procedure));

        mCityAdapter.notifyDataSetChanged();
    }

    private final View.OnClickListener RefreshListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick() called with: v = [" + v + "]");
            mCitiesTaskFragment = (CitiesTaskFragment) getChildFragmentManager()
                    .findFragmentByTag(CitiesTaskFragment.class.getSimpleName());
            if (mCitiesTaskFragment != null) {
                v.setEnabled(false);
                mCitiesTaskFragment.startFetch(mGroupOption);
            }
        }
    };
}

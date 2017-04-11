package com.kdotj.demo.sqlbenchmarkdemo.cities;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kdotj.demo.sqlbenchmarkdemo.R;
import com.kdotj.demo.sqlbenchmarkdemo.data.CityResponse;

import java.util.List;

/**
 * Created by kyle.jablonski on 4/11/17.
 */

class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder>{

    private Context mContext;
    private List<CityResponse.City> mCityList;


    public CityAdapter(Context context, List<CityResponse.City> cityList){
        mContext = context;
        mCityList = cityList;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {

        if(mCityList != null && mCityList.size() > 0) {
            CityResponse.City city = mCityList.get(position);

            holder.mCityName.setText(city.name);
            holder.mCityCountry.setText(city.country);
            holder.mCitySubCountry.setText(city.subCountry);
            holder.mCityGeoNameId.setText(city.geoNameId);

        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(mCityList != null && mCityList.size() > 0){
            count = mCityList.size();
        }
        return count;
    }


    static class CityViewHolder extends RecyclerView.ViewHolder{

        AppCompatTextView mCityName;
        AppCompatTextView mCityCountry;
        AppCompatTextView mCitySubCountry;
        AppCompatTextView mCityGeoNameId;

        CityViewHolder(View itemView) {
            super(itemView);

            mCityName = (AppCompatTextView) itemView.findViewById(R.id.city_name);
            mCityCountry = (AppCompatTextView) itemView.findViewById(R.id.city_country);
            mCitySubCountry = (AppCompatTextView) itemView.findViewById(R.id.city_sub_country);
            mCityGeoNameId = (AppCompatTextView) itemView.findViewById(R.id.city_geo_name_id);
        }
    }
}

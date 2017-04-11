package com.kdotj.demo.sqlbenchmarkdemo.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by kyle.jablonski on 4/11/17.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CityResponse {

    public static List<City> fromJson(String json){
        try{
            return new ObjectMapper().readValue(json, new TypeReference<List<City>>(){});
        }catch(JsonParseException ex){
            throw new IllegalStateException("Invalid json!");
        }catch(IOException ex){
            throw new RuntimeException("Unable to read json");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class City implements Parcelable{

        @JsonProperty("name")
        public String name;

        @JsonProperty("country")
        public String country;

        @JsonProperty("subcountry")
        public String subCountry;

        @JsonProperty("geonameid")
        public String geoNameId;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(country);
            dest.writeString(subCountry);
            dest.writeString(geoNameId);
        }

        public static final Creator<City> CREATOR = new Creator<City>() {
            @Override
            public City createFromParcel(Parcel source) {
                City instance = new City();
                instance.name = source.readString();
                instance.country = source.readString();
                instance.subCountry = source.readString();
                instance.geoNameId = source.readString();
                return instance;
            }

            @Override
            public City[] newArray(int size) {
                return new City[size];
            }
        };
    }

}

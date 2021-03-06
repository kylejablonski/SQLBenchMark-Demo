package com.kdotj.demo.sqlbenchmarkdemo.db;

import android.provider.BaseColumns;

/**
 * Created by kyle.jablonski on 4/11/17.
 */

public class CitiesContracts implements BaseColumns{

    public static final String TABLE_NAME = "Cities";

    public static final String KEY_NAME = "name";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_SUB_COUNTRY = "sub_country";
    public static final String KEY_GEO_NAME_ID = "geo_name_id";
    public static final String KEY_INDEX = "idx_geo_name_id";


    public static final String CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + "( "
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_NAME + " TEXT, "
            + KEY_COUNTRY + " TEXT, "
            + KEY_SUB_COUNTRY + " TEXT, "
            + KEY_GEO_NAME_ID + " TEXT);";

    public static final String SQL_INSERT = "INSERT INTO "+ TABLE_NAME + " ( "
            + CitiesContracts.KEY_NAME + ", "
            + CitiesContracts.KEY_COUNTRY + ", "
            + CitiesContracts.KEY_SUB_COUNTRY + ", "
            + CitiesContracts.KEY_GEO_NAME_ID
            + " ) VALUES (?, ?, ? , ?)";

    public static final String SQL_INDEX_GEO_NAME_ID = "CREATE UNIQUE INDEX "+ KEY_INDEX + " ON "+ TABLE_NAME + "("+ KEY_GEO_NAME_ID+")";
}

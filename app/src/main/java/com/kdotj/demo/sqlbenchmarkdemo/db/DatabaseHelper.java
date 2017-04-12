package com.kdotj.demo.sqlbenchmarkdemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.kdotj.demo.sqlbenchmarkdemo.data.CityResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton DB Access
 *
 * Created by kyle.jablonski on 4/11/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DB_NAME = "benchmark_demo.db";
    private static final int DB_VERSION = 1;

    static DatabaseHelper sInstance;

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Singleton instance method
     * @param context application context (use context.getApplicationContext())
     * @return this
     */
    public static DatabaseHelper getInstance(Context context){
        if(sInstance == null) {
            sInstance = new DatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CitiesContracts.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            db.execSQL("DROP TABLE IF EXISTS "+ CitiesContracts.TABLE_NAME);
            onCreate(db);
        }
    }

    /**
     * Reads the City data out of the database
     * @return List<CityResponse.City> data
     */
    public List<CityResponse.City> readCities(){
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(CitiesContracts.TABLE_NAME, new String[]{
                CitiesContracts._ID,
                CitiesContracts.KEY_NAME,
                CitiesContracts.KEY_COUNTRY,
                CitiesContracts.KEY_SUB_COUNTRY,
                CitiesContracts.KEY_GEO_NAME_ID},
                null, null, null, null, CitiesContracts.KEY_COUNTRY + " ASC");

        cursor.moveToFirst();
        List<CityResponse.City> cityList = new ArrayList<>();
        while(!cursor.isAfterLast()){
            CityResponse.City city = new CityResponse.City();
            city.id = cursor.getInt(cursor.getColumnIndexOrThrow(CitiesContracts._ID));
            city.name = cursor.getString(cursor.getColumnIndexOrThrow(CitiesContracts.KEY_NAME));
            city.country = cursor.getString(cursor.getColumnIndexOrThrow(CitiesContracts.KEY_COUNTRY));
            city.subCountry = cursor.getString(cursor.getColumnIndexOrThrow(CitiesContracts.KEY_SUB_COUNTRY));
            city.geoNameId = cursor.getString(cursor.getColumnIndexOrThrow(CitiesContracts.KEY_GEO_NAME_ID));
            cityList.add(city);
            cursor.moveToNext();
        }
        cursor.close();

        Log.d(TAG, "Reading cities took "+ (System.currentTimeMillis() - startTime) + "ms");
        return cityList;
    }

    /**
     * Insert via ContentValues only (no transactions)
     * @param cityList - the list of cities
     * @return time spent
     */
    public long storeCitiesInDb(List<CityResponse.City> cityList){

        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();
        for(CityResponse.City city: cityList){

            ContentValues contentValues = new ContentValues();
            contentValues.put(CitiesContracts.KEY_NAME, city.name);
            contentValues.put(CitiesContracts.KEY_COUNTRY, city.country);
            contentValues.put(CitiesContracts.KEY_SUB_COUNTRY, city.subCountry);
            contentValues.put(CitiesContracts.KEY_GEO_NAME_ID, city.geoNameId);

            db.insert(CitiesContracts.TABLE_NAME, null, contentValues);
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * Inserts via ContentValues using transcations
     * @param cityList the list of cities
     * @return time spent
     */
    public long storeCitiesInDbTransactions(List<CityResponse.City> cityList){
        long startTime = System.currentTimeMillis();

        SQLiteDatabase db = getWritableDatabase();
        try{
            db.beginTransaction();

            for(CityResponse.City city: cityList){

                ContentValues contentValues = new ContentValues();
                contentValues.put(CitiesContracts.KEY_NAME, city.name);
                contentValues.put(CitiesContracts.KEY_COUNTRY, city.country);
                contentValues.put(CitiesContracts.KEY_SUB_COUNTRY, city.subCountry);
                contentValues.put(CitiesContracts.KEY_GEO_NAME_ID, city.geoNameId);

                db.insert(CitiesContracts.TABLE_NAME, null, contentValues);
            }

            db.setTransactionSuccessful();
        }catch(Exception ex){

        }finally{
            db.endTransaction();
        }


        return System.currentTimeMillis() - startTime;
    }

    /**
     * Insert via Prepared statement (no transactions)
     * @param cityList the list of cities
     * @return time spent
     */
    public long storeCitiesInDbPrepared(List<CityResponse.City> cityList){
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmtInsert = db.compileStatement(CitiesContracts.SQL_INSERT);

        for (CityResponse.City city : cityList) {
            stmtInsert.clearBindings();
            stmtInsert.bindString(1, city.name);
            stmtInsert.bindString(2, city.country);
            stmtInsert.bindString(3, city.subCountry);
            stmtInsert.bindString(4, city.geoNameId);
            stmtInsert.executeInsert();
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * Insert via Prepared statement with transactions
     * @param cityList - the list of cities
     * @return time spent
     */
    public long storeCitiesInDbPreparedTransaction(List<CityResponse.City> cityList){
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmtInsert = db.compileStatement(CitiesContracts.SQL_INSERT);
        try{
            db.beginTransaction();

            for (CityResponse.City city : cityList) {
                stmtInsert.clearBindings();
                stmtInsert.bindString(1, city.name);
                stmtInsert.bindString(2, city.country);
                stmtInsert.bindString(3, city.subCountry);
                stmtInsert.bindString(4, city.geoNameId);
                stmtInsert.executeInsert();
            }

            db.setTransactionSuccessful();
        }catch(Exception ex){
            // .. handle your exceptions
        }finally{
            db.endTransaction();
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * Deletes all cities from the database
     */
    public void deleteCities(){
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = getReadableDatabase();
        db.delete(CitiesContracts.TABLE_NAME, null, null);
        Log.d(TAG, "Delete all cities took "+ (System.currentTimeMillis() - startTime) + "ms");
    }

}

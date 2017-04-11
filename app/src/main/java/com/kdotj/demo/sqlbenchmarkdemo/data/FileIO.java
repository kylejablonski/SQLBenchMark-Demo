package com.kdotj.demo.sqlbenchmarkdemo.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple FileIO class to read a raw asset from local
 * Created by kyle.jablonski on 4/11/17.
 */

public class FileIO {

    Context context;

    public FileIO(Context context){
        this.context = context;
    }

    public String readCities(int resourceId) throws IOException{
        InputStream is =  context.getResources().openRawResource(resourceId);
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }
}

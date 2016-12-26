package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesse on 12/20/2016.
 */

public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    private static final String LOG_TAG = EarthquakeLoader.class.getName();
    private String mUrl;

    public EarthquakeLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    public List<Earthquake> loadInBackground() {
        if(mUrl==null) return null;
        ArrayList<Earthquake> earthquakes = (ArrayList<Earthquake>) QueryUtils.fetchEarthquakeData(mUrl);
        return earthquakes;
    }

    @Override
    public void onStartLoading(){
        forceLoad();
    }
}

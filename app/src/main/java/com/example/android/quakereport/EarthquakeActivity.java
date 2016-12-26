package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Earthquake>> {


    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    private static final int EARTHQUAKE_LOADER_ID = 1;

    private EarthquakeAdapter mAdapter;

    private TextView mEmptyStateTextView;

    /** URL for earthquake data from the USGS dataset */
    private static final String USGS_REQUEST_URL =
            "http://earthquake.usgs.gov/fdsnws/event/1/query";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());


        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        /*This code checks connectivity */
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo !=null && networkInfo.isConnected()){
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        }else{
            View loadingIndicator = findViewById(R.id.loading_indicator);
            // hide the loading indicator since we're not loading anything
            loadingIndicator.setVisibility(View.GONE);
            // update empty state with no internet message
            mEmptyStateTextView.setText(R.string.no_internet_connection);

        }

    }

    /* This inflates the menu bar with the menu layout defined in res/menu/main.xml*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> data) {
        // Clear the adapter of previous earthquake data
        Log.d(LOG_TAG, "onLoadFinished");
        findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
        mEmptyStateTextView.setText(R.string.no_earthquakes);
        mAdapter.clear();
        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
        mAdapter.clear();
    }
}
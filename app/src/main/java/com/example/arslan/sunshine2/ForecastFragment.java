package com.example.arslan.sunshine2;

import android.app.Activity;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

public   ArrayAdapter<String> mForecastAdapter;
   private ListView listView;
    private Activity activity;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String [] forecastArray =
                {
                        "Today-sunny-89/3",
                      /*  "Today-sunny-89/3",
                        "Today-sunny-89/3",
                        "Today-sunny-89/3"
*/
                };
        List<String> weekForecast = new ArrayList<String>(
                Arrays.asList(forecastArray)

        );

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

         listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem =  mForecastAdapter.getItem(position);
                Toast.makeText(getActivity(),selectedItem,Toast.LENGTH_SHORT).show();
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id  = item.getItemId();
        if (id == R.id.action_refresh){
            FetchWeatherTask  weatherTask = new FetchWeatherTask(this.activity);
            weatherTask.execute("94043");

            return  true;
        }
        return super.onOptionsItemSelected(item);

    }

    public class FetchWeatherTask extends AsyncTask<String, Void, List<String>> {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private final Activity activity;

        FetchWeatherTask(Activity activity){

            this.activity = activity;
        }



        @Override
        protected List<String> /*String[]*/ doInBackground(String... params) {

            if (params.length == 0) {

                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            String key = "86f6394831d1ecd53dc9e8f2cb954b93";
            int numDays = 7;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APP_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APP_PARAM, key)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, forecastJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

                JSONHELPER jsonhelper = new JSONHELPER();

          List<String> result  = new ArrayList<String>();

            try {
               result = Arrays.asList(jsonhelper.getWeatherDataFromJson(forecastJsonStr, 10));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ForecastFragment", "json exception", e.fillInStackTrace());
            }
           return result;

        }

        @Override
        protected void onPostExecute(List<String> result) {
            Log.v(LOG_TAG, "onpost execute");
            Log.v(LOG_TAG, result.toString());
            if (result != null){
             mForecastAdapter.clear();


                for (String dayForecastStr:result ){

                    if (dayForecastStr!=null)
                    mForecastAdapter.add(dayForecastStr);
                }

                mForecastAdapter.notifyDataSetChanged();

                listView.setAdapter(mForecastAdapter);

            }
        }

    }
}

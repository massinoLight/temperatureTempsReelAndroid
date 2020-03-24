package com.example.temperaturetempsreel;


import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import android.app.Activity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private static final Random RANDOM = new Random();
    private static final String TAG = "MyActivity";
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    GraphView graph ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // we get graph view instance
        graph = (GraphView) findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<DataPoint>();
        series.setTitle("Température en temps reel");
        series.setColor(Color.GREEN);


        if (!internet())
        {
            Toast.makeText(getApplicationContext(), "pas d'inernet donc pas de données", Toast.LENGTH_SHORT).show();
        }
        else{
            runThread();

        }

    }


/***
 * creation d un thread qui va déssiner le graphe
 * */

    private void runThread() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 100 new entries
                while (true) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            downloadJSON("http://192.168.0.12/temperature/result.php");
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }

    /*****
     * ajouter les données recu par la bdd
     * et qui son conténu dans le fichier JSON
     * au graph
     * ****/
    private void addEntry(String json) throws JSONException{

        double y;
        JSONArray jsonArray = new JSONArray(json);
        String[] stocks = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            stocks[i] = obj.getString("VALEUR");
            Log.e(TAG, "MyClass.getView() — la valeur  " + stocks[i]);

            y = Double.valueOf(stocks[i]);
            //series.appendData(new DataPoint(x, y), true, 100);
            series.appendData(new DataPoint(lastX++,y), true, 50);

            }
        graph.addSeries(series);

    }

    /**
     * fonction qui check si internet il y a ou pas
     * **/

    private boolean internet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /*****
     * fonction qui va s executer en arriére plan et recupérer
     * les valeurs de la bdd sous format JSON
     * elles seront ajouté au graphe en fesant appélle a la fonction addEntry
     * **/

    private void downloadJSON(final String urlWebService) {

        class DownloadJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    addEntry(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
        DownloadJSON getJSON = new DownloadJSON();
        getJSON.execute();
    }


}
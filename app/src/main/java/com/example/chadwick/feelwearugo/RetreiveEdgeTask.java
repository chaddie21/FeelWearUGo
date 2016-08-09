package com.example.chadwick.feelwearugo;

/**
 * Created by Photonovation on 08/05/2016.
 */
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RetreiveEdgeTask extends AsyncTask <String, String, Boolean>{
    UWIMap uwiMap = UWIMap.getUWIMap();


@Override
protected Boolean doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        boolean status = false;

        try {
        URL url = new URL(params[0]);
        connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream inputStream= connection.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer  = new StringBuffer();
        String line= "";
        String finalJson = buffer.toString();
        while ((line= reader.readLine())!= null){
        buffer.append(line);
        }

        JSONObject parentObject_Edge = new JSONObject(finalJson);
        JSONArray parentArray = parentObject_Edge.getJSONArray("edges");
        List<Edge> edgeList = new ArrayList<>();

        for (int i=0; i< parentArray.length(); i++){
            JSONObject object = parentArray.getJSONObject(i);
            int edge_id = object.getInt("0");
            int start_node_id = object.getInt("1");
            int end_node_id  = object.getInt("2");
            double length = object.getDouble("3");
            boolean isRoad = object.getBoolean("4");
            boolean isPed = object.getBoolean("5");
            boolean hasStreetConnect = object.getBoolean("6");
            status = uwiMap.addEdgeToMap(new Edge(edge_id, start_node_id, end_node_id, length, isRoad, isPed, hasStreetConnect));

        }
        return status;

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
            return false;
}

@Override
protected void onPreExecute() {
        super.onPreExecute();
        }

@Override
protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        }

@Override
protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        }



@Override
protected void onCancelled() {
        super.onCancelled();
        }
}

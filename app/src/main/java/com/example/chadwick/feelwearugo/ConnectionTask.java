package com.example.chadwick.feelwearugo;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Photonovation on 07/12/2016.
 */
public class ConnectionTask extends AsyncTask<String, String, List<Node> >{
    private ProgressDialog dialog;
    private final String URL_TO_HIT = "http://104.155.128.82";
    private String ROUTE_URL = "http://104.155.128.82/ids/";

    List<Node> pathList = null;



    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        //dialog.show();
    }

    @Override
    protected List<Node> doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader= null;


        try{

            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream =connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line= "";

            while ((line=reader.readLine()) != null){
                buffer.append(line);
            }

            String finalJson = buffer.toString();

            JSONObject parentObject = new JSONObject(finalJson);
            JSONArray parentArray = parentObject.getJSONArray("nodes");
            List<Node> pathList2 = new ArrayList<>();



            Log.d("parentArray", parentArray.toString());

            Gson gson = new Gson();

            //TODO: Error along here
            for (int i=0; i< parentArray.length(); i++){
                JSONObject object = parentArray.getJSONObject(i);

//                Log.d("Object", object.toString());



                //Node node = gson.fromJson(object.toString(), Node.class);
                //pathList2.add(node);
                //Node node = null;
                int node_id = object.getInt("0");
                String name = object.getString("1");
                double latitude = object.getDouble("2");
                double longitude = object.getDouble("3");
                Node node = new Node(node_id, name, latitude, longitude);
                pathList2.add(node);

                Log.d("doInBackground", node.toString());
            }

//            Log.d("doInBackground", pathList2.toString());

            return pathList2;

        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        finally{
            if (connection != null){
                connection.disconnect();
            }
            try{
                if(reader != null){
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(final List<Node> result){
        super.onPostExecute(result);
        //dialog.dismiss();

         try {
             Log.d("onPostExecute", result.toString());
             createPathList2(result);


             //_____________TEMPORARY SOLUTION for MGI demo______________


             PathFinder pathFinder = new PathFinder();
             float[] bearings = pathFinder.getPathBearingTemp(result);
             float[] nextTurn = pathFinder.setNextTurnAngles(bearings);
             pathFinder.setNextTurnInstructionTemp(nextTurn, result);

             Log.d("Node location", pathFinder.getLats(result));


         }catch (NullPointerException e){
             e.printStackTrace();
         }
    }

    public List<Node> getPathList2(){
        return pathList;
    }

    private void createPathList2(List<Node> path){
        pathList = path;
    }




}

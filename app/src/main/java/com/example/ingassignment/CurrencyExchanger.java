package com.example.ingassignment;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Jean on 2017/3/31.
 */

public class CurrencyExchanger extends Thread {
    private final String LOG_TAG = this.getClass().getName();
    private static CurrencyExchanger instance;
    private HashMap<String, Float> currencyMap = new HashMap<String, Float>();
    private boolean initial = false;

    public static CurrencyExchanger getInstance(){
        if(instance == null) instance = new CurrencyExchanger();
        return instance;
    }

    public boolean isInitial(){ return initial;}

    public void run(){
        initial= true;
        //Log.i(LOG_TAG, "ThreadInfo:" +Thread.currentThread());
        InputStream inputStream = null;
        String result = "";
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        HttpConnectionParams.setSoTimeout(httpParams, 7000);//For socket
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        try {
            HttpResponse httpResponse = httpClient.execute(new HttpGet("http://api.fixer.io/latest"));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null) result = convertInputStreamToString(inputStream);
            //Log.i(LOG_TAG, "["+Thread.currentThread()+"] "+result);
            convertStringToJsonObject(result);
        } catch(java.io.IOException ioe){;
            ioe.printStackTrace();
        } finally {
            //Log.i(LOG_TAG,"Disconnect");
            httpClient.getConnectionManager().shutdown();
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "", result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private void convertStringToJsonObject(String str) {
        JSONObject json = null;
        if (str == null || str.length() == 0){
            run();
            return;
        }
        try {
            json = new JSONObject(new JSONObject(str).getString("rates"));
            Iterator<String> keys = json.keys();
            currencyMap.clear();
            while(keys.hasNext()){
                String key = (String)keys.next();
                double value = json.getDouble(key);
                currencyMap.put(key, (float)value);
                //Log.i(LOG_TAG,"currencyMap("+currencyMap.size()+"):"+key+"("+value+")");
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public HashMap<String, Float> getCurrencyMap(){
        //Log.i(LOG_TAG,"getCurrencyMap:"+ currencyMap.size());
        return currencyMap;
    }

}

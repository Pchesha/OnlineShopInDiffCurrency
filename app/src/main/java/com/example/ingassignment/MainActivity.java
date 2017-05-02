package com.example.ingassignment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = this.getClass().getName();
    final int REQUEST_CART_RESPONSE = 100;

    public final static int Product_BaseNumber = 10001;
    public final static String Intent_Data_Cart_Detail = "data_check_cart";
    public final static String Intent_Data_Currency_Rate = "data_currency_rate";
    public final static String Action_Cart_Changed = "action_notify_cart_changed";
    public final static String Intent_Data_ProductId = "product_id";

    private Context mContext;
    private CartChangedReceiver cartChangedReceiver = new CartChangedReceiver();
    private IntentFilter cartFilter = new IntentFilter(Action_Cart_Changed);
    private IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    Resources res;
    private ListView listView;
    List<ProductItem> product_list = new ArrayList<ProductItem>();
    private ProductListAdapter adapter;
    HashMap<Integer,Integer> cartMap = new HashMap<Integer,Integer>();//ProductId, Quantity; Use to record Cart data
    List<ProductItem> cart_list = new ArrayList<ProductItem>();// Use to transfer data to the other activity.
    private HashMap<String,Float> currencyMap = new HashMap<String,Float>();//Currency rate list.
    private MenuItem cartBtn;
    private int totalItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        res = mContext.getResources();
        actCurrencyRateLoading();

        String[] list = res.getStringArray(R.array.productList);
        String[] priceList = res.getStringArray(R.array.productPriceList);
        for(int i=0; i<list.length; i++){
            ProductItem item = new ProductItem(i+Product_BaseNumber, list[i], priceList[i]);
            product_list.add(item);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarText = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        toolbar.setTitle(R.string.mainActivity_name);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.product_listview);
        adapter = new ProductListAdapter(mContext, product_list);
        listView.setAdapter(adapter);
        registerReceiver(cartChangedReceiver, cartFilter);
        registerReceiver(cartChangedReceiver, networkFilter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(cartChangedReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(cart_list.size()>0) outState.putParcelableArrayList("cartList",(ArrayList<ProductItem>) cart_list);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        cart_list = savedInstanceState.getParcelableArrayList("cartList");
        if(cart_list != null) syncCartMapFromArrList(cart_list);
    }

    private void actCurrencyRateLoading(){
        if(currencyMap.size()>0) return;
        if(!isNetworkAvailable())
            Toast.makeText(mContext, mContext.getResources().getText(R.string.notify_network_unavailable), Toast.LENGTH_SHORT).show();
        else
            new CurrencyDownloadTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        cartBtn = menu.getItem(0);
        updateTotalOfCart();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.go_cart_button:
                Intent intent = new Intent(mContext, CartDetailActivity.class);
                intent.putParcelableArrayListExtra(Intent_Data_Cart_Detail, (ArrayList<ProductItem>) cart_list);
                intent.putExtra(Intent_Data_Currency_Rate, currencyMap);
                    startActivityForResult(intent, REQUEST_CART_RESPONSE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CART_RESPONSE:
                if (data == null || data.getExtras() == null) break;
                List<ProductItem> list = data.getParcelableArrayListExtra(Intent_Data_Cart_Detail);
                if (list != null) syncCartMapFromArrList(list);
                    cart_list = list;
                    updateTotalOfCart();
                break;
        }
    }

    /**
     * get hashMap result from list, after screen rotation.
     * @param list
     */
    // Todo: should work on background thread.
    private void syncCartMapFromArrList(List list){
            cartMap.clear();
            totalItem = 0;
            for(Iterator it = list.iterator(); it.hasNext(); ) {
                ProductItem item = (ProductItem) it.next();
                cartMap.put(item.getProductId(), item.getQuantity());
                totalItem += item.getQuantity();
            }
        }

    /**
     * Update list from map.
     */
    // Todo: should work on background thread.
    private void arrangeCartList(){
            cart_list.clear();
            totalItem = 0;
            for(int key : cartMap.keySet()) {
                try {
                    ProductItem item = product_list.get(key - Product_BaseNumber);
                    item = item.clone();
                    item.setQuantity(cartMap.get(key));
                    totalItem += cartMap.get(key);
                    cart_list.add(item);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

    private void updateTotalOfCart(){
        cartBtn.setTitle("("+totalItem+")");
    }

    public class CartChangedReceiver extends BroadcastReceiver {
        private String LOG_TAG = "CartChangedReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, Thread.currentThread()+"onReceive: "+intent.getAction());
            if (intent.getAction().equals(Action_Cart_Changed)) {
                int productId = intent.getIntExtra(Intent_Data_ProductId, -1);
                if(productId == -1) return;
                if(cartMap.containsKey(productId)){
                    cartMap.put(productId,cartMap.get(productId)+1);
                } else cartMap.put(productId, 1);
                arrangeCartList();
                updateTotalOfCart();
            } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                actCurrencyRateLoading();
            }
        }
    }

    private boolean isNetworkAvailable() {
        if(mContext == null) return false;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class CurrencyDownloadTask extends AsyncTask<Void, Void, HashMap> {
        protected HashMap doInBackground(Void... urls) {
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
            return null;
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
            if (str == null || str.length() == 0) return;
            try {
                json = new JSONObject(new JSONObject(str).getString("rates"));
                Iterator<String> keys = json.keys();
                if(currencyMap.size()>0) currencyMap.clear();
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

        protected void onProgressUpdate(HashMap... progress) {
        }

        protected void onPostExecute(HashMap result) {
        }
    }
}

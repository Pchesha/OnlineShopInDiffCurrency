package com.example.ingassignment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = this.getClass().getName();
    final int REQUEST_CART_RESPONSE = 100;

    public final static int Product_BaseNumber = 10001;
    public final static String Intent_Data_Cart_detail = "action_check_cart";
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
        if(!isNetworkAvailable()) Toast.makeText(mContext, mContext.getResources().getText(R.string.notify_network_unavailable)
                ,Toast.LENGTH_SHORT).show();
        else{
            if(!CurrencyExchanger.getInstance().isInitial()) CurrencyExchanger.getInstance().start();
        }
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
                synchronized (cartMap) {
                    intent.putParcelableArrayListExtra(Intent_Data_Cart_detail, (ArrayList<ProductItem>) cart_list);
                    startActivityForResult(intent, REQUEST_CART_RESPONSE);
                }
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
                List<ProductItem> list = data.getParcelableArrayListExtra(Intent_Data_Cart_detail);
                if (list != null) {
                    synchronized (cartMap) {
                        syncCartMapFromArrList(list);
                    }
                    cart_list = list;
                    updateTotalOfCart();
                }
                break;
        }
    }

    private void syncCartMapFromArrList(List list){
        synchronized(cartMap) {
            cartMap.clear();
            totalItem = 0;
            for(Iterator it = list.iterator(); it.hasNext(); ) {
                ProductItem item = (ProductItem) it.next();
                cartMap.put(item.getProductId(), item.getQuantity());
                totalItem += item.getQuantity();
            }
        }
    }

    private void arrangeCartList(){
        synchronized (cartMap) {
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
    }

    private void updateTotalOfCart(){
        cartBtn.setTitle("("+totalItem+")");
    }

    public class CartChangedReceiver extends BroadcastReceiver {
        private String LOG_TAG = "CartChangedReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "onReceive: "+intent.getAction());
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
}

package com.example.ingassignment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CartDetailActivity extends AppCompatActivity {
    private final String LOG_TAG = this.getClass().getName();

    private List<ProductItem> cart_list;
    CartListAdapter adapter;
    private ListView listView;
    private CartModifiedReceiver receiver = new CartModifiedReceiver();
    private IntentFilter filter = new IntentFilter(MainActivity.Action_Cart_Changed);
    TextView totalPrice, currencyTextView;
    Spinner currencySelection;
    HashMap<String, Float> currencyMap;
    String lastCurrency;
    String[] currencyCategeory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView barTitle = (TextView) findViewById(R.id.toolbar_title);
        barTitle.setText(R.string.cart_detail_label);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        if(intent != null) {
            cart_list = intent.getParcelableArrayListExtra(MainActivity.Intent_Data_Cart_Detail);
            currencyMap = (HashMap<String,Float>)intent.getSerializableExtra(MainActivity.Intent_Data_Currency_Rate);
        }
        adapter = new CartListAdapter(this.getApplicationContext(),cart_list);
        listView = (ListView) findViewById(R.id.product_listview);
        listView.setAdapter(adapter);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        lastCurrency = this.getResources().getString(R.string.default_currency);
        totalPrice = (TextView) findViewById(R.id.totalPrice);
        totalPrice.setText(Float.toString(calculateTotal(lastCurrency)));
        currencyTextView = (TextView) findViewById(R.id.currency_cate);
        currencySelection = (Spinner) findViewById(R.id.currency_cate_spinner);
        currencyCategeory = convertCurrencyCateToArr();
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<String>(CartDetailActivity.this,
                android.R.layout.simple_spinner_item,
                currencyCategeory);
        currencySelection.setAdapter(spinnerArrayAdapter);
        // According to localised country to display default currency.
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();
        Log.i(LOG_TAG, "localised :"+countryCode);
        if(!countryCode.isEmpty()){
            for(int i=0; i<currencyCategeory.length;i++){
                if(countryCode.toUpperCase().equals(currencyCategeory[i].substring(0,2))){
                    // Todo: how to more efficient to find locale is existed in map.
                    currencySelection.setSelection(i);
                    lastCurrency=currencyCategeory[i];
                    break;
                }
            }
        }
        currencySelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                lastCurrency = currencyCategeory[adapterView.getSelectedItemPosition()];
                totalPrice.setText(Float.toString(calculateTotal(lastCurrency)));
                currencyTextView.setText(lastCurrency);
            }
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Button checkBtn = (Button) findViewById(R.id.btn_checkout);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartDetailActivity.this);
                alertDialog.setTitle("Go to pay!");
                alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        operationBeforeDead();
        super.onBackPressed();
        finish();
    }

    private float calculateTotal(String currency){
        float unitPrice=0, total=0, rate = (float)1.0;
        int quantity=0;
        if(!currency.equals("EUR")) rate = currencyMap.get(currency);
        for(Iterator it = cart_list.iterator(); it.hasNext();){
            ProductItem item = (ProductItem) it.next();
            unitPrice = item.getUnitPrice();
            quantity = item.getQuantity();
            if(unitPrice>0 & quantity>0) total+=(unitPrice*quantity);
        }
        return (total*rate);
    }

    private String[] convertCurrencyCateToArr(){
        if(currencyMap == null || currencyMap.size()==0) return new String[]{this.getString(R.string.default_currency)};
        String[] currencyCate = new String[currencyMap.size()+1];
        currencyCate[0] = this.getString(R.string.default_currency);
        int index=1;
        for(String key : currencyMap.keySet()) {
            currencyCate[index] = key;
            index++;
        }
        return currencyCate;
    }

    private void operationBeforeDead(){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MainActivity.Intent_Data_Cart_Detail, (ArrayList<ProductItem>)cart_list);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }


    public class CartModifiedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("CartModifiedReceiver","onReceive:"+intent.getAction());
            if (intent.getAction().equals(MainActivity.Action_Cart_Changed)) {
                totalPrice.setText(Float.toString(calculateTotal(lastCurrency)));
                currencyTextView.setText(lastCurrency);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
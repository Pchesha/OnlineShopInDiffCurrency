package com.example.ingassignment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Jean on 2017/3/31.
 */

public class ProductListAdapter extends BaseAdapter {

    final String Product_Name_Field = "Product: ";
    final String Product_UnitPrice_Field = "Price: EUR ";

    protected Context mContext;
    protected LayoutInflater myInflater;
    private ViewHolder holder = null;
    protected List<ProductItem> list;

    public ProductListAdapter(Context context, List<ProductItem> list){
        this.mContext = context;
        this.myInflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    @Override
    public long getItemId(int position) {
        return list.indexOf(getItem(position));
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public ProductItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.item_in_stock, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ProductListAdapter.ViewHolder) convertView.getTag();
        }
        synchronized(this.list) {
            final ProductItem item = (ProductItem) getItem(position);
            if (item == null) return convertView;
            holder.mProductImg.setImageResource(item.getproductImgId());
            holder.mProductName.setText(Product_Name_Field + item.getProductName());
            holder.mProductPrice.setText(Product_UnitPrice_Field + Float.toString(item.getUnitPrice()));
            holder.addToCart.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.Action_Cart_Changed);
                    intent.putExtra(MainActivity.Intent_Data_ProductId,item.getProductId());
                    mContext.sendBroadcast(intent);
                    Toast.makeText(mContext, mContext.getResources().getText(R.string.addToCart),Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;
        }
    }

    public class ViewHolder{
        ImageView mProductImg;
        TextView mProductName;
        TextView mProductPrice;
        Button addToCart;

        public ViewHolder(View view) {
            mProductImg = (ImageView) view.findViewById(R.id.productImg);
            mProductName = (TextView) view.findViewById(R.id.productNameId);
            mProductPrice = (TextView) view.findViewById(R.id.productPriceId);
            addToCart = (Button) view.findViewById(R.id.add_to_cart_button);
        }
    }
}

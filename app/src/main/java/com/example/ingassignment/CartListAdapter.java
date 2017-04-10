package com.example.ingassignment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jean on 2017/3/31.
 */

public class CartListAdapter extends ProductListAdapter {
    final int MAX_QUANTITY = 99;
    final int MIN_QUANTITY = 1;

    private CartListAdapter.ViewHolder holder;

    public CartListAdapter(Context context, List<ProductItem> list){
        super(context,list);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.item_in_cart, null);
            holder = new CartListAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (CartListAdapter.ViewHolder) convertView.getTag();
        }
        synchronized(this.list) {
            final ProductItem item = (ProductItem) getItem(position);
            if (item == null) return convertView;
            holder.mProductImg.setImageResource(item.getproductImgId());
            holder.mProductName.setText(item.getProductName());
            holder.mQuantity.setText(Integer.toString(item.getQuantity()));

            holder.btn_increase.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int curentQuantity = item.getQuantity();
                    if(curentQuantity >= MAX_QUANTITY) holder.btn_increase.setClickable(false);
                    else {
                        holder.btn_increase.setClickable(true);
                        list.get(list.indexOf(item)).setQuantity(curentQuantity+1);
                        Intent intent = new Intent(MainActivity.Action_Cart_Changed);
                        intent.putExtra(MainActivity.Intent_Data_ProductId,item.getProductId());
                        mContext.sendBroadcast(intent);
                    }
                }
            });
            holder.btn_decrease.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int curentQuantity = item.getQuantity();
                    if(curentQuantity <= MIN_QUANTITY) holder.btn_decrease.setClickable(false);
                    else {
                        holder.btn_decrease.setClickable(true);
                        list.get(list.indexOf(item)).setQuantity(curentQuantity-1);
                        Intent intent = new Intent(MainActivity.Action_Cart_Changed);
                        intent.putExtra(MainActivity.Intent_Data_ProductId,item.getProductId());
                        mContext.sendBroadcast(intent);
                    }

                }
            });

            holder.removeFromCart.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    list.remove(item);
                    Intent intent = new Intent(MainActivity.Action_Cart_Changed);
                    mContext.sendBroadcast(intent);
                }
            });
            return convertView;
        }
    }

    public class ViewHolder extends ProductListAdapter.ViewHolder{
        TextView mQuantity;
        Button removeFromCart;
        Button btn_increase;
        Button btn_decrease;

        public ViewHolder(View view) {
            super(view);
            mProductImg = (ImageView) view.findViewById(R.id.productImgInCart);
            mProductName = (TextView) view.findViewById(R.id.descrition_in_cartDetail);
            mQuantity = (TextView) view.findViewById(R.id.text_quantity);
            btn_increase = (Button) view.findViewById(R.id.btn_quan_increase);
            btn_decrease = (Button) view.findViewById(R.id.btn_quan_decrease);
            removeFromCart = (Button) view.findViewById(R.id.remove_item_button);
        }
    }
}

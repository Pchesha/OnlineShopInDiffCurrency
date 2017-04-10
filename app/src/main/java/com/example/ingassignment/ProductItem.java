package com.example.ingassignment;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by Jean on 2017/3/31.
 */

public class ProductItem implements Parcelable{
    private int productId;
    private String productName;
    private float unitPrice;
    private int quantity;
    private int productImgId;

    // For Product List
    public ProductItem(int productId, String productName, String uPrice){
        this.productId= productId;
        this.productName = productName;
        this.quantity = 0;
        this.unitPrice = Float.parseFloat(uPrice);
        setupProductImg();
    }
    // for Cart
    public ProductItem(int productId, String productName, float uPrice, int quantity, int productImgId){
        this.productId= productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = uPrice;
        this.productImgId = productImgId;
    }

    private void setupProductImg(){
        switch (this.productId-MainActivity.Product_BaseNumber){
            case 0:
                productImgId = R.drawable.peas;
                break;
            case 1:
                productImgId = R.drawable.eggs;
                break;
            case 2:
                productImgId = R.drawable.beans;
                break;
            case 3:
                productImgId = R.drawable.milk;
                break;
            default:
                productImgId = -1;
                break;
        }
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof ProductItem)
            if (((ProductItem) obj).getProductId() == this.productId) return true;
        return false;
    }

    @Override
    // Could copy item form product list to cart.
    public ProductItem clone(){
        ProductItem item = new ProductItem(this.getProductId(), this.getProductName(),this.getUnitPrice(),1,this.getproductImgId());
        return item;
    }

    public int getproductImgId(){
        return this.productImgId;
    }
    public int getProductId(){
        return this.productId;
    }
    public String getProductName(){
        return this.productName;
    }
    public float getUnitPrice() {
        return this.unitPrice;
    }
    public int getQuantity(){
        return this.quantity;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.productId);
        dest.writeString(this.productName);
        dest.writeFloat(this.unitPrice);
        dest.writeInt(this.quantity);
        dest.writeInt(this.productImgId);
    }

    public static final Parcelable.Creator<ProductItem> CREATOR = new Parcelable.Creator<ProductItem>() {
        @Override
        public ProductItem createFromParcel(Parcel source) {
            return new ProductItem(source.readInt(), source.readString(), source.readFloat(), source.readInt(),source.readInt());
        }

        @Override
        public ProductItem[] newArray(int size) {
            return new ProductItem[size];
        }
    };
}

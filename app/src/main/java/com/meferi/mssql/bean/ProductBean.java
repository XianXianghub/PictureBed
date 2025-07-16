package com.meferi.mssql.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductBean implements Parcelable {
    private String name;
    private String price;
    private String barcode;
    private String dbinfo;

    public String getDbinfo() {
        return dbinfo;
    }

    public void setDbinfo(String dbinfo) {
        this.dbinfo = dbinfo;
    }

    protected ProductBean(Parcel in) {
        name = in.readString();
        price = in.readString();
        barcode = in.readString();
        dbinfo = in.readString();
        priceunt = in.readString();
        img = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(barcode);
        dest.writeString(dbinfo);
        dest.writeString(priceunt);
        dest.writeString(img);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductBean> CREATOR = new Creator<ProductBean>() {
        @Override
        public ProductBean createFromParcel(Parcel in) {
            return new ProductBean(in);
        }

        @Override
        public ProductBean[] newArray(int size) {
            return new ProductBean[size];
        }
    };

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }



    private String priceunt;
    private String img;

    public ProductBean() {
        // 默认构造函数
    }

    // Getters 和 Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceUnit() {
        return priceunt;
    }

    public void setPriceunt(String priceunt) {
        this.priceunt = priceunt;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
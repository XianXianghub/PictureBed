package com.meferi.mssql;

public class Product {
    private String name;
    private String price;
    private String barcode;

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", barcode='" + barcode + '\'' +
                ", priceunt='" + priceunt + '\'' +
                ", img='" + img + '\'' +
                '}';
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }



    private String priceunt;
    private String img;

    public Product() {
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

    public String getPriceunt() {
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
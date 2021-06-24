package com.example.myapplication;

import android.graphics.Bitmap;

public class BitmapPixels {
    private Bitmap bitmap;
    private int width;
    private int height;
    private int[] pixels;

    public BitmapPixels(Bitmap bitmap){
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        setPixels();
    }

    private void setPixels(){
        this.pixels = new int[this.width*this.height];
        this.bitmap.getPixels(this.pixels,0, this.width,0,0, this.width, this.height);
    }

    public int getPixel(int x,int y){
        return pixels[x+y*this.width];
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    public Bitmap getBitmap(){
        return this.bitmap;
    }

}

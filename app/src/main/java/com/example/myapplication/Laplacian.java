package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.PrimitiveIterator;

import static com.example.myapplication.Utils.getResizedBitmap;

public class Laplacian {
    private int targetWidth = 600;  // just for an example we can change it but this affects to treshold
    private int targetHeight = 800; // just for an example we can change it but this affects to treshold
    private float treshold = 200.0f; // treshold will be adjusted after detailed testing
    private BitmapPixels bitmapPixels;  // This is representation of Bitmap with overwritten setPixel and getPixel methods
    private double variance;
    private int[] filter= {
            -1, -1, -1,
            -1, 8, -1,
            -1, -1, -1 }; // there are two types of filters. another is: [0, -1, 0,
                                                                      //  -1, 4, -1,
                                                                      //  0, -1, 0]
    private ImageQuality imageQuality;
    public enum ImageQuality {
        BLURRED,
        SHARP,
    }

    public Laplacian(Bitmap bitmap){
        this.bitmapPixels = new BitmapPixels(getResizedBitmap(bitmap, this.targetWidth, this.targetHeight));
        this.variance = calculateScore();
        this.imageQuality = setImageQuality();
    }

    public double getVariance() {
        return variance;
    }

    private ImageQuality setImageQuality(){
        if(variance < this.treshold){
            return ImageQuality.BLURRED;
        } else {
            return ImageQuality.SHARP;
        }

    }

    public ImageQuality getImageQuality() {
        return imageQuality;
    }

    private double calculateScore(){
        // detect edges with use of laplacian filter
        int[] lap_pixels = apply_Mask(this.filter, this.bitmapPixels);

        // calculate variance based on tranformed image by laplacian kernel
        double variance = variance(lap_pixels, this.targetWidth, this.targetHeight);

        return variance;
    }

    public int[] apply_Mask(int[] mask, BitmapPixels bitmapPixels) {
        int c;
        Bitmap preview = bitmapPixels.getBitmap().copy(bitmapPixels.getBitmap().getConfig(), true);

        int r=0;
        int g=0;
        int b=0;

        int weight=0;
        int weightSum=0;
        int counter=0;

        int width = bitmapPixels.getWidth();
        int height = bitmapPixels.getHeight();
        int x = width-1;
        int y = height-1;
        int index = 0;
        int[] pixels = new int[width*height];

        for (int i = 0; i < mask.length; i++) {
            weightSum += (int)mask[i];
        }

        for (int i = 0; i < width-1; i++)
        {
            for (int j = 0; j < height-1; j++)
            {
                r = g = b = 0;
                counter = 0;

                for (int o = i - 1; o <= i + 1; o++)
                {
                    for (int p = j - 1; p <= j + 1; p++)
                    {
                        if ((o == -1 && (p == -1 || p == y)) || (o == x && (p == -1 || p == y)))
                            c = bitmapPixels.getPixel(i, j);
                        else if (o == -1 || o == x)
                            c = bitmapPixels.getPixel(i, p);
                        else if (p == -1 || p == y)
                            c = bitmapPixels.getPixel(o, j);
                        else
                            c = bitmapPixels.getPixel(o, p);

                        weight = (int)mask[counter];

                        r += (Color.red(c)* weight);
                        b += (Color.blue(c) * weight);
                        g += (Color.green(c) * weight);

                        counter++;
                    }
                }

                if (weightSum == 0)
                    weightSum = 1;
                r = r / weightSum;
                b = b / weightSum;
                g = g / weightSum;

                if (r < 0)
                    r = 0;
                if (r > 255)
                    r = 255;

                if (b < 0)
                    b = 0;
                if (b > 255)
                    b = 255;

                if (g < 0)
                    g = 0;
                if (g > 255)
                    g = 255;

                pixels[index] = Color.rgb(r, g, b);
                index+=1;
            }

        }

        preview.setPixels(pixels, 0, x, 0,0, x, y);

        return pixels;
    }

    private static double variance(int[] pixels, int width, int height){

        int x = width-1;
        int y = height-1;
        double mean = mean(pixels, width, height);
        double sum = 0;
        double[][] tmp_img= new double[x][y];
        int pix;

        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                pix = pixels[i+j*width];
                // get value of the pixel
                tmp_img[i][j] = (int)(Color.red(pix) + Color.blue(pix) + Color.green(pix))/3;
                // substract mean
                tmp_img[i][j] -= mean;
                // square each term
                tmp_img[i][j] *= tmp_img[i][j];
            }
        }

        for (int i = 0; i < x; i++)
            for (int j = 0; j < y; j++)
                sum += tmp_img[i][j];



        return sum / (x * y);
    }

    private static double mean(int[] pixels, int width, int height){

        int sum = 0;
        int pix;
        int tmp;
        double mean;
        int x = width-1;
        int y = height-1;

        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                pix = pixels[i+j*width];
                tmp = Color.red(pix) + Color.blue(pix) + Color.green(pix);
                sum += (int)(tmp/3);
            }
        }
        mean = sum/(width * height);

        return mean;
    }

}


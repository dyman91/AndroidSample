package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Pair;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

public class Drawing {
    private static final float TEXT_SIZE = 50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final float TEXT_MARGIN = 5.0f;

    public static Bitmap drawBoundary(Face face, Bitmap bitmap){
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5.0f);
        p.setColor(Color.WHITE);
        float x = face.getBoundingBox().centerX();
        float y = face.getBoundingBox().centerY();
        float xOffset = face.getBoundingBox().width() / 2.0f;
        float yOffset = face.getBoundingBox().height() / 2.0f;
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, p);

        return bitmap;
    }

    public static Bitmap drawContours(Face face, Bitmap bitmap){
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5.0f);
        p.setColor(Color.WHITE);
        for (FaceContour contour : face.getAllContours()) {
            for (PointF point : contour.getPoints()) {
                canvas.drawCircle(
                        point.x, point.y, 8.0f, p);
            }
        }
        return bitmap;
    }

    public static Bitmap drawHeadOrientation(float eulerX, float eulerY, float eulerZ, Pair<String, Integer> headOrientation, Bitmap bitmap){
        Canvas canvas = new Canvas(bitmap);
        Paint rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(BOX_STROKE_WIDTH);


        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(TEXT_SIZE);  //set text size
        float textSize = textPaint.getTextSize();
        float lineHeight = TEXT_SIZE + BOX_STROKE_WIDTH;

        String eulerXMsg = String.format("EulerX: %.2f", eulerX);
        String eulerYMsg = String.format("EulerY: %.2f", eulerY);
        String eulerZMsg = String.format("EulerZ: %.2f", eulerZ);
        String orientation = "Orientacja: " + headOrientation.first;

        if(headOrientation.second != 2){
            rectPaint.setColor(Color.RED);
        } else {
            rectPaint.setColor(Color.GREEN);
        }

        float textWidth = 0.0f;
        textWidth = Math.max(textWidth,textPaint.measureText(eulerXMsg));
        textWidth = Math.max(textWidth,textPaint.measureText(eulerYMsg));
        textWidth = Math.max(textWidth,textPaint.measureText(eulerZMsg));
        textWidth = Math.max(textWidth,textPaint.measureText(orientation));

        canvas.drawRect(300 - TEXT_MARGIN, 300 - textSize*4, 300 + textWidth + TEXT_MARGIN, 300, rectPaint);
        canvas.drawText(eulerXMsg, 300, 300,textPaint);
        canvas.drawText(eulerYMsg, 300, 300 - lineHeight, textPaint);
        canvas.drawText(eulerZMsg, 300, 300 - lineHeight*2, textPaint);
        canvas.drawText(orientation, 300, 300 - lineHeight*3, textPaint);

        return bitmap;
    }

    public static Bitmap drawLaplacianVariance(double variance, Laplacian.ImageQuality imageQuality, Bitmap bitmap){
        Canvas canvas = new Canvas(bitmap);
        Paint rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(BOX_STROKE_WIDTH);


        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(TEXT_SIZE);  //set text size
        float textSize = textPaint.getTextSize();
        float lineHeight = TEXT_SIZE + BOX_STROKE_WIDTH;

        String varianceMsg = String.format("Wariancja: %.2f", variance);
        String imageQualityMsg;

        if(imageQuality.equals(Laplacian.ImageQuality.SHARP)){
            imageQualityMsg = "Zdjęcie jest ostre";
            rectPaint.setColor(Color.GREEN);
        } else {
            imageQualityMsg = "Zdjęcie jest rozmyte";
            rectPaint.setColor(Color.RED);
        }

        float textWidth = 0.0f;
        textWidth = Math.max(textWidth,textPaint.measureText(varianceMsg));
        textWidth = Math.max(textWidth,textPaint.measureText(imageQualityMsg));

        canvas.drawRect(1200 - TEXT_MARGIN, 300 - textSize*2, 1200 + textWidth + TEXT_MARGIN, 300, rectPaint);
        canvas.drawText(varianceMsg, 1200, 300,textPaint);
        canvas.drawText(imageQualityMsg, 1200, 300 - lineHeight, textPaint);

        return bitmap;
    }

}

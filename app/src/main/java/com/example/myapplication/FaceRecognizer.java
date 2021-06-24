package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Pair;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FaceRecognizer {
    protected FaceDetector faceDetector;

    public FaceRecognizer(){
        this.faceDetector = setFaceDetector();
    }

    private FaceDetector setFaceDetector(){
        FaceDetectorOptions opts =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        return FaceDetection.getClient(opts);
    }

    public Pair<String, Integer> estimateFacePosition(float x, float y){

        if (x >= 15.0F){
            return new Pair<>("Za bardzo do góry!", 1);
        }
        if (x <= -9.0F){
            return new Pair<>("Za bardzo w dół!", 1);
        }

        if (y >= 15.0F){
            return new Pair<>("Za bardzo w prawo!", 1);
        }
        if (y <= -15.0F){
            return new Pair<>("Za bardzo w lewo!", 1);
        }

        return new Pair<>("OK", 2);

    }
}

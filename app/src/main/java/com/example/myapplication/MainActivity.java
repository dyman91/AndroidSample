package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.myapplication.Utils.rotateBitmap;


public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PERMISSION = 101;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private ImageView imageView;
    private Bitmap drawableBitmap;
    private FaceRecognizer faceRecognizer = new FaceRecognizer();
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imageView);
        if (!allPermissionsGranted()) {
           ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    public void takePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private int getOrientation(String imagePath) {
        int rotate = 0;

        try{
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }

    private void setImageView(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    private void showVarianceResult(Bitmap bitmap){

        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // by object creation variance calculation is already performed
        Laplacian laplacian = new Laplacian(bitmap);
        // get score
        double variance = laplacian.getVariance();
        // get status based on score
        Laplacian.ImageQuality imageQuality = laplacian.getImageQuality();

        // visualization
        drawableBitmap = Drawing.drawLaplacianVariance(variance, imageQuality, mutableBitmap);
        setImageView(drawableBitmap);
    }

    private void showFaceRecognitionResult(Bitmap bitmap){

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        faceRecognizer.faceDetector.process(image).addOnSuccessListener(
                new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if (faces.size() == 0) {
                            showToast("No face found", Color.RED);
                            setImageView(bitmap);
                        } else if (faces.size() > 1) {
                            showToast("More than one face found", Color.RED);
                            setImageView(bitmap);
                        } else {
                            Face face = faces.get(0);

                            // draw boundary box
                            Bitmap bmp = Drawing.drawBoundary(face, drawableBitmap);
                            // draw facial landmarks
                            bmp = Drawing.drawContours(face, bmp);
                            Pair<String, Integer> headOrientation = faceRecognizer.estimateFacePosition(
                                    face.getHeadEulerAngleX(),
                                    face.getHeadEulerAngleY());
                            // draw head orientation. status is based on euler angles
                            bmp = Drawing.drawHeadOrientation(
                                    face.getHeadEulerAngleX(),
                                    face.getHeadEulerAngleY(),
                                    face.getHeadEulerAngleZ(),
                                    headOrientation,
                                    bmp);

                            setImageView(bmp);
                        }

                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                                showToast("ERROR", Color.RED);
                                e.printStackTrace();
                            }
                        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            int orientation = getOrientation(currentPhotoPath);
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            bitmap = rotateBitmap(bitmap, orientation);
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            showVarianceResult(bitmap);
            showFaceRecognitionResult(mutableBitmap);


        }
    }

    public void showToast(String message, int color) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        View view = toast.getView();
        TextView text = view.findViewById(android.R.id.message);

        text.setTextColor(color);
        toast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
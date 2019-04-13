package com.jjoe64.motiondetection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.jjoe64.motiondetection.motiondetection.MotionDetector;
import com.jjoe64.motiondetection.motiondetection.MotionDetectorCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private TextView txtStatus;
    private MotionDetector motionDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceView));
        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected(byte[] data, int width, int height) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(80);
                txtStatus.setText("Motion detected");

                savePicture(data, width, height);
            }

            @Override
            public void onTooDark() {
                txtStatus.setText("Too dark here");
            }
        });

        ////// Config Options
        //motionDetector.setCheckInterval(500);
        //motionDetector.setLeniency(20);
        //motionDetector.setMinLuma(1000);
    }

    ByteArrayOutputStream baos;
    byte[] rawImage;
    Bitmap bitmap;
    public void savePicture(byte[] data, int width, int height) {
        //camera.setOneShotPreviewCallback(null);
        //处理data
        //Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,
                width,
                height,
                null);
        baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
        rawImage = baos.toByteArray();
        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);

        SoftReference<Bitmap> softRef = new SoftReference<>(BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options));//方便回收
        bitmap = softRef.get();

        try {
            FileOutputStream fos = new FileOutputStream(getFilePath());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush(); // 刷新
            fos.close();// 关闭流
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath() {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MotionDetect/";
        File dir  = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        return dirPath + UUID.randomUUID() + ".png";
    }

    @Override
    protected void onResume() {
        super.onResume();
        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            txtStatus.setText("Camera found");
        } else {
            txtStatus.setText("No camera available");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionDetector.onPause();
    }

}

package com.demo.mycamerax;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import android.util.Size;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.OrientationEventListener;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 11;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE"};
//private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    List<String> mPermissionList = new ArrayList<>();
    PreviewView mPreviewView;
    ImageAnalysis mImageAnalysis;
    CameraSelector cameraSelector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreviewView = findViewById(R.id.view_finder);

        if(PermissionUtil.checkMultiPermission(MainActivity.this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS)){
            setCamera();
            Log.i("permission", ""+REQUEST_CODE_PERMISSIONS+"已被允许2");// 此处在次获取权限是不会运行的，第二次开始运行此处代码。
        }
    }

    private  void setCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                setPreview(cameraProvider);
                setImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }
    void setPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(mPreviewView.getPreviewSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }

    private void setImageAnalysis(@NonNull ProcessCameraProvider cameraProvider){

        mImageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        mImageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                // insert your code here.
            }
        });

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
//                textView.setText(Integer.toString(orientation));
            }
        };
        orientationEventListener.enable();

        Preview preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(mPreviewView.getPreviewSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, mImageAnalysis, preview);
    }

    // 检查多个权限。返回true表示已完全启用权限，返回false表示未完全启用权限
    public static boolean checkMultiPermission(Activity act, String[] permissions, int requestCode) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = PackageManager.PERMISSION_GRANTED;
            // 通过权限数组检查是否都开启了这些权限
            for (String permission : permissions) {
                check = ContextCompat.checkSelfPermission(act, permission);
                if (check != PackageManager.PERMISSION_GRANTED) {
                    break;
                }
            }
            if (check != PackageManager.PERMISSION_GRANTED) {
                // 未开启该权限，则请求系统弹窗，好让用户选择是否立即开启权限
                ActivityCompat.requestPermissions(act, permissions, requestCode);
                result = false;
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("permission", ""+REQUEST_CODE_PERMISSIONS+"已被允许1");
                    setCamera();
                }else {
                    Toast.makeText(this, "需要允许camera和SD卡权限才能使用", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void checkLocalPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, REQUEST_CODE_PERMISSIONS);
        Log.i("permission","checkLocalPermission");
    }

}
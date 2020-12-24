package com.demo.mycamerax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
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
    PreviewView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = findViewById(R.id.view_finder);

        if(PermissionUtil.checkMultiPermission(MainActivity.this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS)){
            startCamera(MainActivity.this);
            Log.i("permission", ""+REQUEST_CODE_PERMISSIONS+"已被允许2");// 此处在次获取权限是不会运行的，第二次开始运行此处代码。
        }
    }

    private void startCamera(Context context) {
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                    Preview preview = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        preview = new Preview.Builder()
                                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                                .setTargetRotation(mPreview.getDisplay().getRotation())
                                .build();
                    }

                    preview.setSurfaceProvider(mPreview.getPreviewSurfaceProvider());
//                    processCameraProvider.bindToLifecycle((LifecycleOwner) context, CameraSelector.DEFAULT_FRONT_CAMERA,preview);
                    processCameraProvider.bindToLifecycle((LifecycleOwner) context, CameraSelector.DEFAULT_BACK_CAMERA, preview);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(context));
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
                    startCamera(MainActivity.this);
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
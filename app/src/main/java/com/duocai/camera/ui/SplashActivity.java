package com.duocai.camera.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.duocai.camera.ParticleView.ParticleView;
import com.duocai.camera.R;
import com.duocai.camera.base.BaseActivity;
import com.duocai.camera.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by david on 2017/7/2.
 */
public class SplashActivity extends BaseActivity {

    @Bind(R.id.particle_view)
    ParticleView particleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        particleView.startAnim();
        particleView.setOnParticleAnimListener(new ParticleView.ParticleAnimListener() {
            @Override
            public void onAnimationEnd() {

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        initPermission();
                    }
                }, 500);
            }
        });
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_WRITE_EXTERNAL_STORAGE_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, READ_CAMERA_CODE);
        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }

    private static final int READ_PHONE_STATE_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_CODE = 2;
    private static final int READ_WRITE_EXTERNAL_STORAGE_CODE = 3;
    private static final int READ_CAMERA_CODE = 4;

    @Override
    protected void onPermissionSuccess(int requestCode) {
        switch (requestCode) {
            case READ_PHONE_STATE_CODE:
            case READ_EXTERNAL_STORAGE_CODE:
            case READ_WRITE_EXTERNAL_STORAGE_CODE:
            case READ_CAMERA_CODE:
                initPermission();
                break;
        }
    }

    @Override
    protected void onPermissionFaliure(int requestCode) {
        ToastUtils.longToast(this, "请开启权限才可正常使用");
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

        finish();
    }
}

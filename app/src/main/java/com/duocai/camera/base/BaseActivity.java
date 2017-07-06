package com.duocai.camera.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.List;

public class BaseActivity extends FragmentActivity {
    private boolean flagBarTint = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(View.inflate(this, layoutResID, null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initBarTint(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FragmentManager fm = getSupportFragmentManager();
        int index = requestCode >> 16;
        if (index != 0) {
            index--;
            if (fm.getFragments() == null || index < 0 || index >= fm.getFragments().size()) {
                return;
            }
            Fragment frag = fm.getFragments().get(index);
            if (frag != null) {
                handleResult(frag, requestCode, resultCode, data);
            }
            return;
        }
    }

    private void handleResult(Fragment frag, int requestCode, int resultCode, Intent data) {
        frag.onActivityResult(requestCode & 0xffff, resultCode, data);
        List<Fragment> frags = frag.getChildFragmentManager().getFragments();
        if (frags == null) return;
        for (Fragment f : frags) {
            if (f != null)
                handleResult(f, requestCode, resultCode, data);
        }
    }

    private void initBarTint(View view) {
        if (!flagBarTint) return;
    }

    protected void cancelLoadingDialog() {
        if (!isFinishing()) {
        }
    }

    protected void flagBarTint(boolean bool) {
        flagBarTint = bool;
    }

    /**
     * Android 6.0 授权机制管理
     *
     * @param permission
     * @param permissionCode
     */
    public void requestPermission(String permission, int permissionCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, permissionCode);
        } else {
            onPermissionSuccess(permissionCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionSuccess(requestCode);
        } else {
            onPermissionFaliure(requestCode);
        }
    }

    protected void onPermissionSuccess(int requestCode) {
    }

    protected void onPermissionFaliure(int requestCode) {

    }

}

package com.duocai.camera.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;


import com.duocai.camera.model.PhotoItem;
import com.duocai.camera.ui.ProcessPhotoActivity;
import com.duocai.camera.utils.ImageUtils;
import com.duocai.camera.utils.ToastUtils;

import java.io.File;
import java.util.Stack;

public class CameraManager {

    private static CameraManager mInstance;
    private Stack<Activity> cameras = new Stack<Activity>();

    public static CameraManager getInst() {
        if (mInstance == null) {
            synchronized (CameraManager.class) {
                if (mInstance == null)
                    mInstance = new CameraManager();
            }
        }
        return mInstance;
    }

    public static void openCamera(Activity context) {
        String cameraPath = ImageUtils.getImagePath();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraPath)));
        context.startActivityForResult(intent, CAMERA_CODE);
    }

    /**
     * 进入编辑
     *
     * @param activity
     * @param uri
     */
    public void processPhoto(Activity activity, Uri uri) {
        Intent intent = new Intent();
        intent.setClass(activity, ProcessPhotoActivity.class);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public void close() {
        for (Activity act : cameras) {
            try {
                act.finish();
            } catch (Exception e) {

            }
        }
        cameras.clear();
    }

    public void addActivity(Activity act) {
        cameras.add(act);
    }

    public void removeActivity(Activity act) {
        cameras.remove(act);
    }

    public static final int CAMERA_CODE = 0x008;

}

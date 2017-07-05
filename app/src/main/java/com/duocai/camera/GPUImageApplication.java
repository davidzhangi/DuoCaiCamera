package com.duocai.camera;

import android.app.Application;

import com.duocai.camera.utils.imageloader.GPUImageLoader;

public class GPUImageApplication extends Application {

    private static GPUImageApplication mInstance = null;

    public static synchronized GPUImageApplication getInstance() {
        if (mInstance == null) {
            mInstance = new GPUImageApplication();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        init();
    }

    private void init() {
        GPUImageLoader.build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GPUImageLoader.clearMemoryCache();
    }

}

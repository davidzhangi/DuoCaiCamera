package com.duocai.camera.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;


import com.duocai.camera.GPUImageApplication;
import com.duocai.camera.view.StickerView;

import java.util.List;

public abstract class ImageProcessor {
    public void process(List<StickerView> mStickers) {
        Bitmap targetBitmap = preProcess();

//        int suitableWidth = UIUtil.getScreenWidth(GPUImageApplication.getInstance());
//        final Bitmap newBitmap = Bitmap.createBitmap(suitableWidth, suitableWidth, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(targetBitmap);
        RectF dst = new RectF(0, 0, targetBitmap.getWidth(), targetBitmap.getHeight());
        Bitmap capture = null;

        cv.drawBitmap(targetBitmap, null, dst, null);
        DataHandler.recycleBitmap(capture);

        for (StickerView stickerView : mStickers) {
            Bitmap bmSticker = stickerView.getBitmap();
            cv.drawBitmap(bmSticker, 0, 0, null);
        }
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();

        new SavePicToFileTask().execute(targetBitmap);
    }

    private class SavePicToFileTask extends AsyncTask<Bitmap, Void, String> {
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            String fileName = null;
            try {
                bitmap = params[0];
                Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, bitmap.getWidth(), bitmap.getHeight());

                fileName = ImageUtils.saveToFile(ImageUtils.saveCropPath(), true, thumb, 90);
                DataHandler.recycleBitmap(bitmap);
                DataHandler.recycleBitmap(thumb);
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.shortToast(GPUImageApplication.getInstance(), "图片处理错误");
            }
            return fileName;
        }

        @Override
        protected void onPostExecute(String fileName) {
            super.onPostExecute(fileName);
            postResult(fileName);
        }
    }

    public abstract Bitmap preProcess();

    public abstract void postResult(String fileName);


}

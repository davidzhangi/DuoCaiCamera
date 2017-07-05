package com.duocai.camera.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.duocai.camera.model.ActivityEntity;
import com.duocai.camera.model.Diary;
import com.duocai.camera.model.OrderDataItem;
import com.duocai.camera.model.TagImage;
import com.duocai.camera.ui.MainActivity;

import java.io.Closeable;
import java.io.IOException;

public class DiaryUtils {
    private static String appendParams;

    public static void appendParams(String params) {
        appendParams = params;
    }

    public static void closeStream(Closeable stream) {
        try {
            if (stream != null)
                stream.close();
        } catch (IOException e) {

        }
    }

    public static void openPublishDiary(Context context, Object obj) {
        Intent intent = new Intent(context, MainActivity.class);
        if (obj instanceof Diary) {
            intent.putExtra(Constants.DIARY_OBJ, (Diary) obj);
        } else if (obj instanceof TagImage) {
            intent.putExtra(Constants.DIARY_IMAGE_OBJ, (TagImage) obj);
        }
        context.startActivity(intent);
    }

    public static void openPublishDiary(Activity context, Object obj, Object product) {
        if (obj == null) {
            openPublishDiary(context);
            return;
        }
        if (product == null) {
            openPublishDiary(context, obj);
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);
        if (obj instanceof Diary) {
            intent.putExtra(Constants.DIARY_OBJ, (Diary) obj);
        } else if (obj instanceof TagImage) {
            intent.putExtra(Constants.DIARY_IMAGE_OBJ, (TagImage) obj);
        }
        if (product instanceof OrderDataItem) {
            intent.putExtra(Constants.DIARY_ORDER_OBJ, (OrderDataItem) product);
        } else if (product instanceof ActivityEntity) {
            intent.putExtra(Constants.DIARY_ACTIVITY_OBJ, (ActivityEntity) product);
        }
        context.startActivity(intent);
    }

    public static void openPublishDiary(Activity context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }
}

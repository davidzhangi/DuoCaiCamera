package com.duocai.camera.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.duocai.camera.R;
import com.duocai.camera.base.BaseActivity;
import com.duocai.camera.controller.CameraManager;
import com.duocai.camera.utils.GalleryUtil;
import com.duocai.camera.view.MyDialog;
import com.martin.ads.omoshiroilib.ui.CameraPreviewActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {

    public static final int CAMERA_PHOTO = 0x999;
    public static final int LOCAL_PHOTO = 0x888;

    @Bind(R.id.lsq_entry_editor)
    RelativeLayout lsqEntryEditor;
    @Bind(R.id.lsq_entry_camera)
    RelativeLayout lsqEntryCamera;

    private MyDialog alertDialog;
    private String cameraPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_diary_layout);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.lsq_entry_camera)
    public void cameraClick() {
//        cameraPath = ImageUtils.getImagePath();
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraPath)));
//        startActivityForResult(intent, CAMERA_PHOTO);

        startActivity(new Intent(this, CameraPreviewActivity.class));
    }

    @OnClick(R.id.lsq_entry_editor)
    public void editClick() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, 1);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        startActivityForResult(intent, LOCAL_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        switch (requestCode) {
            case LOCAL_PHOTO:
                if (resultCode != RESULT_OK || data == null) {
                    return;
                }
                Uri uri = data.getData();
                if (uri == null) {
                    return;
                }
                cameraPath = GalleryUtil.getPath(this, uri);
                break;
        }

        CameraManager.getInst().processPhoto(this, Uri.parse("file://" + cameraPath));

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void cancel() {
        alertDialog = new MyDialog(this);
        alertDialog.setTitle("确定退出？");
        alertDialog.setCancelName("取消");
        alertDialog.setSubmitName("确定");
        alertDialog.setOnClickListener(new MyDialog.OnClickButtonListener() {
            @Override
            public void onClick(View v, MyDialog.ClickType type) {
                if (type == MyDialog.ClickType.CONFIRM) {
                    Process.killProcess(Process.myPid());
                }
            }
        });

        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }
}

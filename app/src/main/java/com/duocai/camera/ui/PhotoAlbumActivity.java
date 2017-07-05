package com.duocai.camera.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.duocai.camera.R;
import com.duocai.camera.adapter.AlbumGirdAdapter;
import com.duocai.camera.base.BaseActivity;
import com.duocai.camera.controller.CameraManager;
import com.duocai.camera.model.Album;
import com.duocai.camera.model.CloseActivity;
import com.duocai.camera.model.PhotoItem;
import com.duocai.camera.model.TagImage;
import com.duocai.camera.ui.fragment.DiaryDialogFragment;
import com.duocai.camera.utils.Constants;
import com.duocai.camera.utils.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class PhotoAlbumActivity extends BaseActivity {

    @Bind(R.id.tv_select_album)
    TextView tv_select_album;
    @Bind(R.id.album_gird)
    GridView album_gird;
    @Bind(R.id.iv_cancel)
    ImageView iv_cancel;
    @Bind(R.id.take_pic)
    ImageView take_pic;

    private List<Album> albumList = new ArrayList<>();
    private AlbumGirdAdapter girdAdapter;
    private List<TagImage> tagImageList;
    private Bundle bundle;
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    private boolean hasFolderGened = false;
    private ArrayList<Album> mResultFolder = new ArrayList<>();


    public void onEventMainThread(CloseActivity closeActivity) {
        if (closeActivity != null) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);
        initParam();
        initData();
    }

    private void initParam() {
        bundle = getIntent().getExtras();
        if (bundle != null) {
            tagImageList = (List<TagImage>) bundle.getSerializable(Constants.EXTRA_DATA);
            bundle.remove(Constants.EXTRA_DATA);
        }
    }

    private void initData() {
        getSupportLoaderManager().restartLoader(LOADER_ALL, null, new LoaderCallbacksImpl(this));
    }

    @OnClick(R.id.tv_select_album)
    public void showAlbumPop() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EXTRA_DATA, (Serializable) albumList);
        DiaryDialogFragment dialogFragment = DiaryDialogFragment.newInstance(bundle);
        dialogFragment.setOnSelect(new DiaryDialogFragment.OnSelectListener() {
            @Override
            public void onSelect(Album album) {
                girdAdapter = new AlbumGirdAdapter(PhotoAlbumActivity.this, album.photos);
                album_gird.setAdapter(girdAdapter);
                tv_select_album.setText(album.title);
            }
        });
        dialogFragment.onDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                tv_select_album.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.expanded_arrow, 0);
            }
        });
        tv_select_album.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.expanded_arrow_up, 0);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        dialogFragment.show(transaction, "dialogFragment");
    }

    @OnClick(R.id.iv_cancel)
    public void finishActivity() {
        this.finish();
    }

    @OnClick(R.id.take_pic)
    public void takePicture() {
        CameraManager.getInst().openCamera(this);
    }

    public void saveImageToGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(this.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + "/sdcard/" + fileName)));
    }

    private boolean isAdded(PhotoItem item) {
        if (item != null && tagImageList != null) {
            for (TagImage tag : tagImageList) {
                if (TextUtils.equals(tag.localPath, item.imageUri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private class LoaderCallbacksImpl implements LoaderManager.LoaderCallbacks<Cursor> {
        Context context;
        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
        };

        public LoaderCallbacksImpl(Context context) {
            this.context = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(context,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION,
                        null,
                        null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(context,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
                        null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if (data != null) {
                List<PhotoItem> images = new ArrayList<>();
                int count = data.getCount();

                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        if (new File(path).exists()) {
                            PhotoItem image = new PhotoItem(path, dateTime);
                            images.add(image);
                            if (!hasFolderGened) {
                                // 获取文件夹名称
                                File imageFile = new File(path);
                                File folderFile = imageFile.getParentFile();
                                Album folder = new Album();
                                folder.title = folderFile.getName();
                                folder.albumUri = folderFile.getAbsolutePath();
                                if (!mResultFolder.contains(folder)) {
                                    ArrayList<PhotoItem> imageList = new ArrayList<>();
                                    imageList.add(image);
                                    folder.photos = imageList;
                                    mResultFolder.add(folder);
                                } else {
                                    Album album = mResultFolder.get(mResultFolder.indexOf(folder));
                                    album.photos.add(image);
                                }
                            }
                        }
                    } while (data.moveToNext());

                    Album album = null;
                    if (images != null) {
                        album = new Album();
                        album.photos = images;
                        album.title = "所有照片";
                        if (girdAdapter == null) {
                            girdAdapter = new AlbumGirdAdapter(context, album.photos);
                            tv_select_album.setText("所有照片");
                            album_gird.setAdapter(girdAdapter);
                        }
                    }
                    if (TextUtils.equals(mResultFolder.get(0).title, "所有照片")) {
                        mResultFolder.remove(0);
                        mResultFolder.add(0, album);
                    } else {
                        mResultFolder.add(0, album);
                    }
                    albumList = mResultFolder;
                    hasFolderGened = true;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
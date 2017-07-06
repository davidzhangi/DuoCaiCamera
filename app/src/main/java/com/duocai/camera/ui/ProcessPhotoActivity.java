package com.duocai.camera.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duocai.camera.R;
import com.duocai.camera.adapter.FilterAdapter;
import com.duocai.camera.adapter.WaterMarkAdapter;
import com.duocai.camera.base.BaseActivity;
import com.duocai.camera.controller.CameraManager;
import com.duocai.camera.model.CloseActivity;
import com.duocai.camera.model.EventType;
import com.duocai.camera.model.FilterEffect;
import com.duocai.camera.model.OrderDataItem;
import com.duocai.camera.model.TagImage;
import com.duocai.camera.model.TagInfo;
import com.duocai.camera.utils.Constants;
import com.duocai.camera.utils.DataHandler;
import com.duocai.camera.utils.DiaryUtils;
import com.duocai.camera.utils.GPUImageFilterTools;
import com.duocai.camera.utils.ImageCreator;
import com.duocai.camera.utils.ImageProcessor;
import com.duocai.camera.utils.ToastUtils;
import com.duocai.camera.utils.UIUtil;
import com.duocai.camera.view.MyDialog;
import com.duocai.camera.view.ProgressWheel;
import com.duocai.camera.view.StickerView;
import com.duocai.camera.view.TagView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class ProcessPhotoActivity extends BaseActivity {

    @Bind(R.id.gpu_image)
    GPUImageView mGPUImageView;
    @Bind(R.id.drawing_view_container)
    ViewGroup drawArea;
    @Bind(R.id.tv_go_on)
    TextView tv_go_on;
    @Bind(R.id.tag_layout)
    View tag_layout;
    @Bind(R.id.back)
    View back;
    @Bind(R.id.list_tools)
    HListView filterListView;
    @Bind(R.id.tv_tag_hint)
    TextView tv_tag_hint;
    @Bind(R.id.list_water_mark)
    HListView list_water_mark;
    @Bind(R.id.pb_web_loading)
    ProgressWheel wheel;
    @Bind(R.id.indicator)
    ImageView indicator;

    FrameLayout overlay;

    private Bitmap currentBitmap;
    private int currentPosition = 0;
    private final int TAG_REQUEST_CODE = 0X004;
    private HashMap<Float, TagView> map = new HashMap<>();
    private TagImage tagImage = new TagImage();
    private List<TagInfo> infoList = new ArrayList<>();
    private TagImage editTagImg = null;
    private String url;
    public boolean is_edit;
    private MyDialog alertDialog;
    private OrderDataItem objOrder;
    private Object objActivity;
    private FilterAdapter adapter;
    private int screenW;
    private List<StickerView> mStickers = new ArrayList<>();
    private View[] viewArr;
    private int lastPosition = 0;
    private StickerView selectSticker = null;
    private Activity mContext;

    public void onEventMainThread(EventType event) {
        if (event != null) {
            float type = event.getType();
            switch ((int) type) {
                case -1:
                    currentPosition = 0;
                    doEvent();
                    break;
                case -2:
                    currentPosition = 1;
                    doEvent();
                    break;
                case -3:
                    currentPosition = 2;
                    doEvent();
                    break;
                default:
                    doEventDefault(event);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flagBarTint(false);
        setContentView(R.layout.activity_process_photo_layout);
        ButterKnife.bind(this);

        mContext = ProcessPhotoActivity.this;
        EventBus.getDefault().register(this);
        screenW = UIUtil.getScreenWidth(this);
        mStickers.clear();

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchText();
    }

    private void init() {
        initIntent();
        initUrl();
        initView();
        initEvent();
        loadImage();
        addEditTag();
    }

    private void initIntent() {
        editTagImg = (TagImage) getIntent().getSerializableExtra(Constants.EXTRA_DATA);
        is_edit = getIntent().getBooleanExtra(Constants.IS_EDIT, false);
        objOrder = (OrderDataItem) getIntent().getSerializableExtra(Constants.DIARY_ORDER_OBJ);
        objActivity = getIntent().getSerializableExtra(Constants.DIARY_ACTIVITY_OBJ);
    }

    private void initUrl() {
        if (editTagImg != null) {
            if (!TextUtils.isEmpty(editTagImg.Pic)) {
                if (editTagImg.Pic.startsWith("http://") || editTagImg.Pic.startsWith("https://")) {
                    url = editTagImg.Pic;
                } else {
                    url = "file://" + editTagImg.Pic;
                }
            }
        } else {
            url = "file://" + getIntent().getData().getPath();
        }
    }

    private void initView() {
        viewArr = new View[]{filterListView, list_water_mark, tag_layout};
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) indicator.getLayoutParams();
        params.leftMargin = (screenW / 6 - UIUtil.dp2px(8));
        indicator.requestLayout();
        RelativeLayout.LayoutParams overlayParam = new RelativeLayout.LayoutParams(screenW, screenW);
        overlay = (FrameLayout) LayoutInflater.from(ProcessPhotoActivity.this).inflate(
                R.layout.water_mark_overlay, null);
        drawArea.addView(overlay, overlayParam);
        filterListView.setVisibility(View.VISIBLE);
        alertDialog = new MyDialog(this);
        list_water_mark.setAdapter(new WaterMarkAdapter(mContext));
    }

    private void initEvent() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
        drawArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (currentPosition == 2) {
                    if (map.size() == 3) {
                        ToastUtils.shortToast(mContext, "单张图片不能超过三个标签");
                        return false;
                    }
                    Intent intent = new Intent(mContext, EditTagActivity.class);
                    float percentX = event.getX() / screenW;
                    float percentY = event.getY() / screenW;
                    TagInfo tagInfo = new TagInfo();
                    tagInfo.XPoint = percentX;
                    tagInfo.YPoint = percentY;
                    tagInfo.firstPointX = percentX;
                    intent.putExtra(Constants.IS_EDIT, false);
                    intent.putExtra(Constants.EXTRA_DATA, tagInfo);
                    startActivityForResult(intent, TAG_REQUEST_CODE);
                    overridePendingTransition(R.anim.push_bottom_in, 0);
                }
                return false;
            }
        });
        alertDialog.setOnClickListener(new MyDialog.OnClickButtonListener() {
            @Override
            public void onClick(View v, MyDialog.ClickType type) {
                if (type == MyDialog.ClickType.CONFIRM) {
                    finish();
                }
            }
        });

        list_water_mark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mStickers.size() >= 15) {
                    ToastUtils.shortToast(mContext, "单张图片不能超过15张贴纸");
                } else {
                    addStickerItem(position);
                }

            }
        });
    }

    private void loadImage() {
        ImageCreator creator = new ImageCreator() {
            @Override
            public void preload() {
                wheel.setVisibility(View.VISIBLE);
                tv_go_on.setEnabled(false);
            }

            @Override
            public void getDisplayImage(Bitmap bitmap) {
                currentBitmap = bitmap;

                ViewGroup.LayoutParams layoutParams = mGPUImageView.getLayoutParams();
                layoutParams.height = currentBitmap.getHeight();
                layoutParams.width = currentBitmap.getWidth();
                mGPUImageView.setLayoutParams(layoutParams);

                mGPUImageView.setImage(currentBitmap);
                tv_go_on.setEnabled(true);
            }

            @Override
            public void getSmallImage(List<Bitmap> smallList) {
                initFilter(smallList);
                tv_go_on.setEnabled(true);

                wheel.setVisibility(View.GONE);
            }
        };


        creator.loadImage(url);
    }

    private void addEditTag() {
        if (editTagImg != null) {
            for (TagInfo tagInfo : editTagImg.TagInfo) {
                addTag(tagInfo, true);
            }
        }
    }

    private void addTag(TagInfo tagInfo, boolean flag) {
        if (tagInfo == null) {
            return;
        }
        TagView tagView = new TagView(this);
        tagView.setTagInfo(tagInfo, flag);
        if (currentPosition != 2) {
            tagView.setJump(false);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenW, screenW);
        tagView.setLayoutParams(params);
        drawArea.addView(tagView, params);
        map.put(tagInfo.firstPointX, tagView);
    }

    private void initFilter(List<Bitmap> bitmapList) {
        final List<FilterEffect> filters = DataHandler.filters;
        adapter = new FilterAdapter(mContext, bitmapList);
        if (editTagImg != null) {
            GPUImageFilter filter = GPUImageFilterTools.createFilterForType(
                    mContext, filters.get(editTagImg.filterType).getType());
            mGPUImageView.setFilter(filter);
            adapter.setSelected(editTagImg.filterType);
            adapter.setSelectFilter(editTagImg.filterType);
        } else {
            adapter.setSelected(0);
            adapter.setSelectFilter(0);
        }
        filterListView.setAdapter(adapter);
        filterListView.post(new Runnable() {
            @Override
            public void run() {
                if (editTagImg != null) {
                    filterListView.smoothScrollToPosition(editTagImg.filterType);
                }
            }
        });
        filterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (adapter.getSelectFilter() != arg2) {
                    adapter.setSelectFilter(arg2);
                    adapter.setSelected(arg2);
                    tagImage.filterType = arg2;
                    FilterEffect effect = filters.get(arg2);
                    GPUImageFilter filter = GPUImageFilterTools.createFilterForType(
                            mContext, effect.getType());
                    mGPUImageView.setFilter(filter);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                TagInfo tagInfo = (TagInfo) data.getSerializableExtra(Constants.EXTRA_DATA);
                addTag(tagInfo, false);
            }
        }
    }

    @OnClick(R.id.tv_go_on)
    public void onClick() {
        tv_go_on.setEnabled(false);
        savePic();
    }

    private void doEventDefault(EventType event) {
        if (TextUtils.equals(event.getTag(), Constants.DEL_TAG)) {
            float viewId = event.getType();
            drawArea.removeView(map.get(viewId));
            map.remove(viewId);
        }
        if (TextUtils.equals(event.getTag(), Constants.SAVE_TAG)) {
            float viewId = event.getType();
            drawArea.removeView(map.get(viewId));
            map.remove(viewId);
            addTag(event.getItem(), false);
        }
    }

    private void doEvent() {
        if (currentPosition == lastPosition) {
            return;
        }
        View targetView = viewArr[currentPosition];
        for (int i = 0; i < viewArr.length; i++) {
            viewArr[i].setVisibility(View.GONE);
        }
        translateIndicator(currentPosition);
        translateBottom(targetView);
        resetStickersFocus();
        if (currentPosition == 1) {
            if (selectSticker != null) {
                selectSticker.setFocusable(true);
            }
        }
        if (currentPosition == 2) {
            resetTag(true);
        } else {
            resetTag(false);
        }
    }

    private void savePic() {
        ImageProcessor processor = new ImageProcessor() {
            @Override
            public Bitmap preProcess() {
                wheel.setVisibility(View.VISIBLE);
                Bitmap capture = null;
                try {
                    capture = mGPUImageView.capture();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    capture = currentBitmap;
                }
                return capture;
            }

            @Override
            public void postResult(String fileName) {
                wheel.setVisibility(View.GONE);

                tv_go_on.setEnabled(true);
                CameraManager.getInst().close();
                finish();
            }
        };
        processor.process(mStickers);
    }

    private boolean checkEdit() {
        int size = map.size();
        if (adapter == null) {
            return false;
        }
        return size > 0 || adapter.getSelectFilter() > 0 || mStickers.size() > 0;
    }

    private void switchText() {
        if (map.size() > 0) {
            tv_tag_hint.setText(R.string.add_tag_hint_has);
        } else {
            tv_tag_hint.setText(R.string.add_tag_hint);
        }
    }

    private void resetTag(boolean jump) {
        for (Map.Entry<Float, TagView> entry : map.entrySet()) {
            TagView value = entry.getValue();
            value.setJump(jump);
        }
    }


    public void translateIndicator(int position) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(indicator, "x", position * screenW / 3 + (screenW / (3 * 2)));
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(200 * Math.abs(lastPosition - position));
        animSet.playTogether(animX);
        animSet.start();
        lastPosition = position;
    }


    private void resetStickersFocus() {
        for (StickerView stickerView : mStickers) {
            if (stickerView.isFocusable()) {
                selectSticker = stickerView;
            }
            stickerView.setFocusable(false);
        }
    }

    private void addStickerItem(int position) {
        resetStickersFocus();
        StickerView stickerView = new StickerView(this);
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(DataHandler.markList.get(position).getUri());
        stickerView.setWaterMark(bitmap);
        stickerView.addTo(overlay);
        mStickers.add(stickerView);
        stickerView.setOnStickerDeleteListener(new StickerView.OnStickerDeleteListener() {
            @Override
            public void onDelete(StickerView stickerView) {
                if (mStickers.contains(stickerView)) {
                    mStickers.remove(stickerView);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (currentPosition == 1) {
                resetStickersFocus();
                int x = (int) ev.getX();
                int y = (int) ev.getY() - UIUtil.dp2px(50);
                for (int i = mStickers.size() - 1; i >= 0; i--) {
                    StickerView stickerView = mStickers.get(i);
                    RectF rectF = new RectF(stickerView.getContentRect());
                    boolean isContains = rectF.contains(x, y);
                    boolean isDelete = stickerView.isInDelete(x, y);
                    boolean isControl = stickerView.isInController(x, y);
                    if (isContains || isDelete || isControl) {
                        stickerView.setFocusable(true);
                        break;
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void translateBottom(final View inView) {
        if (inView != null) {
            Animation animationIn = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left_fast);
            animationIn.setRepeatMode(0);
            inView.startAnimation(animationIn);
            inView.setVisibility(View.VISIBLE);
        }
    }

    private void showAlert() {
        if (!is_edit) {
            if (!checkEdit()) {
                finish();
                return;
            }
        }
        alertDialog.setTitle("确认放弃编辑过的图片吗?");
        alertDialog.setCancelName("继续编辑");
        alertDialog.setSubmitName("放弃编辑");
        alertDialog.show(back);
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataHandler.recycleBitmap(currentBitmap);
        if (mGPUImageView != null) {
            mGPUImageView.recycleSurface();
        }
        if (adapter != null && adapter.getList() != null) {
            for (Bitmap bitmap : adapter.getList()) {
                DataHandler.recycleBitmap(bitmap);
            }
            adapter.getList().clear();
        }
        EventBus.getDefault().unregister(this);
        System.gc();
    }
}



package com.duocai.camera.ui.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;


import com.duocai.camera.R;
import com.duocai.camera.adapter.AlbumsListAdapter;
import com.duocai.camera.model.Album;
import com.duocai.camera.utils.Constants;
import com.duocai.camera.utils.UIUtil;

import java.util.List;

public class DiaryDialogFragment extends android.support.v4.app.DialogFragment {
    private View convertView;
    private ListView list_album;
    private View iv_pop_cancel;
    private List<Album> albums;
    private AlbumsListAdapter albumsAdapter;
    private OnSelectListener selectListener;
    private DialogInterface.OnDismissListener onDismissListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
    }

    private void initParam() {
        Bundle arguments = getArguments();
        albums = (List<Album>) arguments.getSerializable(Constants.EXTRA_DATA);
        albumsAdapter = new AlbumsListAdapter(getActivity(),albums);
//        albumsAdapter.setAlbums(albums);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(R.layout.pop_album_layout, null);
        View main_content = convertView.findViewById(R.id.main_content);
        ViewGroup.LayoutParams layoutParams = main_content.getLayoutParams();
        layoutParams.height = UIUtil.getScreenHeight(getActivity()) / 2;
        layoutParams.width = UIUtil.getScreenHeight(getActivity()) * 2 / 5;
        return convertView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list_album = (ListView) view.findViewById(R.id.list_album);
        iv_pop_cancel = view.findViewById(R.id.iv_pop_cancel);
        list_album.setAdapter(albumsAdapter);
        list_album.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectListener != null) {
                    selectListener.onSelect(albums.get(position));
                    dismiss();
                }

            }
        });
        iv_pop_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.anim_push_bottom);
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new BitmapDrawable());
        return dialog;
    }

    public interface OnSelectListener {
        void onSelect(Album album);
    }

    public void setOnSelect(OnSelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public void onDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onDismissListener.onDismiss(dialog);
    }


    public static DiaryDialogFragment newInstance(Bundle bundle) {
        DiaryDialogFragment fragment = new DiaryDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


}

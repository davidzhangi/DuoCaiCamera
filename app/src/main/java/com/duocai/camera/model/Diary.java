package com.duocai.camera.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Diary implements Serializable {

    public String NoteId = System.currentTimeMillis() + "";
    public String Os = "android";
    public String Ip = null;
    public String OsVer = android.os.Build.VERSION.RELEASE;
    public String ActivityId = null;
    public String CkId = null;
    public String Imei = null;
    public String OrderId = null;
    public String Position = null;

    @SerializedName("TagImage")
    public List<TagImage> TagImages = new LinkedList<>();
    public String UserId = null;
    public String UserName = null;
    public String SellerNewsId = null;
    public String Idfa = null;
    public String Content = null;
    public String ActivityName = null;
    public int NoteSource = 3;
    public String NoteVersion = null;

    public String ProductDes = null;
    public String ProductPic = null;
    public int Status = 0;
    public List<Tag> CustomTags = null;

    public boolean isUpdateDiry = false;
    public String SellerId;
    public boolean isDelete;
}

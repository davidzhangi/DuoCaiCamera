package com.duocai.camera.model;


import com.duocai.camera.base.BaseResult;

public class DiaryCollectModel extends BaseResult<DiaryCollectModel.Data> {
    public class Data{
        public boolean IsFav = false;
        public boolean IsSuccess = false;
        public long FavCount = 0;
    }
}

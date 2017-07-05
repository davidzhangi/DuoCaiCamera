package com.duocai.camera.base;

/**
 * Created by david on 2017/7/2.
 */
public class BaseResult<T> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public int Status;
    public String Msg;
    public T Result;
    public String ServerTime;
}

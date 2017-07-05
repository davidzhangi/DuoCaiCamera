package com.duocai.camera.model;

import java.io.Serializable;

/**
 * Created by david on 2017/7/2.
 */
public class ActivityEntity implements Serializable{
    public String activityId = null;
    public String activityName = null;

    public ActivityEntity(String activityId, String activityName) {
        this.activityId = activityId;
        this.activityName = activityName;
    }
}

package com.jokin.cbarrage;

/**
 * Created by jokinkuang on 2017/9/13.
 */

public class Barrage {
    private String mType;
    private String mText;

    public Barrage(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}

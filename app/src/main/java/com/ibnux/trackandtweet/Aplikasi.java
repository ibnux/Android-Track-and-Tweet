package com.ibnux.trackandtweet;

import android.app.Application;

import com.ibnux.trackandtweet.data.ObjectBox;

public class Aplikasi extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectBox.init(this);
    }
}

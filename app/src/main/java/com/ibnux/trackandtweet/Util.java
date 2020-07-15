package com.ibnux.trackandtweet;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static void log(Object obj){
        Log.d("TnT","------------------");
        Log.d("TnT",obj+"");
        Log.d("TnT","------------------");
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context cx) {
        ActivityManager manager = (ActivityManager) cx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                log(serviceClass.getName()+" is running");
                return true;
            }
        }
        log(serviceClass.getName()+" is NOT running");
        return false;
    }
}

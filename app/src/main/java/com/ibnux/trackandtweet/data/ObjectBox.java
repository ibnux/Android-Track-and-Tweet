package com.ibnux.trackandtweet.data;

import android.content.Context;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class ObjectBox {
    private static BoxStore boxStore;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
    }

    public static BoxStore get() { return boxStore; }

    public static Box<Akun> getAkun(){
        return get().boxFor(Akun.class);
    }

    public static Akun getAkun(String userid){
        return getAkun().query().equal(Akun_.username,userid).or().equal(Akun_.userid, userid).build().findFirst();
    }

    public static long putAkun(Akun akun){
        return getAkun().put(akun);
    }

    public static Box<Aktivitas> getAktivitas(){
        return get().boxFor(Aktivitas.class);
    }

    public static Aktivitas getAktivitas(long id){
        return getAktivitas().query().equal(Aktivitas_.id,id).build().findFirst();
    }

    public static long putAktivitas(Aktivitas acara){
        return getAktivitas().put(acara);
    }

    public static Box<Tweet> getTweet(){
        return get().boxFor(Tweet.class);
    }

    public static Tweet getTweet(long id){
        return getTweet().query().equal(Tweet_.id,id).build().findFirst();
    }

    public static long putAktivitas(Tweet tweet){
        return getTweet().put(tweet);
    }
}

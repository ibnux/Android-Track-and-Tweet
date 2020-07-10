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
        return get().boxFor(Akun.class).query().equal(Akun_.username,userid).or().equal(Akun_.userid, userid).build().findFirst();
    }

    public static long putAkun(Akun akun){
        return get().boxFor(Akun.class).put(akun);
    }

    public static Box<Acara> getAcara(){
        return get().boxFor(Acara.class);
    }

    public static Acara getAcara(long id){
        return get().boxFor(Acara.class).query().equal(Acara_.id,id).build().findFirst();
    }

    public static long putAcara(Acara acara){
        return get().boxFor(Acara.class).put(acara);
    }
}
package com.ibnux.trackandtweet.data;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Tweet {
    @Id
    public long id;
    public int urutan, speed;
    public String text;
    public double lat,lon;
    public long waktu;
    public ToOne<Aktivitas> acara;

    public Tweet(int urutan, String text){
        this.urutan = urutan;
        this.text = text;
    }

}

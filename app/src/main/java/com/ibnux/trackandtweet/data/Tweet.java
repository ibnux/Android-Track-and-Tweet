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
    //lastLatlong
    public double lat,lon,alt;
    public long waktu;
    public ToOne<Aktivitas> acara;
    public String track ="", tweetID, TweetResultText;

    public Tweet(int urutan, String text){
        this.urutan = urutan;
        this.text = text;
    }

    /**
     * String to add
     * @param latLong lat,long,alt
     */
    public void addTrack(String latLong){
        track += latLong+";";
    }
}

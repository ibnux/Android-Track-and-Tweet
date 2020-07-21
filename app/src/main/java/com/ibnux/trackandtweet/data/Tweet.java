package com.ibnux.trackandtweet.data;


import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Tweet {
    @Id
    public long id;
    public int speed;
    public int jarak;
    public String text;
    //lastLatlong
    public double lat=0,lon=0,alt=0;
    public long waktu, statusId;
    public ToOne<Aktivitas> aktivitas;
    public String track ="", TweetResultText, username, userid;

    public Tweet(){
    }

    /**
     * String to add
     * @param latLong lat,long,alt,speed
     */
    public void addTrack(String latLong){
        track += latLong+";";
    }
}

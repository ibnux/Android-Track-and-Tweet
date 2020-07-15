package com.ibnux.trackandtweet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Aktivitas {
    @Id
    public long id;
    public String namaAcara, template, hashTag, satuan;
    public long waktu;
    public int status = 0; // 0. draft 1. started 2. paused 3. finished
    public long interval = 0; // 5 minutes
    public boolean byTime = true;
    public double lastLatitude = 0;
    public double lastLongitude = 0;
    public double lastAltitude = 0;
    public int lastDistance = 0;
    public ToMany<Akun> akuns;
    public ToMany<Tweet> tweets;

    public Aktivitas(){}

    public Aktivitas(String namaAcara, String template, String hashTag, long waktu){
        this.namaAcara = namaAcara;
        this.template = template;
        this.hashTag = hashTag;
        this.waktu = waktu;
    }

    public void addUsername(Akun akun){
        akuns.add(akun);
    }
}

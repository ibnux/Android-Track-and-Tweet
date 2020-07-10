package com.ibnux.trackandtweet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Acara {
    @Id
    public long id;
    public String namaAcara, firstTweet, hashTag;
    public long waktu;
    public int status = 0; // draft 1. started 2. finished 3. archived
    public ToMany<Akun> akuns;

    public Acara(String namaAcara, String firstTweet, String hashTag, long waktu){
        this.namaAcara = namaAcara;
        this.firstTweet = firstTweet;
        this.hashTag = hashTag;
        this.waktu = waktu;
    }

    public void addUsername(Akun akun){
        akuns.add(akun);
    }
}

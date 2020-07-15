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
    public int status = 0; // draft 1. started 2. paused 3. finished
    public long interval = 0; // 5 minutes
    public boolean byTime = true;
    public ToMany<Akun> akuns;

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

package com.ibnux.trackandtweet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Akun {
    @Id
    public long id;
    public String username, name="", userid, token, secret, avatar, tkey, tsecret;

    public Akun(String username, String name, String userid, String token, String secret){
        this.username = username;
        this.name = name;
        this.userid = userid;
        this.token = token;
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "@" + username ;
    }
}

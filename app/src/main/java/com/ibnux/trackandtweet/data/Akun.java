package com.ibnux.trackandtweet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Akun {
    @Id
    public long id;
    public String username, userid, token, secret, avatar, tkey, tsecret;

    public Akun(String username, String userid, String token, String secret){
        this.username = username;
        this.userid = userid;
        this.token = token;
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "@" + username ;
    }
}

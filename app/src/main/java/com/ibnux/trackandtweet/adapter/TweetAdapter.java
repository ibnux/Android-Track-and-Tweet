package com.ibnux.trackandtweet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ibnux.trackandtweet.R;
import com.ibnux.trackandtweet.Util;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.data.Tweet;
import com.ibnux.trackandtweet.data.Tweet_;
import com.ibnux.trackandtweet.ui.AktivitasActivity;

import java.util.List;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.MyViewHolder> {
    private List<Tweet> datas;
    AktivitasActivity callback;
    long idAktivitas;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardview;
        TextView txtUsername,txtTanggal,txtTweet;
        public MyViewHolder(View v) {
            super(v);
            txtTweet = v.findViewById(R.id.txtTweet);
            cardview = v.findViewById(R.id.cardview);
            txtUsername = v.findViewById(R.id.txtUsername);
            txtTanggal = v.findViewById(R.id.txtTanggal);
        }
    }

    public TweetAdapter(long idAktivitas, AktivitasActivity callback){
        this.idAktivitas = idAktivitas;
        this.callback = callback;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getTweet().query().equal(Tweet_.aktivitasId,idAktivitas).orderDesc(Tweet_.waktu).build().find();
        notifyDataSetChanged();
        if(callback==null) return;

        int jml = getItemCount();
        StringBuilder data = new StringBuilder();
        for(int n=0;n<jml;n++){
            Tweet twt = datas.get(n);
            if(twt.waktu>callback.timeH)
                callback.timeH = twt.waktu;
            if(twt.waktu<callback.timeL)
                callback.timeL = twt.waktu;
            callback.jarak += twt.jarak;
            data.append(twt.track);
        }
        callback.lama = callback.timeH-callback.timeL;
        callback.hitungStatistik(data.toString());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TweetAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tweet, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Tweet tweet = datas.get(position);
        holder.txtTanggal.setText(Util.getDate(tweet.waktu,"dd/MM/yyyy HH:mm"));
        holder.txtUsername.setText("@"+tweet.username);
        holder.txtTweet.setText(tweet.TweetResultText);
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onTweetClicked(tweet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface TweetCallback {
        void onTweetClicked(Tweet tweet);
    }
}

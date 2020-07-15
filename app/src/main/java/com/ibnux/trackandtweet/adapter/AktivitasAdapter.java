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
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.Aktivitas_;
import com.ibnux.trackandtweet.data.ObjectBox;

import java.util.List;

public class AktivitasAdapter extends RecyclerView.Adapter<AktivitasAdapter.MyViewHolder> {
    private List<Aktivitas> datas;
    AktivitasCallback callback;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtJudul, txtUsername, txtHesteg,txtLastTweet, txtDetail;
        CardView layoutCard;
        public MyViewHolder(View v) {
            super(v);
            txtJudul = v.findViewById(R.id.txtJudul);
            txtUsername = v.findViewById(R.id.txtUsername);
            txtHesteg = v.findViewById(R.id.txtHesteg);
            layoutCard = v.findViewById(R.id.layoutCard);
            txtLastTweet = v.findViewById(R.id.txtLastTweet);
            txtDetail = v.findViewById(R.id.txtDetail);
        }
    }

    public AktivitasAdapter(AktivitasCallback callback){
        this.callback = callback;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getAktivitas().query().orderDesc(Aktivitas_.waktu).build().find();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aktivitas, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Aktivitas aktivitas = datas.get(position);
        holder.txtJudul.setText(aktivitas.namaAcara);
        holder.txtLastTweet.setText(Util.getDate(aktivitas.waktu,"dd/MM/yyyy hh:mm"));
        String username = "";
        int jml = aktivitas.akuns.size();
        for(int n=0;n<jml;n++){
            username += aktivitas.akuns.get(n).toString()+", ";
        }
        if(!username.isEmpty())
            username = username.substring(0, username.length()-2);
        holder.txtUsername.setText(username);
        holder.txtHesteg.setText(aktivitas.hashTag);
        holder.txtDetail.setText("Tweet setiap "+aktivitas.interval+" "+aktivitas.satuan);
        holder.layoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null){
                    callback.onAktivitasClicked(aktivitas);
                }
            }
        });
        holder.layoutCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(callback!=null){
                    callback.onAktivitasLongClicked(aktivitas);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface AktivitasCallback {
        void onAktivitasClicked(Aktivitas aktivitas);
        void onAktivitasLongClicked(Aktivitas aktivitas);
    }
}

package com.ibnux.trackandtweet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ibnux.trackandtweet.R;
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.Akun;
import com.ibnux.trackandtweet.data.ObjectBox;

import java.util.List;

public class AkunAdapter extends RecyclerView.Adapter<AkunAdapter.MyViewHolder> {
    private List<Akun> datas;
    AkunCallback callback;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView txtName,txtUsername;
        LinearLayout layout;
        public MyViewHolder(View v) {
            super(v);
            avatar = v.findViewById(R.id.avatar);
            txtUsername = v.findViewById(R.id.txtUsername);
            txtName = v.findViewById(R.id.txtName);
            layout = v.findViewById(R.id.layout);
        }
    }

    public AkunAdapter(AkunCallback callback){
        reload();
        this.callback = callback;
    }

    public void reload(){
        datas = ObjectBox.getAkun().getAll();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AkunAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_akun, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface AkunCallback {
        void onAkunClicked(Aktivitas aktivitas);
    }
}

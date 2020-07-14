package com.ibnux.trackandtweet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ibnux.trackandtweet.adapter.AkunAdapter;
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.databinding.ActivityAkunListBinding;

public class AkunListActivity extends AppCompatActivity implements View.OnClickListener, AkunAdapter.AkunCallback{
    ActivityAkunListBinding binding;
    AkunAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAkunListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Akun Twitter");
        binding.btnAddAkun.setOnClickListener(this);
        adapter = new AkunAdapter(this);
        binding.listView.setHasFixedSize(true);
        binding.listView.setLayoutManager(new LinearLayoutManager(this));
        binding.listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnAddAkun){
            startActivityForResult(new Intent(this, TwitterLogin.class),221);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==221){
                adapter.reload();
            }
        }
    }

    @Override
    public void onAkunClicked(Aktivitas aktivitas) {

    }
}
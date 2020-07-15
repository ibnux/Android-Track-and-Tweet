package com.ibnux.trackandtweet.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.trackandtweet.databinding.ActivityAktivitasBinding;

public class AktivitasActivity extends AppCompatActivity {
    ActivityAktivitasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Aktivitas");
        binding = ActivityAktivitasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tableStats.setVisibility(View.GONE);
    }
}
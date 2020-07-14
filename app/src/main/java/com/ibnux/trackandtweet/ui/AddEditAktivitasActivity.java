package com.ibnux.trackandtweet.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.trackandtweet.databinding.ActivityAddEditAktivitasBinding;

public class AddEditAktivitasActivity extends AppCompatActivity {
    ActivityAddEditAktivitasBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditAktivitasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Aktivitas");
    }
}
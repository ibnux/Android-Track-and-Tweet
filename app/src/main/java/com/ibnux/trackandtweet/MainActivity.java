package com.ibnux.trackandtweet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.trackandtweet.databinding.ActivityMainBinding;
import com.ibnux.trackandtweet.ui.TwitterLogin;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAddAkun.setOnClickListener(this);
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
                //refresh spinner
            }
        }
    }
}
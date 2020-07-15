package com.ibnux.trackandtweet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.Akun;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.databinding.ActivityAddEditAktivitasBinding;

import java.util.ArrayList;
import java.util.List;

public class AddEditAktivitasActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    ActivityAddEditAktivitasBinding binding;
    Aktivitas aktivitas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditAktivitasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.layoutJarak.setVisibility(View.GONE);


        Intent i = getIntent();
        if(i.hasExtra("id")){
            setTitle("Edit Aktivitas");
            aktivitas = ObjectBox.getAktivitas(i.getLongExtra("id",0L));
            binding.txtJudul.setText(aktivitas.namaAcara);
            binding.txtHashtag.setText(aktivitas.hashTag);
            binding.txtTweet.setText(aktivitas.template);
            binding.spinnerSetiap.setSelection((aktivitas.byTime)?0:1);
            if(aktivitas.byTime){
                binding.layoutJarak.setVisibility(View.GONE);
                binding.layoutWaktu.setVisibility(View.VISIBLE);
                binding.txtDetik.setText(String.valueOf(aktivitas.interval));
                binding.spinnerWaktu.setSelection((aktivitas.satuan.equals("detik"))?0:1);
            }else {
                binding.layoutJarak.setVisibility(View.VISIBLE);
                binding.layoutWaktu.setVisibility(View.GONE);
                binding.txtMeter.setText(String.valueOf(aktivitas.interval));
                binding.spinnerJarak.setSelection((aktivitas.satuan.equals("meter"))?0:1);
            }
        }else{
            setTitle("Tambah Aktivitas");
        }
        binding.spinnerSetiap.setOnItemSelectedListener(this);
        binding.btnSimpan.setOnClickListener(this);
        binding.txtAkun.setOnClickListener(this);
        reloadAkun();
    }

    public void reloadAkun(){
        binding.layoutAkun.removeAllViews();
        List<Akun> akuns = ObjectBox.getAkun().getAll();
        int jml = akuns.size();
        for(int n=0;n<jml;n++){
            CheckBox cb = new CheckBox(this);
            cb.setText(akuns.get(n).toString());
            cb.setTag(akuns.get(n).id);
            if(aktivitas!=null && aktivitas.akuns.getById(akuns.get(n).id)!=null){
                cb.setChecked(true);
            }
            binding.layoutAkun.addView(cb);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
            binding.layoutJarak.setVisibility(View.GONE);
            binding.layoutWaktu.setVisibility(View.VISIBLE);
        }else{
            binding.layoutJarak.setVisibility(View.VISIBLE);
            binding.layoutWaktu.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnSimpan){
            simpan();
        }else if(v==binding.txtAkun){
            startActivityForResult(new Intent(this, AkunListActivity.class),323);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==323){
                reloadAkun();
            }
        }
    }

    private void simpan(){
        if(binding.txtJudul.getText().toString().isEmpty()){
            binding.txtJudul.setError("Mohon diisi");
            return;
        }
        List<Akun> akuns = new ArrayList<>();
        int jml = binding.layoutAkun.getChildCount();
        for(int n=0;n<jml;n++){
            CheckBox cb = (CheckBox) binding.layoutAkun.getChildAt(n);
            if(cb.isChecked()){
                akuns.add(ObjectBox.getAkun().get((long)cb.getTag()));
            }
        }
        if(akuns.size()==0){
            Toast.makeText(this,"Akun Twitter harus ditambahkan",Toast.LENGTH_LONG).show();
            return;
        }

        if(aktivitas==null) {
            aktivitas = new Aktivitas();
            aktivitas.waktu = System.currentTimeMillis();
        }

        long nilai = 0;
        if(binding.spinnerSetiap.getSelectedItemPosition()==0){
            aktivitas.byTime = true;
            nilai = Long.parseLong(binding.txtDetik.getText().toString());
            aktivitas.satuan = binding.spinnerWaktu.getSelectedItem().toString();
        }else{
            aktivitas.byTime = false;
            nilai = Long.parseLong(binding.txtDetik.getText().toString());
            aktivitas.satuan = binding.spinnerJarak.getSelectedItem().toString();
        }
        aktivitas.interval = nilai;
        aktivitas.namaAcara = binding.txtJudul.getText().toString();
        aktivitas.template = binding.txtTweet.getText().toString();
        aktivitas.hashTag = binding.txtHashtag.getText().toString();
        aktivitas.akuns.addAll(akuns);
        ObjectBox.getAktivitas().put(aktivitas);
        setResult(RESULT_OK);
        finish();
    }
}
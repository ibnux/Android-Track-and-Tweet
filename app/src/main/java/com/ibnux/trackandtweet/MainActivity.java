package com.ibnux.trackandtweet;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ibnux.trackandtweet.adapter.AktivitasAdapter;
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.databinding.ActivityMainBinding;
import com.ibnux.trackandtweet.ui.AddEditAktivitasActivity;
import com.ibnux.trackandtweet.ui.AktivitasActivity;
import com.ibnux.trackandtweet.ui.AkunListActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AktivitasAdapter.AktivitasCallback {
    ActivityMainBinding binding;
    AktivitasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.listAktivitas.setHasFixedSize(true);
        binding.listAktivitas.setLayoutManager(new LinearLayoutManager(this));

        // specify an adapter (see also next example)
        adapter = new AktivitasAdapter(this);
        binding.listAktivitas.setAdapter(adapter);

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();

        binding.btnAddAktivitas.setOnClickListener(this);
        binding.btnUser.setOnClickListener(this);

    }

    @Override
    public void onAktivitasClicked(Aktivitas aktivitas) {
        Intent i = new Intent(this, AktivitasActivity.class);
        i.putExtra("id",aktivitas.id);
        startActivity(i);
    }

    @Override
    public void onAktivitasLongClicked(Aktivitas aktivitas) {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Apa yang akan dilakukan?")
                .setMessage(aktivitas.namaAcara)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(MainActivity.this, AddEditAktivitasActivity.class);
                        i.putExtra("id",aktivitas.id);
                        startActivityForResult(i,220);
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("Yakin mau dihapus?")
                                .setMessage(aktivitas.namaAcara)
                                .setPositiveButton("Yakin", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ObjectBox.getAktivitas().remove(aktivitas);
                                        adapter.reload();
                                    }
                                })
                                .setNegativeButton("Batal",null)
                                .show();
                    }
                })
                .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        aktivitas.id = 0L;
                        aktivitas.waktu = System.currentTimeMillis();
                        Intent i = new Intent(MainActivity.this, AddEditAktivitasActivity.class);
                        i.putExtra("id",ObjectBox.getAktivitas().put(aktivitas));
                        startActivityForResult(i,220);
                        adapter.reload();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnAddAktivitas)
            startActivityForResult(new Intent(this, AddEditAktivitasActivity.class),220);
        else if(v==binding.btnUser)
            startActivity(new Intent(this, AkunListActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==220){
                adapter.reload();
            }
        }
    }
}
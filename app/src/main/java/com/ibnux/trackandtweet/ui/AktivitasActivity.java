package com.ibnux.trackandtweet.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ibnux.trackandtweet.R;
import com.ibnux.trackandtweet.Util;
import com.ibnux.trackandtweet.adapter.TweetAdapter;
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.Akun;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.data.Tweet;
import com.ibnux.trackandtweet.data.Tweet_;
import com.ibnux.trackandtweet.databinding.ActivityAktivitasBinding;
import com.ibnux.trackandtweet.services.TrackNTweetService;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import im.delight.android.location.SimpleLocation;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class AktivitasActivity extends AppCompatActivity implements View.OnClickListener, TweetAdapter.TweetCallback, OnMapReadyCallback, SimpleLocation.Listener {
    ActivityAktivitasBinding binding;
    Aktivitas aktivitas;
    TweetAdapter adapter;

    private SimpleLocation location;

    public int wGrafik = 0, hGrafik = 0, jarak=0;
    public long timeH = 0, timeL = 0, lama = 0;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Aktivitas");
        binding = ActivityAktivitasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.statistik.setDrawingCacheEnabled(true);
        binding.statistik.setDrawingCacheQuality(LinearLayout.DRAWING_CACHE_QUALITY_HIGH);
        binding.peta.setDrawingCacheEnabled(true);
        binding.peta.setDrawingCacheQuality(LinearLayout.DRAWING_CACHE_QUALITY_HIGH);

        if(timeL==0)
            timeL = System.currentTimeMillis();

        Intent i = getIntent();
        if(i.hasExtra("id")){
            aktivitas = ObjectBox.getAktivitas(i.getLongExtra("id",0));
            if(aktivitas==null) finish();
        }else{
            finish();
        }

        Util.log("Status "+aktivitas.status);
        LocalBroadcastManager.getInstance(this).registerReceiver(br,new IntentFilter(aktivitas.id+""));

        if(Util.isMyServiceRunning(TrackNTweetService.class,this)) {
            // jika ada service jalan, tanya apakah punya aktivitas ini
            Intent is = new Intent("TrackNTweetService");
            is.putExtra("isRunning", "isRunning");
            is.putExtra("broadcast", aktivitas.id + "");
            LocalBroadcastManager.getInstance(this).sendBroadcast(is);
        }else{
            if (aktivitas.status == 1) {
                aktivitas.status = 2;
                ObjectBox.putAktivitas(aktivitas);
            }
        }
        setData();
        location = new SimpleLocation(this, true, false, 1000, true);


        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(this);
        }
        binding.peta.onCreate(savedInstanceState);
        binding.peta.getMapAsync(this);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));


        hitungHeight();

        // specify an adapter (see also next example)
        adapter = new TweetAdapter(aktivitas.id,this);
        binding.recyclerView.setAdapter(adapter);

        if(aktivitas.status>0){
            binding.statistik.setVisibility(View.VISIBLE);
        }else{
            binding.statistik.setVisibility(View.GONE);
            location.beginUpdates();
            location.setListener(this);
        }
    }



    public void hitungHeight(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // 4:3
        wGrafik = displayMetrics.widthPixels;
        hGrafik = (wGrafik * 3 / 4)/2;
        Util.log("Ukuran: "+wGrafik+" "+hGrafik);
        ViewGroup.LayoutParams lp = binding.peta.getLayoutParams();
        lp.height = hGrafik;
        binding.peta.setLayoutParams(lp);
        binding.grafikElevation.setLayoutParams(lp);
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnMulai){
            if(!aktivitas.tweets.isEmpty()){
                startTracking();
            }else{
                sendFirstTweet();
            }
        }else if(v==binding.btnTweetStats){
            binding.btnTweetStats.setEnabled(false);
            sendStats();
        }else if(v==binding.txtJudul){
            Intent i = new Intent(this,AddEditAktivitasActivity.class);
            i.putExtra("id",aktivitas.id);
            startActivityForResult(i,223);
        }else if(v==binding.btnPause){
            Intent is = new Intent("TrackNTweetService");
            is.putExtra("pause","pause");
            is.putExtra("broadcast", aktivitas.id + "");
            LocalBroadcastManager.getInstance(this).sendBroadcast(is);
        }else if(v==binding.btnStop){
            new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Hentikan aktivitas?")
                    .setMessage(aktivitas.namaAcara)
                    .setPositiveButton("Stop", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent is = new Intent("TrackNTweetService");
                            is.putExtra("stop","stop");
                            is.putExtra("broadcast", aktivitas.id + "");
                            LocalBroadcastManager.getInstance(AktivitasActivity.this).sendBroadcast(is);
                        }
                    })
                    .setNeutralButton("Batal",null)
                    .show();

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    public void sendFirstTweet(){
        aktivitas.lastLatitude = location.getLatitude();
        aktivitas.lastLongitude = location.getLongitude();
        aktivitas.lastAltitude = location.getAltitude();
        new Thread(new Runnable() {
            public void run() {
                for(int n=0; n<aktivitas.akuns.size();n++) {
                    Akun akun = aktivitas.akuns.get(n);
                    Tweet tweet = new Tweet();

                    tweet.lat = aktivitas.lastLatitude;
                    tweet.lon = aktivitas.lastLongitude;
                    tweet.speed = 0;
                    tweet.alt = aktivitas.lastAltitude;
                    tweet.jarak = 0;
                    tweet.track = "";
                    tweet.text = aktivitas.template + " " + aktivitas.hashTag + "\n" +
                            "https://www.google.com/maps/place/" + tweet.lat + "," + tweet.lon + "/@" + tweet.lat + "," + tweet.lon + ",19z";
                    Util.log("Send Tweet: " + tweet.text);
                    try {
                        ConfigurationBuilder builder = new ConfigurationBuilder();
                        builder.setOAuthConsumerKey(akun.tkey);
                        builder.setOAuthConsumerSecret(akun.tsecret);
                        Configuration configuration = builder.build();
                        Twitter twitter = new TwitterFactory(configuration).getInstance(new AccessToken(akun.token, akun.secret));
                        StatusUpdate su = new StatusUpdate(tweet.text);
                        su.setLocation(new GeoLocation(tweet.lat, tweet.lat));
                        su.setDisplayCoordinates(true);

                        Status status = twitter.updateStatus(su);
                        tweet.username = akun.username;
                        tweet.userid = akun.userid;
                        tweet.statusId = status.getId();
                        tweet.TweetResultText = status.getText();
                        tweet.waktu = status.getCreatedAt().getTime();
                        tweet.aktivitas.setTargetId(aktivitas.id);
                        aktivitas.tweets.add(tweet);
                    }catch (Exception e){
                        Util.log("Exception: "+e.getMessage());
                        return;
                    }
                }
                ObjectBox.putAktivitas(aktivitas);
                startTracking();
                binding.recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.reload();
                    }
                });
            }
        }).start();
    }

    public void startTracking(){
        Intent is = new Intent(this, TrackNTweetService.class);
        is.putExtra("id",aktivitas.id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(is);
        }else startService(is);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==223){
                aktivitas = ObjectBox.getAktivitas(aktivitas.id);
                setData();
            }
        }
    }

    public void setData(){
        binding.txtJudul.setText(aktivitas.namaAcara);
        binding.txtTweet.setText(aktivitas.template);
        String username = "";
        int jml = aktivitas.akuns.size();
        for(int n=0;n<jml;n++){
            username += aktivitas.akuns.get(n).toString()+", ";
        }
        if(!username.isEmpty())
            username = username.substring(0, username.length()-2);
        binding.txtDetail.setText(aktivitas.hashTag+" akan diTweet ke "+username+" setiap "+aktivitas.interval+" "+aktivitas.satuan);
        binding.btnMulai.setOnClickListener(this);
        binding.btnPause.setOnClickListener(this);
        binding.btnStop.setOnClickListener(this);
        binding.txtJudul.setOnClickListener(this);
        binding.btnTweetStats.setOnClickListener(this);
        checkStatus();
    }

    public void checkStatus(){
        if(aktivitas.status==0 || aktivitas.status==2){
            binding.layoutTombol.setVisibility(View.GONE);
            binding.btnMulai.setVisibility(View.VISIBLE);
            binding.btnTweetStats.setVisibility(View.GONE);
        }else if(aktivitas.status==1){
            binding.layoutTombol.setVisibility(View.VISIBLE);
            binding.btnMulai.setVisibility(View.GONE);
            binding.btnTweetStats.setVisibility(View.GONE);
        }else if(aktivitas.status==3){
            binding.layoutTombol.setVisibility(View.GONE);
            binding.btnMulai.setVisibility(View.GONE);
            binding.btnTweetStats.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPositionChanged() {
        if(map!=null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
            map.animateCamera(cameraUpdate);
            location.endUpdates();
        }
    }

    @Override
    protected void onResume() {
        //Daftarkan broadcast berdasarkan id aktivitas
        if(aktivitas!=null)
            LocalBroadcastManager.getInstance(this).registerReceiver(br,new IntentFilter(aktivitas.id+""));
        binding.peta.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //hapus pendaftaran broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        location.endUpdates();
        binding.peta.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.peta.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.peta.onLowMemory();
    }

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //cek apakah yang running punya aktivitas yang dibuka
            if(intent.hasExtra("isRunning")) {
                if (intent.getBooleanExtra("isRunning", false)) {
                    if (aktivitas.id != intent.getLongExtra("id", 0L)) {
                        binding.layoutTombol.setVisibility(View.GONE);
                        binding.btnMulai.setVisibility(View.GONE);
                    } else {
                        if (aktivitas.status != 1) {
                            aktivitas.status = 1;
                        }
                    }
                }
            }else if(intent.hasExtra("tweet")){
                //reload setiap ngeTweet
                adapter.reload();
            }else{
                // ada perubahan status aktivitas
                aktivitas = ObjectBox.getAktivitas(aktivitas.id);
                setData();
            }
            checkStatus();
        }
    };

    @Override
    public void onTweetClicked(Tweet tweet) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/kangibnux/status/" + tweet.statusId));
            startActivity(i);
        }catch (Exception e){
            //
        }
    }



    public void hitungStatistik(String data){
        if(data!=null && data.length()>1) {
            new Thread(new Runnable() {
                public void run() {
                    String[] datas = data.split(";");
                    int jml = datas.length;
                    int smin = 999999, smax = 0, stot = 0, amin = 99999, amax = 0, atot = 0;
                    DataPoint[] altData = new DataPoint[jml];
                    List<LatLng> locData = new ArrayList<>();
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    Random r = new Random();
                    for (int n = 0; n < jml; n++) {
                        String[] dts = datas[n].split(",");
                        if (dts.length > 0) {
                            Float lat = Float.valueOf(dts[0]);
                            Float lon = Float.valueOf(dts[1]);
                            LatLng ll = new LatLng(lat, lon);
                            locData.add(ll);
                            builder.include(ll);
                            int speed = Integer.parseInt(dts[3]);
                            stot += speed;
                            if (speed > smax) smax = speed;
                            if (speed < smin)
                                if (speed > 0) smin = speed;
                            int alt = (int) Double.parseDouble(dts[2]);
                            altData[n] = new DataPoint(n, alt);
                            atot += alt;
                            if (alt > amax) amax = alt;
                            if (alt < amin) amin = alt;

                        }
                    }


                    for (int n = 0; n < jml; n++) {
                        altData[n] = new DataPoint(altData[n].getX(), altData[n].getY() - amin);
                        ;
                    }
                    amax = amax - amin;
                    atot = atot - amin;
                    amin = 0;

                    //-6.36432, -6.3641148, 106.70431, 106.70442


                    final int fsmin = smin, fsmax = smax, famin = amin, famax = amax, fstot = stot, fatot = atot;
                    final DataPoint[] fAltData = altData;
                    final List<LatLng> fLocData = locData;
                    final LatLngBounds bounds = builder.build();
                    binding.statistik.post(new Runnable() {
                        public void run() {
                            binding.topSpeed.setText(fsmax + "m/m");
                            binding.lowSpeed.setText(fsmin + "m/m");
                            binding.avgSpeed.setText((fstot / jml) + "m/m");
                            binding.topAltitude.setText(famax + "m");
                            binding.lowAltitude.setText(famin + "m");
                            binding.avgAltitude.setText((fatot / jml) + "m");
                            binding.grafikElevation.removeAllSeries();
                            binding.grafikElevation.addSeries(new LineGraphSeries<>(fAltData));
                            binding.grafikElevation.setTitle("Altitude (meter)");
                            binding.grafikElevation.getViewport().setScrollable(true);
                            binding.grafikElevation.getViewport().setScrollableY(true);
                            binding.grafikElevation.getViewport().setScalable(true);
                            binding.grafikElevation.getGridLabelRenderer().setHorizontalLabelsVisible(false);
                            if(map!=null) {
                                map.addPolyline(new PolylineOptions()
                                        .addAll(fLocData)
                                        .width(5)
                                        .color(Color.RED));
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                                map.animateCamera(cu);
                            }
                        }
                    });
                }
            }).start();
        }
        hitungLamaDanJarak();
    }



    public void hitungLamaDanJarak(){
        lama = lama/1000L;
        if(lama>3600){
            int jam = (int)(lama/3600);
            int menit = (int)((lama%3600)/60);
            binding.times.setText(jam+"j"+menit+"m");
        }else{
            int menit = (int)(lama/60);
            binding.times.setText(menit+"m");
        }
        if(jarak>1000){
            int km = (jarak/1000);
            int m = (jarak%1000)/10;
            binding.distance.setText(km+","+m+"km");
        }
    }

    public void sendStats(){
        Util.log("sendStats");
        binding.peta.setVisibility(View.GONE);
        new Thread(new Runnable() {
            public void run() {
                binding.statistik.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AktivitasActivity.this, "Mengirim statistik", Toast.LENGTH_SHORT).show();
                    }
                });
                File file = saveBitMap();
                if (file != null) {
                    for (int n = 0; n < aktivitas.akuns.size(); n++) {
                        Akun akun = aktivitas.akuns.get(n);
                        Util.log("akun: " + akun.username);
                        Tweet tweet = new Tweet();
                        ConfigurationBuilder builder = new ConfigurationBuilder();
                        builder.setOAuthConsumerKey(akun.tkey);
                        builder.setOAuthConsumerSecret(akun.tsecret);
                        Configuration configuration = builder.build();
                        Twitter twitter = new TwitterFactory(configuration).getInstance(new AccessToken(akun.token, akun.secret));
                        StatusUpdate su = new StatusUpdate(aktivitas.template + " " + aktivitas.hashTag + "\nStatistik");
                        su.setMedia(file);
                        if (aktivitas.tweets.size() > 0) {
                            Tweet tw = ObjectBox.getTweet().query()
                                    .equal(Tweet_.aktivitasId, aktivitas.id)
                                    .and()
                                    .equal(Tweet_.userid, akun.userid)
                                    .or()
                                    .equal(Tweet_.username, akun.username)
                                    .orderDesc(Tweet_.waktu)
                                    .build().findFirst();
                            if (tw != null && tw.statusId > 0) {
                                Util.log("Last status " + tw.statusId);
                                su.setInReplyToStatusId(tw.statusId);
                            }
                        }
                        Util.log("sendTweet");
                        try {
                            Status status = twitter.updateStatus(su);
                            tweet.username = akun.username;
                            tweet.userid = akun.userid;
                            tweet.statusId = status.getId();
                            tweet.TweetResultText = status.getText();
                            tweet.waktu = status.getCreatedAt().getTime();
                            tweet.aktivitas.setTargetId(aktivitas.id);
                            aktivitas.tweets.add(tweet);
                            Util.log("tweet "+status.getId());
                        } catch (TwitterException e) {
                            Util.log("Exception: " + e.getMessage());
                        }
                    }
                    ObjectBox.putAktivitas(aktivitas);
                    binding.statistik.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AktivitasActivity.this, "Sukses Mengirim statistik", Toast.LENGTH_SHORT).show();
                            binding.peta.setVisibility(View.VISIBLE);
                            adapter.reload();
                        }
                    });
                } else {
                    Util.log("null bitmap");
                    binding.statistik.post(new Runnable() {
                        @Override
                        public void run() {
                            binding.peta.setVisibility(View.VISIBLE);
                            Toast.makeText(AktivitasActivity.this, "Gagal Mengirim statistik", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private File saveBitMap() {
        Util.log("saveBitMap");
        String filename = "temp.jpg";
        OutputStream fos;
        File img = null;
        try {
            img = new File(getCacheDir(),filename);
            fos = new FileOutputStream(img);
            Bitmap bitmap = getBitmapFromView(binding.statistik);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan Gambar", Toast.LENGTH_SHORT).show();
            Log.i("TAG", "There was an issue saving the image.");
        }
        return img;
    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}
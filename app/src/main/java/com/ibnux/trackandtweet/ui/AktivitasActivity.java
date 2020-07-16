package com.ibnux.trackandtweet.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ibnux.trackandtweet.R;
import com.ibnux.trackandtweet.Util;
import com.ibnux.trackandtweet.adapter.TweetAdapter;
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.Akun;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.data.Tweet;
import com.ibnux.trackandtweet.databinding.ActivityAktivitasBinding;
import com.ibnux.trackandtweet.services.TrackNTweetService;

import im.delight.android.location.SimpleLocation;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class AktivitasActivity extends AppCompatActivity implements View.OnClickListener, TweetAdapter.TweetCallback{
    ActivityAktivitasBinding binding;
    Aktivitas aktivitas;
    TweetAdapter adapter;

    private SimpleLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Aktivitas");
        binding = ActivityAktivitasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tableStats.setVisibility(View.GONE);

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
        location = new SimpleLocation(this);
        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(this);
        }

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // specify an adapter (see also next example)
        adapter = new TweetAdapter(aktivitas.id,this);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnMulai){
            if(!aktivitas.tweets.isEmpty()){
                startTracking();
            }else{
                sendFirstTweet();
            }
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
        checkStatus();
    }

    public void checkStatus(){
        if(aktivitas.status==0 || aktivitas.status==2){
            binding.layoutTombol.setVisibility(View.GONE);
            binding.btnMulai.setVisibility(View.VISIBLE);
        }else if(aktivitas.status==1){
            binding.layoutTombol.setVisibility(View.VISIBLE);
            binding.btnMulai.setVisibility(View.GONE);
        }else if(aktivitas.status==3){
            binding.layoutTombol.setVisibility(View.GONE);
            binding.btnMulai.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        //Daftarkan broadcast berdasarkan id aktivitas
        if(aktivitas!=null)
            LocalBroadcastManager.getInstance(this).registerReceiver(br,new IntentFilter(aktivitas.id+""));
        location.beginUpdates();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //hapus pendaftaran broadcast
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        location.endUpdates();
        super.onPause();
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
}
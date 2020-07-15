package com.ibnux.trackandtweet.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.StrictMode;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ibnux.trackandtweet.R;
import com.ibnux.trackandtweet.Util;
import com.ibnux.trackandtweet.data.Aktivitas;
import com.ibnux.trackandtweet.data.Akun;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.data.Tweet;
import com.ibnux.trackandtweet.data.Tweet_;
import com.ibnux.trackandtweet.ui.AktivitasActivity;

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

public class TrackNTweetService extends Service implements SimpleLocation.Listener {
    NotificationManager notificationManager;
    Aktivitas aktivitas;
    CountDownTimer cdt;
    int lastShownNotificationId;
    PendingIntent pendingIntent;
    int distance, lastDistance, distanceInterval, speed = 0;
    double lastLat = 0, lastLon = 0;
    double latitude, longitude, altitude, jarak;
    String track="";
    private SimpleLocation location;

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Util.log("TrackNTweetService BroadcastReceiver onReceive");
            if (intent.hasExtra("isRunning")) {
                Util.log("TrackNTweetService BroadcastReceiver onReceive isRunning");
                if (aktivitas != null) {
                    Intent i = new Intent(intent.getStringExtra("broadcast"));
                    i.putExtra("isRunning", true);
                    i.putExtra("id", aktivitas.id);
                    i.putExtra("name", aktivitas.namaAcara);
                    LocalBroadcastManager.getInstance(TrackNTweetService.this).sendBroadcast(i);
                } else {
                    Intent i = new Intent(intent.getStringExtra("broadcast"));
                    i.putExtra("isRunning", false);
                    LocalBroadcastManager.getInstance(TrackNTweetService.this).sendBroadcast(i);
                }
            } else if (intent.hasExtra("stop")) {
                Util.log("TrackNTweetService BroadcastReceiver onReceive stop");
                sendTweet("SELESAI: ");
                if (cdt != null) cdt.cancel();
                aktivitas.status = 3;
                ObjectBox.putAktivitas(aktivitas);
                notificationManager.cancel(lastShownNotificationId);
                Intent i = new Intent(intent.getStringExtra("broadcast"));
                i.putExtra("stopped", false);
                LocalBroadcastManager.getInstance(TrackNTweetService.this).sendBroadcast(i);
                stopSelf();
            } else if (intent.hasExtra("pause")) {
                Util.log("TrackNTweetService BroadcastReceiver onReceive pause");
                sendTweet("ISTIRAHAT: ");
                if (cdt != null) cdt.cancel();
                aktivitas.status = 2;
                ObjectBox.putAktivitas(aktivitas);
                notificationManager.cancel(lastShownNotificationId);
                Intent i = new Intent(intent.getStringExtra("broadcast"));
                i.putExtra("paused", false);
                LocalBroadcastManager.getInstance(TrackNTweetService.this).sendBroadcast(i);
                stopSelf();
            }
        }
    };

    public TrackNTweetService() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleStart(intent, startId);
        return START_NOT_STICKY;
    }

    private void handleStart(Intent intent, int startId) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
        if (notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        aktivitas = ObjectBox.getAktivitas(intent.getLongExtra("id", 0L));
        if (aktivitas == null) {
            return;
        }

        lastLon = aktivitas.lastLongitude;
        lastLat = aktivitas.lastLatitude;
        altitude = aktivitas.lastAltitude;
        distance = aktivitas.lastDistance;

        location = new SimpleLocation(this, true, false, 1000, true);
        if (!location.hasLocationEnabled()) {
            Util.log("no gps");
            Intent i = new Intent(aktivitas.id + "");
            i.putExtra("nogps", "nogps");
            LocalBroadcastManager.getInstance(TrackNTweetService.this).sendBroadcast(i);
            return;
        }
        location.setListener(this);
        location.beginUpdates();


        Intent pi = new Intent(this, AktivitasActivity.class);
        pi.putExtra("id", aktivitas.id);
        pendingIntent = PendingIntent.getActivity(this, 0, pi, 0);

        aktivitas.status = 1;
        ObjectBox.putAktivitas(aktivitas);
        Intent is = new Intent(aktivitas.id + "");
        is.putExtra("running", "running");
        LocalBroadcastManager.getInstance(this).sendBroadcast(is);

        if (aktivitas.byTime) {
            long interval = aktivitas.interval;
            if (aktivitas.satuan.equals("menit"))
                interval *= 60;
            interval *= 1000L;
            int max = (int) (interval / 1000L);
            showNotif(this, "Mulai beraktivitas", max - 1, max, 32);
            startCountDown(max, interval);
        } else {
            long interval = aktivitas.interval;
            if (!aktivitas.satuan.equals("meter"))
                interval *= 1000;
            distanceInterval = (int)interval;
            lastDistance = distance%distanceInterval;
            showNotif(this, "Mulai beraktivitas", 33);
        }
    }

    private void startCountDown(int max, long interval) {
        cdt = new CountDownTimer(interval, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                showNotif(TrackNTweetService.this,distance + " meter, " + speed + " m/menit", (int) (millisUntilFinished / 1000L), max, 32);
            }

            @Override
            public void onFinish() {
                sendTweet("");
                startCountDown(max, interval);
            }
        };
        cdt.start();
    }

    private void showNotif(Service yourService, String content, int progress, int max, int notificationId) {

        final NotificationCompat.Builder builder = getNotificationBuilder(yourService, "tracking"); //Low importance prevent visual appearance for this notification channel on top
        builder.setOngoing(true)
                .setContentIntent(pendingIntent)
                .setProgress(max, progress, false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(aktivitas.namaAcara)
                .setContentText(content);

        Notification notification = builder.build();
        yourService.startForeground(notificationId, notification);

        if (notificationId != lastShownNotificationId) {
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = notificationId;
    }

    private void showNotif(Service yourService, String content, int notificationId) {

        final NotificationCompat.Builder builder = getNotificationBuilder(yourService, "tracking"); //Low importance prevent visual appearance for this notification channel on top
        builder.setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(aktivitas.namaAcara)
                .setContentText(content);

        Notification notification = builder.build();
        yourService.startForeground(notificationId, notification);

        if (notificationId != lastShownNotificationId) {
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = notificationId;
    }

    public NotificationCompat.Builder getNotificationBuilder(Context context, String channelId) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, NotificationManager.IMPORTANCE_LOW);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = "Tweeting Services";

        if (notificationManager != null) {
            NotificationChannel nChannel = notificationManager.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                notificationManager.createNotificationChannel(nChannel);
            }
        }
    }

    @Override
    public void onCreate() {
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("TrackNTweetService"));
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        location.endUpdates();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onPositionChanged() {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = (int) Math.round(((location.getSpeed() * 3.6) * 1000) / 60);;
        altitude  = location.getAltitude();
        track += latitude + "," + longitude + "," + altitude + "," + speed+";";
        jarak = 0;
        if (lastLat != 0 && lastLon != 0) {
            jarak = SimpleLocation.calculateDistance(lastLat, lastLon, latitude, longitude);
            distance += Math.round(jarak);
        }
        lastLat = latitude;
        lastLon = longitude;


        if (!aktivitas.byTime) {
            showNotif(this, distance+" meter, Kecepatan " + speed + " meter/menit", 33);
            lastDistance += jarak;
            if (lastDistance >= distanceInterval) {
                sendTweet("");
            }
        }
        Util.log("Location: " + latitude + "," + longitude + "," + altitude + "z," + speed + " mm, "+lastDistance+"/"+distanceInterval+"," + distance + "meter");
    }

    public void sendTweet(String statuss) {

        for(int n=0; n<aktivitas.akuns.size();n++) {
            Akun akun = aktivitas.akuns.get(n);
            Tweet tweet = new Tweet();
            tweet.lat = latitude;
            tweet.lon = longitude;
            tweet.speed = speed;
            tweet.alt = altitude;
            tweet.jarak = distance;
            tweet.track = track;
            tweet.text = statuss+aktivitas.template + " " + aktivitas.hashTag + "\n" +
                    "Kecepatan: " + tweet.speed + " meter/menit\n" +
                    "Telah menempuh: " + tweet.jarak + " meter\n"+
                    "https://www.google.com/maps/place/" + tweet.lat + "," + tweet.lon + "/@" + tweet.lat + "," + tweet.lon + ",19z";
            Util.log("Send Tweet: " + tweet.text);
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(akun.tkey);
                builder.setOAuthConsumerSecret(akun.tsecret);
                Configuration configuration = builder.build();
                Twitter twitter = new TwitterFactory(configuration).getInstance(new AccessToken(akun.token, akun.secret));
                StatusUpdate su = new StatusUpdate(tweet.text);
                su.setLocation(new GeoLocation(tweet.lat, tweet.lat));
                su.setDisplayCoordinates(true);
                if(aktivitas.tweets.size()>0) {
                    Tweet tw = ObjectBox.getTweet().query()
                            .equal(Tweet_.aktivitasId,aktivitas.id)
                            .and()
                            .equal(Tweet_.userid,akun.userid)
                            .or()
                            .equal(Tweet_.username,akun.username)
                            .orderDesc(Tweet_.waktu)
                            .build().findFirst();
                    if(tw!=null && tw.statusId>0) {
                        Util.log("Last status "+tw.statusId);
                        su.setInReplyToStatusId(tw.statusId);
                    }
                }

            try {
                Status status = twitter.updateStatus(su);
                tweet.username = akun.username;
                tweet.userid = akun.userid;
                tweet.statusId = status.getId();
                tweet.TweetResultText = status.getText();
                tweet.waktu = status.getCreatedAt().getTime();
                tweet.aktivitas.setTargetId(aktivitas.id);
                aktivitas.tweets.add(tweet);
            }catch (TwitterException e){
                Util.log("Exception: "+e.getMessage());
            }
        }
        aktivitas.lastLatitude = latitude;
        aktivitas.lastLongitude = longitude;
        aktivitas.lastAltitude = altitude;
        aktivitas.lastDistance = distance;
        ObjectBox.putAktivitas(aktivitas);
        Intent i = new Intent(aktivitas.id+"");
        i.putExtra("tweet", "tweet");
        LocalBroadcastManager.getInstance(TrackNTweetService.this).sendBroadcast(i);
        lastDistance = 0;
        track = "";
    }
}

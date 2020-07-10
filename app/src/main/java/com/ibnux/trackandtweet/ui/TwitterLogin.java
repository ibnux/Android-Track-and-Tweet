package com.ibnux.trackandtweet.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.trackandtweet.data.Akun;
import com.ibnux.trackandtweet.data.ObjectBox;
import com.ibnux.trackandtweet.databinding.ActivityTwitterLoginBinding;

import im.delight.android.webview.AdvancedWebView;
import twitter4j.JSONObject;

public class TwitterLogin extends AppCompatActivity implements AdvancedWebView.Listener {
    ActivityTwitterLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTwitterLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.mWebView.setListener(this, this);
        binding.mWebView.loadUrl("https://twitter.ibnux.net/");
        binding.mWebView.addPermittedHostname("twitter.ibnux.net");
        binding.mWebView.addPermittedHostname("api.twitter.com");
        binding.mWebView.addPermittedHostname("twitter.com");
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        binding.mWebView.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        binding.mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.mWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        binding.mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (!binding.mWebView.onBackPressed()) { return; }
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        binding.progressBar.setIndeterminate(true);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        binding.progressBar.setVisibility(View.GONE);
        if(url.contains("/selesai/")){
            if(url.contains("?session=")){
                try {
                    String session = new String(Base64.decode(url.split("session=")[1], Base64.DEFAULT), "UTF-8");
                    JSONObject json = new JSONObject(session);
                    JSONObject user = json.getJSONObject("user");
                    Akun akun = new Akun(user.getString("screen_name"),user.getString("id_str"),json.getString("token"),json.getString("secret"));
                    akun.tkey = json.getString("tkey");
                    akun.tsecret = json.getString("tsecret");
                    akun.avatar = user.getString("profile_image_url_https");
                    ObjectBox.getAkun().put(akun);
                    setResult(RESULT_OK);
                }catch (Exception e){
                    Toast.makeText(this,"Gagal Memvalidasi login",Toast.LENGTH_LONG).show();
                }
            }else if(url.contains("?error=")){
                Toast.makeText(this,"Gagal Login Twitter",Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        binding.progressBar.setVisibility(View.GONE);
        finish();
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }
}
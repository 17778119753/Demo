package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.banner.Banner;
import com.example.myapplication.banner.listener.OnBannerListener;
import com.example.myapplication.banner.loader.GlideImageLoader;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnBannerListener {
    private Banner banner;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        banner = (Banner) findViewById(R.id.banner);


        List<String> url = Arrays.asList(getResources().getStringArray(R.array.url));
        banner.setImages(url)
                .setImageLoader(new GlideImageLoader())
                .setOnBannerListener(this)
                .start();
        Log.e("url","size ="+url.size());
    }

    @Override
    public void OnBannerClick(int position) {
        Log.e("OnBannerClick","点击项："+position);

    }
}
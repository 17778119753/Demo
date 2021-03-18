package com.example.myapplication.banner.loader;

import android.content.Context;
import android.widget.ImageView;

import com.example.myapplication.banner.ImageLoaderInterface;


public abstract class ImageLoader implements ImageLoaderInterface<ImageView> {

    @Override
    public ImageView createImageView(Context context) {
        ImageView imageView = new ImageView(context);
        return imageView;
    }

}

package com.example.myapplication.banner.loader;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;


public class GlideImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        //具体方法内容自己去选择，次方法是为了减少banner过多的依赖第三方包，所以将这个权限开放给使用者去选择
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.no_banner)//图片加载出来前，显示的图片
                .fallback( R.drawable.no_banner) //url为空的时候,显示的图片
                .error(R.drawable.no_banner);//图片加载失败后，显示的图片

        Glide.with(context.getApplicationContext())
                .load(path)
                .apply(options)
                .into(imageView);
    }

//    @Override
//    public ImageView createImageView(Context context) {
//        //圆角
//        return new RoundAngleImageView(context);
//    }
}

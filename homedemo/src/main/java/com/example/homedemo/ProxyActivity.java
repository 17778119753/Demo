package com.example.homedemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyActivity extends Activity {

  public static final String PLUGIN_DEX_PATH = "plugin.dex.path";
  public static final String PLUGIN_ACTIIVTY_CLASS_NAME = "plugin.activity.class.name";
  private static final String TAG = ProxyActivity.class.getSimpleName() + ":chenhan";
  Activity mPluginActivity = null;
  String pluginDexPath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.e("chenhan", "主工程中的onCreate方法被调用了");
    Intent intent = getIntent();
    if (intent != null) {
      // 从Intent中获得要启动的功能Apk的路径和Activity完整类名
      pluginDexPath = intent.getStringExtra(PLUGIN_DEX_PATH);
      String pluginActivityClassName = intent.getStringExtra(PLUGIN_ACTIIVTY_CLASS_NAME);

      if (TextUtils.isEmpty(pluginDexPath) || TextUtils.isEmpty(pluginActivityClassName)) {
        Log.e(TAG, "pluginDexPath or pluginActivityClassName = null");
        return;
      }

      loadApkResources(); // 加载插件资源

      // 根据apk路径加载apk代码到DexClassLoader中
      File dexOutputDir = this.getDir("dex", 0);
      DexClassLoader dexClassLoader =
          new DexClassLoader(
              pluginDexPath,
              dexOutputDir.getAbsolutePath(),
              null,
              ClassLoader.getSystemClassLoader());
      if (dexClassLoader == null) {
        Log.e("chenhan", "dexClassLoader == null,是为什么呢？");
        return;
      }

      // 从DexClassLoader中获得功能Activity Class对象并通过反射创建一个功能Activity实例，通过newInstance(),拿到实例是为了设置代
      Class pluginActivityClass = null;
      try {
        pluginActivityClass = dexClassLoader.loadClass(pluginActivityClassName);
        mPluginActivity = (Activity) pluginActivityClass.newInstance();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      }

      // 调用功能Activity的setProxyActivity方法，填充其代理方法
      try {
        Method setProxyActivityMethod =
            pluginActivityClass.getDeclaredMethod("setProxyActivity", Activity.class);
        setProxyActivityMethod.invoke(mPluginActivity, this);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }

      // 调用功能Activity实例的onCreate方法，填充其代理方法
      try {
        Method onCreateMethod = Activity.class.getDeclaredMethod("onCreate", Bundle.class);
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(mPluginActivity, savedInstanceState);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  // 加载插件Apk的资源
  private void loadApkResources() {
    try {
      AssetManager assetManager = AssetManager.class.newInstance(); // 通过反射创建一个AssetManager对象
      Method addAssetPathMethod =
          AssetManager.class.getDeclaredMethod(
              "addAssetPath", String.class); // 获得AssetManager对象的addAssetPath方法
      addAssetPathMethod.invoke(
          assetManager, pluginDexPath); // 调用AssetManager的addAssetPath方法，将apk的资源添加到AssetManager中管理
      mPluginResourcs =
          new Resources(
              assetManager,
              super.getResources().getDisplayMetrics(),
              super.getResources().getConfiguration()); // 根据AssetMananger创建一个Resources对象
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  Resources mPluginResourcs = null;
  // 重写ProxyActivity的getResources方法，让其返回插件Apk的资源对象
  @Override
  public Resources getResources() {
    if (mPluginResourcs != null) {
      return mPluginResourcs;
    }
    return super.getResources();
  }
}

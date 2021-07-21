插件化相关
第一步：实现一个Apk中加载另外一个Apk并运行
动态代理：通过动态代理实现，因为加载运行Apk是PackageManagerService（PMS）和ActivityManagerService（AMS）一起完成的，两个APK吊起属于进程间的通信，无法绕过Bind，因此采取欺骗PMS和AMS
主Demo中定义一个壳子ProxyActivity
功能Demo中定义一个启动LoadActivity，加载到对应通过ClassLoad中
主Demo通过反射拿到LoadActivity的实例，在ProxyActivity的生命周期中回调LoadActivity对应的生命周期

第二步：动态加载Apk到对应的ClassLoader中
2.1、DexClassLoader ：可以加载文件系统上任意的jar、dex、apk
2.2、PathClassLoader ：可以加载/data/app目录下的apk，这也意味着，它只能加载已经安装的apk（安装目录）
因此这里采用DexClassLoader来加载,主Demo中根据功能Apk路径将功能APk解析并加载到ClassLoader中，详情看代码HomeDemo/ProxyActivity中

注意：通过以上步骤，我们可以成功唤起插件的Activity，但是此时我们发现页面是空的，即相关的资源均没有加载出来，以及无法跳转(需要xml中注册Activity等，也需要访问功能插件资源)----这里我们知道加载资源用的是上下文context，这个context是属于功能Apk的
那么我们在代码中创建一个View视图，加载到setContentView中呢？答案是可以的。详细看代码PluginDemo01/MainActivity

第三步：访问资源文件
怎么访问插件中的资源文件呢，我们知道上下文信息是拿到资源文件的核心，最终是通过AssetMansger拿到Resources，因此关键在AssetMansger
1.通过反射拿到主工程AssetMansger实例
2.进而获取AssetMansger中的addAssetPath方法，通过这个方法可以增加一个Asset的path，将插件Asset插入到主工程的AssetManager中
3.反射调用invoke，将插件dexclassload的path传入进去.//addAssetPathMethod.invoke(assetManager, mPluginDexPath);
4.重新实例化Resources
5.重写getResources方法，返回包含插件Resources的放大，即该壳子ProxyActivity中已经可以访问插件资源了
详情看代码HomeDemo/ProxyActivity中
注意：采用这种动态代理的插件化方式，需要继承Activity，不能继承AppCompatActivity，否则会报'void androidx.appcompat.widget.DecorContentParent.setWindowCallback(android.view.Window$Callback)' on a null object reference





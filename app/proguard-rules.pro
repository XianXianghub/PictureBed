-keep class com.dawn.** { *; }
-keep class com.hsm.** { *; }
-keep class nlscan.** { *; }
-keep class com.zebra.** { *; }



-keep class com.meferi.scanner.ICameraSwitch { *; }
-keep class com.meferi.scanner.IFwScan { *; }
-keep class com.meferi.scanner.IReadActionListener { *; }
-keep class com.meferi.scanner.IScanCallback { *; }
-keep class com.meferi.scanner.IScanChangeListener { *; }
-keep class com.meferi.scanner.IScanImgSend { *; }
-keep class com.meferi.scanner.IScanListener { *; }
-keep class com.meferi.scanner.IStatusActionListener { *; }
-keep class com.meferi.scanner.IScanServer { *; }
-keep class com.meferi.sdk.** { *; }



-libraryjars libs/sdkapi.jar


#-keep class com.meferi.mssql.db.** { *; }

-keepattributes *Annotation*

# 保留所有继承 RoomDatabase 的类及构造函数
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}


# 保留 Room 相关的类（Database、Dao、Entity）
-keep class com.meferi.mssql.db.** { *; }




# 保留实现了 Parcelable 接口的类的 CREATOR 字段
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# 保留所有字段名和类名  JSON 序列化
-keep class com.meferi.mssql.bean.ProductBean {
    <fields>;
    <methods>;
}

-keep class com.bumptech.** { *; }

-keep class com.google.zxing.** { *; }



-keep class org.apache.**{*;}
-dontwarn org.apache**
-keep class org.slf4j.**{*;}
-dontwarn org.slf4j**

-keep class com.google.**{*;}
-dontwarn com.google**

-keep class org.json.**{*;}
-dontwarn org.json**


-keep class java.beans.** { *; }
-keep class org.apache.** { *; }
-keep class org.apache.** { *; }

-keep class android.annotation.** { *; }
-dontwarn com.msc.**
-dontshrink
-dontoptimize

# 指定代码的压缩级别 0 - 7(指定代码进行迭代优化的次数，在Android里面默认是5，这条指令也只有在可以优化时起作用。)
-optimizationpasses 5
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers
#不进行优化，建议使用此选项，
-dontoptimize
 # 不进行预校验,Android不需要,可加快混淆速度。
-dontpreverify
# 屏蔽警告
-ignorewarnings
# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
 #优化时允许访问并修改有修饰符的类和类的成员，这可以提高优化步骤的结果。
# 比如，当内联一个公共的getter方法时，这也可能需要外地公共访问。
# 虽然java二进制规范不需要这个，要不然有的虚拟机处理这些代码会有问题。当有优化和使用-repackageclasses时才适用。
#指示语：不能用这个指令处理库中的代码，因为有的类和类成员没有设计成public ,而在api中可能变成public
-allowaccessmodification
#当有优化和使用-repackageclasses时才适用。
-repackageclasses ''
 # 混淆时记录日志(打印混淆的详细信息)
 # 这句话能够使我们的项目混淆后产生映射文件
 # 包含有类名->混淆后类名的映射关系
-verbose
# ----------------------------- 默认保留 -----------------------------
# 保持哪些类不被混淆
#继承activity,application,service,broadcastReceiver,contentprovider....不进行混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep class android.support.** {*;}## 保留support下的所有类及其内部类
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
#表示不混淆上面声明的类，最后这两个类我们基本也用不上，是接入Google原生的一些服务时使用的。
#----------------------------------------------------
# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**


-keep public class * extends android.annotation.**
#表示不混淆任何包含native方法的类的类名以及native方法名，这个和我们刚才验证的结果是一致
-keepclasseswithmembernames class * {
    native <methods>;
}
#这个主要是在layout 中写的onclick方法android:onclick="onClick"，不进行混淆
#表示不混淆Activity中参数是View的方法，因为有这样一种用法，在XML中配置android:onClick=”buttonClick”属性，
#当用户点击该按钮时就会调用Activity中的buttonClick(View view)方法，如果这个方法被混淆的话就找不到了
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
#表示不混淆枚举中的values()和valueOf()方法，枚举我用的非常少，这个就不评论了
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#表示不混淆任何一个View中的setXxx()和getXxx()方法，
#因为属性动画需要有相应的setter和getter的方法实现，混淆了就无法工作了。
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#表示不混淆Parcelable实现类中的CREATOR字段，
#毫无疑问，CREATOR字段是绝对不能改变的，包括大小写都不能变，不然整个Parcelable工作机制都会失败。
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
# 这指定了继承Serizalizable的类的如下成员不被移除混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 保留R下面的资源
-keep class **.R$* {
 *;
}
#不混淆资源类下static的
-keepclassmembers class **.R$* {
    public static <fields>;
}
# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}
# 保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class com.example.odm.garbagesorthelper.widget.** { *; }
#
#----------------------------- WebView(项目中没有可以忽略) -----------------------------
#
#webView需要进行特殊处理
#在app中与HTML5的JavaScript的交互进行特殊处理
#我们需要确保这些js要调用的原生方法不能够被混淆，于是我们需要做如下处理：
#
#---------------------------------实体类---------------------------
#--------(实体Model不能混淆，否则找不到对应的属性获取不到值)-----
#
-keep class com.example.odm.garbagesorthelper.model.entity.** { *; }
-dontwarn  com.example.odm.garbagesorthelper.model.entity.**
#对含有反射类的处理
-keep class com.example.odm.garbagesorthelper.utils.** { *; }
#
# ----------------------------- 其他的 -----------------------------
#
# 删除代码中Log相关的代码
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}
# 保持测试相关的代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
#
# ----------------------------- 第三方库、框架、SDK -----------------------------
#logger
-dontwarn com.orhanobut.logger.**
-keep class com.orhanobut.logger.**{*;}
-keep interface com.orhanobut.logger.**{*;}
# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}
-keep interface com.google.gson.**{*;}
#gson
#如果用到Gson解析包的，直接添加下面这几行就能成功混淆，不然会报错。
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Application classes that will be serialized/deserialized over Gso
# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
#-keepattributes Signature-keepattributes Exceptions
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#LiveEventBus
-dontwarn com.jeremyliao.liveeventbus.**
-keep class com.jeremyliao.liveeventbus.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }
#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#utilcodex
-keep class  com.blankj.utilcode.util.** { *; }
#
-keep class site.gemus.** { *; }
#XUI
-keep class com.xuexiang.xui.** { *; }


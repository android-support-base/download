package com.amlzq.asb.downloadmanager;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;

/**
 * 下载服务
 */
public class DownloadService extends Service {

    DownloadStatusReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        task();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void task() {

        Uri uri = Uri.parse("http://s1.music.126.net/download/android/CloudMusic_2.8.1_official_4.apk");
        DownloadManager.Request request = new DownloadManager.Request(uri);

        // 通知栏中将出现的内容
        request.setTitle("正在下载最新安装包");
        request.setDescription("网易云安装包");

        request.setAllowedOverRoaming(false);//漫游网络是否可以下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);

        //设置文件类型，可以在下载结束后自动打开该文件
// MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
// String mimeString = mimeTypeMap.getMimeTypeFromExtension(
// MimeTypeMap.getFileExtensionFromUrl(urlStr));
// request.setMimeType(mimeString);

        // 在通知栏中显示，默认是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        // 下载过程和下载完成后通知栏有通知消息。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
                | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 设置文件的存放目录
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ease-music-by-dm.apk");
// request.setDestinationInExternalFilesDir(context,type, filepath) // 也可以自己指定下载路径

        // 7.0以上的系统适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.setRequiresDeviceIdle(false);
            request.setRequiresCharging(false);
        }

        // >API 11的设备允许扫描
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //表示允许MediaScanner扫描到这个文件，默认不允许。
            request.allowScanningByMediaScanner();
        }

        // 制定下载的文件类型为APK
        request.setMimeType("application/vnd.android.package-archive");

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        mReceiver = new DownloadStatusReceiver();
        mReceiver.setDownloadId(downloadId);
        registerReceiver(mReceiver, new IntentFilter());

        // 以下两行代码可以让下载的apk文件被直接安装而不用使用FileProvider,系统7.0或者以上才启动。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

}

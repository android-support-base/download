package com.amlzq.asb.downloadmanager;

import android.support.annotation.NonNull;

/**
 * 下载状态监听器
 */
public interface DownloadStatusListener {

    public void onRetry(@NonNull DownloadInfo info);

    public void onConnected(@NonNull DownloadInfo info);

    public void onProgress(@NonNull DownloadInfo info);

    public void onStarted(@NonNull DownloadInfo info);

    public void onCompleted(@NonNull DownloadInfo info);

    public void onCanceled(@NonNull DownloadInfo info);

    public void onError(@NonNull DownloadInfo info, @NonNull Exception e);

    public void onWarn(@NonNull DownloadInfo info);

}

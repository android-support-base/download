package com.zswh.game.box.game.manager;

import android.support.annotation.NonNull;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;

/**
 * 游戏下载监听
 */
public interface GameDownloadListener {
    public void onRetry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause);

    public void onConnected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength);

    public void onProgress(@NonNull DownloadTask task, long currentOffset, long totalLength);

    public void onStarted(@NonNull DownloadTask task);

    public void onCompleted(@NonNull DownloadTask task);

    public void onCanceled(@NonNull DownloadTask task);

    public void onError(@NonNull DownloadTask task, @NonNull Exception e);

    public void onWarn(@NonNull DownloadTask task);
}

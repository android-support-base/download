package com.amlzq.asb.downloadmanager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

/**
 * 下载状态信息广播接收器
 */
public class DownloadStatusReceiver extends BroadcastReceiver {
    private final String TAG = "DownloadStatusReceiver";

    public long mDownloadId;
    private DownloadStatusListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mDownloadId);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            DownloadInfo info = new DownloadInfo();
            info.id = mDownloadId;
            info.status = status;
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.i(TAG, "下载暂停");
                    // mListener.on
                    break;
                case DownloadManager.STATUS_PENDING:
                    Log.i(TAG, "下载延迟");

                    break;
                case DownloadManager.STATUS_RUNNING:
                    Log.i(TAG, "正在下载...");

                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i(TAG, "下载完成");

                    // Play Act
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i(TAG, "下载失败");

                    break;
            }
        }
    }

    public long getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(long downloadId) {
        this.mDownloadId = downloadId;
    }

}

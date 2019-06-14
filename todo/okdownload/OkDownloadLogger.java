package com.zswh.game.box.lib.okdownload;

import com.amlzq.android.log.Log;

/**
 * 设置okdownload日志记录器
 */
public class OkDownloadLogger implements com.liulishuo.okdownload.core.Util.Logger {

    @Override
    public void e(String tag, String msg, Exception e) {
        Log.e(tag, msg, e);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(tag, msg);
    }

}

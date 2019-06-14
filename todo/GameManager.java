package com.zswh.game.box.game.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.amlzq.android.log.Log;
import com.amlzq.android.util.AppUtil;
import com.amlzq.android.util.IntentUtil;
import com.amlzq.android.util.PackageUtil;
import com.amlzq.android.util.SystemUtil;
import com.amlzq.android.util.ToastUtil;
import com.liulishuo.okdownload.DownloadTask;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zswh.game.box.BuildConfig;
import com.zswh.game.box.R;
import com.zswh.game.box.Util;
import com.zswh.game.box.data.entity.GameInfo;
import com.zswh.game.box.widget.DownloadProgressButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * GameManager
 * 游戏管理，包括管理状态，下载，更新
 */
public class GameManager {

    /**
     * @serialField The status of apk is un download and isn't download
     * 未下载
     * 台词：下载
     */
    public final static int UNDOWNLOAD = 0;

    /**
     * @serialField The status of apk is waiting for download
     * 等待下载
     * 台词：排队中
     */
    public final static int WAITING = 1;

    /**
     * @serialField The status of apk is undownloaded, but is downloading
     * 正在下载
     * 台词：下载进度
     */
    public final static int DOWNLOADING = 2;

    /**
     * @serialField The status of apk is downloaded
     * 下载完成
     * 台词：安装
     */
    public final static int DOWNLOADED = 3;

    /**
     * @serialField The status of apk is pause for downloading
     * 暂停下载
     * 台词：继续
     */
    public final static int PAUSE = 4;

    /**
     * @serialField The status of apk is error in downloading
     * 下载出错
     * 台词：重试
     */
    public final static int ERROR = 5;

    /**
     * @serialField The status of apk is installed
     * 安装中
     * 台词：安装中
     */
    public final static int INSTALLING = 6;

    /**
     * @serialField The status of apk is installed
     * 已经安装
     * 台词：启动
     */
    public final static int INSTALLED = 7;

    private static void updateButtonState(TextView view, int textResId, int textColorResId,
                                          int bgDrawableResId) {
        Context context = view.getContext();
        view.setClickable(true);
        view.setText(textResId);
        view.setTextColor(ContextCompat.getColor(context, textColorResId));
        view.setBackgroundResource(bgDrawableResId);
    }

    public static void setButtonState(TextView view, GameInfo bean) {
        if (view == null) {
            return;
        }
        int status = bean.getStatus();
        if (status == GameManager.UNDOWNLOAD) {
            updateButtonState(view, R.string.state_undownload, R.color.game_normal, R.drawable.shape_game_normal);
        } else if (status == GameManager.WAITING) {
            updateButtonState(view, R.string.state_waiting, R.color.game_download, R.drawable.shape_game_download);
        } else if (status == GameManager.DOWNLOADING) {
            updateButtonState(view, R.string.state_downloading, R.color.game_download, R.drawable.shape_game_download);
            view.setText(bean.getPercent());
        } else if (status == GameManager.DOWNLOADED) {
            updateButtonState(view, R.string.state_downloaded, R.color.game_download, R.drawable.shape_game_download);
        } else if (status == GameManager.PAUSE) {
            updateButtonState(view, R.string.state_pause, R.color.game_download, R.drawable.shape_game_download);
        } else if (status == GameManager.ERROR) {
            updateButtonState(view, R.string.state_error, R.color.game_download, R.drawable.shape_game_download);
        } else if (status == GameManager.INSTALLED) {
            updateButtonState(view, R.string.state_installed, R.color.game_complete, R.drawable.shape_game_completed);
        }
    }

    private static void updateButton2State(DownloadProgressButton view, int textResId, int state) {
        Context context = view.getContext();
        view.setClickable(true);
        view.setState(state);
        view.setCurrentText(context.getString(textResId));
    }

    /**
     * 第二种按钮
     *
     * @param view
     * @param bean
     */
    public static void setButton2State(DownloadProgressButton view, GameInfo bean) {
        if (view == null) {
            return;
        }
        Context context = view.getContext();
        int status = bean.getStatus();
        if (status == GameManager.UNDOWNLOAD) {
            updateButton2State(view, R.string.state_undownload, DownloadProgressButton.STATE_NORMAL);

        } else if (status == GameManager.WAITING) {
            updateButton2State(view, R.string.state_waiting, DownloadProgressButton.STATE_NORMAL);

        } else if (status == GameManager.DOWNLOADING) {
            updateButton2State(view, R.string.state_downloading, DownloadProgressButton.STATE_DOWNLOADING);
            view.setProgress(bean.getProgress());
            view.setCurrentText(context.getString(R.string.downloading) + bean.getPercent());

        } else if (status == GameManager.DOWNLOADED) {
            updateButton2State(view, R.string.state_downloaded, DownloadProgressButton.STATE_NORMAL);

        } else if (status == GameManager.PAUSE) {
            updateButton2State(view, R.string.state_pause, DownloadProgressButton.STATE_PAUSE);
            view.setProgress(bean.getProgress());
            view.setProgressText(context.getString(R.string.downloading), bean.getProgress());

        } else if (status == GameManager.ERROR) {
            updateButton2State(view, R.string.state_error, DownloadProgressButton.STATE_NORMAL);

        } else if (status == GameManager.INSTALLING) {
            updateButton2State(view, R.string.state_installing, DownloadProgressButton.STATE_FINISH);

        } else if (status == GameManager.INSTALLED) {
            updateButton2State(view, R.string.state_installed, DownloadProgressButton.STATE_NORMAL);

        }
    }

    private static void actionByState(Context context, GameInfo bean) {
        int status = bean.getStatus();
        if (status == GameManager.UNDOWNLOAD) {
            // 执行"下载"流程
            GameManagerService.addTask(bean);

        } else if (status == GameManager.WAITING) {
            Log.d(bean.getProgressStatus());

        } else if (status == GameManager.DOWNLOADING) {
            // 执行"暂停"流程
            GameManagerService.pauseGame(bean);

        } else if (status == GameManager.DOWNLOADED) {
            File apkFile = new File(QueueController.getSingleton().mQueueDir
                    + File.separator + bean.getAppName() + ".apk");
            Log.d(apkFile.toString());
            if (apkFile.exists()) {
                Intent intent = IntentUtil.getInstallCompat(context, BuildConfig.FILES_AUTHORITY, apkFile);
                context.startActivity(intent);
            } else {
                // 在下载完成之后，安装之前，手动删除了APK文件
                ToastUtil.showShort(context, "安装文件被删除，已经开始重新下载");
                GameManagerService.deleteGame(bean, false);
                // 执行"下载"流程
                GameManagerService.addTask(bean);
            }

        } else if (status == GameManager.PAUSE || status == GameManager.ERROR) {
            GameManagerService.resumeGame(bean);

        } else if (status == GameManager.INSTALLED) {
            SystemUtil.open(bean.getPackageName());
        }
    }

    @SuppressLint("CheckResult")
    public static void actionByState(Fragment fragment, TextView view, GameInfo bean) {
        Context context = view.getContext();
        RxPermissions rxPermissions = new RxPermissions(fragment);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        actionByState(context, bean);
                    } else {
                        // At least one permission is denied
                        ToastUtil.showShort(context, "请授予权限，否则无法下载");
                    }
                }, throwable -> {
                    Log.w(Log.TAG, throwable);
                });
    }

    @SuppressLint("CheckResult")
    public static void actionByState(FragmentActivity activity, TextView view, GameInfo bean) {
        Context context = view.getContext();
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        actionByState(context, bean);
                    } else {
                        // At least one permission is denied
                        ToastUtil.showShort(context, "请授予权限，否则无法下载");
                    }
                }, throwable -> {
                    Log.w(Log.TAG, throwable);
                });
    }

    /**
     * 从顺序表中获取索引
     *
     * @param data 目标集合
     * @param item 目标项
     * @return
     */
    public static int getPosition(List<GameInfo> data, GameInfo item) {
        int position = -1;
        for (int i = 0; i < data.size(); i++) {
            GameInfo target = data.get(i);
            if (isSameDownloadInfo(target, item)) {
                target.setStatus(item.getStatus());
                target.setProgress(item.getProgress());
                target.setProgressStatus(item.getProgressStatus());
                target.setPercent(item.getPercent());
                target.setSpeed(item.getSpeed());
                position = i;
                break;
            }
        }
        return position;
    }

    public static boolean isSameDownloadInfo(GameInfo bean, GameInfo bean2) {
        String murl = getKey(bean);
        String murl2 = getKey(bean2);
        return murl.equals(murl2);
    }

    public static boolean isSameDownloadInfo(GameInfo bean, DownloadTask bean2) {
        String murl = getKey(bean);
        String murl2 = getKey(bean2);
        return murl.equals(murl2);
    }

    private static String getKey(GameInfo bean) {
        return bean.getDownloadUrl() + bean.getAppName();
    }

    private static String getKey(DownloadTask bean) {
        return bean.getUrl() + bean.getFilename();
    }

    public static String getAPKPath(String name) {
        return QueueController.getSingleton().mQueueDir
                + File.separator
                + name.replace(".apk", "") + ".apk";
    }

    public static void setGameState(List<GameInfo> data) {
        if (data == null || data.size() <= 0) return;
        for (GameInfo item : data) {
            item.setStatus(getGameState(item));
        }
    }

    public static void setGameState(GameInfo bean) {
    }

    /**
     * Get apk status
     *
     * @return The status of the apk
     */
    public static int getGameState(GameInfo bean) {
        if (bean.getStatus() == GameManager.INSTALLED && !bean.isUpgrade()) {
            return GameManager.INSTALLED;
        }
        if (bean.getPackageName() != null && !bean.isUpgrade()) {
            if (!bean.getPackageName().equals(AppUtil.getPackageName())) {
                if (packageNames.contains(bean.getPackageName()))
                    return GameManager.INSTALLED;
            }
        }
        int status = bean.getStatus();
        if (status == GameManager.DOWNLOADED) {
            String path = getAPKPath(bean.getAppName());
            File file = new File(path);
            if (file.exists()) {
                String packageName = getApkPackageName(file);
                bean.setFileSize(Util.getMKBStr((int) file.length()));
                if (!packageName.equals(bean.getPackageName())) {
                    bean.setPackageName(packageName);
                    if (packageNames.contains(packageName)) {
                        status = GameManager.INSTALLED;
                    } else {
                        status = GameManager.DOWNLOADED;
                    }
                } else {
                    status = GameManager.DOWNLOADED;
                }
            } else {
                bean.setDelete(true);
            }
        } else {
            File tempfile = new File(getAPKPath(bean.getAppName()));
            if (tempfile.exists()) {
                bean.setDelete(false);
                status = bean.getStatus();
            } else {
                //文件下载完成 状态未更新
                String path = getAPKPath(bean.getAppName());
                File file = new File(path);
                if (file.exists()) {
                    String packageName = getApkPackageName(file);
                    bean.setFileSize(Util.getMKBStr((int) file.length()));
                    if (!packageName.equals(bean.getPackageName())) {
                        bean.setPackageName(packageName);
                        if (packageNames.contains(packageName)) {
                            status = GameManager.INSTALLED;
                        } else {
                            status = GameManager.DOWNLOADED;
                        }
                    } else {
                        status = GameManager.DOWNLOADED;
                    }
                } else {
                    bean.setDelete(true);
                }
            }
        }
        return status;
    }

    public static String getApkPackageName(File file) {
        String packeName = "";
        PackageManager pm = PackageUtil.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        if (info != null && info.packageName != null) {
            packeName = info.packageName;

        }
        return packeName;
    }

    /**
     * 设备上已安装的应用
     */
    public static List<String> packageNames = new ArrayList<>();

    /**
     * @return 获取设备上安装的应用
     */
    @SuppressLint("CheckResult")
    public static void initPackageNames() {
        // 创建被观察者
        Observable.create(new ObservableOnSubscribe<List<String>>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<List<String>> e) throws Exception {
                Log.d(this);
                for (PackageInfo bean : PackageUtil.getInstalledPackages2()) {
                    packageNames.add(bean.packageName);
                }
                e.onNext(packageNames);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())//将被观察者切换到子线程
                .observeOn(AndroidSchedulers.mainThread())//将观察者切换到主线程
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> data) throws Exception {
                        Log.d(data.size());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(Log.TAG, throwable);
                    }
                });
    }

}

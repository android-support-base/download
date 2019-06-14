package com.zswh.game.box.game.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.amlzq.android.content.ContextHolder;
import com.amlzq.android.io.PrefsUtil;
import com.amlzq.android.log.Log;
import com.zswh.game.box.R;
import com.zswh.game.box.data.MessageEvent;
import com.zswh.game.box.data.MyConstant;
import com.zswh.game.box.data.bean.BadgeInfo;
import com.zswh.game.box.data.entity.GameInfo;
import com.zswh.game.box.util.GsonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 游戏管理服务
 */
public class GameManagerService extends Service {
    public static final String TAG = "GameManagerService";

    public Context mContext;
    public static List<GameInfo> mDownloadings;
    public static List<GameInfo> mDownloadeds;
    public static List<GameInfo> mUpgrades;
    public static boolean isCanDownloadTo123G = false; // 初始化获取设置值且设置里面动态改这个参数

    private PackageReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initVars();
        initManager();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(this);
        super.onCreate();
        EventBus.getDefault().register(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        receiver = new PackageReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Log.d(this);
        stopAllTask();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void initVars() {
        mContext = getBaseContext();
        mDownloadings = new ArrayList<>();
        mDownloadeds = new ArrayList<>();
        mUpgrades = new ArrayList<>();
    }

    public void initManager() {

        for (String s : PrefsUtil.getArray(this, MyConstant.SPKEY_DOWNLOADED)) {
            GameInfo bean = GsonUtil.str2obj(s, GameInfo.class);
            if (bean == null) {
                continue;
            }
//            bean.setStatus(GameManager.getApkStatus(mContext, bean));
            mDownloadeds.add(bean);
//            if (!bean.isDelete()) {
//                mDownloadeds.add(bean);
//            } else {
//                deleteTask(bean, true);
//            }
        }

        for (String s : PrefsUtil.getArray(this, MyConstant.SPKEY_DOWNLOADING)) {
            GameInfo bean = GsonUtil.str2obj(s, GameInfo.class);
            if (bean == null) {
                continue;
            }
//            bean.setStatus(GameManager.getApkStatus(mContext, bean));
            mDownloadings.add(bean);
            // 恢复下载
            if (bean.getStatus() == GameManager.PAUSE) {
                QueueController.getSingleton().resumeTask(bean.getAppName(), bean.getDownloadUrl());
            }
        }

        for (String s : PrefsUtil.getArray(this, MyConstant.SPKEY_UPGRADE)) {
            GameInfo bean = GsonUtil.str2obj(s, GameInfo.class);
            if (bean == null) {
                continue;
            }
//            bean.setStatus(GameManager.getApkStatus(mContext, bean));
            mUpgrades.add(bean);
            if (bean.getStatus() == GameManager.PAUSE) {
                QueueController.getSingleton().resumeTask(bean.getAppName(), bean.getDownloadUrl());
            }
            continue;
        }
    }

    public static void addTask(GameInfo bean) {
        for (GameInfo temp : mDownloadings) {
            if (GameManager.isSameDownloadInfo(bean, temp)) {
                return;
            }
        }
        for (GameInfo temp : mDownloadeds) {
            if (GameManager.isSameDownloadInfo(bean, temp)) {
                return;
            }
        }
        for (GameInfo temp : mUpgrades) {
            if (GameManager.isSameDownloadInfo(bean, temp)) {
                return;
            }
        }
        if (bean.isUpgrade()) {
            mUpgrades.add(0, bean);
        } else {
            mDownloadings.add(0, bean);
        }
        QueueController.getSingleton().addQueue(bean.getAppName(), bean.getDownloadUrl());

        bean.setStatus(GameManager.WAITING);
        bean.setPercent("0%");
        bean.setProgress(0);
        bean.setProgressStatus(ContextHolder.getString(R.string.state_waiting));
        EventBus.getDefault().post(bean);

        // 广播下载红点消息
        EventBus.getDefault().post(new BadgeInfo(MessageEvent.BADGE_DOWNLOAD, mDownloadings.size()));
    }

    public static void saveData() {

        Set<String> set = new HashSet<>();
        for (GameInfo temp : mDownloadings) {
            set.add(GsonUtil.obj2str(temp));
        }
        PrefsUtil.saveArray(ContextHolder.getContext(), MyConstant.SPKEY_DOWNLOADING, set);

        for (GameInfo temp : mDownloadeds) {
            set.add(GsonUtil.obj2str(temp));
        }
        PrefsUtil.saveArray(ContextHolder.getContext(), MyConstant.SPKEY_DOWNLOADED, set);

        for (GameInfo temp : mUpgrades) {
            set.add(GsonUtil.obj2str(temp));
        }
        PrefsUtil.saveArray(ContextHolder.getContext(), MyConstant.SPKEY_UPGRADE, set);
    }

    public static void pauseGame(GameInfo bean) {
        QueueController.getSingleton().pauseTask(bean.getAppName(), bean.getDownloadUrl());
    }

    public static void resumeGame(GameInfo bean) {
        QueueController.getSingleton().resumeTask(bean.getAppName(), bean.getDownloadUrl());
    }

    public static void deleteGame(GameInfo bean, boolean isDeleteFile) {
        try {
            String path = "";
            if (bean.getStatus() == GameManager.DOWNLOADED || bean.getStatus() == GameManager.INSTALLED) {
                path = GameManager.getAPKPath(bean.getAppName());
            } else {
                if (bean.getStatus() == GameManager.DOWNLOADING || bean.getStatus() == GameManager.WAITING) {
                    GameManagerService.pauseGame(bean);
                }
                path = GameManager.getAPKPath(bean.getAppName()) + ".temp";
            }

            // 删除下载任务
            boolean delete = false;
            if (isDeleteFile) {
                try {
                    delete = QueueController.getSingleton().deleteTask(bean.getAppName(), bean.getDownloadUrl());
                } catch (Exception e) {
                    Log.e("删除文件失败->", e);
                }
            }

            // 更新下载信息集合
            if (bean.getStatus() == GameManager.DOWNLOADED || bean.getStatus() == GameManager.INSTALLED) {
                mDownloadeds.remove(bean);
            } else {
                if (bean.isUpgrade()) {
                    mUpgrades.remove(bean);
                } else {
                    mDownloadings.remove(bean);
                }
            }

            // 更新此游戏信息
            if (delete) {
                bean.setStatus(GameManager.UNDOWNLOAD);
            } else {
                if (bean.getStatus() == GameManager.INSTALLED) {
                    bean.setStatus(GameManager.UNDOWNLOAD);
                }
            }

//            EventBus.getDefault().post(EventBusMessage.BADGE);
            EventBus.getDefault().post(bean);
//            EventBus.getDefault().post(EventBusMessage.DOWNLIST_STATUS_CHANGE);
        } catch (Exception e) {
            Log.e("删除下载信息失败->", e);
        }
    }

    public static void stopAllTask() {
        QueueController.getSingleton().pauseQueue();

        Set<String> strings = new HashSet<>();
        for (GameInfo bean : mDownloadings) {
            bean.setStatus(GameManager.PAUSE);
            String s = GsonUtil.obj2str(bean);
            strings.add(s);
            PrefsUtil.saveArray(ContextHolder.getContext(), MyConstant.SPKEY_DOWNLOADING, strings);
            EventBus.getDefault().post(bean);
        }
        for (GameInfo bean : mDownloadeds) {
            bean.setStatus(GameManager.PAUSE);
            String s = GsonUtil.obj2str(bean);
            strings.add(s);
            PrefsUtil.saveArray(ContextHolder.getContext(), MyConstant.SPKEY_DOWNLOADED, strings);
            EventBus.getDefault().post(bean);
        }
        for (GameInfo bean : mUpgrades) {
            bean.setStatus(GameManager.PAUSE);
            String s = GsonUtil.obj2str(bean);
            strings.add(s);
            PrefsUtil.saveArray(ContextHolder.getContext(), MyConstant.SPKEY_UPGRADE, strings);
            EventBus.getDefault().post(bean);
        }
        saveData();
    }

    private static GameInfo getGame(GameInfo bean) {
        if (bean == null) return null;
        GameInfo target = null;
        boolean flag = false;
        for (int i = 0; i < mDownloadings.size(); i++) {
            target = mDownloadings.get(i);
            target.setStatus(bean.getStatus());
            if (GameManager.isSameDownloadInfo(target, bean)) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            for (int i = 0; i < mUpgrades.size(); i++) {
                target = mUpgrades.get(i);
                target.setStatus(bean.getStatus());
                if (GameManager.isSameDownloadInfo(target, bean)) {
                    break;
                }
            }
        }
        return target;
    }

    /**
     * 下载完成
     *
     * @param bean
     */
    public static void updateDownloading(GameInfo bean) {
        final GameInfo target = getGame(bean);
        try {
            mDownloadings.remove(target);
            mDownloadeds.add(0, target);
            EventBus.getDefault().post(new BadgeInfo(MessageEvent.BADGE_DOWNLOAD, mDownloadings.size()));
        } catch (Exception e) {
            Log.e("更新下载信息失败", e);
        }
    }

    /**
     * 安装完成
     *
     * @param bean
     */
    public static void updateDownloaded(GameInfo bean) {
        final GameInfo target = getGame(bean);
        try {
            mDownloadeds.remove(target);
            EventBus.getDefault().post(new BadgeInfo(MessageEvent.BADGE_DOWNLOAD, mDownloadings.size()));
        } catch (Exception e) {
            Log.e("更新下载信息失败", e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GameInfo event) {
        Log.d(event.toDownloadInfo());
        if (event.getStatus() == GameManager.DOWNLOADED) {
            updateDownloading(event);
        } else if (event.getStatus() == GameManager.INSTALLED) {
            updateDownloaded(event);
        }
    }

}

package com.zswh.game.box.game.manager;

import com.amlzq.android.log.Log;
import com.amlzq.android.util.StorageUtil;
import com.liulishuo.okdownload.DownloadSerialQueue;
import com.liulishuo.okdownload.DownloadTask;
import com.zswh.game.box.data.entity.GameInfo;
import com.zswh.game.box.lib.okdownload.TagUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 串行队列调度员
 */
public class SerialQueueController {
    public static final String TAG = "SerialQueueController";

    private volatile static SerialQueueController instance; // 声明成 volatile

    public static SerialQueueController getSingleton() {
        if (instance == null) {
            synchronized (SerialQueueController.class) {
                if (instance == null) {
                    instance = new SerialQueueController();
                }
            }
        }
        return instance;
    }

    /**
     * 串行下载队列
     */
    private DownloadSerialQueue mSerialQueue;
    public List<DownloadTask> mTasks = new ArrayList<>();
    private QueueListener mListener = new QueueListener();
    public File mQueueDir;

    private SerialQueueController() {
        mSerialQueue = new DownloadSerialQueue(mListener);
        mQueueDir = StorageUtil.getDownload();
        //        int workingTaskId = mSerialQueue.getWorkingTaskId();
    }

    /**
     * 添加到队列
     */
    public void addQueue(String name, String url) {
        DownloadTask task = new DownloadTask.Builder(url, mQueueDir)
                .setFilename(name + ".temp")
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(100)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
//        TagUtil.saveTaskIcon(task, icon);
        TagUtil.saveTaskName(task, name);
        TagUtil.savePriority(task, 100);
        mTasks.add(task);
        mSerialQueue.enqueue(task);
        Log.d("Task Count:" + queueSize());
    }

    public void pauseQueue() {
        // 队列暂停下载
        mSerialQueue.pause();
    }

    public void resumeQueue() {
        // 队列恢复下载
        mSerialQueue.resume();
    }

    public void deleteQueue() {
        // 队列清空
        if (mQueueDir != null) {
            String[] children = mQueueDir.list();
            if (children != null) {
                for (String child : children) {
                    if (!new File(mQueueDir, child).delete()) {
                        Log.w(TAG, "delete " + child + " failed!");
                    }
                }
            }
            if (!mQueueDir.delete()) {
                Log.w(TAG, "delete " + mQueueDir + " failed!");
            }
        }
        for (DownloadTask task : mTasks) {
            TagUtil.clearProceedTask(task);
        }
        mTasks.clear();
    }

    public void pauseTask(String name, String url) {
        // 暂停单个任务下载
        String target = name + ".temp" + url;
        for (DownloadTask task : mTasks) {
            if (target.equals(task.getFilename() + task.getUrl())) {
                task.cancel(); // 取消任务
                break;
            }
        }
    }

    public void resumeTask(String name, String url) {
        pauseQueue();// 由于卡顿，所以限制只能单个任务下载

        // 恢复单个任务下载
        String target = name + ".temp" + url;
        for (DownloadTask task : mTasks) {
            if (target.equals(task.getFilename() + task.getUrl())) {
                task.enqueue(mListener); // 异步执行任务
                break;
            }
        }
    }

    public boolean deleteTask(String name, String url) {
        // 删除单个任务
        boolean result = false;
        String path = "";
        DownloadTask target = null;
        for (DownloadTask task : mTasks) {
            if ((task.getFilename() + task.getUrl()).equals(name + ".apk" + url)
                    || (task.getFilename() + task.getUrl()).equals(name + ".temp" + url)) {
                path = mQueueDir + File.separator + task.getFilename();
                target = task;
                break;
            }
        }
        // 删除本地文件和任务
        File file = new File(path);
        if (file.exists()) {
            result = file.delete();
        }
        TagUtil.clearProceedTask(target);
        mTasks.remove(target);
        return result;
    }

    /**
     * 设置优先级
     *
     * @param bean
     * @param priority
     */
    public void setPriority(GameInfo bean, int priority) {
        for (DownloadTask task : mTasks) {
            if (GameManager.isSameDownloadInfo(bean, task)) {
                final DownloadTask newTask = task.toBuilder().setPriority(priority).build();
                newTask.setTags(task);
                TagUtil.savePriority(newTask, priority);
                this.mTasks.add(newTask);
                break;
            }
        }
        // priority
//        final int priority = TagUtil.getPriority(task);
    }

    public int queueSize() {
        return mSerialQueue.getWaitingTaskCount();
    }

}

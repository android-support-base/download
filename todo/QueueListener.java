package com.zswh.game.box.game.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amlzq.android.content.ContextHolder;
import com.amlzq.android.log.Log;
import com.amlzq.android.util.FormatUtil;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.zswh.game.box.R;
import com.zswh.game.box.data.entity.GameInfo;
import com.zswh.game.box.lib.amlzq.NetworkUtil;
import com.zswh.game.box.lib.okdownload.TagUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;
import java.util.Map;

public class QueueListener extends DownloadListener4WithSpeed {
    public static final String TAG = "QueueListener";

    @Override
    public void taskStart(@NonNull DownloadTask task) {
        final String status = "taskStart";
        final String name = TagUtil.getTaskName(task);
        Log.d("id:" + task.getId() + ",filename:" + name + ",status:" + status);
        TagUtil.saveStatus(task, status);
    }

    @Override
    public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, @NonNull boolean fromBreakpoint,
                          @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
        final String status = "infoReady";
        final String name = TagUtil.getTaskName(task);
        Log.d("id:" + task.getId() + ",filename:" + name + ",status:" + status);
        TagUtil.saveStatus(task, status);

        String readableTotalLength = Util.humanReadableBytes(info.getTotalLength(), true);
        final float percent = (float) info.getTotalOffset() / info.getTotalLength();
        TagUtil.saveTotal(task, info.getTotalLength());

        GameInfo event = new GameInfo();
        event.setAppName(name.replace(".temp", ""));
        event.setDownloadUrl(task.getUrl());
        event.setStatus(GameManager.WAITING);
        event.setFileSize(readableTotalLength);
        event.setPercent(FormatUtil.getPercent2(percent));
        event.setProgress((int) (percent * 100));
        event.setProgressStatus(ContextHolder.getString(R.string.state_waiting));
        EventBus.getDefault().post(event);
    }

    @Override
    public void connectStart(@NonNull DownloadTask task, int blockIndex,
                             @NonNull Map<String, List<String>> requestHeaders) {
        final String status = "connectStart";
        final String name = TagUtil.getTaskName(task);
        Log.d("id:" + task.getId() + ",filename:" + name + ",status:" + status);
        TagUtil.saveStatus(task, status);
    }

    @Override
    public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
                           @NonNull Map<String, List<String>> responseHeaders) {
        final String status = "connectEnd";
        final String name = TagUtil.getTaskName(task);
        Log.d("id: " + task.getId() + ",filename: " + name + ",status:" + status);
        TagUtil.saveStatus(task, status);
    }

    @Override
    public void progressBlock(@NonNull DownloadTask task, int blockIndex,
                              long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
        final String status = "progressBlock";
        final String name = TagUtil.getTaskName(task);
        Log.d("id: " + task.getId() + ",filename: " + name + ",status:" + status);
        TagUtil.saveStatus(task, status);
    }

    @Override
    public void progress(@NonNull DownloadTask task, long currentOffset,
                         @NonNull SpeedCalculator taskSpeed) {
        final String status = "progress";
        final String name = TagUtil.getTaskName(task);
        Log.d("id: " + task.getId() + ",filename: " + name + ",status:" + status + ",currentOffset:" + currentOffset);
        TagUtil.saveStatus(task, status);
        TagUtil.saveOffset(task, currentOffset);

        final BreakpointInfo info = StatusUtil.getCurrentInfo(task);
        final String readableOffset = Util.humanReadableBytes(currentOffset, true);
        final String readableTotalLength = Util.humanReadableBytes(info.getTotalLength(), true);
        final String progressStatus = readableOffset + "/" + readableTotalLength; // 19.2 MB/115.5 MB
        final String speed = taskSpeed.speed(); // 197.0 kB/s
        final String progressStatusWithSpeed = progressStatus + "(" + speed + ")";
        final float percent = (float) currentOffset / info.getTotalLength(); // 0.16630857

        GameInfo event = new GameInfo();
        event.setAppName(name.replace(".temp", ""));
        event.setDownloadUrl(task.getUrl());
        event.setStatus(GameManager.DOWNLOADING);
        event.setPercent(FormatUtil.getPercent2(percent));
        event.setProgress((int) (percent * 100));
        event.setProgressStatus(progressStatusWithSpeed);
        event.setSpeed(speed);
        EventBus.getDefault().post(event);
    }

    @Override
    public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info,
                         @NonNull SpeedCalculator blockSpeed) {
        final String status = "blockEnd";
        Log.d("id:" + task.getId() + ",filename:" + task.getFilename() + ",status:" + status);
        TagUtil.saveStatus(task, status);
    }

    @Override
    public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                        @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {

        final String status = cause.toString();
        final String name = TagUtil.getTaskName(task);
        TagUtil.saveStatus(task, status);
        Log.d("id:" + task.getId() + ",filename:" + task.getFilename() + ",status:" + status);

        final String statusWithSpeed = cause.toString() + " " + taskSpeed.averageSpeed();

        // mark
//        task.setTag(null);

        GameInfo event = new GameInfo();
        event.setAppName(name.replace(".temp", ""));
        event.setDownloadUrl(task.getUrl());
        event.setProgressStatus(statusWithSpeed);
        switch (cause) {
            case SAME_TASK_BUSY:
                event.setStatus(GameManager.WAITING);
                event.setProgressStatus(ContextHolder.getString(R.string.state_waiting));
                break;
            case COMPLETED:
                event.setStatus(GameManager.DOWNLOADED);
                event.setProgressStatus(ContextHolder.getString(R.string.download_finish));

                Log.d(task.getFile().getAbsolutePath() + "," + task.getFile().toString());
                // 重命名为".apk"格式
                File newFile = new File(task.getFile().getParent() + File.separator + task.getFilename().replace(".temp", ".apk"));
                task.getFile().renameTo(newFile);
                Log.d(task.getFile().getAbsolutePath() + "," + task.getFile().toString());
                break;
            case CANCELED:
                event.setStatus(GameManager.PAUSE);
                event.setProgressStatus(ContextHolder.getString(R.string.download_stop));
                break;
            case ERROR:
                Log.e(realCause);
                event.setStatus(GameManager.ERROR);
                event.setProgressStatus(NetworkUtil.handleException(realCause));
                break;
            default:
                event.setStatus(GameManager.ERROR);
                event.setProgressStatus(cause.toString());
        }
        EventBus.getDefault().post(event);
    }

}

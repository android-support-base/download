/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zswh.game.box.lib.okdownload;

import android.os.Build;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressUtil {

    public static void updateProgressToViewWithMark(ProgressBar bar, long currentOffset) {
        updateProgressToViewWithMark(bar, currentOffset, true);
    }

    public static void updateProgressToViewWithMark(ProgressBar bar, long currentOffset,
                                                    boolean anim) {
        if (bar.getTag() == null) return;

        final int shrinkRate = (int) bar.getTag();
        final int progress = (int) ((currentOffset) / shrinkRate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bar.setProgress(progress, anim);
        } else {
            bar.setProgress(progress);
        }
    }

    public static void updateProgressToViewWithMark(TextView tv, long currentOffset) {
        if (tv.getTag() == null) return;

        final int shrinkRate = (int) tv.getTag(); // 已下载
        final int progress = (int) ((currentOffset) / shrinkRate);

        String ratio = String.valueOf(progress) + "%";
        tv.setText(ratio);
    }


    public static void calcProgressToViewAndMark(ProgressBar bar, long offset, long total) {
        calcProgressToViewAndMark(bar, offset, total, true);
    }

    /**
     * 计算进度，并到视图和标记
     *
     * @param bar
     * @param offset
     * @param total
     * @param anim
     */
    public static void calcProgressToViewAndMark(ProgressBar bar, long offset, long total,
                                                 boolean anim) {
        final int contentLengthOnInt = reducePrecision(total);
        final int shrinkRate = contentLengthOnInt == 0
                ? 1 : (int) (total / contentLengthOnInt);
        bar.setTag(shrinkRate);
        final int progress = (int) (offset / shrinkRate);

        bar.setMax(contentLengthOnInt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bar.setProgress(progress, anim);
        } else {
            bar.setProgress(progress);
        }
    }

    public static void calcProgressToViewAndMark(TextView tv, long offset, long total) {
        final int contentLengthOnInt = reducePrecision(total);
        final int shrinkRate = contentLengthOnInt == 0
                ? 1 : (int) (total / contentLengthOnInt);
        tv.setTag(shrinkRate);
        final int progress = (int) (offset / shrinkRate);

        String ratio = String.valueOf(progress) + "%";
        tv.setText(ratio);
    }

    /**
     * 降低精度
     *
     * @param origin
     * @return
     */
    private static int reducePrecision(long origin) {
        if (origin <= Integer.MAX_VALUE) return (int) origin;
        int shrinkRate = 10;
        long result = origin;
        while (result > Integer.MAX_VALUE) {
            result /= shrinkRate;
            shrinkRate *= 5;
        }
        return (int) result;
    }

}
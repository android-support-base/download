package com.amlzq.asb.downloadmanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amlzq.asb.R;

/**
 * public class DownloadManager
 * extends Object
 * <p>
 * java.lang.Object
 * ↳	android.app.DownloadManager
 * <p>
 * Added in API level 9
 */
public class DownloadManagerSampleActivity extends Activity {

    TextView mStatusView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manager_sample);

        mStatusView = findViewById(R.id.status);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Code_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被用户同意,做相应的事情
                startService();
            } else {
                //权限被用户拒绝，做相应的事情
                Toast.makeText(this, "拒绝了权限", Toast.LENGTH_SHORT);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onDownload(View view) {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Code_PERMISSION)) {
            startService();
        }
    }

    private void startService() {
        Intent intent = new Intent(this, DownloadService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

    }

    /**
     * 请求权限
     */
    int Code_PERMISSION = 0;

    /**
     * 权限申请
     */
    private boolean requestPermission(final String manifestPermission, final int CODE) {
        //1. 检查是否已经有该权限
        if (ContextCompat.checkSelfPermission(this, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, manifestPermission)) {
                new AlertDialog.Builder(this)
                        .setTitle("权限申请")
                        .setMessage("亲，没有权限我会崩溃，请把权限赐予我吧！")
                        .setPositiveButton("赏给你", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // 用户同意 ，再次申请
                                ActivityCompat.requestPermissions(DownloadManagerSampleActivity.this, new String[]{manifestPermission}, CODE);
                            }
                        })
                        .setNegativeButton("就不给", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // 用户拒绝 ，如果APP必须有权限否则崩溃，那就继续重复询问弹框~~
                            }
                        }).show();
            } else {
                //2. 权限没有开启，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{manifestPermission}, CODE);
            }

        } else {
            //3. 权限已开，处理逻辑
            return true;
        }
        return false;
    }

}

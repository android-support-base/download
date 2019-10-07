package com.amlzq.asb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.amlzq.asb.downloadmanager.DownloadManagerSampleActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDownloadManager(View view) {
        startActivity(new Intent(this, DownloadManagerSampleActivity.class));
    }

    public void onOkDownload(View view) {
    }

    public void onFileDownloader(View view) {
    }

    public void onMultiThreadDownload(View view) {
    }

}

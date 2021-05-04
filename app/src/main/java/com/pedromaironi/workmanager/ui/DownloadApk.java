package com.pedromaironi.workmanager.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pedromaironi.workmanager.BuildConfig;
import com.pedromaironi.workmanager.R;
import com.pedromaironi.workmanager.services.DownloadApp;
import com.pedromaironi.workmanager.services.DownloadJson;
import com.pedromaironi.workmanager.utils.Constants;
import com.pedromaironi.workmanager.viewmodel.CheckApp;
import com.pedromaironi.workmanager.viewmodel.CheckVersion;

import java.io.File;

public class DownloadApk extends AppCompatActivity {
    public static Context mContext;
    SharedPreferences data;
    SharedPreferences.Editor edit;
    private TextView textViewProgressValue;
    private ProgressBar progressBarDownload;
    private CheckApp mCheckApp;
    //private boolean isDownloading = false;

    @Override
    protected void onResume() {
        super.onResume();
        data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = data.edit();
        edit.putBoolean("IS_APP_IN_BACKGROUND", false);
        edit.apply();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = data.edit();
        edit.putBoolean("IS_APP_IN_BACKGROUND", true);
        edit.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_apk);
        mContext = this;

        init();
    }

    private void init() {

        textViewProgressValue = findViewById(R.id.textView_progressValue);
        progressBarDownload = findViewById(R.id.progressBar_download);

//        buttonDownload.setOnClickListener(this);
        textViewProgressValue.setText(getResources().getString(R.string.status_idle_text));

        //viewModel initialize
        mCheckApp = new CheckApp();
        subscribe();
        textViewProgressValue.setText(getResources().getString(R.string.status_downloading_text));
        mCheckApp.startDownload();
        if(mCheckApp.Downloaded()) {
            WorkManager.getInstance(DownloadApp.getApp())
                    .getWorkInfoByIdLiveData(CheckApp.downloadingWork.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo.getState().name().equals("SUCCEEDED")) {
                                openApk();
                            }
                        }
                    });
        }
    }

    private void openApk() {
        File apkFile = new File(Constants.Path + Constants.DOWNLOAD_FILE_APP_NAME + Constants.APK_EXTENSION );
        Log.e("apkFile", String.valueOf(apkFile));
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(uriFromFile(getApplicationContext(), apkFile), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }
        }else{
            intent.setData(Uri.fromFile(apkFile));
            intent.setType("application/vnd.android.package-archive");
            startActivity(intent);
        }
    }

    private static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    private void subscribe() {
        final Observer<Long> downloadProgressObserver = new Observer<Long>() {
            @Override
            public void onChanged(@Nullable final Long aLong) {

                int progressValue = 0;
                if (aLong != null) {
                    progressValue = aLong.intValue();
                }
                if (progressValue > 0) {
                    //isDownloading = true;
//                    buttonDownload.setClickable(false);
                    String text = getResources().getString(R.string.status_downloaded_text) + " " + progressValue + "%";
                    textViewProgressValue.setText(text);
                }

                if(progressValue == 100){
//                    buttonDownload.setClickable(true);
                }

                progressBarDownload.setProgress(progressValue);
            }
        };

        mCheckApp.getDownloadStatus().observe(this, downloadProgressObserver);
    }

    }
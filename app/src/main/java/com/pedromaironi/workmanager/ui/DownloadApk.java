package com.pedromaironi.workmanager.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pedromaironi.workmanager.R;
import com.pedromaironi.workmanager.services.DownloadApp;

import com.pedromaironi.workmanager.utils.Constants;
import com.pedromaironi.workmanager.viewmodel.CheckApp;


import java.io.File;

public class DownloadApk extends AppCompatActivity {

    public static final String TAG = "LIFE";
    public static Context mContext;

    private TextView textViewProgressValue;
    private ProgressBar progressBarDownload;

    private CheckApp mCheckApp;
    private int progressValue = 0;

    //private boolean isDownloading = false;
    Button btnUpdate;

    SharedPreferences data;
    SharedPreferences.Editor edit;
    @Override
    protected void onResume() {
        super.onResume();
        data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = data.edit();
        edit.putBoolean("IS_APP_IN_BACKGROUND", false);
        edit.apply();

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (notificationManager != null) {
//            notificationManager.cancelAll();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Log.d(TAG, "onStart");
        if (progressValue == 100) {
            openApk();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Log.d(TAG, "onPause");
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
        // Log.d(TAG, "onCreate");
        init();
    }

    private void init() {

        // Instance
        textViewProgressValue = findViewById(R.id.textView_progressValue);
        textViewProgressValue.setText(getResources().getString(R.string.status_downloading_text));
        progressBarDownload = findViewById(R.id.progressBar_download);
        btnUpdate = findViewById(R.id.button_download);

        // Se puede obviar el boton pero si quiere puede modificar para que funcione
        btnUpdate.setVisibility(View.GONE);
        textViewProgressValue.setText(getResources().getString(R.string.status_idle_text));

        //viewModel initialize
        mCheckApp = new CheckApp();

        // Subscribe progress value
        subscribe();

        //Donwload app
        DownloadApp();
    }

    private void DownloadApp() {
        // Download app
        mCheckApp.startDownload();
        /*
         Verificacion de la descarga para
         cuando esta acabe abrir la apk automaticamente
         */
        if (mCheckApp.Downloaded()) {
            WorkManager.getInstance(DownloadApp.getApp())
                    .getWorkInfoByIdLiveData(CheckApp.downloadingWork.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            // SUCCEEDED
                            if (workInfo.getState().name().equals("SUCCEEDED")) {
                                openApk();
                            }
                        }
                    });
        }
    }

    private void openApk() {
        File apkFile = new File(Constants.Path + Constants.DOWNLOAD_FILE_APP_NAME + Constants.APK_EXTENSION);
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // >= Api 24
            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".FileProvider", apkFile);
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            // < Api 24
            Uri apkUri = Uri.fromFile(apkFile);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

    private void subscribe() {
        final Observer<Long> downloadProgressObserver = new Observer<Long>() {
            @Override
            public void onChanged(@Nullable final Long aLong) {

                if (aLong != null) {
                    progressValue = aLong.intValue();
                }
                if (progressValue > 0) {
                    //isDownloading = true;
//                    btnUpdate.setClickable(false);

                    String text = getResources().getString(R.string.status_downloaded_text) + " " + progressValue + "%";
                    textViewProgressValue.setText(text);
                }

                if (progressValue == 100) {
//                    btnUpdate.setClickable(true);
                }

                progressBarDownload.setProgress(progressValue);
            }
        };

        mCheckApp.getDownloadStatus().observe(this, downloadProgressObserver);
    }

}
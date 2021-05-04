package com.pedromaironi.workmanager.viewmodel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.pedromaironi.workmanager.services.DownloadApp;
import com.pedromaironi.workmanager.services.DownloadWorkerApp;
import com.pedromaironi.workmanager.ui.DownloadApk;
import com.pedromaironi.workmanager.utils.Constants;

import java.util.List;
import java.util.UUID;

public class CheckApp extends ViewModel {

    public static WorkManager mWorkManager;
    public static OneTimeWorkRequest downloadingWork;

    private MutableLiveData<Long> progressValue = new MutableLiveData<>();

    public CheckApp(){
        LocalBroadcastManager.getInstance(DownloadApk.mContext).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.INTENT_LOCAL_BROADCAST));
    }

    public LiveData<Long> getDownloadStatus(){
        return progressValue;
    }

    public void startDownload(){
        SetupScheduler();
    }

    public void stopDownload(){
        if(downloadingWork != null){
            UUID id = downloadingWork.getId();
            mWorkManager.cancelWorkById(id);

        }else{
            Log.d("tttt", "downloadingWork is null");
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            long value = intent.getIntExtra(Constants.INTENT_KEY, 0);
            progressValue.postValue(value);
        }
    };

    private void SetupScheduler(){
        mWorkManager = WorkManager.getInstance(DownloadApk.mContext);
        LiveData<List<WorkInfo>> mSavedWorkStatus = mWorkManager.getWorkInfosByTagLiveData(Constants.TAG_WORKER_THREAD);

        if(mSavedWorkStatus.getValue() != null && !mSavedWorkStatus.getValue().isEmpty()) {

            if (mSavedWorkStatus.getValue().get(0).getState().isFinished()){
                scheduleTask();
            }
        }else {
            scheduleTask();
        }
    }

    private void scheduleTask(){
        downloadingWork =
                new OneTimeWorkRequest.Builder(DownloadWorkerApp.class)
                        .addTag(Constants.TAG_WORKER_THREAD)
                        .build();
        mWorkManager.enqueue(downloadingWork);
        Downloaded();
    }

    public boolean Downloaded(){
        return true;
    }
}

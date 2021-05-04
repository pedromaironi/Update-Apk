package com.pedromaironi.workmanager.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.pedromaironi.workmanager.services.DownloadJson;
import com.pedromaironi.workmanager.services.DownloadWorkerJson;
import com.pedromaironi.workmanager.utils.Constants;

import java.util.List;

public class CheckVersion {

    public static WorkManager mWorkManager;
    public static OneTimeWorkRequest downloadingWork;
    public static LiveData<List<WorkInfo>> mSavedWorkStatus;

    public CheckVersion(){
    }

    public void startDownloadJson(){
        SetupScheduler();
    }

    private void SetupScheduler(){
        mWorkManager = WorkManager.getInstance(DownloadJson.getJson());
        mSavedWorkStatus = mWorkManager.getWorkInfosByTagLiveData(Constants.TAG_WORKER_THREAD);
        Log.d("mSavedWorkStatus1", String.valueOf(mSavedWorkStatus));
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
                new OneTimeWorkRequest.Builder(DownloadWorkerJson.class)
                        .addTag(Constants.TAG_WORKER_THREAD)
                        .build();
        mWorkManager.enqueue(downloadingWork);
        Downloaded();
    }

    public boolean Downloaded(){
        return true;
    }
}

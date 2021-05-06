package com.pedromaironi.workmanager.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pedromaironi.workmanager.models.JsonInfo;
import com.pedromaironi.workmanager.ui.DownloadApk;
import com.pedromaironi.workmanager.utils.Constants;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadWorkerApp extends Worker {

//    private NotificationHelper notificationHelper;
    JsonInfo mJsonInfo;

    public DownloadWorkerApp(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Worker.Result doWork() {

        Intent intent = new Intent(Constants.INTENT_LOCAL_BROADCAST);
//        notificationHelper = new NotificationHelper(DownloadApk.mContext);

        int count;
        String url_;
        mJsonInfo = new JsonInfo();
        try {
            // Protocols
            // Esto era necesario si la pagina tenia ambos protocolos http y https, aun asi la funcion esta ahi
            /*
             * Entonces, como sysnotes no poseee https el codigo siguiente no es necesario
             * ya que http es valido para todas las versiones
             */
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                // Si en el json pone: //www.url.com/file
                // Tambien es valido: Constants.protocolKitKatMinus + mCheckJson.getDownloadUrl()
                url_ = mJsonInfo.getDownloadUrl();
                System.out.println(url_);
            }else{
                // Si en el json pone: //www.url.com/file
                // Tambien es valido: Constants.protocolKitKatPlus + mCheckJson.getDownloadUrl()
                url_ = mJsonInfo.getDownloadUrl();
            }
            URL url = new URL(url_);
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            OutputStream output = new FileOutputStream(Constants.Path + Constants.DOWNLOAD_FILE_APP_NAME + Constants.APK_EXTENSION);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                //Log.d("tttt", "Progress: " + (int) ((total * 100) / lengthOfFile));
                int value = (int) ((total * 100) / lengthOfFile);

                intent.putExtra(Constants.INTENT_KEY, value);
                LocalBroadcastManager.getInstance(DownloadApk.mContext).sendBroadcast(intent);

                //notify user
//                showNotification(value);

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            WorkManager.getInstance(DownloadApk.mContext).cancelAllWorkByTag(Constants.TAG_WORKER_THREAD);
            return Worker.Result.failure();
        }

        return Worker.Result.success();
    }

//    private void showNotification(int percent) {
//        data = PreferenceManager.getDefaultSharedPreferences(DownloadApk.mContext);
//        boolean isInBack = data.getBoolean("IS_APP_IN_BACKGROUND", false);
//
//        if(isInBack && percent > 0 && percent % 5 == 0){
//            if(current != percent) {
//                notificationHelper.createNotification(Constants.TITLE_NOTIFICATION, percent + "%");
//                current = percent;
//            }
//        }
//    }
}

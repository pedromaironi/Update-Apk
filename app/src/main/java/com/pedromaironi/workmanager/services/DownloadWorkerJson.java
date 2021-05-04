package com.pedromaironi.workmanager.services;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pedromaironi.workmanager.ui.MainActivity;
import com.pedromaironi.workmanager.utils.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadWorkerJson extends Worker {

    public DownloadWorkerJson(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Worker.Result doWork() {

        int count;
        String url_;
        try {
            // Protocols
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                url_ = Constants.protocolKitKatMinus + Constants.DOWNLOAD_FILE_JSON_URL;
            }else{
                url_ = Constants.protocolKitKatPlus + Constants.DOWNLOAD_FILE_JSON_URL;
            }
            URL url = new URL(url_);
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            //External directory path to save file
            //path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator;
            //Create folder if it does not exist

            // Output stream to write file
            OutputStream output = new FileOutputStream(Constants.Path + Constants.DOWNLOAD_FILE_JSON_NAME + Constants.JSON_EXTENSION);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                int value = (int) ((total * 100) / lengthOfFile);
                //notify user
                output.write(data, 0, count);
            }


            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            WorkManager.getInstance(DownloadJson.getJson()).cancelAllWorkByTag(Constants.TAG_WORKER_THREAD);
            Log.e("Error: ", String.valueOf(Result.failure()));
            return Worker.Result.failure();
        }

        return Worker.Result.success();
    }


}

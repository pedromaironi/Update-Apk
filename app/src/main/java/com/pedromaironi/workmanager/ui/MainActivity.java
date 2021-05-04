package com.pedromaironi.workmanager.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pedromaironi.workmanager.models.CheckJson;
import com.pedromaironi.workmanager.services.DownloadJson;
import com.pedromaironi.workmanager.R;
import com.pedromaironi.workmanager.utils.Constants;
import com.pedromaironi.workmanager.viewmodel.CheckVersion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView textViewProgressValue;
    private ProgressBar progressBarDownload;
    private AlertDialog mDialog;
    public static final String TAG = "MainActivity";
    CheckVersion mCheckVersion;
    Button btn;
    public static Context mContext;
    private static final int PERMISSION_REQUEST = 100;
    private CheckJson mCheckJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mContext = this;
    }

    @Override
    protected void onPause() {
        super.onPause();

//        DownloadJson.getJson().getSharedPref().setCommonBooleanValue(SharedPref.IS_APP_IN_BACKGROUND, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        DownloadJson.getJson().getSharedPref().setCommonBooleanValue(SharedPref.IS_APP_IN_BACKGROUND, false);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    void init() {
        mCheckJson = new CheckJson();
        textViewProgressValue = findViewById(R.id.textView_progressValue);
        progressBarDownload = findViewById(R.id.progressBar_download);
        btn = findViewById(R.id.button_download);
        mCheckVersion = new CheckVersion();
        textViewProgressValue.setText(getResources().getString(R.string.status_idle_text));
        checkForPermission();
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
                    btn.setClickable(false);
                    String text = getResources().getString(R.string.status_downloaded_text) + " " + progressValue + "%";
                    textViewProgressValue.setText(text);
                }

                if(progressValue == 100){
                    btn.setClickable(true);
                }

                progressBarDownload.setProgress(progressValue);
            }
        };

//        mCheckVersion.getDownloadStatus().observe(this, downloadProgressObserver);
    }
    private void checkForPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                mDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getApplicationInfo().name)
                        .setMessage("You need to grant this permissions for future updates")
                        .setPositiveButton("Accept",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);

                                    }
                                }).setNegativeButton("No, thanks",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).create();
                mDialog.show();
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(MainActivity.this, "This permissions are needed to update app",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                DownloadButtonClicked();
            }
        } else {
            DownloadButtonClicked();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            DownloadButtonClicked();
        }
    }
    private void DownloadButtonClicked() {
        mCheckVersion.startDownloadJson();
        if(mCheckVersion.Downloaded()) {
            WorkManager.getInstance(DownloadJson.getJson())
                    .getWorkInfoByIdLiveData(CheckVersion.downloadingWork.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo.getState().name().equals("SUCCEEDED")) {
                                getJsonFromApplication();
                            }
                        }
                    });
        }
    }


    private void getJsonFromApplication() {
        File jsonFile;
        jsonFile = new File(Constants.Path + Constants.DOWNLOAD_FILE_JSON_NAME + Constants.JSON_EXTENSION);

        Uri uri = Uri.parse(String.valueOf(Uri.fromFile(jsonFile)));
        String myJson = "";
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int in;
            in = inputStream.read();
            while (in != -1)
            {
                byteArrayOutputStream.write(in);
                in = inputStream.read();
            }
            inputStream.close();

            myJson = byteArrayOutputStream.toString();
        }catch (IOException e) {
            e.printStackTrace();
        }

        try {

            JSONObject obj = new JSONObject(myJson);
            //Log.d("MyaApp", obj.toString());
            Map<String, Object> js = toMap(obj);
            //Log.d("jsonToMap", js.toString());
            for (Map.Entry<String, Object> entry : js.entrySet()) {
                //Log.d("entry", entry.getKey());
                //Log.d("entry", String.valueOf(entry.getValue()));
                switch(entry.getKey()){
                    case Constants.downloadUrl:
                        mCheckJson.setDownloadUrl(String.valueOf(entry.getValue()));
                        break;
                    case Constants.currentVersionCode:
                        mCheckJson.setCurrentVersionCode(String.valueOf(entry.getValue()));
                        break;
                    case Constants.currentVersionName:
                        mCheckJson.setCurrentVersionName(String.valueOf(entry.getValue()));
                        break;
                    case Constants.oldVersionCode:
                        mCheckJson.setOldVersionCode(String.valueOf(entry.getValue()));
                        break;
                    case Constants.oldVersionName:
                        mCheckJson.setOldVersionName(String.valueOf(entry.getValue()));
                        break;
                }
            }
        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \""  + "\"");
        }

        VerifyVersion();
    }

    private void VerifyVersion() {

    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
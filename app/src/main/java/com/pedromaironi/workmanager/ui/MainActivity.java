package com.pedromaironi.workmanager.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.pedromaironi.workmanager.models.AppInfo;
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
    public static final String TAG = "LIFE";
    CheckVersion mCheckVersion;
    public static Context mContext;
    private static final int PERMISSION_REQUEST = 100;
    private final int REQUEST_CODE_UKNOWN_APPS = 300;

    private CheckJson mCheckJson;
    private AppInfo mAppInfo;
    private AlertDialog mDialogDownloadApp;

    private TextView mVersionCodeTextView;
    private TextView mVersionNameTextView;

    SharedPreferences data;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        init();
    }

    private boolean InternetConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!(data.getBoolean("AlertDialog", false))){
            checkForPermission();
        }
    }



    void init() {
        // TextView
        mVersionCodeTextView = findViewById(R.id.versionCode);
        mVersionNameTextView = findViewById(R.id.versionName);

        // Instance
        mAppInfo = new AppInfo();
        mCheckJson = new CheckJson();
        mCheckVersion = new CheckVersion();

        // Set Version < Codes, Names >
        setInfoApp();

        // Verify connection to start
        if (InternetConnected()) {
            // Verify permission
            checkForPermission();
        }
    }

    private void checkForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            } else {
                DownloadJson();
            }
        } else {
            DownloadJson();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DownloadJson();
                }else{
                    checkForPermission();
                }
            }

        }
    }
    private void DownloadJson() {
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
                    case Constants.nameApp:
                        mCheckJson.setNameApp(String.valueOf(entry.getValue()));
                        break;
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
        VerifyVersions();
    }

    private void setInfoApp() {
        /* These variables are for get version code and name of the app */

        int versionCode = 0;
        String versionName = "";

        try {
            /* Get android:versionName */
            versionName = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
            mAppInfo.setCurrentVersionName(versionName);
            /* Get android:versionCode*/
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            mAppInfo.setCurrentVersionCode(String.valueOf(versionCode));

            mAppInfo.setNameApp(getNameApp());

            /* Test */
            Log.e("VersionNAME:",versionName);
            Log.e("versionCode:", String.valueOf(versionCode));

        }catch (Exception e){}
    }

    private String getNameApp() {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(getApplicationContext().getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    private void VerifyVersions (){
        Log.d(TAG, mAppInfo.getNameApp());
        Log.d(TAG, mCheckJson.getNameApp());
        Log.d(TAG, mAppInfo.getCurrentVersionCode());
        Log.d(TAG, mCheckJson.getCurrentVersionCode());
        Log.d(TAG, mCheckJson.getOldVersionCode());
        // nameApp version actual == nameApp version old
        // VersionCode actual == VersionCode old
        // Luego
        // Si la version de la app es menor a la version actual que viene desde el json
        // do - hacer
        if ((mAppInfo.getNameApp().equals(mCheckJson.getNameApp())) && (mAppInfo.getCurrentVersionCode().equals(mCheckJson.getOldVersionCode()))){
             if (Integer.parseInt(mAppInfo.getCurrentVersionCode()) < Integer.parseInt(mCheckJson.getCurrentVersionCode())){
//                Toast.makeText(MainActivity.this, "update", Toast.LENGTH_LONG).show();
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                     edit = data.edit();
                     edit.putBoolean("AlertDialog", true);
                     edit.apply();
                     mDialogDownloadApp = new AlertDialog.Builder(MainActivity.this)
                             .setTitle("New version available")
                             .setMessage("Please, update app to new version.")
                             .setCancelable(false)
                             .setPositiveButton("Update",
                                     new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
                                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                 if (!getPackageManager().canRequestPackageInstalls()) {
                                                     edit.putBoolean("AlertDialog", false);
                                                     edit.apply();
                                                     startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), REQUEST_CODE_UKNOWN_APPS);
                                                 } else {
                                                     startActivity(new Intent(getApplicationContext(), DownloadApk.class));
                                                 }
                                             } else {
                                                 startActivity(new Intent(getApplicationContext(), DownloadApk.class));
                                             }

                                         }
                                     }).setNegativeButton("No, thanks",
                                     new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which) {
//                                             edit.putBoolean("AlertDialog", false);
//                                             edit.apply();
                                             VerifyVersions();
                                         }
                                     }).create();
                     mDialogDownloadApp.show();
                 } else {
                     Intent i = new Intent(getApplicationContext(), DownloadApk.class);
                     startActivity(i);
//                     DownloadButtonClicked();
                 }
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (requestCode == REQUEST_CODE_UKNOWN_APPS && resultCode == Activity.RESULT_OK) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    startActivity(new Intent(getApplicationContext(), DownloadApk.class));
                }
            } else {
                //give the error
            }
        }

    }
}
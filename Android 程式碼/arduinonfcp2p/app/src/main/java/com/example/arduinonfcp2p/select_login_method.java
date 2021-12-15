package com.example.arduinonfcp2p;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class select_login_method extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 100 ;
    NfcAdapter nNfcAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_login_method);

        // 當前頁面禁用android beam
        nNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nNfcAdapter.setNdefPushMessage(null, this);

        // 按鈕設定

        Button imei_layout = (Button) findViewById(R.id.Login_with_IMEI);
        imei_layout.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(select_login_method.this, imei_login.class);
                startActivity(i);
                finish();
            }
        });
        Button remove_user = (Button) findViewById(R.id.Remove_User);
        remove_user.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(select_login_method.this, remove_User.class);
                startActivity(i);
                finish();
            }
        });
        Button user_layout = (Button) findViewById(R.id.Login_with_User);
        user_layout.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(select_login_method.this, user_login.class);
                startActivity(i);
                finish();
            }
        });
        Button create_new_user = (Button) findViewById(R.id.Create_New_User);
        create_new_user.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(select_login_method.this, create_new_user.class);
                startActivity(i);
                finish();
            }
        });
        Button Shut_Down_App = (Button)findViewById(R.id.Shut_Down_App);
        Shut_Down_App.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // 確認NFC 功能 是否開啟
        NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdpt != null) {
            if (nfcAdpt.isEnabled()) {

            } else {
                new AlertDialog.Builder(select_login_method.this)
                        .setTitle("警告視窗")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("需有開啟NFC權限才可使用")
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("前往設定開啟NFC", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openAppSettingsIntent();
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                            }
                        })
                        .show();
            }
        }
        // 確認 IMEI 功能是否開啟
        if (ContextCompat.checkSelfPermission(select_login_method.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(select_login_method.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }
    // 檢測 是否提供IMEI 需要的權限
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case  PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }else{
                    new AlertDialog.Builder(select_login_method.this)
                            .setTitle("警告視窗")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("需有權限才可使用")
                            .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setNegativeButton("前往設定給予權限", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    openAppSettingsIntent();
                                    finish();
                                }
                            })
                            .show();
                    }
            }
    }

    //開啟設定
    private void openAppSettingsIntent(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    // 當前頁面禁用android beam
    protected void onResume() {
        super.onResume();
        // catch all NFC intents
        Intent intent = new Intent(getApplicationContext(), getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        nNfcAdapter.enableForegroundDispatch(this, pIntent, null, null);
    }
    // 當前頁面禁用android beam
    protected void onPause() {
        super.onPause();
        nNfcAdapter.disableForegroundDispatch(this);
    }
    // 當前頁面禁用android beam
    protected void onNewIntent(Intent intent) {
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            return;
        }
    }

}



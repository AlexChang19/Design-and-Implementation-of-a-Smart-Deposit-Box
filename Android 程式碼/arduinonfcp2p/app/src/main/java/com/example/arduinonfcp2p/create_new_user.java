package com.example.arduinonfcp2p;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class create_new_user extends AppCompatActivity {
    private  TextView IMEI_number;
    private  EditText new_username;
    private  EditText new_password;
    public static String NdefData_user = "0";
    public static String NDEFdata_Admin = "0";
    public static String mDeviceIMEI = "0";
    TelephonyManager mTelephonyManager = null;
    private final String TAG = imei_login.class.getSimpleName();
    NfcAdapter nNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_new_user);
        // 當前頁面禁用android beam
        nNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nNfcAdapter.setNdefPushMessage(null, this);

        getDeviceId();

        IMEI_number = (TextView) findViewById(R.id.IMEI_number);
        IMEI_number.setText(mDeviceIMEI);
        new_username = (EditText) findViewById(R.id.new_username);
        new_password = (EditText ) findViewById(R.id.new_password);
        Button Create_New_User = (Button) findViewById(R.id.Create_New_User);
        Create_New_User.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if ("".equals(new_username.getText().toString().trim())){
                            new AlertDialog.Builder(create_new_user.this)
                                    .setTitle("警告視窗")
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setMessage("請輸入NewUserName")
                                    .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }else {
                            if ("".equals(new_password.getText().toString().trim())){
                                new AlertDialog.Builder(create_new_user.this)
                                        .setTitle("警告視窗")
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setMessage("請輸入NewPassWord")
                                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                            }else {
                                String  new_username_str = new_username.getText().toString();
                                String new_password_str = new_password.getText().toString();
                                NdefData_user = new_username_str+","+new_password_str+","+mDeviceIMEI;
                                Intent intent_create_user = new Intent();
                                intent_create_user.setClass(create_new_user.this, nfc_ndef_p2p_new_user_only.class);
                                intent_create_user.putExtra("TNFdata","create_new_user");
                                intent_create_user.putExtra("NDEFdata",NdefData_user);
                                startActivity(intent_create_user);
                                finish();
                            }
                        }
                    }
        });

        Button back_select_method = (Button) findViewById(R.id.back_select_method);
        back_select_method.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(create_new_user.this, select_login_method.class);
                startActivity(i);
                finish();
            }
        });

    }
    private void getDeviceId(){
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if( Build.VERSION.SDK_INT >= 26 ) {
                mDeviceIMEI = mTelephonyManager.getImei();
            }else {
                mDeviceIMEI = mTelephonyManager.getDeviceId();
            }
        } catch (SecurityException e) {
            // expected
            Log.d(TAG, "SecurityException e");
        }
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

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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class remove_User extends AppCompatActivity {
    private EditText username;
    private EditText password;
    public static String Ndef_data = "0";
    public static String mDeviceIMEI = "0";
    TelephonyManager mTelephonyManager = null;
    private final String TAG = imei_login.class.getSimpleName();
    NfcAdapter nNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove__user);

        // 當前頁面禁用android beam
        nNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nNfcAdapter.setNdefPushMessage(null, this);

        username = (EditText) findViewById(R.id.Remove_username);
        password = (EditText ) findViewById(R.id.Remove_password);
        getDeviceId();

        // imei標籤設定
        TextView imei_number = (TextView) findViewById(R.id.imei_number);
        imei_number.setText(mDeviceIMEI);

        // Button 確認
        final Button Remove_User = (Button) findViewById(R.id.Remove_User);
        Remove_User.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if ("".equals(username.getText().toString().trim()))
                {
                    new AlertDialog.Builder(remove_User.this)
                            .setTitle("警告視窗")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("請輸入UserName")
                            .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else if ("".equals(password.getText().toString().trim())){
                           new AlertDialog.Builder(remove_User.this)
                            .setTitle("警告視窗")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("請輸入UserPassword")
                            .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else{
                    new AlertDialog.Builder(remove_User.this)
                            .setTitle("警告視窗")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("確認要刪除 帳號 ? ")
                            .setNegativeButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String  username_str = username.getText().toString();
                                    String password_str = password.getText().toString();
                                    Ndef_data = username_str+","+password_str+","+mDeviceIMEI;
                                    Intent intent_remove_user = new Intent();
                                    intent_remove_user.setClass(remove_User.this, nfc_ndef_p2p_imei.class);
                                    intent_remove_user.putExtra("TNFdata","remove_user");
                                    intent_remove_user.putExtra("NDEFdata",Ndef_data);
                                    startActivity(intent_remove_user);
                                    finish();
                                }
                            })
                            .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                }

        });

        // Button 退回設定
        Button back_select_method = (Button) findViewById(R.id.back_select_method);
        back_select_method.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(remove_User.this, select_login_method.class);
                startActivity(i);
                finish();
            }
        });
        /// 密碼顯示 function
    }
    // 取得 imei id
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

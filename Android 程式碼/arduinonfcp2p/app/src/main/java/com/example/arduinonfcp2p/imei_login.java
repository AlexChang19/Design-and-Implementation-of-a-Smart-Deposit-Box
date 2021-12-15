package com.example.arduinonfcp2p;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class imei_login extends AppCompatActivity {

    NfcAdapter nNfcAdapter;
    public static String mDeviceIMEI = "0";
    TelephonyManager mTelephonyManager = null;
    private final String TAG = imei_login.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei_login);

        // 當前頁面禁用android beam
        nNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nNfcAdapter.setNdefPushMessage(null, this);

        getDeviceId();
        // imei標籤設定
        TextView imei_number = (TextView) findViewById(R.id.imei_msg);
        imei_number.setText(mDeviceIMEI);

        // 按鈕設定
        Button imei_login = (Button) findViewById(R.id.Login_IMEI);
        imei_login.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent_imei = new Intent();
                intent_imei.setClass(imei_login.this, nfc_ndef_p2p_imei.class);
                intent_imei.putExtra("TNFdata","imei");
                intent_imei.putExtra("NDEFdata",mDeviceIMEI);

                startActivity(intent_imei);
                finish();

            }
        });

        Button back_select_method = (Button) findViewById(R.id.back_select_method);
        back_select_method.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(imei_login.this, select_login_method.class);
                startActivity(i);
                finish();
            }
        });
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

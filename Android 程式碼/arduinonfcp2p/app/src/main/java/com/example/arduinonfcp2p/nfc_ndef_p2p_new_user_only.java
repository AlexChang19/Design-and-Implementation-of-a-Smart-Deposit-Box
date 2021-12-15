package com.example.arduinonfcp2p;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.charset.Charset;

public class nfc_ndef_p2p_new_user_only extends AppCompatActivity {

    NfcAdapter nNfcAdapter;
    public  String MIMETYPE = " ";
    public  String ndef_data_in="0";
    public  String ndef_data_admin="0";
    public  String ndef_data="0";
    private static  final  int BEAM_BEAMED = 0x1001;
    private final String TAG = nfc_ndef_p2p_new_user_only.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_ndef_p2p_imei);
        TextView Ndefinfornation = (TextView) findViewById(R.id.NDEF_MESSAGE);
        TextView NdefTNFMIME = (TextView) findViewById(R.id.NDEF_TNF_MIME);
        Intent intent_imei = this.getIntent();
        String tnf_str = intent_imei.getStringExtra("TNFdata");
        String ndef_str = intent_imei.getStringExtra("NDEFdata");

        ndef_data_in = ndef_str;
        MIMETYPE = tnf_str;
        ndef_data =  ndef_str;
        Ndefinfornation.setText(ndef_data);
        NdefTNFMIME.setText(MIMETYPE);
        // nfc 傳輸功能
        //  檢查 nfc 開啟狀態
        nNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nNfcAdapter == null){

        }
        // NFC 傳輸資料
        nNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback(){
           @Override
           public NdefMessage createNdefMessage(NfcEvent event){
                   String Message = ndef_data.toString();
                   String text = (Message);
                   byte[] mime = MIMETYPE.getBytes(Charset.forName("US-ASCII"));
                   NdefRecord mimeMessage = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mime, new byte[0], text.getBytes());
                   NdefMessage msg = new NdefMessage(new NdefRecord[]{mimeMessage, NdefRecord.createApplicationRecord("com.arduinoandroid.arduinonfc")});
                   return msg;




           }
        },this);
        nNfcAdapter.setOnNdefPushCompleteCallback(new NfcAdapter.OnNdefPushCompleteCallback() {
            @Override
            public void onNdefPushComplete(NfcEvent event) {
                mHandler.obtainMessage(BEAM_BEAMED).sendToTarget();

            }
        }, this);

        // 按鈕
        Button re_transmit = (Button) findViewById(R.id.re_transmit);
        re_transmit.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });
        Button back_select_method = (Button) findViewById(R.id.back_select_method);
        back_select_method.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(nfc_ndef_p2p_new_user_only.this, select_login_method.class);
                startActivity(i);
                finish();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case BEAM_BEAMED:
                    Intent i = new Intent();
                    i.setClass(nfc_ndef_p2p_new_user_only.this, select_login_method.class);
                    startActivity(i);
                    finish();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            try {
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                NdefRecord[] records = msg.getRecords();
                byte[] firstPayload = records[0].getPayload();
                String message = new String(firstPayload);

            } catch (Exception e) {
                Log.e(TAG, "Error retrieving beam message.", e);
            }
        }
    }
    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
    }
}

package com.example.arduinonfcp2p;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class user_login extends AppCompatActivity {
    NfcAdapter nNfcAdapter;
    private EditText username;
    private EditText password;
    public static String NdefData_user = "0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // 當前頁面禁用android beam
        nNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nNfcAdapter.setNdefPushMessage(null, this);

        username = (EditText) findViewById(R.id.login_username);
        password = (EditText ) findViewById(R.id.login_password);

        final CheckBox show_password = (CheckBox)findViewById(R.id.show_password);
        final CheckBox remerber_me = (CheckBox)findViewById(R.id.Remember_Me);

        SharedPreferences remdname=getPreferences(Activity.MODE_PRIVATE);
        String name_str=remdname.getString("name", "");
        String pass_str=remdname.getString("password", "");
        username.setText(name_str);
        password.setText(pass_str);

        // 按鈕

        Button back_select_method = (Button) findViewById(R.id.back_select_method);
        back_select_method.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent();
                i.setClass(user_login.this, select_login_method.class);
                startActivity(i);
                finish();
            }
        });
        // 記憶
        if(! remerber_me.isChecked())
        {
            SharedPreferences.Editor edit=remdname.edit();
            edit.putString("name","");
            edit.putString("password","");
            edit.commit();
        }
        remerber_me.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // 存帳號 reback
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    SharedPreferences remdname=getPreferences(Activity.MODE_PRIVATE);
                    SharedPreferences.Editor edit=remdname.edit();
                    edit.putString("name", username.getText().toString());
                    edit.putString("password", password.getText().toString());
                    edit.commit();
                }
            }
        });



        /// 密碼顯示 function
        show_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(show_password.isChecked()){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());//顯示密碼
                }else{
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());  //隱藏密碼
                }
            }
        });

        /// 確認 function
        Button uspw_login = (Button) findViewById(R.id.Login_User);
        uspw_login.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if ("".equals(username.getText().toString().trim())){
                    new AlertDialog.Builder(user_login.this)
                            .setTitle("警告視窗")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("請輸入UserName")
                            .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else {
                                 if ("".equals(password.getText().toString().trim())){
                                     new AlertDialog.Builder(user_login.this)
                                             .setTitle("警告視窗")
                                             .setIcon(R.mipmap.ic_launcher)
                                             .setMessage("請輸入PassWord")
                                             .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialog, int which) {
                                                 }
                                             })
                                             .show();
                    }else {
                                     if(remerber_me.isChecked())//檢測使用者名密碼
                                     {
                                         SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
                                         SharedPreferences.Editor edit = remdname.edit();
                                         edit.putString("name",username.getText().toString());
                                         edit.putString("password",password.getText().toString());
                                         edit.commit();
                                         String  username_str = username.getText().toString();
                                         String password_str = password.getText().toString();
                                         NdefData_user = username_str+","+password_str;
                                         Intent intent_user = new Intent();
                                         intent_user.setClass(user_login.this, nfc_ndef_p2p_imei.class);
                                         intent_user.putExtra("TNFdata","user_login");
                                         intent_user.putExtra("NDEFdata",NdefData_user);
                                         startActivity(intent_user);
                                         finish();
                                     }else{

                                         String  username_str = username.getText().toString();
                                         String password_str = password.getText().toString();
                                         NdefData_user = username_str+","+password_str;
                                         Intent intent_user = new Intent();
                                         intent_user.setClass(user_login.this, nfc_ndef_p2p_imei.class);
                                         intent_user.putExtra("TNFdata","user_login");
                                         intent_user.putExtra("NDEFdata",NdefData_user);
                                         startActivity(intent_user);
                                         finish();
                                     }
                                 }
                }
            }
        });

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

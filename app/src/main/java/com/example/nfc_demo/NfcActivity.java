package com.example.nfc_demo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NfcActivity extends AppCompatActivity {
    private TextView tv_data,tv_title;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        tv_title = findViewById(R.id.tv_title);
        tv_data = findViewById(R.id.tv_data);

        //1.检测nfc
        nfcCheck();
        //2.初始化
        initNfc();
    }

    private void nfcCheck() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter==null){
            Toast.makeText(this, "请打开NFC", Toast.LENGTH_SHORT).show();
        }else{
            if(!mNfcAdapter.isEnabled()){
                Intent setNFC = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(setNFC);
            }
        }
    }

    private void initNfc() {
        mPendingIntent = PendingIntent.getActivity(this,0,
                new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE,null);
        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            intentFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //这里必须setIntent，set  NFC事件响应后的intent才能拿到数据
        setIntent(intent);
        Log.i("FlashTestNFC", "onNewIntent");
        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //TODO 获取数据进行下一步处
        tv_data.setText(""+bytesToHex(tag.getId()));
        Log.i("FlashTestNFC--Tag", bytesToHex(tag.getId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("FlashTestNFC", "onResume");
        if (mNfcAdapter != null) {
            //添加intent-filter
            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            IntentFilter[] filters = new IntentFilter[]{ndef, tag, tech};

            //添加 ACTION_TECH_DISCOVERED 情况下所能读取的NFC格式，这里列的比较全，实际我这里是没有用到的，因为测试的卡是NDEF的
            String[][] techList = new String[][]{
                    new String[]{
                            "android.nfc.tech.Ndef",
                            "android.nfc.tech.NfcA",
                            "android.nfc.tech.NfcB",
                            "android.nfc.tech.NfcF",
                            "android.nfc.tech.NfcV",
                            "android.nfc.tech.NdefFormatable",
                            "android.nfc.tech.MifareClassic",
                            "android.nfc.tech.MifareUltralight",
                            "android.nfc.tech.NfcBarcode"
                    }
            };
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, filters, techList);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("FlashTestNFC", "onPause");
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * 2进制to 16进制
     * @param src
     * @return
     */
    private static String bytesToHex(byte[] src){
        StringBuffer sb = new StringBuffer();
        if (src == null || src.length <= 0) {
            return null;
        }
        String sTemp;
        for (int i = 0; i < src.length; i++) {
            sTemp = Integer.toHexString(0xFF & src[i]);
            if (sTemp.length() < 2){
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}

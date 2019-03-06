package com.qytech.serialuartdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private boolean mRunning = true;
    private Button m_btnQueryId, m_btnGetStatus, m_btnSetStatus, m_btnEncryptMessage;

    private TextView m_tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        m_tvMessage = findViewById(R.id.tv_read_msg);
        m_btnEncryptMessage = (Button) findViewById(R.id.btn_encrypt_message);
        m_btnGetStatus = (Button) findViewById(R.id.btn_get_status);
        m_btnQueryId = (Button) findViewById(R.id.btn_query_id);
        m_btnSetStatus = (Button) findViewById(R.id.btn_set_status);
        m_btnSetStatus.setOnClickListener(mOnClickListener);
        m_btnQueryId.setOnClickListener(mOnClickListener);
        m_btnGetStatus.setOnClickListener(mOnClickListener);
        m_btnEncryptMessage.setOnClickListener(mOnClickListener);
        SerialPort.getInstance().open("/dev/ttyS3");
        ReadThread thread = new ReadThread();

        thread.start();

        Timber.d("get checkout sum AA7504 %s", getCheckSum(0xAA, 0x75, 0x04, 0x04, 0x01, 0x01, 0x05, 0x00));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Timber.d("onClick: %s", ((Button) view).getText());
                switch (view.getId()) {
                    case R.id.btn_query_id:
                        byte[] arr = toBytes("AA750100DE");//0xAA0x750x010x000xDE
                        SerialPort.getInstance().write(arr);
                        //回传消息为 55 7A 01 01 01 2E
                        break;
                    case R.id.btn_set_status:
                        SerialPort.getInstance().write(toBytes("AA75020401010500DC"));
                        //回传消息为 55 7A 02 18 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08 00 09 00 0A 00 0B 00 0C 00 39
                        break;
                    case R.id.btn_get_status:
                        SerialPort.getInstance().write(toBytes("AA750300DC"));
                        //回传消息为 55 7A 03 18 01 00 02 00 03 00 04 00 05 00 06 00 07 00 08 00 09 00 0A 00 0B 00 0C 00 38
                        break;
                    case R.id.btn_encrypt_message:
                        SerialPort.getInstance().write(toBytes("AA75040401010500DA"));
                        //回传消息55 7A 04 00 2b
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0x01:
                    m_tvMessage.setText(String.format(Locale.getDefault(), "回读到的消息为:%s", msg.obj));
                    break;
            }
            return false;
        }
    });


    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (mRunning && !interrupted()) {
                    byte[] result = SerialPort.getInstance().read();
                    if (result != null && result.length > 0) {
                        byte[] res = new byte[result.length - 5];
                        System.arraycopy(result, 4, res, 0, result[3]);
                        Timber.d("\r\n");
                        Message msg = mHandler.obtainMessage();
                        msg.what = 0x01;
                        msg.obj = String.format(Locale.getDefault(), "\n返回结果：%s \n数据结果：%s", bytesToHexFun(result), bytesToHexFun(res));
                        mHandler.sendMessage(msg);
                        Timber.d("ReadThread %s", bytesToHexFun(result));

                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunning = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRunning = false;
        SerialPort.getInstance().close();
    }

    public byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    public String bytesToHexFun(byte[] bytes) {
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02X ", b & 0xff));
        }

        return buf.toString();
    }


    public String getCheckSum(int... values) {
        int result = 0x00;
        for (int item : values) {
            result ^= item;
        }
        return Integer.toHexString(result);
    }
}

package com.qytech.serialuartdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean mRunning = true;

    private TextView m_tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_tvMessage = findViewById(R.id.tv_read_msg);
        SerialPort.getInstance().open("/dev/ttyS1");
        ReadThread thread = new ReadThread();

        thread.start();

    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0x01:
                    m_tvMessage.setText("回读到的消息为:" + String.valueOf(msg.obj));
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
                    String result = SerialPort.getInstance().read();
                    if (!TextUtils.isEmpty(result)) {
                        Log.d(TAG, "running == >: " + result);
                        Message msg = mHandler.obtainMessage();
                        msg.what = 0x01;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRunning = false;
        SerialPort.getInstance().close();
    }
}

package com.qytech.serialuartdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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
        SerialPort.getInstance().open("/dev/ttysWK2");
        ReadThread thread = new ReadThread();
        thread.start();
        m_tvMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                SerialPort.getInstance().write("test".getBytes());
            }
        }, 3000);

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
                    byte[] result = SerialPort.getInstance().read();
                    if (result != null && result.length > 0) {
                        Log.d(TAG, "serialUartOut size is : " + result.length);
                        for (byte aResult : result) {
                            Log.d(TAG, "run: " + String.format("%x", aResult));
                        }
                        Log.d(TAG, "run: " + new String(result));
                        Message msg = mHandler.obtainMessage();
                        msg.what = 0x01;
                        msg.obj = new String(result);
                        mHandler.sendMessage(msg);
                    }
                    Thread.sleep(500);
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

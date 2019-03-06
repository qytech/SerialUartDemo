package com.qytech.serialuartdemo;

/**
 * Created by Jax on 2018/10/12.
 * Description :
 * Version : V1.0.0
 */
public class SerialPort {

    private static SerialPort mInstance;


    public static SerialPort getInstance() {
        if (mInstance == null)
            mInstance = new SerialPort();
        return mInstance;
    }

    private SerialPort() {
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native int open(String path);
//
    public native int close();


    public native int write(byte[] message);

    public native byte[] read();


}

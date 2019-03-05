#include <jni.h>
#include <string>
#include <asm/termbits.h>
#include <pty.h>
#include <unistd.h>
#include <fcntl.h>
#include "android/log.h"


static const char *TAG = "SerialPort";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


#define TRUE 0
#define FALSE -1

int fd;

//设置串口属性信息
void set_config(int Pport) {
    struct termios ti;

    tcflush(Pport, TCIOFLUSH);
    tcgetattr(Pport, &ti);
    cfmakeraw(&ti);
    ti.c_cflag |= (CLOCAL | CREAD);
    ti.c_cflag &= ~(PARENB | CSIZE | CSTOPB | CRTSCTS);
    ti.c_cflag |= CS8;
    ti.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

    cfsetospeed(&ti, B9600);
    cfsetispeed(&ti, B9600);
    tcsetattr(Pport, TCSANOW, &ti);
    tcflush(Pport, TCIOFLUSH);
}

jstring charTojstring(JNIEnv *env, const char *pat) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("java/lang/String");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte *) pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("UTF-8");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

char *jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("UTF-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}


extern "C" JNIEXPORT jint JNICALL
Java_com_qytech_serialuartdemo_SerialPort_open(JNIEnv *env, jobject /*this*/, jstring _path) {
    const char *path = jstringToChar(env, _path);
    fd = open(path, O_RDWR);
    LOGD("open device result is %d", fd);
    if (fd < 0) {
        LOGE("open device %s fail !", path);
        return FALSE;
    }
    set_config(fd);
    return TRUE;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_qytech_serialuartdemo_SerialPort_close(JNIEnv *env, jobject /*this*/) {
    return close(fd);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_qytech_serialuartdemo_SerialPort_read(JNIEnv *env, jobject /*this*/) {
    fd_set rd;
    struct timeval tv;
    char buf[64];
    int ret = 0;
    FD_ZERO(&rd);
    FD_SET(fd, &rd);

    tv.tv_sec = 1;
    tv.tv_usec = 0;
    int i = 0;
    ret = select(fd + 1, &rd, NULL, NULL, &tv);
    if (ret < 0)
        return NULL;
    else if (ret == 0)
        return NULL;
    else {
        memset(buf, '\0', sizeof(buf));

        while (1) {
            read(fd, buf + i, 1);
            LOGE("write message %x  !", buf[i]);
            if ((buf[i] == 0x20)){
                break;
            }
            i++;
        }
    }
     jbyteArray jArray = env->NewByteArray(strlen(buf));
     env->SetByteArrayRegion(jArray, 0, strlen(buf),
                                            reinterpret_cast<const jbyte *>(buf));
    //char c_data[5] = {0};
    //char g_data[5] = {0};
    //if (i == 8)  //
    //{
    //   memcpy(c_data, buf + 3, 4);
    //    sprintf(g_data, "%u\n",
    //            c_data[0] * 256 * 256 * 256 + c_data[1] * 256 * 256 + c_data[2] * 256 + c_data[3]);
    //} else if (i == 9) {
    //    memcpy(c_data, buf + 3, 5);
    //    sprintf(g_data, "%u\n",
    //            c_data[1] * 256 * 256 * 256 + c_data[2] * 256 * 256 + c_data[3] * 256 + c_data[4]);
    //}
    //jstring str = charTojstring(env, buf);//
    return jArray;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_qytech_serialuartdemo_SerialPort_write(JNIEnv *env, jobject /*this*/,
                                                jbyteArray _message) {
    jbyte *bBuffer = env->GetByteArrayElements(_message, 0);
    unsigned char *buf = (unsigned char *) bBuffer;
    ssize_t ret = write(fd, buf, static_cast<size_t>(env->GetArrayLength(_message)));
    LOGD("write result %d", ret);
    if (ret < 0) {
        LOGE("write message %s fail !", buf);
        return FALSE;
    }
    return TRUE;
}


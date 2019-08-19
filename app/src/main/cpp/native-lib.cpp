#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>
#include "headFile/Ji_add.cpp"

#define MLOG_NAME "Native层"
/*C语言标志*/
extern "C"
JNIEXPORT jstring JNICALL Java_jinye_demo_jidub_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */ obj) {
    /*std 是c++标准库*/
    std::string hello = "Hello from C++";
    hello = "美丽的";
    hello.append("姑娘你好");
    /*通过Java层类对象获取类实例*/
    jclass mMainC=env->GetObjectClass(obj);
    /*获取要操作的类属性的ID 类对象/属性名/属性签名就是对应类型在JNI层的标志*/
    jfieldID  mMianNumFieldId=env->GetFieldID(mMainC,"mTestNum","I");
    /*通过属性ID 获取属性值*/
    jint mMianNum=env->GetIntField(obj,mMianNumFieldId);
    /*打印*/
    __android_log_print(ANDROID_LOG_INFO,MLOG_NAME,"从上层获取到%d",mMianNum);
    /*底层修改上层的属性*/
    env->SetIntField(obj,mMianNumFieldId,20);
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL Java_jinye_demo_jidub_MainActivity_toolAdditionFromJNI(JNIEnv *env, jobject instance
        ,jint mOne, jint mTwo) {
    /*构造初始化*/
    Ji_add m(4);
    return m.ToolAdd(mOne,mTwo);

}

#include <jni.h>
#include <Ultralight/Ultralight.h>
#include "gl/GPUDriverGL.h"
#include "FileSystemBasic.h"
#include <iostream>

using namespace ultralight;

String* jstringToUltralightString(JNIEnv* env_, jstring& str)
{
    const char* ch = env_->GetStringUTFChars(str, 0);
    const int length = env_->GetStringUTFLength(str);
    return new String(ch,length);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_qwq_webcraft_client_ClientEventListener_setNativeConfig(JNIEnv* env_, jclass clazz, jstring font_family_standard, jstring font_family_fixed, jstring font_family_serif, jstring font_family_sans_serif, jstring user_agent)
{
    Config* config = new Config();
    config->font_family_standard = jstringToUltralightString(env_,font_family_standard)->utf16();
    config->font_family_fixed = jstringToUltralightString(env_,font_family_fixed)->utf16();
    config->font_family_serif = jstringToUltralightString(env_,font_family_serif)->utf16();
    config->font_family_sans_serif = jstringToUltralightString(env_,font_family_sans_serif)->utf16();
    config->user_agent = jstringToUltralightString(env_,user_agent)->utf16();
    //config->device_scale_hint = 2;
    return (jlong)config;
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_client_ClientEventListener_nativeInit(JNIEnv* env_, jclass clazz, jlong pointer, jlong pointer2, jlong config)
{
    set_glfwGetTime_address((void*)pointer2);
    Platform::instance().set_file_system(new FileSystemBasic(""));
    Platform::instance().set_config(*((Config*)config));
    gladLoadGLLoader((GLADloadproc)pointer);
    GPUDriver* gpu_driver = new GPUDriverGL();
    Platform::instance().set_gpu_driver(gpu_driver);
}
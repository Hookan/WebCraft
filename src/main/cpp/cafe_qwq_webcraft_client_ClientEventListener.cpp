#include <jni.h>
#include <Ultralight/Ultralight.h>
#include "gl/GPUDriverGL.h"
#include "FileSystemBasic.h"
#include <iostream>

using namespace ultralight;

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_client_ClientEventListener_nativeInit(JNIEnv* env_, jclass clazz, jlong pointer, jlong pointer2)
{
    set_glfwGetTime_address((void*)pointer2);
    Config config;
    //config.animation_timer_delay = 1.0/80.0;
    Platform::instance().set_config(config);
    gladLoadGLLoader((GLADloadproc)pointer);
    GPUDriver* gpu_driver = new GPUDriverGL();
    Platform::instance().set_gpu_driver(gpu_driver);
    Platform::instance().set_file_system(new FileSystemBasic("mods/webcraft/cache/"));
}
#include <jni.h>
#include <Ultralight/Ultralight.h>
#include "glad/glad.h"

using namespace ultralight;

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_createRenderer(JNIEnv* env_, jclass clazz)
{
    return (jlong)(new RefPtr<Renderer>(Renderer::Create()));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_ncreateView(JNIEnv* env_, jobject obj, jlong pointer, jint width, jint height, jboolean transparent)
{
    return (jlong)(new RefPtr<View>(((RefPtr<Renderer>*)pointer)->
                        get()->CreateView((uint32_t)width, (uint32_t)height, (unsigned char)transparent)));
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_npurgeMemory(JNIEnv* env_, jobject obj, jlong pointer)
{
    ((RefPtr<Renderer>*)pointer)->get()->PurgeMemory();
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_update(JNIEnv* env_, jobject obj, jlong pointer)
{
    ((RefPtr<Renderer>*)pointer)->get()->Update();
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_render(JNIEnv* env_, jobject obj, jlong pointer)
{
    ((RefPtr<Renderer>*)pointer)->get()->Render();
    GPUDriver* gpu_driver=Platform::instance().gpu_driver();
    if (gpu_driver->HasCommandsPending())
    {
        gpu_driver->DrawCommandList();
    }
    //glBindBuffer(GL_ARRAY_BUFFER, 0);
}
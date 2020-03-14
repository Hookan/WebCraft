#include <jni.h>
#include <Ultralight/Ultralight.h>
#include "gl/GPUDriverGL.h"
#include <iostream>

using namespace ultralight;

//debug code
/*void printInt(JNIEnv* env_, jobject obj, int x)
{
    jclass clazz = env_->GetObjectClass(obj);
    jmethodID id = env_->GetStaticMethodID(clazz,"test","(I)V");
    env_->CallStaticVoidMethod(clazz,id,x);
}*/

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nloadURL(JNIEnv* env_, jobject obj, jlong pointer, jstring str)
{
    const char* url = env_->GetStringUTFChars(str, 0);
    const int length = env_->GetStringUTFLength(str);
    ((RefPtr<View>*)pointer)->get()->LoadURL(String(url,length));
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nloadHTML(JNIEnv* env_, jobject obj, jlong pointer, jstring str)
{
    const char* html = env_->GetStringUTFChars(str, 0);
    const int length = env_->GetStringUTFLength(str);
    ((RefPtr<View>*)pointer)->get()->LoadHTML(String(html,length));
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_resize(JNIEnv* env_, jobject obj, jlong pointer, jint width, jint height)
{
    ((RefPtr<View>*)pointer)->get()->Resize((uint32_t)width, (uint32_t)height);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_qwq_webcraft_api_View_getRTT(JNIEnv* env_, jobject obj, jlong pointer)
{
    RenderTarget* rttn = new RenderTarget();
    *rttn = (((RefPtr<View>*)pointer)->get()->render_target());
    return (jlong)rttn;
}

extern "C"
JNIEXPORT jint JNICALL
Java_cafe_qwq_webcraft_api_View_getRTTTextureID(JNIEnv* env_, jobject obj, jlong pointer)
{
    RenderTarget* rttn = (RenderTarget*)pointer;
    GPUDriverGL* gpu_driver=(GPUDriverGL*)(Platform::instance().gpu_driver());
    return (jint)(gpu_driver->GetOpenGLTextureByUT(rttn->texture_id));
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_cafe_qwq_webcraft_api_View_getRTTTop(JNIEnv* env_, jobject obj, jlong pointer)
{
    return (jfloat)((((RenderTarget*)pointer)->uv_coords).top);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_cafe_qwq_webcraft_api_View_getRTTBottom(JNIEnv* env_, jobject obj, jlong pointer)
{
    return (jfloat)((((RenderTarget*)pointer)->uv_coords).bottom);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_cafe_qwq_webcraft_api_View_getRTTRight(JNIEnv* env_, jobject obj, jlong pointer)
{
    //GPUDriver* gpu_driver=Platform::instance().gpu_driver();
    //gpu_driver->BindTexture(0,((RenderTarget*)pointer)->texture_id);
    return (jfloat)((((RenderTarget*)pointer)->uv_coords).right);
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_cafe_qwq_webcraft_api_View_getRTTLeft(JNIEnv* env_, jobject obj, jlong pointer)
{
    return (jfloat)((((RenderTarget*)pointer)->uv_coords).left);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_destroyRTT(JNIEnv* env_, jobject obj, jlong pointer)
{
    delete ((RenderTarget*)pointer);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_destroyView(JNIEnv* env_, jobject obj, jlong pointer)
{
    delete ((RefPtr<View>*)pointer);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nfireScrollEvent(JNIEnv* env_, jobject obj, jlong pointer,jint amount)
{
    ScrollEvent event;
    event.type = ScrollEvent::kType_ScrollByPixel;
    event.delta_y = (int)amount;
    event.delta_x = 0;
    ((RefPtr<View>*)pointer)->get()->FireScrollEvent(event);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nfireMouseEvent(JNIEnv* env_, jobject obj, jlong pointer, jint type, jint button, jint x, jint y)
{
    MouseEvent event;
    event.type = (MouseEvent::Type)type;
    event.button = (MouseEvent::Button)button;
    event.x = x;
    event.y = y;
    ((RefPtr<View>*)pointer)->get()->FireMouseEvent(event);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nfireKeyEvent(JNIEnv* env_, jobject obj, jlong pointer, jint type, jint modifiers, jstring str, jint native_code, jint virtual_code)
{
    KeyEvent event;
    event.type = (KeyEvent::Type)type;
    if(type==KeyEvent::kType_Char)
    {
        const char* text = env_->GetStringUTFChars(str, 0);
        const int length = env_->GetStringUTFLength(str);
        //printInt(env_,obj,length);
        //for(int i=0;i<length;i++) printInt(env_,obj,text[i]);
        String text1 = String(text,length);
        event.text = text1;
        event.unmodified_text = text1;
    }
    else
    {
        event.modifiers = (unsigned)modifiers;
        event.virtual_key_code = virtual_code;
        event.native_key_code = native_code;
        GetKeyIdentifierFromVirtualKeyCode(event.virtual_key_code, event.key_identifier);
    }
    ((RefPtr<View>*)pointer)->get()->FireKeyEvent(event);
}
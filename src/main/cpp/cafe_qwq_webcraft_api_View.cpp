#include <jni.h>
#include <Ultralight/Ultralight.h>
#include "gl/GPUDriverGL.h"
#include <iostream>
#include <string>
#include <map>

using namespace ultralight;

JNIEnv* env;

std::map<View*,jobject> view_map;
std::map<JSObjectRef,View*> func_view_map;
std::map<JSObjectRef,jstring> name_map;

inline JSValueRef jstringToJSONValue(JNIEnv* env_, JSContextRef ctx, jstring jstr)
{
    const char* cstr = env_->GetStringUTFChars(jstr, 0);
    //std::cout<<"CSTR="<<cstr<<std::endl;
    JSStringRef jsStr = JSStringCreateWithUTF8CString(cstr);
    JSValueRef value = JSValueMakeFromJSONString(ctx,jsStr);
    JSStringRelease(jsStr);
    return value;
}

inline jstring jsonValueToJstring(JNIEnv* env_, JSContextRef ctx, JSValueRef value)
{
    JSStringRef jsStr = JSValueCreateJSONString(ctx, value, 0, NULL);
    size_t siz = JSStringGetMaximumUTF8CStringSize(jsStr);
    char* cstr = (char*)malloc(siz);
    JSStringGetUTF8CString(jsStr, cstr, siz);
    jstring jstr = env_->NewStringUTF(cstr);
    free(cstr);
    JSStringRelease(jsStr);
    return jstr;
}

JSValueRef JSFuncCallback(JSContextRef ctx, JSObjectRef func,
               JSObjectRef thisObject, size_t count, const JSValueRef args[], JSValueRef* exception)
{
    //std::cout<<"JS Function Callback pid="<<((int)syscall(SYS_gettid))<<std::endl;
    View* view = func_view_map[func];
    jobject jview = view_map[view];
    jclass clazz = env->GetObjectClass(jview);
    jstring result = 0;
    if(!count)
    {
        jmethodID mid = env->GetMethodID(clazz, "jsFuncCallback", "(Ljava/lang/String;)Ljava/lang/String;");
        result = (jstring)(env->CallObjectMethod(jview, mid, name_map[func]));
    }
    else
    {
        jstring jstr = jsonValueToJstring(env, ctx, args[0]);
        jmethodID mid = env->GetMethodID(clazz, "jsFuncCallback", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
        result = (jstring)(env->CallObjectMethod(jview, mid, name_map[func], jstr));
    }
    if(result)
    {
         return jstringToJSONValue(env, ctx, result);
    }
    return JSValueMakeNull(ctx);
}

class WCCLoadListener:public LoadListener
{
public:
    virtual void OnDOMReady(View* view) override
    {
        jobject jview = view_map[view];
        jclass clazz = env->GetObjectClass(jview);
        jmethodID mid = env->GetMethodID(clazz, "onDOMReady", "()V");
        env->CallVoidMethod(jview, mid);
    }
};

inline std::string ToUTF8(const String& str)
{
    String8 utf8 = str.utf8();
    return std::string(utf8.data(), utf8.length());
}

inline const char* Stringify(MessageSource source) {
    switch(source)
    {
        case kMessageSource_XML: return "XML";
        case kMessageSource_JS: return "JS";
        case kMessageSource_Network: return "Network";
        case kMessageSource_ConsoleAPI: return "ConsoleAPI";
        case kMessageSource_Storage: return "Storage";
        case kMessageSource_AppCache: return "AppCache";
        case kMessageSource_Rendering: return "Rendering";
        case kMessageSource_CSS: return "CSS";
        case kMessageSource_Security: return "Security";
        case kMessageSource_ContentBlocker: return "ContentBlocker";
        case kMessageSource_Other: return "Other";
        default: return "";
    }
}

inline const char* Stringify(MessageLevel level)
{
    switch(level)
    {
        case kMessageLevel_Log: return "Log";
        case kMessageLevel_Warning: return "Warning";
        case kMessageLevel_Error: return "Error";
        case kMessageLevel_Debug: return "Debug";
        case kMessageLevel_Info: return "Info";
        default: return "";
    }
}

class WCCViewListener:public ViewListener
{
public:
    virtual void OnAddConsoleMessage(View* caller, MessageSource source, MessageLevel level,
                   const String& message, uint32_t line_number, uint32_t column_number, const String& source_id)
    {
        std::cout << "[WebCraft-Console]: [" << Stringify(source) << "] [" << Stringify(level) << "] " << ToUTF8(message);

        if (source == kMessageSource_JS)
        {
            std::cout << " (" << ToUTF8(source_id) << " @ line " << line_number << ", col " << column_number << ")";
        }

        std::cout << std::endl;
    }
};

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_addJSFuncWithCallback(JNIEnv* env_, jobject obj, jlong pointer, jstring jname)
{
    RefPtr<View> &view = *((RefPtr<View>*)pointer);
    JSContextRef ctx = view->js_context();
    const char* cname = env_->GetStringUTFChars(jname, 0);
    JSStringRef name = JSStringCreateWithUTF8CString(cname);
    JSObjectRef func = JSObjectMakeFunctionWithCallback(ctx, name, JSFuncCallback);
    func_view_map[func] = view.get();
    name_map[func] = (jstring)(env->NewGlobalRef(jname));
    JSObjectRef globalObj = JSContextGetGlobalObject(ctx);
    JSObjectSetProperty(ctx, globalObj, name, func, 0, 0);
    JSStringRelease(name);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_ncreateView(JNIEnv* env_, jobject obj, jlong pointer, jint width, jint height, jboolean transparent,jobject jview)
{
    env = env_;
    RefPtr<View> view = ((RefPtr<Renderer>*)pointer)->get()->CreateView((uint32_t)width, (uint32_t)height, (unsigned char)transparent);
    view->set_load_listener(new WCCLoadListener());
    view->set_view_listener(new WCCViewListener());
    //view_map[view.get()] = env->NewGlobalRef(jview);
    return (jlong)(new RefPtr<View>(view));
}


extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_WebRenderer_update(JNIEnv* env_, jobject obj, jlong pointer)
{
    env = env_;
    ((RefPtr<Renderer>*)pointer)->get()->Update();
}

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
    env = env_;
    const char* url = env_->GetStringUTFChars(str, 0);
    const int length = env_->GetStringUTFLength(str);
    ((RefPtr<View>*)pointer)->get()->LoadURL(String(url,length));
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nloadHTML(JNIEnv* env_, jobject obj, jlong pointer, jstring str)
{
    env = env_;
    const char* html = env_->GetStringUTFChars(str, 0);
    const int length = env_->GetStringUTFLength(str);
    ((RefPtr<View>*)pointer)->get()->LoadHTML(String(html,length));
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_resize(JNIEnv* env_, jobject obj, jlong pointer, jint width, jint height)
{
    //std::cout<<(int)width<<" "<<(int)height<<std::endl;
    env = env_;
    ((RefPtr<View>*)pointer)->get()->Resize((uint32_t)width, (uint32_t)height);
    //((RefPtr<View>*)pointer)->get()->set_needs_paint(true);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_cafe_qwq_webcraft_api_View_getRTT(JNIEnv* env_, jobject obj, jlong pointer)
{
    env = env_;
    RenderTarget* rttn = new RenderTarget();
    *rttn = (((RefPtr<View>*)pointer)->get()->render_target());
    return (jlong)rttn;
}

extern "C"
JNIEXPORT jint JNICALL
Java_cafe_qwq_webcraft_api_View_getRTTTextureID(JNIEnv* env_, jobject obj, jlong pointer)
{
    env = env_;
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
    RefPtr<View>* view = ((RefPtr<View>*)pointer);
    auto it = func_view_map.begin();
    while(it != func_view_map.end())
    {
        if(it->second == view->get())
        {
            name_map.erase(it->first);
            func_view_map.erase(it++);
        }
        else
            it++;
    }
    delete view;
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_nfireScrollEvent(JNIEnv* env_, jobject obj, jlong pointer,jint amount)
{
    env = env_;
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
    env = env_;
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
    env = env_;
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

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_disable(JNIEnv* env, jobject jview, jlong pointer)
{
    RefPtr<View>* view = ((RefPtr<View>*)pointer);
    jobject jview2 = view_map[view->get()];
    view_map.erase(view->get());
    env->DeleteGlobalRef(jview2);
}

extern "C"
JNIEXPORT void JNICALL
Java_cafe_qwq_webcraft_api_View_enable(JNIEnv* env, jobject jview, jlong pointer)
{
    RefPtr<View>* view = ((RefPtr<View>*)pointer);
    if(view_map.find(view->get()) != view_map.end()) return;
    view_map[view->get()] = env->NewGlobalRef(jview);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cafe_qwq_webcraft_api_View_evaluateScript(JNIEnv* env_, jobject jview, jlong pointer, jstring jstr)
{
    env = env_;
    RefPtr<View>* view = ((RefPtr<View>*)pointer);
    const char* script = env_->GetStringUTFChars(jstr, 0);
    const int length = env_->GetStringUTFLength(jstr);
    JSValueRef result = view->get()->EvaluateScript(String(script,length));
    if(JSValueIsNull(view->get()->js_context(), result)) return 0;
    return jsonValueToJstring(env_, view->get()->js_context(), result);
}
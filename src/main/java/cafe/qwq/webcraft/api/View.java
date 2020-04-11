package cafe.qwq.webcraft.api;

import cafe.qwq.webcraft.api.math.Vec2i;
import cafe.qwq.webcraft.api.math.Vec4i;
import cafe.qwq.webcraft.util.FileUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class View
{
    private Vec4i bounds;
    private final long viewPointer;
    private final boolean isTransparent;
    private IResizeCallback resizeCallback;
    private final float scale;
    private final List<Runnable> domReadyCallbakList = new LinkedList<>();
    private final Map<String, IJSFuncCallback> jsCallbackMap = new HashMap<>();
    private static JsonParser jsonParser = new JsonParser();
    
    public View()
    {
        this(0, 0, 100, 100);
    }
    
    public View(int x, int y, int width, int height)
    {
        this(new Vec4i(x, y, width, height), true, 2.0f);
    }
    
    public void finalize() throws Throwable
    {
        super.finalize();
        destroyView(viewPointer);
    }
    
    /**
     * @param vec           一个表示坐标和长宽的向量
     * @param isTransparent 表示view的背景是否透明
     */
    public View(Vec4i vec, boolean isTransparent, float scale)
    {
        bounds = vec;
        this.scale = scale;
        this.isTransparent = isTransparent;
        viewPointer = WebRenderer.INSTANCE.createView((int) (vec.w * scale), (int) (vec.h * scale), isTransparent, this);
    }
    
    /**
     * @return 这个view的背景是否透明（默认为true且一旦已实例化则不能修改）
     */
    public boolean isTransparent()
    {
        return isTransparent;
    }
    
    /**
     * @return view的位置信息
     */
    public Vec4i getBounds()
    {
        return bounds;
    }
    
    /**
     * 设置view的坐标以及长宽
     */
    public View setBounds(Vec4i bounds)
    {
        this.bounds = bounds;
        resize(viewPointer, (int) (bounds.w * scale), (int) (bounds.h * scale));
        return this;
    }
    
    /**
     * 设置WebScreen窗口大小改变时的回调函数
     */
    public View setResizeCallback(IResizeCallback callback)
    {
        resizeCallback = callback;
        return this;
    }
    
    void onResize(Vec2i vec)
    {
        if (resizeCallback != null) setBounds(resizeCallback.onResize(vec));
    }
    
    public View loadHTML(String html)
    {
        nloadHTML(viewPointer, html);
        return this;
    }
    
    public View loadHTML(ResourceLocation location) throws IOException
    {
        String path = "/assets/" + location.getNamespace() + "/web/" + location.getPath();
        FileUtils.upzipIfNeeded(path);
        String url = "file:///" + "mods/webcraft" + path;
        loadURL(url);
        return this;
    }
    
    public View loadURL(URL url)
    {
        loadURL(url.toString());
        return this;
    }
    
    public View loadURL(String url)
    {
        System.out.println(url);
        nloadURL(viewPointer, url);
        return this;
    }
    
    /**
     * 绘制view，注意本方法必须在离屏渲染之后执行
     */
    public void draw()
    {
        long rtt = getRTT(viewPointer);
        int textureID = getRTTTextureID(rtt);
        float t = getRTTTop(rtt), b = getRTTBottom(rtt), l = getRTTLeft(rtt), r = getRTTRight(rtt);
        //int pID = WCShaders.getSRGBToLinearProgramID();
        //glUseProgram(pID);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.bindTexture(textureID);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableLighting();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(bounds.x, bounds.y, 0.0).tex(l, t).endVertex();
        bufferbuilder.pos(bounds.x + bounds.w, bounds.y, 0.0).tex(r, t).endVertex();
        bufferbuilder.pos(bounds.x + bounds.w, bounds.y + bounds.h, 0.0).tex(r, b).endVertex();
        bufferbuilder.pos(bounds.x, bounds.y + bounds.h, 0.0).tex(l, b).endVertex();
        tessellator.draw();
        RenderSystem.enableLighting();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        destroyRTT(rtt);
    }
    
    public void fireMouseEvent(int type, int buttonType, int x, int y)
    {
        nfireMouseEvent(viewPointer, type, buttonType, (int) (x * scale), (int) (y * scale));
    }
    
    public void fireScrollEvent(int amount)
    {
        nfireScrollEvent(viewPointer, amount);
    }
    
    public void fireKeyEvent(int type, int modifiers, String text, int scanCode, int keyCode)
    {
        nfireKeyEvent(viewPointer, type, modifiers, text, scanCode, keyCode);
    }
    
    public View addJSFuncWithCallback(String name, IJSFuncCallback callback)
    {
        jsCallbackMap.put(name, callback);
        return this;
    }
    
    public void onDOMReady()
    {
        jsCallbackMap.keySet().forEach(name -> addJSFuncWithCallback(viewPointer, name));
    }
    
    public void makeUnuse()
    {
        nmakeUnuse(this.viewPointer);
    }
    
    public String jsFuncCallback(String funcName)
    {
        IJSFuncCallback callback = jsCallbackMap.get(funcName);
        JsonObject obj = callback.callback(null);
        return obj == null ? null : obj.toString();
    }
    
    public String jsFuncCallback(String funcName, String jsonStr)
    {
        IJSFuncCallback callback = jsCallbackMap.get(funcName);
        JsonObject obj = callback.callback(jsonParser.parse(jsonStr).getAsJsonObject());
        return obj == null ? null : obj.toString();
    }
    
    /*public static void test(int x)
    {
        WebCraft.LOGGER.info(x);
    }*/
    
    /**
     * 当WebScreen的尺寸发生改变时会调用的回调函数
     */
    public interface IResizeCallback
    {
        /**
         * @param vec 用一个二维向量表示WebScreen的尺寸，其中x为长，y为宽
         * @return 用一个四维向量表示的view的坐标和长宽
         */
        Vec4i onResize(Vec2i vec);
    }
    
    public interface IJSFuncCallback
    {
        JsonObject callback(JsonObject obj);
    }
    
    private native void nloadURL(long pointer, String url);
    
    private native void nloadHTML(long pointer, String html);
    
    private native void resize(long pointer, int width, int height);
    
    private native long getRTT(long pointer);
    
    private native int getRTTTextureID(long rttPointer);
    
    private native float getRTTTop(long rttPointer);
    
    private native float getRTTBottom(long rttPointer);
    
    private native float getRTTRight(long rttPointer);
    
    private native float getRTTLeft(long rttPointer);
    
    private native void nfireScrollEvent(long pointer, int amount);
    
    private native void nfireMouseEvent(long pointer, int type, int buttonType, int x, int y);
    
    private native void destroyRTT(long pointer);
    
    private native void destroyView(long pointer);
    
    private native void nfireKeyEvent(long pointer, int type, int modifiers, String text, int scanCode, int keyCode);
    
    private native void addJSFuncWithCallback(long pointer, String funcName);
    
    private native void nmakeUnuse(long pointer);
}

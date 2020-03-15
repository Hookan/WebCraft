package cafe.qwq.webcraft.api;

import cafe.qwq.webcraft.api.math.Vec2i;
import cafe.qwq.webcraft.api.math.Vec4i;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.net.URL;

public class View
{
    private Vec4i bounds;
    private final long viewPointer;
    private final boolean isTransparent;
    private IResizeCallback resizeCallback;
    private int vaoID;

    public View()
    {
        this(0, 0, 100, 100);
    }

    public View(int x, int y, int width, int height)
    {
        this(new Vec4i(x, y, width, height), true);
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
    public View(Vec4i vec, boolean isTransparent)
    {
        bounds = vec;
        this.isTransparent = isTransparent;
        viewPointer = WebRenderer.INSTANCE.createView(vec.w, vec.h, isTransparent);
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
    public void setBounds(Vec4i bounds)
    {
        this.bounds = bounds;
        resize(viewPointer, bounds.w, bounds.h);
    }

    /**
     * 设置WebScreen窗口大小改变时的回调函数
     */
    public void setResizeCallback(IResizeCallback callback)
    {
        resizeCallback = callback;
    }

    void onResize(Vec2i vec)
    {
        if (resizeCallback != null) setBounds(resizeCallback.onResize(vec));
    }

    public void loadHTML(String html)
    {
        nloadHTML(viewPointer, html);
    }

    public void loadURL(URL url)
    {
        loadURL(url.toString());
    }

    public void loadURL(String url)
    {
        nloadURL(viewPointer, url);
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
        nfireMouseEvent(viewPointer, type, buttonType, x, y);
    }

    public void fireScrollEvent(int amount)
    {
        nfireScrollEvent(viewPointer, amount);
    }

    public void fireKeyEvent(int type, int modifiers, String text, int scanCode, int keyCode)
    {
        nfireKeyEvent(viewPointer, type, modifiers, text, scanCode, keyCode);
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
}

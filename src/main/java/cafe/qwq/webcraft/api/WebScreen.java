package cafe.qwq.webcraft.api;

import cafe.qwq.webcraft.api.math.Vec2i;
import cafe.qwq.webcraft.api.math.Vec4i;
import cafe.qwq.webcraft.client.KeyboardHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class WebScreen extends Screen
{
    private List<View> viewList;
    private boolean shouldCloseOnEsc;
    private List<IRenderer> rendererList1;
    private List<IRenderer> rendererList2;
    protected double scale;

    public WebScreen(ITextComponent component)
    {
        super(component);
        viewList = new LinkedList<>();
        rendererList1 = new LinkedList<>();
        rendererList2 = new LinkedList<>();
        shouldCloseOnEsc = true;
    }

    /**
     * 添加一个网页View
     */
    public WebScreen addView(View view)
    {
        viewList.add(view);
        return this;
    }

    /**
     * 设置当按下Esc键时是否关闭GUI
     */
    public WebScreen setShouldCloseOnEsc(boolean shouldCloseOnEsc)
    {
        this.shouldCloseOnEsc = shouldCloseOnEsc;
        return this;
    }

    /**
     * 添加一个Renderer可以在网页渲染前渲染自己的东西
     */
    public WebScreen addPreRenderer(IRenderer renderer)
    {
        rendererList1.add(renderer);
        return this;
    }

    /**
     * 添加一个Renderer可以在网页渲染后渲染自己的东西
     */
    public WebScreen addPostRenderer(IRenderer renderer)
    {
        rendererList2.add(renderer);
        return this;
    }

    protected void init()
    {
        scale = minecraft.getMainWindow().getGuiScaleFactor();
        viewList.forEach(view -> {
            view.enable();
            view.onResize(new Vec2i((int) (width * scale), (int) (height * scale)));
        });
    }

    public void onClose()
    {
        super.onClose();
        viewList.forEach(view -> view.disable());
        WebRenderer.INSTANCE.purgeMemory();
    }

    public boolean shouldCloseOnEsc()
    {
        return shouldCloseOnEsc;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int buttonID)
    {
        mouseX *= scale;
        mouseY *= scale;
        for (View view : viewList)
            view.fireMouseEvent(1, buttonID, (int) mouseX - view.getBounds().x, (int) mouseY - view.getBounds().y);
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int buttonID)
    {
        mouseX *= scale;
        mouseY *= scale;
        for (View view : viewList)
            view.fireMouseEvent(2, buttonID, (int) mouseX - view.getBounds().x, (int) mouseY - view.getBounds().y);
        return true;
    }

    public void mouseMoved(double mouseX, double mouseY)
    {
        mouseX *= scale;
        mouseY *= scale;
        for (View view : viewList)
            view.fireMouseEvent(0, 0, (int) (mouseX - view.getBounds().x), (int) (mouseY - view.getBounds().y));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        for (View view : viewList)
        {
            Vec4i vec = view.getBounds();
            if (vec.x <= mouseX && vec.x + vec.w >= mouseX && vec.y <= mouseY && vec.y + vec.h >= mouseY)
                view.fireScrollEvent((int) (scrollDelta * 25));
        }
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers)
    {
        viewList.forEach(view -> view.fireKeyEvent(3, 0, Character.toString(codePoint), 0, 0));
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        int uKeyCode = KeyboardHelper.glfwKeyCodeToUltralightKeyCode(keyCode);
        int uModifiers = KeyboardHelper.glfwModsToUltralightMods(modifiers);
        viewList.forEach(view -> view.fireKeyEvent(2, uModifiers, null, scanCode, uKeyCode));
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            charTyped('\r', 0);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        int uKeyCode = KeyboardHelper.glfwKeyCodeToUltralightKeyCode(keyCode);
        int uModifiers = KeyboardHelper.glfwModsToUltralightMods(modifiers);
        viewList.forEach(view -> view.fireKeyEvent(1, uModifiers, null, scanCode, uKeyCode));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void renderBackground(int p_renderBackground_1_)
    {
        RenderSystem.pushMatrix();
        RenderSystem.scaled(scale, scale, scale);
        if (this.minecraft.world != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this));
        }
        else
        {
            this.renderDirtBackground(p_renderBackground_1_);
        }
        RenderSystem.popMatrix();
    }

    //double lastTime = 0.0;
    //int fpsSum = 0;

    public void render(int mX, int mY, float pTicks)
    {
        int mouseX = (int) (mX * scale);
        int mouseY = (int) (mY * scale);

        RenderSystem.pushMatrix();
        RenderSystem.scaled(1.0 / scale, 1.0 / scale, 1.0 / scale);

        WebRenderer.INSTANCE.offscreenRender();
        //renderBackground();

        rendererList1.forEach(renderer -> renderer.render(mouseX, mouseY, pTicks));
        viewList.forEach(view -> view.draw());
        rendererList2.forEach(renderer -> renderer.render(mouseX, mouseY, pTicks));

        //fpsSum++;
        //double time = GLFW.glfwGetTime();
        /*if (time - lastTime > 1.0)
        {
            //System.out.printf("FPS = %.1f\n", 1.0 * fpsSum / (time - lastTime));
            int a = (int) (1.0 * fpsSum / (time - lastTime) * 10 + 0.5);
            GLFW.glfwSetWindowTitle(minecraft.getMainWindow().getHandle(),
                    "Minecraft 1.15.2 FPS: " + a / 10 + "." + a % 10);
            lastTime = time;
            fpsSum = 0;
        }*/

        RenderSystem.popMatrix();
    }
}

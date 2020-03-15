package cafe.qwq.webcraft.api;

import cafe.qwq.webcraft.api.math.Vec2i;
import cafe.qwq.webcraft.api.math.Vec4i;
import cafe.qwq.webcraft.client.KeyboardHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class WebScreen extends Screen
{
    private List<View> viewList;
    private boolean shouldCloseOnEsc;

    public WebScreen(ITextComponent component)
    {
        super(component);
        viewList = new LinkedList<>();
        shouldCloseOnEsc = true;
    }

    public WebScreen addView(View view)
    {
        viewList.add(view);
        return this;
    }

    protected void init()
    {
        viewList.forEach(view -> view.onResize(new Vec2i(width, height)));
    }

    public void onClose()
    {
        super.onClose();
        WebRenderer.INSTANCE.purgeMemory();
    }

    public boolean shouldCloseOnEsc()
    {
        return shouldCloseOnEsc;
    }

    public void setShouldCloseOnEsc(boolean shouldCloseOnEsc)
    {
        this.shouldCloseOnEsc = shouldCloseOnEsc;
    }

    public void setSize(int w, int h)
    {
        super.setSize(w, h);
        viewList.forEach(view -> view.onResize(new Vec2i(w, h)));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int buttonID)
    {
        viewList.forEach(view -> view.fireMouseEvent(1, buttonID, (int) mouseX - view.getLocation().x, (int) mouseY - view.getLocation().y));
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int buttonID)
    {
        viewList.forEach(view -> view.fireMouseEvent(2, buttonID, (int) mouseX - view.getLocation().x, (int) mouseY - view.getLocation().y));
        return true;
    }

    public void mouseMoved(double mouseX, double mouseY)
    {
        viewList.forEach(view -> view.fireMouseEvent(0, 0, (int) (mouseX - view.getLocation().x), (int) (mouseY - view.getLocation().y)));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
    {
        for (View view : viewList)
        {
            Vec4i l = view.getLocation();
            if (l.x <= mouseX && l.x + l.w >= mouseX && l.y <= mouseY && l.y + l.h >= mouseY)
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

    double lastTime = 0.0;
    int fpsSum = 0;

    public void render(int mouseX, int mouseY, float pTicks)
    {
        WebRenderer.INSTANCE.offscreenRender();
        renderBackground();
        viewList.forEach(view -> view.draw());
        fpsSum++;
        double time = GLFW.glfwGetTime();
        if (time - lastTime > 1.0)
        {
            //System.out.printf("FPS = %.1f\n", 1.0 * fpsSum / (time - lastTime));
            int a = (int) (1.0 * fpsSum / (time - lastTime) * 10 + 0.5);
            GLFW.glfwSetWindowTitle(minecraft.getMainWindow().getHandle(),
                    "Minecraft 1.15.2 FPS: " + a / 10 + "." + a % 10);
            lastTime = time;
            fpsSum = 0;
        }
    }
}

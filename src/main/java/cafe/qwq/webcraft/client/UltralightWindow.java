package cafe.qwq.webcraft.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

public class UltralightWindow
{
    private static UltralightWindow instance;

    private long ulWindowHandle;
    private long mainWindowHandle;

    private UltralightWindow()
    {
        mainWindowHandle = Minecraft.getInstance().getMainWindow().getHandle();
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        ulWindowHandle = glfwCreateWindow(640, 480, "Ultralight", 0, mainWindowHandle);
    }

    static void init()
    {
        instance = new UltralightWindow();
    }

    public static UltralightWindow getInstance()
    {
        return instance;
    }

    public long getHandle()
    {
        return ulWindowHandle;
    }

    public void makeCurrent()
    {
        glfwMakeContextCurrent(ulWindowHandle);
    }

    public void unmakeCurrent()
    {
        glfwMakeContextCurrent(mainWindowHandle);
    }
}

package cafe.qwq.webcraft.client;

import cafe.qwq.webcraft.WebCraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = WebCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventListener
{
    private static Consumer<TickEvent.RenderTickEvent> consumer;
    private static String libPath;

    static
    {
        if (!WebCraft.VERSION.equals("NONE") && !WebCraft.VERSION.equals("NULL"))
        {
            libPath = "mods/webcraft/" + WebCraft.VERSION + "-natives/";
            if (!checkNatives()) downloadNatives();
        }
        else libPath = "mods/webcraft/natives/";

        if (WebCraft.RUNTIME_OS == WebCraft.OS.WINDOWS)
        {
            loadLibrary("UltralightCore");
            loadLibrary("WebCore");
            loadLibrary("Ultralight");
        }

        loadLibrary("webcraft_core");
    }

    private static boolean checkNatives()
    {
        //TODO
        return true;
    }

    private static void downloadNatives()
    {
        //TODO
    }

    private static void loadLibrary(String name)
    {
        String name1;
        switch (WebCraft.RUNTIME_OS)
        {
            case WINDOWS:
                name1 = name + ".dll";
                break;
            case LINUX:
                name1 = "lib" + name + ".so";
                break;
            default:
                name1 = "lib" + name + ".dylib";
                break;
        }
        File f = new File(libPath + name1);
        //为防止部分sb启动器删natives，故将本mod的natives放到mods/webcraft/natives文件夹下
        System.load(f.getAbsolutePath());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event)
    {
        consumer = ClientEventListener::onTick;
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, consumer);
        //WCShaders.init();
    }

    public static void onTick(final TickEvent.RenderTickEvent event)
    {
        MinecraftForge.EVENT_BUS.unregister(consumer);
        UltralightWindow.init();
        UltralightWindow.getInstance().makeCurrent();
        nativeInit(GLFW.Functions.GetProcAddress, GLFW.Functions.GetTime);
        UltralightWindow.getInstance().unmakeCurrent();
    }

    private native static void nativeInit(long pointer1, long pointer2);
}

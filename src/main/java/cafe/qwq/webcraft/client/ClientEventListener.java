package cafe.qwq.webcraft.client;

import cafe.qwq.webcraft.Config;
import cafe.qwq.webcraft.WebCraft;
import cafe.qwq.webcraft.util.FileUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = WebCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventListener
{
    private static Consumer<TickEvent.RenderTickEvent> consumer;
    private static String MAVEN_URL = "https://maven.qwq.cafe/";
    private static String libPath;

    static
    {
        if(WebCraft.RUNTIME_OS== WebCraft.OS.WINDOWS)
            MAVEN_URL = "https://ci.qwq.cafe/maven/";

        if (!WebCraft.VERSION.equals("NONE") && !WebCraft.VERSION.equals("NULL"))
        {
            libPath = "mods/webcraft/natives-" + WebCraft.VERSION + "/";
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

    private static String getNativePath(String name)
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
        return libPath + name1;
    }

    private static boolean checkNatives()
    {
        File f = new File(libPath);
        if (!f.exists() || !f.isDirectory()) return false;

        File f1 = new File(getNativePath("UltralightCore"));
        if (!f1.exists() || !f1.isFile()) return false;

        f1 = new File(getNativePath("WebCore"));
        if (!f1.exists() || !f1.isFile()) return false;

        f1 = new File(getNativePath("Ultralight"));
        if (!f1.exists() || !f1.isFile()) return false;

        f1 = new File(getNativePath("webcraft_core"));
        if (!f1.exists() || !f1.isFile()) return false;

        return true;
    }

    private static void downloadNatives()
    {
        String fileName = "webcraft-" + WebCraft.VERSION + "-natives";
        switch (WebCraft.RUNTIME_OS)
        {
            case WINDOWS:
                fileName += "-win.jar";
                break;
            case LINUX:
                fileName += "-linux.jar";
                break;
            default:
                fileName += "-mac.jar";//虽然并没有MAC版本
                break;
        }

        String urlstr = MAVEN_URL + "cafe/qwq/webcraft/" + WebCraft.VERSION + "/" + fileName;
        try
        {
            URL url = new URL(urlstr);
            File outputFile = new File("mods/webcraft/" + fileName);
            if (Config.getInstance().downloadNativesSilently)
            {
                FileUtils.downloadFile(url, outputFile, null);
            }
            else
            {
                System.setProperty("java.awt.headless", "false");

                JFrame frame = new JFrame();
                JPanel panel = new JPanel();

                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                frame.setContentPane(panel);
                frame.setTitle("WebCraft");
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int width = 800;
                int height = 70;
                int x = (screenSize.width - width) / 2;
                int y = (screenSize.height - height) / 2;
                frame.setBounds(x, y, width, height);
                frame.setAlwaysOnTop(true);
                frame.setResizable(false);
                frame.setUndecorated(true);
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                panel.setBackground(Color.WHITE);
                JLabel label = new JLabel("Downloading " + fileName + "...");
                label.setFont(new Font("Arial", Font.PLAIN, 16));
                panel.add(label);
                JProgressBar bar = new JProgressBar();
                bar.setMaximum(1000);
                panel.add(bar);
                frame.setVisible(true);

                final String finalFileName = fileName;
                FileUtils.downloadFile(url, outputFile, (size, downloadSize, speed) -> {
                    label.setText(String.format("Downloading %s(%.2fMB/%.2fMB %.2fMB/s)...", finalFileName, downloadSize / 1024f, size / 1024f, speed / 1024f));
                    bar.setValue((int) (downloadSize * 1000 / size));
                });

                frame.dispose();
            }
            ZipFile zip = new ZipFile(outputFile);
            File libDir = new File(libPath);
            if (!libDir.exists()) libDir.mkdirs();
            zip.stream().filter(entry -> !entry.isDirectory() && !entry.getName().contains("MANIFEST.MF"))
                    .forEach(entry -> {
                        try
                        {
                            InputStream input = zip.getInputStream(entry);
                            OutputStream output = new FileOutputStream(libPath + entry.getName());
                            int length;
                            byte[] bytes = new byte[1024];
                            while ((length = input.read(bytes)) != -1)
                            {
                                output.write(bytes, 0, length);
                            }
                            input.close();
                            output.close();
                        }
                        catch (Exception e)
                        {
                            WebCraft.LOGGER.error("Error While Unzipping Natives !");
                            e.printStackTrace();
                        }
                    });
        }
        catch (Exception e)
        {
            WebCraft.LOGGER.error("Error While Downloading Natives !");
            e.printStackTrace();
        }
    }

    private static void loadLibrary(String name)
    {
        File f = new File(getNativePath(name));
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
        Config config = Config.getInstance();
        long configPointer = setNativeConfig(config.fontFamilyStandard, config.fontFamilyFixed, config.fontFamilySerif, config.fontFamilySansSerif, config.userAgent);
        nativeInit(GLFW.Functions.GetProcAddress, GLFW.Functions.GetTime, configPointer);
        UltralightWindow.getInstance().unmakeCurrent();
    }

    private native static long setNativeConfig(String fontFamilyStandard, String fontFamilyFixed, String fontFamilySerif, String fontFamilySansSerif, String userAgent);

    private native static void nativeInit(long pointer1, long pointer2, long pointer3);
}

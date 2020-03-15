package cafe.qwq.webcraft;

import net.minecraftforge.fml.MavenVersionStringHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

@Mod(WebCraft.MODID)
public class WebCraft
{
    public static final String MODID = "webcraft";
    public static String VERSION = "NULL";
    public static final Logger LOGGER = LogManager.getLogger();
    public static OS RUNTIME_OS;

    public WebCraft()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) RUNTIME_OS = OS.WINDOWS;
        else if (osName.contains("linux")) RUNTIME_OS = OS.LINUX;
        else RUNTIME_OS = OS.MAC;
        ModList.get().getMods().stream()
                .filter(info -> info.getModId().equals(MODID))
                .forEach(info -> VERSION = MavenVersionStringHelper.artifactVersionToString(info.getVersion()));
        if (VERSION.equals("NULL")) LOGGER.warn("WebCraft got version failed !");
        else LOGGER.info("Welcome to use WebCraft (version:" + VERSION + ") ! ");
        Config.init();
    }

    public enum OS
    {
        WINDOWS, MAC, LINUX
    }
}

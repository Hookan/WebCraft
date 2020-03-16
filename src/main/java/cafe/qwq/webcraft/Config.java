package cafe.qwq.webcraft;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config
{
    private static Config instance;
    private static final String CONFIG_FILE_PATH = "mods/webcraft/config.json";

    @OnlyIn(Dist.CLIENT)
    public boolean downloadNativesSilently = false;

    @OnlyIn(Dist.CLIENT)
    public String fontFamilyStandard;

    @OnlyIn(Dist.CLIENT)
    public String fontFamilyFixed;

    @OnlyIn(Dist.CLIENT)
    public String fontFamilySerif;

    @OnlyIn(Dist.CLIENT)
    public String fontFamilySansSerif;

    @OnlyIn(Dist.CLIENT)
    public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/602.1 (KHTML, like Gecko) Ultralight/1.1.0 Safari/602.1";

    @OnlyIn(Dist.CLIENT)
    public Config()
    {
        if(WebCraft.RUNTIME_OS == WebCraft.OS.WINDOWS)
        {
            fontFamilyStandard = "Microsoft Yahei";
            fontFamilyFixed = "Microsoft Yahei";
            fontFamilySansSerif = "Microsoft Yahei";
            fontFamilySerif = "Times New Roman";
        }
        else
        {
            fontFamilyStandard = "WenQuanYi Micro Hei";
            fontFamilyFixed = "WenQuanYi Micro Hei";
            fontFamilySansSerif = "WenQuanYi Micro Hei";
            fontFamilySerif = "Noto Serif";
        }
    }

    public static void init()
    {
        File f = new File(CONFIG_FILE_PATH);
        if (f.exists() && f.isFile())
        {
            try (JsonReader reader = new JsonReader(new FileReader(f)))
            {
                Gson gson = new Gson();
                instance = gson.fromJson(reader, Config.class);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            instance = new Config();
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            try
            {
                f.createNewFile();
                try (JsonWriter writer = new JsonWriter(new FileWriter(f, false)))
                {
                    Gson gson = new Gson();
                    gson.toJson(instance, Config.class, writer);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static Config getInstance()
    {
        return instance;
    }
}

package cafe.qwq.webcraft;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config
{
    private static Config instance;
    private static final String CONFIG_FILE_PATH = "mods/webcraft/config.json";
    
    //public boolean downloadNativesSilently = false;
    
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

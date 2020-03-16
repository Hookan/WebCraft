package cafe.qwq.webcraft.util;

import cafe.qwq.webcraft.WebCraft;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

public class FileUtils
{
    private static Set<String> unzipSet = new HashSet<>();

    public static void downloadFile(URL url, File outputFile, IDownloadCallback callback) throws IOException
    {
        URLConnection connection = url.openConnection();
        InputStream input = connection.getInputStream();
        OutputStream output = new FileOutputStream(outputFile);
        float size = connection.getContentLength() / 1024f;
        int length, sum = 0, sum2 = 0;
        byte[] bytes = new byte[1024];
        long lastTime = System.currentTimeMillis();
        String name = outputFile.getName();

        while ((length = input.read(bytes)) != -1)
        {
            output.write(bytes, 0, length);
            sum += length;
            sum2 += length;
            long time = System.currentTimeMillis();
            if (time - lastTime >= 1000)
            {
                WebCraft.LOGGER.info(String.format("Downloading %s(%.2fKB/%.2fKB %.2fKB/s)...", name, sum / 1024f, size, sum2 / (1.024f * (time - lastTime))));
                if (callback != null) callback.callback(size, sum / 1024f, sum2 / (1.024f * (time - lastTime)));
                sum2 = 0;
                lastTime = time;
            }
        }

        long time = System.currentTimeMillis();
        WebCraft.LOGGER.info(String.format("Downloading %s(%.2fKB/%.2fKB %.2fKB/s)...", name, sum / 1024f, size, sum2 / (1.024f * (time - lastTime))));
        if (callback != null) callback.callback(size, sum / 1024f, sum2 / (1.024f * (time - lastTime)));

        input.close();
        output.close();
    }

    public static void upzipIfNeeded(ResourceLocation location) throws IOException
    {
        upzipIfNeeded("/assets/" + location.getNamespace() + "/web/" + location.getPath());
    }

    public static void upzipIfNeeded(String resourcePath) throws IOException
    {
        if (unzipSet.contains(resourcePath)) return;

        File ouputFile = new File("mods/webcraft" + resourcePath);
        if (!ouputFile.getParentFile().exists()) ouputFile.getParentFile().mkdirs();

        InputStream input = FileUtils.class.getResourceAsStream(resourcePath);
        OutputStream output = new FileOutputStream(ouputFile);

        int length;
        byte[] bytes = new byte[1024];

        while ((length = input.read(bytes)) != -1)
        {
            output.write(bytes, 0, length);
        }
    }

    public interface IDownloadCallback
    {
        void callback(float size, float downloadSize, float speed);
    }
}

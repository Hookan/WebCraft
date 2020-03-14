package cafe.qwq.webcraft.demo;

import cafe.qwq.webcraft.api.View;
import cafe.qwq.webcraft.api.WebScreen;
import cafe.qwq.webcraft.api.math.Vec4i;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("demo")
@Mod.EventBusSubscriber(modid = "demo", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Demo
{
    public Demo()
    {
        MinecraftForge.EVENT_BUS.addListener(Demo::onGUiOpen);
    }
    
    @SubscribeEvent
    public static void onGUiOpen(final GuiOpenEvent event)
    {
        if (event.getGui() instanceof WorldSelectionScreen)
        {
            //event.setGui(new DemoScreen());
            WebScreen screen = new WebScreen(new StringTextComponent("test"));
            View view = new View();
            view.setResizeCallback(vec -> new Vec4i(0, 0, vec.x, vec.y));
            view.loadURL("https://www.mcbbs.net");
            //view.loadHTML("<p style=\"font-family: WenQuanYi Micro Hei;\">我好可爱啊喵</p>");
            //view.loadHTML("<p style=\"font-family: 微软雅黑;color:#aaa;\">■■■</p>" +
            //        "<p style=\"font-family: 微软雅黑;color:#555;\">草草草</p>" +
            //        "<p style=\"font-family: 微软雅黑;color:#333;\">喵喵喵</p>" +
            //        "<p style=\"font-family: 微软雅黑;color:#777;\">■■■</p>");
            screen.addView(view);
            event.setGui(screen);
        }
    }
}

package me.jomi.mimiRPG.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {
    public static Main plugin;
    public static String path;
    public static ClassLoader classLoader;


    @Override
    public final void onLoad() {
        plugin = this;
        classLoader = this.getClass().getClassLoader();
        path = getDataFolder().getPath() + '/';
    }
    
    @Override
    public void onEnable() {
        String msg = "\n§a╓───┐ ┌───┐ ┌───┐\n§a║   │ │   │ │\n§a╟───┘ ├───┘ │  ─┬\n§a║ \\   │     │   │\n§a║  \\  │     └───┘§1 by Michałas";
        ProxyServer.getInstance().getConsole().sendMessage(msg);
        ProxyServer.getInstance().getConsole().sendMessage("Ala ma kota");
    }

    @Override
    public void onDisable() {

    }
}

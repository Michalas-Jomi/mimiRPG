package me.jomi.mimiRPG.SkyBlock.Multi;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class MultiSkyBlock implements Listener {
	public static final String prefix = Func.prefix(MultiSkyBlock.class);
	public static final GeneratorChunków generatorChunków = new GeneratorChunków();
    
    
    public static Wyspa wyspa(World world) {
    	if (!world.hasMetadata("mimiSkyBlock"))
    		return null;
    	return (Wyspa) world.getMetadata("mimiSkyBlock").get(0).value();
    }
    public static Wyspa wyspa(Player p) {
    	return wyspa(p.getName());
    }
    public static Wyspa wyspa(String nick) {
    	nick = nick.toLowerCase();
    	
    	String nazwa = nazwaŚwiata(nick);
    	
    	World world = Bukkit.getWorld(nazwa);
    	if (world != null)
    		return wyspa(world);
    	
    	if (new File("archiwalne światy/" + nazwa + ".zip").exists())
			return new Wyspa(nazwa);
    	
    	Player p = Bukkit.getPlayer(nick);
    	if (p == null)
    		return null;
    	
    	return new Wyspa(p);
    }

	public static String nazwaŚwiata(Player p) {
		return nazwaŚwiata(p.getName());
	}
	public static String nazwaŚwiata(String nick) {
		nick = nick.toLowerCase();
		
		return "s_" + nick;
	}
	

    static World wczytajŚwiat(String nazwa) {
    	if (new File("archiwalne światy/" + nazwa + ".zip").exists())
			try {
				Func.unzipFile("archiwalne światy/" + nazwa + ".zip", ".");
			} catch (IOException e) {
				e.printStackTrace();
			}

    	return new WorldCreator(nazwa)
    	.generator(Main.plugin.getName() + ":multiskyblock")
    	.environment(Environment.NORMAL)
    	.generator(generatorChunków)
    	.generateStructures(false)
		.type(WorldType.NORMAL)
		.createWorld();
    }
    static void usuńŚwiat(String nazwa) {
		Bukkit.unloadWorld(nazwa, true);
		try {
			File archiwa = new File("archiwalne światy");
			if (!archiwa.exists())
				archiwa.mkdirs();
			
			Func.zipFile(nazwa, "archiwalne światy/" + nazwa);
			FileUtils.deleteDirectory(new File(nazwa));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    }
    
    @EventHandler
    public void join(PlayerJoinEvent ev) {
    	World świat = wyspa(ev.getPlayer()).world;
    	
    	świat.getSpawnLocation().add(0, -2, 0).getBlock().setType(Material.BEDROCK);
    	ev.getPlayer().teleport(świat.getSpawnLocation());
    }
    @EventHandler
    public void quit(PlayerQuitEvent ev) {
    	Bukkit.getScheduler().runTask(Main.plugin, () -> wyspa(ev.getPlayer()).wyszedł(ev.getPlayer()));
    }
}

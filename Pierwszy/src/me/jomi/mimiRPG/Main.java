package me.jomi.mimiRPG;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import me.jomi.mimiRPG.Chat.Mimi;
import me.jomi.mimiRPG.Chat.Raport;
import me.jomi.mimiRPG.Minigry.Paintball;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;
import me.jomi.mimiRPG.PojedynczeKomendy.Przeładuj;
import me.jomi.mimiRPG.PojedynczeKomendy.ZabezpieczGracza;
import me.jomi.mimiRPG.SkyBlock.AutoEventy;
import me.jomi.mimiRPG.SkyBlock.Budownik;

public class Main extends JavaPlugin {
	// Api Vaults
	public static boolean ekonomia = false;
	public static Permission perms;
    public static Economy econ;
    public static Chat chat;
	// Api Iridium Skyblock
    public static boolean iridiumSkyblock = false;
	// Api WorldGuard
	public static WorldGuardPlugin rg;
    public static StringFlag flagaCustomoweMoby;
	public static StateFlag flagaStawianieBaz;
	public static StateFlag flagaC4;

	
	public static JavaPlugin plugin;
	public static Config ust;
	public static String path;	
	public static ClassLoader classLoader;
	
	private void brakPluginu(String plugin) {
		error("Nie wykryto " + plugin + "! Wyłączanie niektórych funkcji");;
	}
	private void włączWorldGuard() {
		try {
			rg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			
			for (Flag<?> flaga : new Flag<?>[]{
				flagaStawianieBaz = new StateFlag("StawianieBaz", true),
				flagaC4 = new StateFlag("C4", false),
				flagaCustomoweMoby = new StringFlag("CustomoweMoby")
			})
				WorldGuard.getInstance().getFlagRegistry().register(flaga);
		} catch (NoClassDefFoundError e) {
			brakPluginu("WorldGuard");
		}
	}
	private void włączVault() {
        try {
	        if (getServer().getPluginManager().getPlugin("Vault") != null) {
		        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		        econ  = rsp.getProvider();
		        chat  = getServer().getServicesManager().getRegistration(Chat.class).getProvider();
		        perms = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
	        }
	    } catch(NullPointerException e) {}
        ekonomia = econ != null;
        if (!ekonomia)
			brakPluginu("Vault");
	}
	private void włączIridiumSkyblock() {
		try {
        	iridiumSkyblock = IridiumSkyblock.getInstance() != null;
        } catch (NoClassDefFoundError e) {
			brakPluginu("IridiumSkyblock");
        }
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onLoad() {
		plugin = this;
		classLoader = this.getClassLoader();
		path = getDataFolder().getPath() + '/';
	
		// Rejestrowanie ConfigurationSerializable
		for (Class<?> clazz : Func.wszystkieKlasy())
			if (ConfigurationSerializable.class.isAssignableFrom(clazz))
				ConfigurationSerialization.registerClass((Class<? extends ConfigurationSerializable>) clazz);
		
		ust = new Config("ustawienia");
		
		włączWorldGuard();
	}
	@Override
	public void onEnable() {
		włączVault();
		włączIridiumSkyblock();
            
		new Baza();
		new Mimi();
        new Raport();
        
		zarejestruj(new Moduły());
		
		new Przeładuj();
        if (!Zegar.zegary.isEmpty())
        	Zegar.aktywuj();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mimirpg:raport");
        
		String msg = "\n§a╓───┐ ┌───┐ ┌───┐\n§a║   │ │   │ │\n§a╟───┘ ├───┘ │  ─┬\n§a║ \\   │     │   │\n§a║  \\  │     └───┘§1 by Michałas";
		Bukkit.getConsoleSender().sendMessage(msg);
		pluginEnabled = true;        
	}
	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers())
			p.closeInventory();
		if (włączonyModół(Miniony.class))
			Miniony.zapiszMiniony();
		if (ZabezpieczGracza.gracze.size() > 0) {
			log("Odbezpieczanie graczy z bezpiecznego gm");
			while (ZabezpieczGracza.gracze.size() > 0)
				ZabezpieczGracza.odbezpiecz(ZabezpieczGracza.gracze.get(0));
		}
		if (włączonyModół(Koniki.class)) {
			log("Usuwanie Koników graczy");
			Koniki.usuńWszystkie();
		}
		if (włączonyModół(Budownik.class))
			Budownik.wyłączanie();
		if (włączonyModół(AutoEventy.class))
			AutoEventy.wyłącz();
		if (włączonyModół(Paintball.class))
			Paintball.wyłącz();
	}

	static boolean pluginEnabled = false;
	static final WyłączonyExecutor wyłączonyExecutor = new WyłączonyExecutor();
	static void zarejestruj(Object obj) {
		if (obj instanceof Listener)
			plugin.getServer().getPluginManager().registerEvents((Listener) obj, plugin);
		if (obj instanceof Zegar)
			Zegar.zarejestruj((Zegar) obj);
		if (obj instanceof Przeładowalny) {
			Przeładowalny p = (Przeładowalny) obj;
			Przeładowalny.przeładowalne.put(obj.getClass().getSimpleName(), p);
			p.przeładuj();
		}
		if (obj instanceof Komenda && !((Komenda) obj)._zarejestrowane_komendy) {
			for (PluginCommand cmd : ((Komenda) obj)._komendy) {
				cmd.setTabCompleter((Komenda) obj);
				cmd.setExecutor((Komenda) obj);
				((Komenda) obj)._zarejestrowane_komendy = true;
			}
		}
	}
	static void wyrejestruj(Object obj) {
		if (obj instanceof Listener)
			HandlerList.unregisterAll((Listener) obj);
		if (obj instanceof Zegar)
			Zegar.wyrejestruj((Zegar) obj);
		if (obj instanceof Przeładowalny)
			Przeładowalny.przeładowalne.remove(obj.getClass().getSimpleName());
		if (obj instanceof Komenda && ((Komenda) obj)._zarejestrowane_komendy)
			for (PluginCommand cmd : ((Komenda) obj)._komendy) {
				cmd.setTabCompleter(wyłączonyExecutor);
				cmd.setExecutor(wyłączonyExecutor);
				((Komenda) obj)._zarejestrowane_komendy = false;
			}
	}
	
	private static final Logger logger = Logger.getLogger("Minecraft");
	private static final String logprefix = "[mimiRPG] ";
	public static void log(Object... msg) {
		logger.info(logprefix + Func.listToString(msg, 0));
	}
	public static void warn(Object... msg) {
		logger.warning(logprefix + Func.listToString(msg, 0));
	}
	public static void error(Object... msg) {
		logger.severe(logprefix + Func.listToString(msg, 0));
	}

	/**
	 * Dodaje permisje do pluginu
	 * 
	 * @param permisje Dowolna ilość permisji
	 */
	public static void dodajPermisje(String... permisje) {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		for (String permisja : permisje) {
			permisja = Func.permisja(permisja);
			pluginManager.addPermission(new org.bukkit.permissions.Permission(permisja));
		}
	}
	
	public static boolean włączonyModół(Class<?> modół) {
		return Moduły.włączony(modół.getSimpleName());
	}
}


class WyłączonyExecutor implements TabExecutor {
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		arg0.sendMessage("§cTa komenda jest aktualnie wyłączona");
		return true;
	}
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return null;
	}
	
}

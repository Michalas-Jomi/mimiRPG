package me.jomi.mimiRPG;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.jomi.mimiRPG.Chat.*;
import me.jomi.mimiRPG.MiniGierki.*;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;
import me.jomi.mimiRPG.PojedynczeKomendy.Przeładuj;
import me.jomi.mimiRPG.PojedynczeKomendy.ZabezpieczGracza;
import me.jomi.mimiRPG.Gracze.Gracze;
import me.jomi.mimiRPG.Maszyny.*;

public class Main extends JavaPlugin {
	// Blokada skrzynek na zwierzęta jest umiejscowiona w klasie menu
	// Info o aktywnych i nieaktywnych jest w klasie KolorPisania
	// Blokada zabijania Invulnerable mobów jest w Klasie KolorPisania
	
	// TODO blok przyciągający itemy
	// TODO ulepszenie spawnerów
	
    public static Permission perms = null;
    public static Economy econ = null;
    public static Chat chat = null;
	
	public static final HashMap<String, MiniGra> minigry = new HashMap<>();
	
	public static JavaPlugin plugin;
	public static String path;
	
	public static boolean ekonomia = false;
	
	public static Config ust;
	public void onLoad() {
		plugin = this;
		path = getDataFolder().getPath() + '/';
		
		ConfigurationSerialization.registerClass(Napis.class);
		ConfigurationSerialization.registerClass(Grupa.class);
		
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
			Func.wyjmijPlik("Configi/Pomoc.txt", path+"Pomoc.txt");
		}
		
		ust = new Config("ustawienia");
	}
	public void onEnable() {
		ekonomia = setupVault();
        if (!ekonomia)
        	error("Nie wykryto Vault! Wyłączanie niektórych funkcji");
        
		new Baza();
		zarejestruj(new Gracze());
		
		new Mimi();
        new Raport();
        
		zarejestruj(new Moduły());
        
		if (włączonyModół(Minigry.class)) {
			new Minigry();
			for (MiniGra minigra : minigry.values()) 
				zarejestruj(minigra);
		}
		
        if (!Przeładowalny.przeładowalne.isEmpty())
			new Przeładuj();
        if (!Zegar.zegary.isEmpty())
        	Zegar.aktywuj();
        // Wiadomość braku dostępu do komendy
        String pref = Func.prefix("Komenda");
        for (Command cmd : PluginCommandYamlParser.parse(this))
        	cmd.setPermissionMessage(pref + "§cNie masz uprawnień ziomuś");

        
        Main.dodajPermisje("powiadomienia");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mimirpg:raport");
        
		String msg = "\n§a╓───┐ ┌───┐ ┌───┐\n§a║   │ │   │ │\n§a╟───┘ ├───┘ │  ─┬\n§a║ \\   │     │   │\n§a║  \\  │     └───┘§1 by Michałas";
		Bukkit.getConsoleSender().sendMessage(msg);
	}
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
		if (obj instanceof Instrukcja)
			Instrukcja.mapa.put(obj.getClass().getSimpleName(), (Instrukcja) obj);
	}
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
		if (!minigry.isEmpty()) {
			log("Wyłączanie minigierek");
			for (MiniGra mg : minigry.values())
				mg.wyłącz();
		}
		if (włączonyModół(Budownik.class))
			Budownik.wyłączanie();
	}
	
	private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        econ = rsp.getProvider();
        try {
        	chat = getServer().getServicesManager().getRegistration(Chat.class).getProvider();
        	perms = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        } catch(NullPointerException e) {}
        return econ != null;
    }
	
	public static boolean powiadom(CommandSender p, String msg) {
		return powiadom(p, msg, true);
	}
	public static boolean powiadom(CommandSender p, String msg, boolean zwrot) {
		p.sendMessage(msg);
		return zwrot;
	}
	
	private static final Logger logger = Logger.getLogger("Minecraft");
	private static final String logprefix = "[mimiRPG] ";
	public static void log(Object... msg) {
		logger.info(logprefix + Func.listToString(msg, 0));
	}
	public static void warn(Object... msg) {
		logger.warning(logprefix + Func.listToString(msg, 0));
	}
	public static void error(Object...msg) {
		logger.severe(logprefix + Func.listToString(msg, 0));
	}

	public static void dodajPermisje(String... permisje) {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		for (String permisja : permisje)
			pluginManager.addPermission(new org.bukkit.permissions.Permission((plugin.getName() + '.' + permisja).toLowerCase()));
	}
	
	public static boolean włączonyModół(Class<?> modół) {
		return Moduły.włączony(modół.getSimpleName());
	}
}

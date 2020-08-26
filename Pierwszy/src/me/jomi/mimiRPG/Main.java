package me.jomi.mimiRPG;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.jomi.mimiRPG.Chat.*;
import me.jomi.mimiRPG.CustomowyDrop.CustomowyDrop;
import me.jomi.mimiRPG.MiniGierki.*;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.*;
import me.jomi.mimiRPG.Edytory.EdytorTabliczek;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import me.jomi.mimiRPG.Gracze.Gracze;
import me.jomi.mimiRPG.JednorekiBandyta.JednorekiBandyta;
import me.jomi.mimiRPG.Maszyny.*;

public class Main extends JavaPlugin {
	// Blokada skrzynek na zwierzęta jest umiejscowiona w klasie menu
	// Info o aktywnych i nieaktywnych jest w klasie KolorPisania
	// Blokada zabijania Invulnerable mobów jest w Klasie KolorPisania
	
	// TODO blok przyciągający itemy
	// TODO drabinki 0-255
	
	
    public static Permission perms = null;
    public static Economy econ = null;
    public static Chat chat = null;
	
	public static final HashMap<String, MiniGra> minigry = new HashMap<>();
	
	public static JavaPlugin plugin;
	
	public static boolean ekonomia = false;
	
	public static Config ust;
	public void onLoad() {
		plugin = this;

		ConfigurationSerialization.registerClass(Napis.class);
		
		ust = new Config("ustawienia", "ustawienia");
		moduły = ust.sekcja("Moduły");
	}
	public void onEnable() {
		ekonomia = setupVault();
        if (!ekonomia)
        	logger.severe(
        		"["+getDescription().getName()+"] - Nie wykryto Vault! Wyłączanie niektórych funkcji");
        
		new Baza();
		zarejestruj(new Gracze());
		
		new Mimi();
        new Raport();
        
		if (włączonyModół(Minigry.class)) {
			new Minigry();
			for (MiniGra minigra : minigry.values()) 
				zarejestruj(minigra);
		}
		
		for (Class<?> klasa : Arrays.asList(Antylog.class, AutoWiadomosci.class, ChatGrupowy.class,
CustomoweCraftingi.class, CustomoweItemy.class, CustomowyDrop.class, Czapka.class, Budownik.class, 
EdytorTabliczek.class, EdytujItem.class, Funkcje.class, Głowa.class, ItemLink.class, JednorekiBandyta.class,
KolorPisania.class, KomendyInfo.class, Koniki.class, Kosz.class, Lootbagi.class, LosowyDropGracza.class,
Menu.class, Menurpg.class, Mi.class, Miniony.class, Osiągnięcia.class, Patrzeq.class, PiszJako.class,
Plecak.class, Poziom.class, Przyjaciele.class, RTP.class, Spawnery.class, Targ.class, Ujezdzaj.class,
UstawAttr.class, WeryfikacjaPelnoletnosci.class, WykonajWszystkim.class, Wymienianie.class,
Wyplac.class, ZabezpieczGracza.class, ZamienEq.class)) {
			try {
				if (!(włączonyModół(klasa)))
					continue;
				zarejestruj(klasa.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				log("§cProblem przy tworzeniu:", klasa.getSimpleName());
			}
		}
        
        if (!Przeładowalny.przeładowalne.isEmpty())
			new Przeładuj();
        if (!Zegar.zegary.isEmpty())
        	Zegar.aktywuj();
        
        // Wiadomoś braku dostępu do komendy do komendy
        String pref = Func.prefix("Komenda");
        for (Command cmd : PluginCommandYamlParser.parse(this))
        	cmd.setPermissionMessage(pref + "§cNie masz uprawnień ziomuś");

        Main.dodajPermisje("powiadomienia");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mimirpg:raport");
        
		String msg = "\n§a╓───┐ ┌───┐ ┌───┐\n§a║   │ │   │ │\n§a╟───┘ ├───┘ │  ─┬\n§a║ \\   │     │   │\n§a║  \\  │     └───┘§1 by Michałas";
		Bukkit.getConsoleSender().sendMessage(msg);
	}
	private void zarejestruj(Object obj) {
		if (obj instanceof Listener)
			getServer().getPluginManager().registerEvents((Listener) obj, this);
		if (obj instanceof Zegar)
			Zegar.zarejestruj((Zegar) obj);
		if (obj instanceof Przeładowalny) {
			Przeładowalny p = (Przeładowalny) obj;
			Przeładowalny.przeładowalne.put(obj.getClass().getSimpleName(), p);
			p.przeładuj();
		}
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
	public static void log(Object... msg) {
		String w = Func.listToString(msg, 0, " ");
		logger.info("[" + plugin.getDescription().getName() + "] " + w);
	}

	public static void dodajPermisje(String... permisje) {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		for (String permisja : permisje)
			pluginManager.addPermission(new org.bukkit.permissions.Permission((plugin.getName() + '.' + permisja).toLowerCase()));
	}
	
	private static ConfigurationSection moduły;
	public static boolean włączonyModół(Class<?> modół) {
		if (moduły == null) return false;
		Object jest = moduły.get(modół.getSimpleName());
		return jest == null ? false : (boolean) jest;
	}
}

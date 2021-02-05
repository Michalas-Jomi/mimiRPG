package me.jomi.mimiRPG;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.SelektorItemów;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Baza implements Listener {
	public static final ItemStack pustySlot		  = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&1&l ");
	public static final ItemStack pustySlotCzarny = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "&1&l ");
	public static final Config config = new Config("configi/Baza");
	public static final HashMap<String, ItemStack> itemy = new HashMap<>();
	public static final HashMap<String, SelektorItemów> selektoryItemów = new HashMap<>();
	public static final HashMap<String, Drop> dropy = new HashMap<>();
	public static ConfigurationSection grupy;
	public static ItemStack walutaPremium;
	

	// Api WorldGuard
	public static WorldGuardPlugin rg;
    public static StringFlag flagaCustomoweMoby;
	public static StateFlag flagaStawianieBaz;
	public static StateFlag flagaC4;
	public static StateFlag flagaUżywanieWiadra;
	public static StateFlag flagaRadiacja;
	// Api WorldEdit
	public static WorldEdit we;
	
	
	public Baza() {	
		przeładuj();
	}
	public static void przeładuj() {
		Main.ust.przeładuj();

		config.przeładuj();
		
		walutaPremium = config.wczytajItem("Waluta Premium");
		
		wczytajGrupy();
		wczytajDropy();
		wczytajCustomoweItemy();
		wczytajSelektoryItemów();
		
		itemy.put("WalutaPremium", walutaPremium);
	}

	public static Grupa grupa(String nazwa) {
		Object obj = grupy.get(nazwa);
		if (obj == null)
			return new Grupa();
		return (Grupa) obj;
	}
	
	private static void wczytajSelektoryItemów() {
		selektoryItemów.clear();
		Config config = new Config("configi/Selektory Itemów");
		for (String klucz : config.klucze(false))
			selektoryItemów.put(klucz, config.wczytajSelektorItemów(klucz));
	}
	private static void wczytajCustomoweItemy() {
		itemy.clear();
		Config config = new Config("Customowe Itemy");
		for (String klucz : config.klucze(false))
			itemy.put(klucz, config.wczytajItem(klucz));
	}
	private static void wczytajDropy() {
		dropy.clear();
		Config config = new Config("Dropy");
		for (String klucz : config.klucze(false))
			dropy.put(klucz, config.wczytajDrop(klucz));
	}
	private static void wczytajGrupy() {
		grupy = config.sekcja("grupy");
	}

	
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		Gracz.mapa.remove(ev.getPlayer().getName().toLowerCase());
	}

	static class APIs {
		private static void brakPluginu(String plugin) {
			Main.error("Nie wykryto " + plugin + "! Wyłączanie niektórych funkcji");;
		}
		static void włączWorldGuard() {
			try {
				Baza.rg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
				
				for (Flag<?> flaga : new Flag<?>[]{
					Baza.flagaUżywanieWiadra = new StateFlag("NapelnianieWiadra", true),
					Baza.flagaStawianieBaz = new StateFlag("StawianieBaz", true),
					Baza.flagaRadiacja = new StateFlag("Radiacja", false),
					Baza.flagaC4 = new StateFlag("C4", false),
					Baza.flagaCustomoweMoby = new StringFlag("CustomoweMoby")
				})
					WorldGuard.getInstance().getFlagRegistry().register(flaga);
			} catch (NoClassDefFoundError e) {
				brakPluginu("WorldGuard");
			}
		}
		static void włączWorldEdit() {
			try {
				Baza.we = WorldEdit.getInstance();
			} catch (NoClassDefFoundError e) {
				brakPluginu("WorldEdit");
			}
		}
		static void włączVault() {
	        try {
		        if (Main.plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
			        RegisteredServiceProvider<Economy> rsp = Main.plugin.getServer().getServicesManager().getRegistration(Economy.class);
			        Main.econ  = rsp.getProvider();
			        Main.chat  = Main.plugin.getServer().getServicesManager().getRegistration(Chat.class).getProvider();
			        Main.perms = Main.plugin.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
		        }
		    } catch(NullPointerException e) {}
	        Main.ekonomia = Main.econ != null;
	        if (!Main.ekonomia)
				brakPluginu("Vault");
		}
	}

}

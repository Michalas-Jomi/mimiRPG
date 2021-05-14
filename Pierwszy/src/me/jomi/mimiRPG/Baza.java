package me.jomi.mimiRPG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.worldedit.WorldEdit;

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
	public static final HashMap<String, ItemStack> itemy = new HashMap<>();
	public static final HashMap<String, SelektorItemów> selektoryItemów = new HashMap<>();
	public static final HashMap<String, Drop> dropy = new HashMap<>();
	public static List<String> bezpieczneKomendy = new ArrayList<>(); // TODO wczytywać
	public static ConfigurationSection grupy;
	public static ItemStack walutaPremium;
	public static Config config;
	private static Config configWiadomości;
	
	// TODO: wczytywać
	public static class BudowanieAren {
		public static int maxBloki = 20000;
		public static int tickiPrzerw = 3;
	}
	
	public static boolean bezpieczna(String cmd) {
		cmd = cmd.toLowerCase();
		for (String bezpieczna : bezpieczneKomendy)
			if (cmd.startsWith(bezpieczna.toLowerCase()))
				return true;
		return false;
	}
	

	// Api WorldEdit
	public static WorldEdit we;
	
	
	public Baza() {	
		configWiadomości = new Config("Wiadomości");
		config = new Config("configi/Baza");
		przeładuj();
	}
	public static void przeładuj() {
		Main.ust.przeładuj();

		configWiadomości.przeładuj();
		config.przeładuj();
		
		walutaPremium = config.wczytajItem("Waluta Premium");
		
		wczytajGrupy();
		wczytajDropy();
		wczytajCustomoweItemy();
		wczytajSelektoryItemów();
		
		itemy.put("WalutaPremium", walutaPremium);
	}

	static class Formater {
		final String placeholder;
		final Object obj;
		
		Formater(String placeholder, Object obj) {
			this.placeholder = placeholder;
			this.obj = obj;
		}
		
		String formatuj(String msg) {
			if (!msg.contains(placeholder))
				return msg;
			
			String format = "§e" + msg_format(obj) + "§6";
			msg = msg.replaceAll("<" + placeholder + ">", format);
			msg = msg.replaceAll("<" + placeholder + ".lower>", format.toLowerCase());
			msg = msg.replaceAll("<" + placeholder + ".upper>", format.toUpperCase());
			if (obj instanceof Player) {
				Player p = (Player) obj;
				msg = new Formater(placeholder + ".loc", p.getLocation()).formatuj(msg);
				msg = new Formater(placeholder + ".inv", p.getInventory()).formatuj(msg);
				msg = new Formater(placeholder + ".displayname", p.getDisplayName()).formatuj(msg);
			}
			if (obj instanceof OfflinePlayer) {
				OfflinePlayer p = (OfflinePlayer) obj;
				msg = new Formater(placeholder + ".nick", p.getName()).formatuj(msg);
				msg = new Formater(placeholder + ".displayname", p.getName()).formatuj(msg);
			}
			if (obj instanceof CommandSender) {
				CommandSender sender = (CommandSender) obj;
				msg = new Formater(placeholder + ".nick", sender.getName()).formatuj(msg);
				msg = new Formater(placeholder + ".displayname", sender.getName()).formatuj(msg);
			}
			if (obj instanceof PlayerInventory) {
				PlayerInventory inv = (PlayerInventory) obj;
				msg = new Formater(placeholder + ".hand", inv.getItemInMainHand()).formatuj(msg);
				msg = new Formater(placeholder + ".offhand", inv.getItemInOffHand()).formatuj(msg);
				msg = new Formater(placeholder + ".head", inv.getHelmet()).formatuj(msg);
				msg = new Formater(placeholder + ".chest", inv.getChestplate()).formatuj(msg);
				msg = new Formater(placeholder + ".legs", inv.getLeggings()).formatuj(msg);
				msg = new Formater(placeholder + ".feet", inv.getBoots()).formatuj(msg);
			}
			if (obj instanceof ItemStack) {
				ItemStack item = (ItemStack) obj;
				msg = new Formater(placeholder + ".type", item.getType()).formatuj(msg);
				msg = new Formater(placeholder + ".amount", item.getAmount()).formatuj(msg);
			}
			if (obj instanceof Location) {
				Location loc = (Location) obj;
				msg = new Formater(placeholder + ".x", loc.getX()).formatuj(msg);
				msg = new Formater(placeholder + ".y", loc.getY()).formatuj(msg);
				msg = new Formater(placeholder + ".z", loc.getZ()).formatuj(msg);
				msg = new Formater(placeholder + ".cx", loc.getBlockX()).formatuj(msg);
				msg = new Formater(placeholder + ".cy", loc.getBlockY()).formatuj(msg);
				msg = new Formater(placeholder + ".cz", loc.getBlockZ()).formatuj(msg);
				msg = new Formater(placeholder + ".yaw", loc.getYaw()).formatuj(msg);
				msg = new Formater(placeholder + ".pitch", loc.getPitch()).formatuj(msg);
				msg = new Formater(placeholder + ".world", loc.getWorld()).formatuj(msg);
			}
			if (obj instanceof World) {
				World world = (World) obj;
				msg = new Formater(placeholder + ".name", world.getName()).formatuj(msg);
				msg = new Formater(placeholder + ".difficulty", world.getDifficulty().toString().toLowerCase()).formatuj(msg);
			}
			
			return msg;
		}
	}
	public static String msg(String prefix, String lokalizacja, Object... args) {
		String format = configWiadomości.wczytajPewnyD(lokalizacja);
		
		for (int i=0; i < args.length; i += 2)
			format = new Formater((String) args[i], args[i+1]).formatuj(format);
		
		return prefix + Func.koloruj(format);
	}
	private static String msg_format(Object obj) {
		if (obj == null)
			return "null";
		if (obj instanceof Double)
			return Func.DoubleToString((double) obj);
		if (obj instanceof Float)
			return Func.DoubleToString((float) obj);
		if (obj instanceof Integer)
			return Func.IntToString((int) obj);
		if (obj instanceof Short)
			return Func.IntToString((short) obj);
		if (obj instanceof Long)
			return Func.IntToString((int) (long) obj);
		if (obj instanceof Character)
			return String.valueOf((char) obj);
		if (obj instanceof String)
			return (String) obj;
		if (obj instanceof Player)
			return ((Player) obj).getName();
		if (obj instanceof ItemStack)
			return (((ItemStack) obj).hasItemMeta() && ((ItemStack) obj).getItemMeta().hasDisplayName()) ? ((ItemStack) obj).getItemMeta().getDisplayName() : Func.enumToString(((ItemStack) obj).getType());
		if (obj.getClass().isEnum())
			return Func.enumToString(Func.pewnyCast(obj));
			
		return obj.toString();
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
		for (String klucz : config.klucze())
			selektoryItemów.put(klucz, config.wczytajSelektorItemów(klucz));
	}
	private static void wczytajCustomoweItemy() {
		itemy.clear();
		Config config = new Config("Customowe Itemy");
		for (String klucz : config.klucze())
			itemy.put(klucz, config.wczytajItem(klucz));
	}
	private static void wczytajDropy() {
		dropy.clear();
		Config config = new Config("Dropy");
		for (String klucz : config.klucze())
			dropy.put(klucz, config.wczytajDrop(klucz));
	}
	private static void wczytajGrupy() {
		grupy = config.sekcja("grupy");
	}

	
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		Gracz.mapa.remove(ev.getPlayer().getName().toLowerCase());
	}

	public static class APIs {
		public static void brakPluginu(String plugin) {
			Main.error("Nie wykryto " + plugin + "! Wyłączanie niektórych funkcji");;
		}
		static void włączWorldGuard() {
			me.jomi.mimiRPG.api._WorldGuard.włącz();
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

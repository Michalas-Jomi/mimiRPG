package me.jomi.mimiRPG;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import me.jomi.mimiRPG.Chat.Mimi;
import me.jomi.mimiRPG.Chat.Raport;
import me.jomi.mimiRPG.Minigry.Paintball;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.Budownik;
import me.jomi.mimiRPG.PojedynczeKomendy.CustomoweCraftingi;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;
import me.jomi.mimiRPG.PojedynczeKomendy.Przeładuj;
import me.jomi.mimiRPG.PojedynczeKomendy.ZabezpieczGracza;
import me.jomi.mimiRPG.SkyBlock.AutoEventy;
import me.jomi.mimiRPG.SkyBlock.SkyBlock;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin implements Listener {
	// Api Vaults
	public static boolean ekonomia = false;
	public static Permission perms;
    public static Economy econ;
    public static Chat chat;
	// Api WorldGuard
	public static WorldGuardPlugin rg;
    public static StringFlag flagaCustomoweMoby;
	public static StateFlag flagaStawianieBaz;
	public static StateFlag flagaC4;
	public static StateFlag flagaUżywanieWiadra;
	public static StateFlag flagaRadiacja;
	// Api WorldEdit
	public static WorldEdit we;

	
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
				flagaUżywanieWiadra = new StateFlag("NapelnianieWiadra", true),
				flagaStawianieBaz = new StateFlag("StawianieBaz", true),
				flagaRadiacja = new StateFlag("Radiacja", false),
				flagaC4 = new StateFlag("C4", false),
				flagaCustomoweMoby = new StringFlag("CustomoweMoby")
			})
				WorldGuard.getInstance().getFlagRegistry().register(flaga);
		} catch (NoClassDefFoundError e) {
			brakPluginu("WorldGuard");
		}
	}
	private void włączWorldEdit() {
		try {
			we = WorldEdit.getInstance();
		} catch (NoClassDefFoundError e) {
			brakPluginu("WorldEdit");
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
		włączWorldEdit();
	}
	@Override
	public void onEnable() {
		włączVault();
		
		zarejestruj(this);
		zarejestruj(new Baza());
		new Mimi();
        new Raport();
        
        try {
        	Bukkit.getPluginCommand("statspurge").setPermission("jbwmstats.statspurge");
        	Bukkit.getPluginCommand("statspurge").setPermissionMessage("§cNie mas dostępu do tego");
        } catch (Throwable e) {
        	Main.warn("Nie wykryto komendy Korala /statspurge");
        }
        
		zarejestruj(new Moduły());
		
		new Przeładuj();
        if (!Zegar.zegary.isEmpty())
        	Zegar.aktywuj();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mimirpg:raport");
        
		String msg = "\n§a╓───┐ ┌───┐ ┌───┐\n§a║   │ │   │ │\n§a╟───┘ ├───┘ │  ─┬\n§a║ \\   │     │   │\n§a║  \\  │     └───┘§1 by Michałas";
		Bukkit.getConsoleSender().sendMessage(msg);
		
		pluginEnabled = true;        
	}
	static Matcher mat;
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
		if (włączonyModół(SkyBlock.class))
			SkyBlock.onDisable();
		
		Func.onDisable();
	}


    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
    	ChunkGenerator generator;
    	if (id.equals("skyblock") && (generator = SkyBlock.worldGenerator(worldName)) != null)
			return generator;
    	return super.getDefaultWorldGenerator(worldName, id);
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
			try {
			pluginManager.addPermission(new org.bukkit.permissions.Permission(permisja));
			} catch (IllegalArgumentException e) {}
		}
	}
	
	public static boolean włączonyModół(Class<?> modół) {
		return Moduły.włączony(modół.getSimpleName());
	}

	public static void reloadBukkitData() {
		Bukkit.getServer().reloadData();
		for (Class<?> clazz : new Class<?>[] {CustomoweCraftingi.class})
			if (Main.włączonyModół(clazz))
				Przeładuj.przeładuj(Bukkit.getConsoleSender(), clazz.getSimpleName());
	}

	
	private static class PanelTakNieHolder extends Func.abstractHolder {
		Runnable tak;
		Runnable nie;
		public PanelTakNieHolder(String nazwa, Runnable tak, Runnable nie) {
			super(3, nazwa);
			this.tak = () -> {
				tak.run();
				this.nie = null;
			};
			this.nie = nie;
			Func.ustawPuste(getInventory());
		}
	}
	public static void panelTakNie(Player p, String tytuł, String Tak, String Nie, Runnable tak, Runnable nie) {
		Inventory inv = new PanelTakNieHolder(tytuł, tak, nie).getInventory();
		
		inv.setItem(12, Func.stwórzItem(Material.LIME_STAINED_GLASS_PANE, Tak));
		inv.setItem(14, Func.stwórzItem(Material.RED_STAINED_GLASS_PANE, Nie));
		
		p.openInventory(inv);
		p.addScoreboardTag(tagBlokWyciąganiaZEq);
	}
	@EventHandler
	public void klikanieEqtaknie(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), PanelTakNieHolder.class, holder -> {
			if (ev.getRawSlot() == 12)
				Func.wykonajDlaNieNull(holder.tak, Runnable::run);
			else if (ev.getRawSlot() == 14)
				Func.wykonajDlaNieNull(holder.nie, Runnable::run);
			else
				return;
			ev.getWhoClicked().closeInventory();
		});
	}
	@EventHandler
	public void zamykanieEqtaknie(InventoryCloseEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), PanelTakNieHolder.class, holder -> Func.wykonajDlaNieNull(holder.nie, Runnable::run));
	}
	
	
	public static void chwilowyGodMode(Entity p, int sekundy) {
		if (p.isInvulnerable()) return;
		p.setInvulnerable(true);
		Func.opóznij(20 * sekundy, () -> p.setInvulnerable(false));
	}
	
	@EventHandler
	public void pobieranieWody(PlayerBucketFillEvent ev) {
		if (Main.rg != null && !Func.regiony(ev.getBlock().getWorld()).getApplicableRegions(Func.locToVec3(ev.getBlock().getLocation()))
				.testState(Main.rg.wrapPlayer(ev.getPlayer()), Main.flagaUżywanieWiadra))
			ev.setCancelled(true);
	}
	
	
	// Nie pozwala wymować nic z aktualnie, bądz dopiero będącego otwartym eq, aż do jego zamknięcia
	public static final String tagBlokWyciąganiaZEq = "mimiBlokadaWyciąganiaZEq";
	// Zamknie pierwsze otwarte menu i zniknie
	public static final String tagBlokOtwarciaJednorazowy = "mimiJednorazowyBlokOtwarciaEq";
	@EventHandler(priority = EventPriority.HIGH)
	public void blokWyciągania(InventoryClickEvent ev) {
		if (ev.getWhoClicked().getScoreboardTags().contains(tagBlokWyciąganiaZEq)) {
			int slot = ev.getRawSlot();
			if (slot >= 0 && slot < ev.getInventory().getSize() || ev.getClick().equals(ClickType.DOUBLE_CLICK))
				ev.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void zamykanieEq(InventoryCloseEvent ev) {
		ev.getPlayer().removeScoreboardTag(tagBlokWyciąganiaZEq);
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void otiweranieEq(InventoryOpenEvent ev) {
		if (ev.getPlayer().removeScoreboardTag(tagBlokOtwarciaJednorazowy))
			ev.setCancelled(true);
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

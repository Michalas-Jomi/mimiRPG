package me.jomi.mimiRPG;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Chat.Mimi;
import me.jomi.mimiRPG.Chat.Raport;
import me.jomi.mimiRPG.Maszyny.Budownik;
import me.jomi.mimiRPG.Minigry.Paintball;
import me.jomi.mimiRPG.Miniony.Miniony;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;
import me.jomi.mimiRPG.PojedynczeKomendy.ZabezpieczGracza;
import me.jomi.mimiRPG.SkyBlock.AutoEventy;
import me.jomi.mimiRPG.SkyBlock.SkyBlock;
import me.jomi.mimiRPG.SkyBlock.Multi.MultiSkyBlock;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Przeładuj;
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
	// Api Essentials
	public static Essentials essentials;
	
	public static JavaPlugin plugin;
	public static Config ust;
	public static String path;	
	public static ClassLoader classLoader;
	
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
		
		Baza.APIs.włączWorldGuard();
		Baza.APIs.włączWorldEdit();
	}
	@Override
	public void onEnable() {
		boolean wl = getServer().hasWhitelist();
		getServer().setWhitelist(true);
		
		Baza.APIs.włączVault();
		essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
		
		zarejestruj(this);
		zarejestruj(new Baza());
		zarejestruj(new Mimi());
        zarejestruj(new Raport());
        
		zarejestruj(new Moduły());
		
		zarejestruj(new Przeładuj());
        if (!Zegar.zegary.isEmpty())
        	Zegar.aktywuj();
        
        Main.dodajPermisje(permBlokowanieKomend);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mimirpg:raport");
        
		String msg = "\n§a╓───┐ ┌───┐ ┌───┐\n§a║   │ │   │ │\n§a╟───┘ ├───┘ │  ─┬\n§a║ \\   │     │   │\n§a║  \\  │     └───┘§1 by Michałas";
		Bukkit.getConsoleSender().sendMessage(msg);

		getServer().setWhitelist(wl);
		
		pluginEnabled = true;
		przeładowywanaBukkitData = false;
	}
	
	public static boolean pluginWyłączany = false;
	@Override
	public void onDisable() {
		pluginWyłączany = true;
		
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
    	ChunkGenerator generator = null;
    	switch (id) {
    	case "skyblock":		generator = SkyBlock.worldGenerator(worldName);	break;
    	case "multiskyblock":	generator = MultiSkyBlock.generatorChunków;		break;
    	}
    	return generator != null ? generator : super.getDefaultWorldGenerator(worldName, id);
    }
    
	
	
	static boolean pluginEnabled = false;
	@SuppressWarnings("unchecked")
	public static void zarejestruj(Object obj) {
		if (obj instanceof Listener)
			plugin.getServer().getPluginManager().registerEvents((Listener) obj, plugin);
		if (obj instanceof Zegar)
			Zegar.zarejestruj((Zegar) obj);
		if (obj instanceof Przeładowalny) {
			Przeładowalny p = (Przeładowalny) obj;
			if (obj.getClass().isAnnotationPresent(Przeładowalny.WymagaReloadBukkitData.class))
				doPrzeładowywaniaBukkitData.add(p);
			Przeładowalny.przeładowalne.put(obj.getClass().getSimpleName(), p);
			p.preReloadBukkitData();
			p.przeładuj();
		}
		if (obj instanceof Komenda)
			try {
				((List<PluginCommand>) Func.dajZField(obj, "_komendy")).forEach(cmd -> {
					try {
						((CommandMap) Func.dajField(Komenda.class, "commandMap").get(null)).register(plugin.getName(), cmd);
						if (pluginEnabled)
							Func.dajMetode(Komenda.class, "syncCommands").invoke(null);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}
	@SuppressWarnings("unchecked")
	public static void wyrejestruj(Object obj) {
		if (obj instanceof Listener)
			HandlerList.unregisterAll((Listener) obj);
		if (obj instanceof Zegar)
			Zegar.wyrejestruj((Zegar) obj);
		if (obj instanceof Przeładowalny)
			Przeładowalny.przeładowalne.remove(obj.getClass().getSimpleName());
		if (obj instanceof Komenda)
			try {
				((List<PluginCommand>) Func.dajZField(obj, "_komendy")).forEach(cmd -> {
					try {
						Map<String, Command> mapaKomend = (Map<String, Command>) Func.dajField(Komenda.class, "mapaKomend").get(null);
						Consumer<String> usuń = alias -> {
							Command usuwana = mapaKomend.remove((cmd.getPlugin().getName() + ":" + alias).toLowerCase());
							if (mapaKomend.get(alias) == usuwana)
								mapaKomend.remove(alias);
						};
						
						usuń.accept(cmd.getName());
						cmd.getAliases().forEach(usuń::accept);
						
						cmd.unregister((CommandMap) Func.dajField(Komenda.class, "commandMap").get(null));
						
						Func.dajMetode(Komenda.class, "syncCommands").invoke(null);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
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

	private static boolean przeładowywanaBukkitData = true;
	public static boolean przeładowywanaBukkitData() {
		return przeładowywanaBukkitData;
	}
	static final List<Przeładowalny> doPrzeładowywaniaBukkitData = Lists.newArrayList();
	public static void reloadBukkitData() {
		boolean wl = plugin.getServer().hasWhitelist();
		plugin.getServer().setWhitelist(true);
		
		przeładowywanaBukkitData = true;
		for (Przeładowalny p : Przeładowalny.przeładowalne.values())
			if (Main.włączonyModół(p.getClass()))
				p.preReloadBukkitData();
		Bukkit.getServer().reloadData();
		for (Przeładowalny p : doPrzeładowywaniaBukkitData)
			if (Main.włączonyModół(p.getClass()))
				Przeładuj.przeładuj(Bukkit.getConsoleSender(), p.getClass().getSimpleName());
		przeładowywanaBukkitData = false;
		
		plugin.getServer().setWhitelist(wl);
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

	static final String permBlokowanieKomend = "mimirpg.blokadakomend.bypass";
	public static final String tagBlokowanieKomendy = "mimiBlokowanieKomend";
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		if (ev.getPlayer().getScoreboardTags().contains(tagBlokowanieKomendy) && !Baza.bezpieczna(ev.getMessage())) {
			if (ev.getPlayer().hasPermission(permBlokowanieKomend)) {
				ev.getPlayer().sendMessage(Func.prefix("Blokada") + "Ta komenda dla zwykłych graczy jest zablokowana");
			} else {
				ev.getPlayer().sendMessage(Func.prefix("Blokada") + "Nie możesz teraz używać komend");
				ev.setCancelled(true);
			}
		}
	}
}

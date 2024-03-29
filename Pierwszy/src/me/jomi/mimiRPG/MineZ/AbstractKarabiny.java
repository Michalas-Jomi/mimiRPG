package me.jomi.mimiRPG.MineZ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KolorRGB;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.ParticleZwykłe;
import me.jomi.mimiRPG.util.Przeładowalny;

public abstract class AbstractKarabiny<T extends AbstractKarabiny.Karabin> extends Komenda implements Listener, Przeładowalny {
	public static abstract class Karabin extends Mapowany {
		@Mapowane String nazwa = "Karabin";
		
		@Mapowane Sound dzwiękStrzału = Sound.ENTITY_WITHER_SHOOT;
		@Mapowane double dzwiękPitch = 2;
		@Mapowane double głośność = 1;
		
		@Mapowane int przybliżenie = 1;
		
		@Mapowane ItemStack item;
		@Mapowane ItemStack ammo;

		@Mapowane double attackCooldown; // w sekundach
		@Mapowane double dmg = 2;
		@Mapowane double dmgPrzeszycie = 0;
		@Mapowane double mocWybuchu;
		
		@Mapowane double rozrzucenie = 0.0;
		@Mapowane int pociski = 1;
		@Mapowane int cooldownPocisków = 0; // ticki
		
		@Mapowane int magazynek = 30;
		@Mapowane int czasPrzeładowania = 5; // w sekundach

		@Mapowane KolorRGB kolorOgonaPocisku;
		@Mapowane ParticleZwykłe particleWybuchu;
		
		@Mapowane double zasięgC4 = 0d;
		
		
		@Override
		protected void Init() {
			if (item != null && nazwa != null) {
				ItemMeta meta = item.getItemMeta();
				meta.getPersistentDataContainer().set(keyKarabin, PersistentDataType.STRING, nazwa);
				item.setItemMeta(meta);
			}
		}

		protected abstract void strzel(Player p, Vector wzrok);
		
		public void strzel(Player p, ItemStack karabin) {
			if (!minąłCooldown(karabin)) return;
			
			boolean granat = magazynek <= 0 && item.isSimilar(ammo);
			
			if (granat) {
				karabin.setAmount(karabin.getAmount() - 1);
				p.getInventory().setItemInMainHand(karabin.getAmount() > 0 ? karabin : null);
			} else if (!zabierzPocisk(p, karabin)) {
				Func.sendActionBar(p, "§cBrak amunicji");
				przeładuj(p, karabin);
				return;
			}
			
			strzel(p, p.getLocation().getDirection(), 0);
			ustawCooldown(karabin);
			
			if (!granat && pociskiWMagazynku(karabin) <= 0)
				przeładuj(p, karabin);
			
			p.incrementStatistic(Statistic.USE_ITEM, Material.SNOWBALL);
		}
		private void strzel(Player p, Vector _wzrok, int poziom) {
			Vector wzrok = _wzrok.clone();
			Supplier<Double> los = () -> Func.losuj(-rozrzucenie, rozrzucenie);
			wzrok.add(new Vector(los.get(), los.get(), los.get()));
			
			strzel(p, wzrok);
			
			if (poziom == 0 || cooldownPocisków > 0)
				p.getWorld().playSound(p.getLocation(), dzwiękStrzału, (float) głośność, (float) dzwiękPitch);
			
			if (poziom + 1 < pociski)
				Func.opóznij(cooldownPocisków, () -> strzel(p, wzrok, poziom+1));
		}
		
		public void przybliż(Player p) {
			if (odbliż(p)) return;
			if (przybliżenie == -1) return;
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*60*60*2, przybliżenie, false, false, false));
			Func.ustawMetadate(p, "mimiKarabinPrzybliżenie", true);
		}
		public static boolean odbliż(HumanEntity p) {
			if (p.hasMetadata("mimiKarabinPrzybliżenie")) {
				p.removePotionEffect(PotionEffectType.SLOW);
				p.removeMetadata("mimiKarabinPrzybliżenie", Main.plugin);
				return true;
			}
			return false;
		}
		
		private static final NamespacedKey keyStart = new NamespacedKey(Main.plugin, "karabin_cooldown_start");
		private static final NamespacedKey keyStop  = new NamespacedKey(Main.plugin, "karabin_cooldown_stop");
		public boolean minąłCooldown(ItemStack item) {
			return item.getItemMeta().getPersistentDataContainer().getOrDefault(keyStop, PersistentDataType.LONG, 0L) < System.currentTimeMillis();
		}
		public void ustawCooldown(ItemStack item) {
			if (item == null || !item.hasItemMeta()) return;
			
			ItemMeta meta = item.getItemMeta();
			long teraz = System.currentTimeMillis();
			
			meta.getPersistentDataContainer().set(keyStart, PersistentDataType.LONG, teraz);
			meta.getPersistentDataContainer().set(keyStop,  PersistentDataType.LONG, teraz + (int) (attackCooldown * 1000));
			
			item.setItemMeta(meta);
		}
		
		private static final Pattern patternPocisków = Pattern.compile("(.*) ⁍(\\d+)");
		private boolean zabierzPocisk(Player p, ItemStack karabin) {
			if (ammo == null) return true;
			
			ItemMeta meta = karabin.getItemMeta();
			
			int pociski;
			String pref;
			Matcher matcher;
			if (!meta.hasDisplayName() || !(matcher = patternPocisków.matcher(Func.getDisplayName(meta))).matches()) {
				pociski = zabierzPociski(p);
				pref = meta.hasDisplayName() ? Func.getDisplayName(meta) : nazwa;
			} else {
				pociski = Integer.parseInt(matcher.group(2));
				if (pociski > magazynek) {
					Main.warn(Karabiny.prefix + "%s miał %s amunicji w karabinie %s na max %s", p.getName(), pociski, nazwa, magazynek);
					return false;
				}
				pref = matcher.group(1);
			}
			
			pociski -= 1;
			
			if (pociski < 0)
				return false;
			
			Func.setDisplayName(meta, pref + " ⁍" + pociski);
			karabin.setItemMeta(meta);
			
			return true;
		}
		public static int pociskiWMagazynku(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			
			if (!meta.hasDisplayName())
				return 0;
			
			Matcher matcher = patternPocisków.matcher(Func.getDisplayName(meta));
			if (!matcher.matches())
				return 0;
			
			return Integer.parseInt(matcher.group(2));
		}
		private int zabierzPociski(Player p) {
			int znalezione = 0;
			
			PlayerInventory inv = p.getInventory();
			for (int i=0; i < inv.getSize(); i++) {
				ItemStack item = inv.getItem(i);
				if (Func.porównaj(ammo, item)) {
					znalezione += item.getAmount();
					if (znalezione >= magazynek) {
						item.setAmount(znalezione - magazynek);
						inv.setItem(i, item.getAmount() > 0 ? item : null);
						return magazynek;
					} else
						inv.setItem(i, null);
					p.updateInventory();
				}
			}
			
			return znalezione;
		}

		Set<ItemStack> przeładowywane = new HashSet<>();
		public void przeładuj(Player p, ItemStack item) {
			if (!p.getInventory().containsAtLeast(ammo, 1)) {
				if (ammo != null)
					Func.sendActionBar(p, "§cBrak naboi");
			} else if (przeładowywane.add(item))
				przeładuj(p, item, czasPrzeładowania * 20 + 1);
		}
		private void przeładuj(Player p, ItemStack item, int ticki) {
			if (!p.getInventory().getItemInMainHand().equals(item)) {
				Func.sendActionBar(p, "§cAnulowano Przeładowywanie");
				przeładowywane.remove(item);
				return;
			}
		
			int zielone = (int) (((czasPrzeładowania*20 - ticki) / (czasPrzeładowania*20d)) * 25);
			
			StringBuilder s = new StringBuilder();
			
			int i = -1;
			s.append("§a");	while (++i < zielone) s.append('|');
			s.append("§c");	while (++i < 25)	  s.append('|');
			
			Func.sendActionBar(p, "§6Przaładowywanie " + s);
				
			if (ticki <= 0)
				przeładujTeraz(p, item);
			else
				Func.opóznij(1, () -> przeładuj(p, item, ticki - 1));
		}
		public void przeładujTeraz(Player p, ItemStack karabin) {
			przeładowywane.remove(karabin);
			
			if (ammo == null) return;
			
			ItemMeta meta = karabin.getItemMeta();
			
			String pref;
			Matcher matcher;
			int pociski = zabierzPociski(p);
			if (!meta.hasDisplayName() || !(matcher = patternPocisków.matcher(Func.getDisplayName(meta))).matches())
				pref = meta.hasDisplayName() ? Func.getDisplayName(meta) : nazwa;
			else
				pref = matcher.group(1);
			
			Func.setDisplayName(meta, pref + " ⁍" + pociski);
			karabin.setItemMeta(meta);
			if (pociski <= 0)
				Func.sendActionBar(p, "§cBrak naboi");
		}
	}
	
	protected static final NamespacedKey keyKarabin = new NamespacedKey(Main.plugin, "mimikarabin");
	
	
	protected final Map<String, T> karabiny = new HashMap<>();
	protected final String configPath;
	protected final Config config;
	
	//protected final Set<String> graczeObrywający = new HashSet<>();
	
	private static final List<AbstractKarabiny<?>> klasy = new ArrayList<>();
	public AbstractKarabiny(String komenda, String config, Class<T> clazz) {
		super(komenda);
		klasy.add(this);
		this.configPath = config;
		this.config = new Config(config);
		this.edytor = new EdytorOgólny<>(komenda, Func.pewnyCast(clazz));
	}
	
	

	public T karabin(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return null;

		String key = item.getItemMeta().getPersistentDataContainer().getOrDefault(keyKarabin, PersistentDataType.STRING, null);
		
		return karabiny.get(key);
	}
	
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getAction().equals(Action.PHYSICAL)) return;
		if (ev.getHand() != EquipmentSlot.HAND) return;
		
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		
		Karabin karabin = karabin(item);
		if (karabin == null) return;
		
		switch (ev.getAction()) {
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			karabin.strzel(ev.getPlayer(), item);
			break;
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			karabin.przybliż(ev.getPlayer());
			break;
		default:
			return;
		}
		
		ev.setCancelled(true);
	}
	@EventHandler public void __(PlayerQuitEvent ev)			{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerDeathEvent ev)			{ Karabin.odbliż(ev.getEntity()); }
	@EventHandler public void __(InventoryOpenEvent ev)			{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerItemHeldEvent ev)		{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerDropItemEvent ev)		{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerSwapHandItemsEvent ev)	{ Karabin.odbliż(ev.getPlayer()); }
	
	@EventHandler
	public void anvil(PrepareAnvilEvent ev) {
		if (karabin(ev.getResult()) != null)
			ev.setResult(null);
	}
	
	public static Set<String> getKarabiny() {
		Set<String> karabiny = new HashSet<>();
		klasy.forEach(klasa -> karabiny.addAll(klasa.karabiny.keySet()));
		return karabiny;
	}
	public static ItemStack getAmmunicje(String karabin) {
		for (AbstractKarabiny<?> klasa : klasy) {
			Karabin k = klasa.karabiny.get(karabin);
			if (k != null)
				return k.ammo;
		}
		return null;
	}
	public static ItemStack getKarabin(String karabin) {
		for (AbstractKarabiny<?> klasa : klasy) {
			Karabin k = klasa.karabiny.get(karabin);
			if (k != null)
				return k.item;
		}
		return null;
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		karabiny.clear();
		config.przeładuj();
		for (String klucz : config.klucze())
			try {
				T karabin = (T) config.wczytaj(klucz);
				karabiny.put(karabin.nazwa, karabin);
			} catch (Throwable e) {
				Main.warn("Niepoprawny karabin " + klucz + " w Karabiny.yml");
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane " + this.getClass().getSimpleName(), karabiny.size());
	}

	final EdytorOgólny<Karabin> edytor;
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return edytor.wymuśConfig_onTabComplete(config, sender, label, args);
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		return edytor.wymuśConfig_onCommand(Func.prefix(this.getClass()), configPath, sender, label, args);
	}

}

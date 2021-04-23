package me.jomi.mimiRPG.MineZ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.MineZ.SkinyItemków.Grupa;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KolorRGB;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Karabiny extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix(Karabiny.class);
	
	
	public Karabiny() {
		super("edytujkarabin");
	}

	private static final NamespacedKey keyKarabin = new NamespacedKey(Main.plugin, "mimikarabin");
	
	public static class Karabin extends Mapowany {
		@Mapowane Sound dzwiękStrzału = Sound.ENTITY_WITHER_SHOOT;
		@Mapowane EntityType typPocisku = EntityType.ARROW;
		@Mapowane double dzwiękPitch = 2;
		@Mapowane double głośność = 1;
		@Mapowane String nazwa = "Karabin";
		@Mapowane double attackCooldown; // w sekundach
		@Mapowane double siłaStrzału = 3;
		@Mapowane int przybliżenie = 1;
		@Mapowane double mocWybuchu;
		@Mapowane double dmg = 2;
		@Mapowane ItemStack item;
		@Mapowane ItemStack ammo;
		@Mapowane double rozrzucenie = 0.0;
		@Mapowane int pociski = 1;
		@Mapowane int cooldownPocisków = 0; // ticki
		@Mapowane int magazynek = 30;
		@Mapowane int czasPrzeładowania = 5; // w sekundach
		@Mapowane KolorRGB kolorOgonaPocisku;
		
		
		//@Override
		protected void _Init() { // XXX temp do nowej edycji
			if (item != null && nazwa != null) {
				ItemMeta meta = item.getItemMeta();
				meta.getPersistentDataContainer().set(Karabiny.keyKarabin, PersistentDataType.STRING, nazwa);
				item.setItemMeta(meta);
			}
		}
		
		public void strzel(Player p, ItemStack karabin) {
			if (!minąłCooldown(karabin)) return;
			if (!zabierzPocisk(p, karabin)) {
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cBrak amunicji"));
				przeładuj(p, karabin);
				return;
			}
			
			strzel(p, p.getLocation().getDirection(), 0);
			ustawCooldown(karabin);
			
			if (pociskiWMagazynku(karabin) <= 0)
				przeładuj(p, karabin);
			
			p.incrementStatistic(Statistic.USE_ITEM, Material.SNOWBALL);
		}
		private void strzel(Player p, Vector _wzrok, int poziom) {
			Vector wzrok = _wzrok.clone();
			Supplier<Double> los = () -> Func.losuj(-rozrzucenie, rozrzucenie);
			wzrok.add(new Vector(los.get(), los.get(), los.get()));
			
			Projectile pocisk = (Projectile) p.getWorld().spawnEntity(p.getEyeLocation(), typPocisku);
			Func.ustawMetadate(pocisk, "mimiPocisk", nazwa);
			pocisk.setVelocity(wzrok.multiply(siłaStrzału));
			pocisk.setInvulnerable(true);
			pocisk.setShooter(p);
			if (kolorOgonaPocisku != null)
				Func.opóznij(2, () -> tickPocisku(pocisk));
			
			if (poziom == 0 || cooldownPocisków > 0)
				p.getWorld().playSound(p.getLocation(), dzwiękStrzału, (float) głośność, (float) dzwiękPitch);
			
			if (poziom + 1 < pociski)
				Func.opóznij(cooldownPocisków, () -> strzel(p, wzrok, poziom+1));
		}
		private void tickPocisku(Projectile pocisk) {
			if (pocisk.isDead()) return;
			Func.particle(pocisk.getLocation(), 1, 0, 0, 0, 0, kolorOgonaPocisku.kolor(), 1f);
			Func.opóznij(1, () -> tickPocisku(pocisk));
		}
		
		private static final NamespacedKey keyStart = new NamespacedKey(Main.plugin, "karabin_cooldown_start");
		private static final NamespacedKey keyStop  = new NamespacedKey(Main.plugin, "karabin_cooldown_stop");
		private boolean minąłCooldown(ItemStack item) {
			return item.getItemMeta().getPersistentDataContainer().getOrDefault(keyStop, PersistentDataType.LONG, 0L) < System.currentTimeMillis();
		}
		private void ustawCooldown(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			long teraz = System.currentTimeMillis();
			
			meta.getPersistentDataContainer().set(keyStart, PersistentDataType.LONG, teraz);
			meta.getPersistentDataContainer().set(keyStop,  PersistentDataType.LONG, teraz + (int) (attackCooldown * 1000));
			
			item.setItemMeta(meta);
		}
		private boolean zabierzPocisk(Player p, ItemStack karabin) {
			if (ammo == null) return true;
			
			ItemMeta meta = karabin.getItemMeta();
			
			int pociski;
			String pref;
			Matcher matcher;
			if (!meta.hasDisplayName() || !(matcher = patternPocisków.matcher(meta.getDisplayName())).matches()) {
				pociski = zabierzPociski(p);
				pref = meta.hasDisplayName() ? meta.getDisplayName() : nazwa;
			} else {
				pociski = Integer.parseInt(matcher.group(2));
				pref = matcher.group(1);
			}
			
			pociski -= 1;
			
			if (pociski < 0)
				return false;
			
			meta.setDisplayName(pref + " ⁍" + pociski);
			karabin.setItemMeta(meta);
			
			return true;
		}
		public static int pociskiWMagazynku(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			
			if (!meta.hasDisplayName())
				return 0;
			
			Matcher matcher = patternPocisków.matcher(meta.getDisplayName());
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

		private static final Pattern patternPocisków = Pattern.compile("(.*) ⁍(\\d+)");
		Set<ItemStack> przeładowywane = new HashSet<>();
		public void przeładuj(Player p, ItemStack item) {
			if (!p.getInventory().containsAtLeast(ammo, 1))
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cBrak naboi"));
			else if (przeładowywane.add(item))
				przeładuj(p, item, czasPrzeładowania * 20 + 1);
		}
		private void przeładuj(Player p, ItemStack item, int ticki) {
			if (!p.getInventory().getItemInMainHand().equals(item)) {
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cAnulowano Przeładowywanie"));
				przeładowywane.remove(item);
				return;
			}
		
			int zielone = (int) (((czasPrzeładowania*20 - ticki) / (czasPrzeładowania*20d)) * 25);
			
			StringBuilder s = new StringBuilder();
			
			int i = -1;
			s.append("§a");	while (++i < zielone) s.append('|');
			s.append("§c");	while (++i < 25)	  s.append('|');
			
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Przaładowywanie " + s));
				
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
			if (!meta.hasDisplayName() || !(matcher = patternPocisków.matcher(meta.getDisplayName())).matches())
				pref = meta.hasDisplayName() ? meta.getDisplayName() : nazwa;
			else
				pref = matcher.group(1);
			
			meta.setDisplayName(pref + " ⁍" + pociski);
			karabin.setItemMeta(meta);
			if (pociski <= 0)
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cBrak naboi"));
		}
		
		public void przybliż(Player p) {
			if (odbliż(p)) return;
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
	}

	static final HashMap<String, Karabin> karabiny = new HashMap<>();
	static final Config config = new Config("Karabiny");
	
	@EventHandler(priority = EventPriority.HIGH)
	public void usuwanieStzał(ProjectileHitEvent ev) {
		Projectile pocisk = ev.getEntity();
		if (!pocisk.hasMetadata("mimiPocisk")) return;
		
		Karabin karabin = karabiny.get(pocisk.getMetadata("mimiPocisk").get(0).asString());	
		if (karabin.mocWybuchu > 0)
			pocisk.getWorld().createExplosion(pocisk.getLocation(), (float) karabin.mocWybuchu, false, false, (Player) pocisk.getShooter());
		
		Location loc = pocisk.getLocation();
		pocisk.remove();
		loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0, 0, 0, .1);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void postrzelenie(EntityDamageByEntityEvent ev) {
		Entity damager = ev.getDamager();
		if (!(damager instanceof Projectile)) return;
		
		Projectile pocisk = (Projectile) damager;
		if (!pocisk.hasMetadata("mimiPocisk")) return;
		
		Karabin karabin = karabiny.get(pocisk.getMetadata("mimiPocisk").get(0).asString());
		ev.setDamage(karabin.dmg);
	}
	

	static final HashMap<String, Karabin> stareKarabiny = new HashMap<>();// XXX temp do nowej edy
	static final Config stareKarabinyConfig = new Config("Karabiny stare");// XXX temp do nowej edy
	private Karabin karabin(ItemStack item, PlayerInteractEvent ev) {
		if (item == null || !item.hasItemMeta()) return null;

		String key = item.getItemMeta().getPersistentDataContainer().getOrDefault(keyKarabin, PersistentDataType.STRING, null);
		if (key == null) { // temp return null;
			// XXX temp do nowej edy //////
			if (Main.włączonyModół(SkinyItemków.class)) {
				Grupa grp = SkinyItemków.wczytaj(item);
				if (grp != null)
					item = SkinyItemków.przetwórz(item.clone(), grp.podstawowy);
			}
			for (Karabin karabin : stareKarabiny.values())
				if (Func.porównaj(karabin.item, item)) {
					ev.getPlayer().getInventory().setItemInMainHand(karabiny.get(karabin.nazwa).item);
					ev.getPlayer().sendMessage(prefix + "Twój karabin " + karabin.nazwa + " został wymieniony na nowszą wersję, baw sie dobrze");
					Main.log(prefix + ev.getPlayer().getName() + " otrzymał nowszą wersję karabinu " + karabin.nazwa);
					return null;
				}
			// XXX temp do nowej edy //////
			return null;
		}
		
		return karabiny.get(key);
	}
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getAction().equals(Action.PHYSICAL)) return;
		
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand(); // XXX temp do nowej edyt / ev.getItem();
		
		Karabin karabin = karabin(item, ev);
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
			break;
		}
	}
	@EventHandler public void __(PlayerQuitEvent ev)			{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerDeathEvent ev)			{ Karabin.odbliż(ev.getEntity()); }
	@EventHandler public void __(InventoryOpenEvent ev)			{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerItemHeldEvent ev)		{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerDropItemEvent ev)		{ Karabin.odbliż(ev.getPlayer()); }
	@EventHandler public void __(PlayerSwapHandItemsEvent ev)	{ Karabin.odbliż(ev.getPlayer()); }
	
	@Override
	public void przeładuj() {
		karabiny.clear();
		config.przeładuj();
		for (String klucz : config.klucze())
			try {
				Karabin karabin = (Karabin) config.wczytaj(klucz);
				karabin._Init();
				karabiny.put(karabin.nazwa, karabin);
			} catch (Throwable e) {
				Main.warn("Niepoprawny karabin " + klucz + " w Karabiny.yml");
			}
		

		stareKarabiny.clear();
		stareKarabinyConfig.przeładuj();
		for (String klucz : stareKarabinyConfig.klucze())
			try {
				Karabin karabin = (Karabin) stareKarabinyConfig.wczytaj(klucz);
				stareKarabiny.put(karabin.nazwa, karabin);
			} catch (Throwable e) {
				Main.warn("Niepoprawny karabin " + klucz + " w Karabiny stare.yml");
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane karabiny", karabiny.size());
	}
	
	public static Set<String> getKarabiny() {
		return karabiny.keySet();
	}
	public static ItemStack getAmmunicje(String karabin) {
		Karabin k = karabiny.get(karabin);
		return k == null ? null : k.ammo;
	}
	public static ItemStack getKarabin(String karabin) throws NullPointerException {
		Karabin k = karabiny.get(karabin);
		return k == null ? null : k.item;
	}


	EdytorOgólny<Karabin> edytor = new EdytorOgólny<>("edytujkarabin", Karabin.class);
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return edytor.wymuśConfig_onTabComplete(config, sender, label, args);
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		return edytor.wymuśConfig_onCommand(prefix, "Karabiny", sender, label, args);
	}
}



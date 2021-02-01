package me.jomi.mimiRPG.MineZ;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.MineZ.SkinyItemków.Grupa;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Karabiny implements Listener, Przeładowalny {
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
		
		void strzel(Player p) {
			if (!minąłCooldown(p)) return;
			if (!zabierzPocisk(p)) {
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cBrak amunicji"));
				return;
			}
			Vector wzrok = p.getLocation().getDirection();
			
			strzel(p, wzrok, 0);
			
			p.incrementStatistic(Statistic.USE_ITEM, Material.SNOWBALL);
			
			if (attackCooldown > 0) 
				Func.ustawMetadate(p, "mimiKarabinCoolown" + nazwa, System.currentTimeMillis() + (attackCooldown * 1000));

			if (attackCooldown > 0)
				tick(p, 0);
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
			
			if (poziom == 0 || cooldownPocisków > 0)
				p.getWorld().playSound(p.getLocation(), dzwiękStrzału, (float) głośność, (float) dzwiękPitch);
			
			if (poziom + 1 < pociski)
				Func.opóznij(cooldownPocisków, () -> strzel(p, wzrok, poziom+1));
		}
		private void tick(Player p, int ticki) {
			if (ticki > attackCooldown*20) return;

			Func.opóznij(1, () -> tick(p, ticki + 1));
			
			if (!Func.porównaj(p.getInventory().getItemInMainHand(), item)) return;
			
			int zielone = (int) ((ticki / (attackCooldown*20)) * 100) / 4;
			
			StringBuilder s = new StringBuilder();
			
			int i = -1;
			while (++i < zielone) s.append("§a|");
			while (++i < 25)	  s.append("§c|");
			
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Przaładowywanie " + s));
			
		}
		private boolean minąłCooldown(Player p) {
			if (attackCooldown <= 0) return true;
			final String meta = "mimiKarabinCoolown" + nazwa;
			long następny = p.hasMetadata(meta) ? p.getMetadata(meta).get(0).asLong() : 0L;
			return następny <= System.currentTimeMillis();
		}
		private boolean zabierzPocisk(Player p) {
			if (ammo == null) return true;
			PlayerInventory inv = p.getInventory();
			for (int i=0; i<inv.getSize(); i++) {
				ItemStack item = inv.getItem(i);
				if (Func.porównaj(ammo, item)) {
					int ile = item.getAmount() - 1;
					item.setAmount(ile);
					inv.setItem(i, ile > 0 ? item : null);
					p.updateInventory();
					return true;
				}
			}
			return false;
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
	
	
	private Karabin karabin(ItemStack item) {
		if (Main.włączonyModół(SkinyItemków.class)) {
			Grupa grp = SkinyItemków.wczytaj(item);
			if (grp != null)
				item = SkinyItemków.przetwórz(item.clone(), grp.podstawowy);
		}
		for (Karabin karabin : karabiny.values())
			if (Func.porównaj(karabin.item, item))
				return karabin;
		return null;
	}
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getAction().equals(Action.PHYSICAL)) return;
		
		Karabin karabin = karabin(ev.getItem());
		if (karabin == null) return;
		
		switch (ev.getAction()) {
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			karabin.strzel(ev.getPlayer());
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
		for (String klucz : config.klucze(false))
			try {
				Karabin karabin = (Karabin) config.wczytaj(klucz);
				karabiny.put(karabin.nazwa, karabin);
			} catch (Throwable e) {
				Main.warn("Niepoprawny karabin " + klucz + " w Karabiny.yml");
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

}



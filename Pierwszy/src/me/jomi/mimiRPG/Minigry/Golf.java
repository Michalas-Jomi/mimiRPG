package me.jomi.mimiRPG.Minigry;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

@Moduł
public class Golf extends Minigra {
	static ItemStack[] kijki;
	static final ItemStack itemReset = Func.stwórzItem(Material.EGG, "&4Reset Piłki");
	static final ItemStack itemTp = Func.stwórzItem(Material.ENDER_PEARL, "&dZnajdz Piłke");
	public static final String prefix = Func.prefix("Golf");
	public static class Arena extends Minigra.Arena {
		Golf inst;
		@Mapowane Location locStart;
		
		
		final List<String> kolejka = Lists.newArrayList();
		
		boolean byłPierwszy = false;
		
		
		// start / koniec
		@Override
		void start() {
			super.start();
			kolejka.clear();
			byłPierwszy = false;
			for (Player p : gracze) {
					p.teleport(locStart);

					p.getInventory().setItem(8, itemReset);
					p.getInventory().setItem(7, itemTp);
					
					for (ItemStack kijek : kijki)
						p.getInventory().addItem(kijek);

					if (Main.ust.wczytajBoolean("Minigry.Golf.fly")) {
						p.setAllowFlight(true);
						p.sendMessage(prefix + "Możeszs latać");
					}
					
					kolejka.add(p.getName());
			}
			
			sprawdzKure();
		}
		
		@Override
		void opuść(Player p, int i, boolean info) {
			super.opuść(p, i, info);

			boolean msg = kolejka.get(0).equals(p.getName());
			
			kolejka.remove(p.getName());
			
			if (msg && kolejka.size() > 0)
				infoKolejka(Func.msg("Kolej gracza %s", Bukkit.getPlayer(kolejka.get(0)).getDisplayName()));
			
			zapomnijKure(p);

			if (Main.ust.wczytajBoolean("Minigry.Golf.fly")) {
				p.setAllowFlight(false);
				p.sendMessage(prefix + "Nie możesz już latać");
			}
		}
		
		
		// rdzeń
		
		void infoKolejka(String msg) {
			napiszGraczom(msg);
			Player p = Bukkit.getPlayer(kolejka.get(0));
			p.sendTitle(Func.koloruj("&bTwój Ruch"), "", 10, 20, 10);
			sprawdzKure();
			Krotka<Integer, Runnable> k = new Krotka<>(Main.ust.wczytajLubDomyślna("Minigry.Golf.czas na uderzenie", 300)+1, null);
			k.b = () -> {
				if (!kolejka.isEmpty() && kolejka.get(0).equals(p.getName())) {
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Func.koloruj("&6" + Func.czas(--k.a))));
					if (k.a <= 0) {
						kolejka.add(kolejka.remove(0));
						infoKolejka(Func.msg("%s stracił swoją szanse, kolej na ", p.getDisplayName(), Bukkit.getPlayer(kolejka.get(0)).getDisplayName()));
					} else
						opóznijTask(20, k.b);
				}
			};
			k.b.run();
		}
		
		void sprawdzKure() {
			Player p = Bukkit.getPlayer(kolejka.get(0));
			if (!p.hasMetadata(metaKury))
				zrespKure(p);
		}
		
		void uderzenie() {
			inst.staty(Bukkit.getPlayer(kolejka.get(0))).uderzenia++;
			kolejka.add(kolejka.remove(0));
			infoKolejka(Func.msg("%s uderzył kolej gracza %s", Bukkit.getPlayer(kolejka.get(kolejka.size() - 1)).getDisplayName(), Bukkit.getPlayer(kolejka.get(0)).getDisplayName()));
		}
		
		void dołek(Player p) {
			if (!byłPierwszy) {
				byłPierwszy = true;
				inst.staty(p).wygraneAreny++;
				for (Player p2 : gracze)
					if (!p2.getName().equals(p.getName()))
						inst.staty(p2).przegraneAreny++;
			}
			Bukkit.broadcastMessage(prefix + Func.msg("%s zalicza Dołek na arenie %s!", p.getDisplayName(), nazwa));
			inst.staty(p).dołki++;
			opuść(p);
		}
		
		
		// util
		
		void zapomnijKure(Player p) {
			if (p.hasMetadata(metaKury)) 
				((Chicken) p.getMetadata(metaKury).get(0).value()).remove();
			p.removeMetadata(metaKury, Main.plugin);
		}
		
		void zrespKure(Player p) {
			Chicken kura = (Chicken) p.getWorld().spawnEntity(locStart, EntityType.CHICKEN);
			kura.setAdult();
			kura.setSilent(true);
			kura.setCollidable(false);
			kura.setCustomName(Func.koloruj("&6Piłka gracza &a" + p.getDisplayName()));
			Func.ustawMetadate(kura, metaKury, new Krotka<>(p, this));
			Func.ustawMetadate(p, metaKury, kura);
			kura.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
		}
		
		
		// Override
		
		@Override
		boolean sprawdzKoniec() {
			if (gracze.size() == 0) {
				koniec();
				return true;
			}
			return false;
		}
		
		@Override Minigra getInstMinigra() 							 { return inst; }
		@Override int policzGotowych() 								 { return gracze.size(); }
		@Override <M extends Minigra> void setInst(M inst) 			 { this.inst = (Golf) inst; }
		@Override Supplier<? extends Minigra.Statystyki> noweStaty() { return Statystyki::new; }
	}
	public static class Statystyki extends Minigra.Statystyki {
		@Mapowane int uderzenia;
		@Mapowane int dołki;
		
		@Override
		void sprawdzTopke(Player p, Minigra minigra) {}
		
		@Override
		void rozpiska(Consumer<String> cons, boolean usuwaćKolor, Minigra minigra) {
			super.rozpiska(cons, usuwaćKolor, minigra);

			cons.accept(_rozpiska("Uderzenia", uderzenia));
			cons.accept(_rozpiska("Dołki", dołki));
		}
	}
	
	// EventHandler
	
	@EventHandler
	public void dmgKury(EntityDamageEvent ev) {
		if (ev.getEntity().hasMetadata(metaKury))
			ev.setCancelled(true);
	}
	@EventHandler
	@SuppressWarnings("unchecked")
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (ev.getEntity().hasMetadata(metaKury) && ev.getEntityType().equals(EntityType.CHICKEN)) {
			ev.setCancelled(true);
			Krotka<Player, Arena> krotka = (Krotka<Player, Arena>) ev.getEntity().getMetadata(metaKury).get(0).value();
			if (krotka.a.getName().equals(ev.getDamager().getName())) {
				if (krotka.b.kolejka.get(0).equals(ev.getDamager().getName())) {
					ev.setCancelled(false);
					ev.setDamage(0);
					krotka.b.uderzenie();
				} else
					ev.getDamager().sendMessage(prefix + "To nie twoja kolej");
			} else
				ev.getDamager().sendMessage(prefix + "To nie twoja piłka stary");
		}
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGHEST)
	public void śmierć(PlayerDeathEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getEntity()), a -> ev.setKeepInventory(true));
	}
	@EventHandler
	public void respawn(PlayerRespawnEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getPlayer()), a -> ev.setRespawnLocation(a.locStart));
	}
	
	@EventHandler
	@SuppressWarnings("unchecked")
	public void dołek(EntityCombustByBlockEvent ev) {
		if (ev.getEntity().hasMetadata(metaKury) && ev.getEntityType().equals(EntityType.CHICKEN) && ev.getCombuster().getType().equals(Material.SOUL_FIRE)) {
			Krotka<Player, Arena> krotka = (Krotka<Player, Arena>) ev.getEntity().getMetadata(metaKury).get(0).value();
			krotka.b.dołek(krotka.a);
		}
	}
	
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getItem() != null && Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK))
			Func.wykonajDlaNieNull(arena(ev.getPlayer()), arena -> {
				Consumer<Chicken> cons = null;
				switch (ev.getItem().getType()) {
				case EGG:
					cons = kura -> {
						if (arena.kolejka.get(0).equals(ev.getPlayer().getName())) {
							arena.zapomnijKure(ev.getPlayer());
							arena.kolejka.add(arena.kolejka.remove(0));
							arena.infoKolejka(Func.msg("%s Zresetował swoją piłkę, kolej gracza %s", ev.getPlayer().getDisplayName(), Bukkit.getPlayer(arena.kolejka.get(0)).getDisplayName()));
						} else
							ev.getPlayer().sendMessage(prefix + "Piłke możesz zresetować tylko gdy jest twoja kolej");
					};
					break;
				case ENDER_PEARL:
					cons = ev.getPlayer()::teleport;
					break;
				default:
					return;
				}
				ev.setCancelled(true);
				if (ev.getPlayer().hasMetadata(metaKury))
					cons.accept((Chicken) ev.getPlayer().getMetadata(metaKury).get(0).value());
				else
					ev.getPlayer().sendMessage(prefix + "Twoja piłka aktualnie nie istnieje");
			});
	}
	
	// Override
	
	@Override
	public void przeładuj() {
		super.przeładuj();
		
		Function<Integer, ItemStack> kijek = lvl ->
				Func.enchantuj(Func.stwórzItem(Material.STICK, "&2&lKijek Golfowy &5&l" + lvl), Enchantment.KNOCKBACK, lvl);
		
		List<Integer> lvle = Main.ust.wczytajLubDomyślna("Minigry.Golf.kijki", Arrays.asList(1, 2, 3, 5, 10));
		
		kijki = new ItemStack[lvle.size()];
		
		int i=0;
		for (Integer lvl : lvle)
			kijki[i++] = kijek.apply(lvl);
	}
	
	static final String metaKury = "mimiMinigraGolfKura";
	static final String metaStaty = "mimiMinigraGolfStatystyki";
	static final String meta = "mimiMinigraGolf";
	@Override String getMetaId()		 { return meta; }
	@Override String getPrefix()		 { return prefix; }
	@Override String getMetaStatystyki() { return metaStaty; }

	@Override @SuppressWarnings("unchecked") Statystyki staty	(Entity p) { return super.staty(p); }
	@Override @SuppressWarnings("unchecked") Arena 		arena	(Entity p) { return super.arena(p); }
}

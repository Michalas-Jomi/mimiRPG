package me.jomi.mimiRPG.Minigry;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

@Moduł
public class Chowany extends Minigra {
	public static class Arena extends Minigra.Arena {
		public static class Box extends Mapowany {
			@Mapowane Material typ;
		}
		@Mapowane List<Box> bloki;
		@Mapowane Location start;
		
		@Mapowane Location poczekalniaSzukających;
		@Mapowane int czasPoczekalniSzukającego = 30;
		
		@Mapowane int minutyGry = 15;
		int timerGry;
		
		int szukający;
		
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Team teamChowający = sb.registerNewTeam("Chowający");
		Team teamSzukający = sb.registerNewTeam("Szukający");		
		void Init() {
			teamChowający.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
			teamChowający.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.NEVER);
			teamChowający.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
			teamChowający.setCanSeeFriendlyInvisibles(false);
			
			teamSzukający.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
			teamSzukający.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.NEVER);
			teamSzukający.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
			teamSzukający.setCanSeeFriendlyInvisibles(true);
		}
		
		// TODO Override poprawna
		
		// obsługa start / koniec
		@Override
		void start() {
			super.start();
			for (Player p : gracze) {
				p.setScoreboard(sb);
				chowający(p);
				
				Status status = new Status();
				status.typ = Func.losuj(bloki).typ;
				Func.ustawMetadate(p, metaStatusId, status);
			}
			szukający = 0;
			szukający(Func.losuj(gracze));
			timerGry = minutyGry * 60;
		}
		@Override
		Player opuść(int i, boolean info) {
			Player p = super.opuść(i, info);
			
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			teamSzukający.removeEntry(p.getName());
			anulujChowającego(p);
			
			Func.wykonajDlaNieNull(status(p), status -> {
				if (status.typ == null)
					szukający--;
			});
			
			p.removeMetadata(metaStatusId, Main.plugin);
			
			if (grane && info)
				sprawdzKoniecNowe();
			
			return p;
		}
		@Override
		void koniec() {
			super.koniec();
			for (int task : taskiDoAnulowania)
				Bukkit.getScheduler().cancelTask(task);
		}
		void minąłCzas() {
			List<String> wygrani = Lists.newArrayList();
			List<String> przegrani = Lists.newArrayList();
			for (Player p : gracze) {
				Statystyki staty = inst.staty(p);
				if (status(p).typ != null) {
					// chowający
					staty.wygraneAreny++;
					wygrani.add(p.getDisplayName());
					wszyscyGracze.remove(p.getName());
				} else {
					// szukający
					wszyscyGracze.remove(p.getName());
					przegrani.add(p.getDisplayName());
				}
			}
			for (String nick : wszyscyGracze)
				przegrani.add(nick);
			Bukkit.broadcastMessage(getInstMinigra().getPrefix() + Func.msg("Zwycięstwo %s na arenie %s z %s",
					Func.listToString(wygrani, 0, "§6, §e"), nazwa, Func.listToString(przegrani, 0, "§6, §e")));
			koniec();
		}

		
		final Set<Integer> taskiDoAnulowania = Sets.newConcurrentHashSet();
		void szukający(Player p) {
			anulujChowającego(p);
			teamSzukający.addEntry(p.getName());
			
			p.teleport(poczekalniaSzukających);
			Krotka<Integer, ?> box = new Krotka<>(null, null);
			box.a = Func.opóznij(czasPoczekalniSzukającego, () -> {
				p.teleport(start);
				taskiDoAnulowania.remove(box.a);
			});
			taskiDoAnulowania.add(box.a);

			szukający++;
			
			p.getInventory().addItem(Func.stwórzItem(Material.IRON_SWORD, "Sztylet Szukającego", "&bZnajdz wszystkich i wygraj"));
		
			sprawdzKoniecNowe();
		}
		void chowający(Player p) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 60 * 60 * 5, 0, false, false, false));
			teamChowający.addEntry(p.getName());
		}
		void anulujChowającego(Player p) {
			anulujChowającego(p, status(p));
		}
		void anulujChowającego(Player p, Status status) {
			teamChowający.removeEntry(p.getName());	
			p.removePotionEffect(PotionEffectType.INVISIBILITY);
			Func.wykonajDlaNieNull(status.blok, FallingBlock::remove);
			Func.wykonajDlaNieNull(status.loc, loc -> loc.getBlock().setType(Material.AIR));
			status.blok = null;
			status.typ = null;
			status.loc = null;
		}
		
		
		
		static final char[] etapyMaskowaniaZnaczki = new char[] {'░', '▒', '▓', '█'};
		void sekunda() {
			if (--timerGry <= 0) {
				minąłCzas();
				return;
			}
			BaseComponent[] czas = TextComponent.fromLegacyText("§aDo końca zostało §b" + Func.czas(timerGry));
			for (Player p : gracze) {
				Status status = status(p);
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, czas);
				if (status.typ == null) continue;
				if (status.blok != null)
					status.blok.setTicksLived(1);
				if (status.loc == null && ++status.nieruchomesekundy >= 5)
					zamaskuj(p, status);
				else if (status.nieruchomesekundy >= 1) {
					String s = "§6Nie poruszaj się aby się zamaskować §a" + etapyMaskowaniaZnaczki[status.nieruchomesekundy - 1];
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(s));
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .8f, ((float) status.nieruchomesekundy - 1) / 3f + 1);
				}
			}
		}
		
		void ruch(Player p, Status status) {
			if (status == null) return;
			if (status.typ == null) return;
			
			status.nieruchomesekundy = -1;
			
			if (status.loc != null) { // jeśli jest zamaskowany to go zdemaskuje
				status.loc.getBlock().setType(Material.AIR);
				status.loc = null;
				p.setGameMode(GameMode.ADVENTURE);
				p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, .8f, 1f);
				p.sendMessage(prefix + "Już §cnie §6jesteś §cZamaskowany");
			}
		
			Func.wykonajDlaNieNull(status.blok, Entity::remove);
			przywołajBlok(p, status);
			
		}
		
		void zamaskuj(Player p, Status status) {
			status.loc = p.getLocation().getBlock().getLocation();
			if (!status.loc.getBlock().getType().isAir()) {
				p.sendMessage(prefix + "§cW tym miejscu nie możesz się zamaskoawć!");
				status.nieruchomesekundy = -1;
				status.loc = null;
				return;
			}
			
			p.setGameMode(GameMode.SPECTATOR);
			
			p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, .8f, 1f);
			
			Block blok = status.loc.getBlock();
			blok.setType(status.typ);
			// XXX
			//Main.log(blok.getBoundingBox().union(p.getBoundingBox())); zrzuca w dół
			blok.getBoundingBox().expand(0);
			
			Func.wykonajDlaNieNull(status.blok, FallingBlock::remove);
			status.blok = null;
			
			p.sendMessage(prefix + "Teraz jesteś §aZamaskowany");
		}
		// TODO broadcast że arena wystartowała
		// TODO możliwość zmiany state bloku
		void przywołajBlok(Player p, Status status) {
			FallingBlock blok = p.getWorld().spawnFallingBlock(p.getLocation(), Bukkit.createBlockData(status.typ));
			blok.setGravity(false);
			blok.setDropItem(false);
			blok.setHurtEntities(false);
			blok.setVelocity(p.getVelocity()); // XXX 
			status.blok = blok;
		}
		
		void znalazł(Player kto, Player kogo) {
			inst.staty(kto).znalezieni++;
			inst.staty(kogo).przegraneAreny++;
			
			napiszGraczom(Func.msg("%s znalazł %s", kto.getDisplayName(), kogo.getDisplayName()));
			
			szukający(kogo);
		}
		
		
		// util

		void sprawdzKoniecNowe() {
			if (szukający <= 0 || gracze.size() - szukający <= 0)
				koniec();
		}
		
		boolean uderzył(Player kto, Player kogo) {
			Status status1 = status(kto);
			Status status2 = status(kogo);
			
			if ((status1.typ != null) == (status2.typ != null)) return true;
			
			if (status1.typ != null) return false;
			
			znalazł(kto, kogo);
			
			return false;
		}
		
		
		// Override
		
		Chowany inst;
		@Override Minigra getInstMinigra() { return inst; }
		@Override <M extends Minigra> void setInst(M inst) { this.inst = (Chowany) inst; }
		@Override Supplier<? extends Minigra.Statystyki> noweStaty() { return Statystyki::new; }
		@Override int policzGotowych() { return gracze.size(); }
		
		@Override boolean sprawdzKoniec() { return false; }
	}
	public static class Statystyki extends Minigra.Statystyki {
		@Mapowane int znalezieni;
		
		@Override void sprawdzTopke(Player p) {}
	}
	static class Status {
		Material typ; // null oznacza szukającego
		
		FallingBlock blok; // null dla zamaskowane
		Location loc;	  // null dla nie zamaskowane
		
		int nieruchomesekundy = -1;
	}
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void ruch(PlayerMoveEvent ev) {
		Arena arena = arena(ev.getPlayer());
		if (arena == null) return;
		if (!arena.grane) return;
		
		Status status = status(ev.getPlayer());
		if (status.typ == null) return;
		
		Location skąd  = ev.getFrom();
		Location gdzie = ev.getTo();
		
		Predicate<Function<Location, Double>> zmiana = func -> func.apply(skąd) != func.apply(gdzie);
		
		if (	zmiana.test(Location::getX) ||
				zmiana.test(Location::getY) ||
				zmiana.test(Location::getZ)
				)
			arena.ruch(ev.getPlayer(), status);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (!(ev.getEntity() instanceof Player && ev.getDamager() instanceof Player)) return;
		
		Arena arena1 = arena(ev.getEntity());
		if (arena1 == null) return;
		if (!arena1.grane) return;
		
		Arena arena2 = arena(ev.getDamager());
		if (arena2 == null) return;
		
		if (!arena1.equals(arena2)) return;
		
		ev.setCancelled(arena1.uderzył((Player) ev.getDamager(), (Player) ev.getEntity()));
	}
	@EventHandler(priority = EventPriority.LOW)
	public void uderzenie(PlayerInteractEvent ev) {
		if (!ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
		
		Status status = status(ev.getPlayer());
		if (status == null || status.typ != null) return;
		
		Arena arena = arena(ev.getPlayer());
		
		Location loc = ev.getClickedBlock().getLocation();
		
		for (Player p : arena.gracze) {
			Status s = status(p);
			if (s.loc != null && s.loc.equals(loc)) {
				ev.setCancelled(true);
				arena.znalazł(ev.getPlayer(), p);
				return;
			}
		}
	}
	
	// TODO rozpisać staty
		
	// util
	
	@Override @SuppressWarnings("unchecked") Arena		arena(Entity p) { return super.arena(p); }
	@Override @SuppressWarnings("unchecked") Statystyki staty(Entity p) { return super.staty(p); }
	static final String metaStatusId = "mimiMinigraChowanyStatus";
	static Status status(Player p) {
		return metadata(p, metaStatusId);
	}
	
	
	// Override
	
	@Override
	public int czas() {
		int w = super.czas();
		
		for (Minigra.Arena arena : mapaAren.values())
			if (arena.grane)
				((Arena) arena).sekunda();
		
		return w;
	}
	
	public static final String prefix = Func.prefix("Chowany");
	final Config configAreny = new Config("configi/minigry/Chowany Areny");
	@Override String getPrefix() { return prefix; }
	@Override Config getConfigAreny() { return configAreny; }
	@Override String getMetaStatystyki() { return "mimiMinigraChowanyStaty"; }
	@Override String getMetaId() { return "mimiMinigraChowany"; }
}

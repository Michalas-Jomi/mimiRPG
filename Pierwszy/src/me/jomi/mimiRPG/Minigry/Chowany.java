package me.jomi.mimiRPG.Minigry;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Chowany extends Minigra {
	public static class Arena extends Minigra.Arena {
		static class Status {
			Material typ; // null oznacza szukającego
			
			FallingBlock blok; // null dla zamaskowane
			Location loc;	  // null dla nie zamaskowane
			
			int nieruchomesekundy = -1;
		}
		@Mapowane List<Material> bloki;
		@Mapowane Location start;
		
		@Mapowane Location zbiórkaSzukających;
		@Mapowane int czasPoczekalniSzukającego;
		
		
		
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Team teamChowający = sb.registerNewTeam("Chowający");
		Team teamSzukający = sb.registerNewTeam("Szukający");		
		void Init() {
			teamChowający.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
			teamChowający.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.NEVER);
			teamChowający.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

			teamSzukający.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
			teamSzukający.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.NEVER);
			teamSzukający.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
		}
		
		
		// obsługa start / koniec
		@Override
		void start() {
			super.start();
			for (Player p : gracze) {
				p.setScoreboard(sb);
				chowający(p);
				
				Status status = new Status();
				status.typ = Func.losuj(bloki);
				Func.ustawMetadate(p, metaStatusId, status);
			}
			szukający(Func.losuj(gracze));
		}
		@Override
		Player opuść(int i, boolean info) {
			Player p = super.opuść(i, info);
			
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			teamSzukający.removeEntry(p.getName());
			anulujChowającego(p);
			
			p.removeMetadata(metaStatusId, Main.plugin);
			
			return p;
		}
		
		
		
		
		void szukający(Player p) {
			anulujChowającego(p);
			teamSzukający.addEntry(p.getName());
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
		
		
		
		void ruch(Player p) {
			Status status = status(p);

			status.nieruchomesekundy = -1;
			
			if (status.loc != null) {
				status.loc.getBlock().setType(Material.AIR);
				status.loc = null;
				p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_DESTROY, .8f, 1f);
				p.sendMessage(prefix + "Jesteś teraz widoczny");
				przywołajBlok(p, status);
			}
			
			status.blok.teleport(p);
		}
		
		// wykonywane co sekunde // TODO
		char[] etapyMaskowaniaZnaczki = new char[] {'░', '▒', '▓', '█'};
		void sekunda() {
			for (Player p : gracze) {
				Status status = status(p);
				if (status.loc == null && ++status.nieruchomesekundy >= 5)
					zamaskuj(p, status);
				else if (status.nieruchomesekundy >= 1) {
					String s = "§6Nie poruszaj się aby się zamaskować §a" + etapyMaskowaniaZnaczki[status.nieruchomesekundy - 1];
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(s));
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .8f, ((float) status.nieruchomesekundy - 1) / 3f + 1);
				}
			}
		}
		
		void zamaskuj(Player p, Status status) {
			status.loc = p.getLocation();
			if (!status.loc.getBlock().getType().isAir()) {
				p.sendMessage(prefix + "§cW tym miejscu nie możesz się zamaskoawć!");
				status.nieruchomesekundy = -1;
				status.loc = null;
				return;
			}
			
			p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, .8f, 1f);
			
			status.loc.getBlock().setType(status.typ);
			
			status.blok.remove();
			status.blok = null;
			
			p.sendMessage(prefix + "Teraz jesteś §aZamaskowany");
		}
		
		// TODO możliwość zmiany state bloku
		void przywołajBlok(Player p, Status status) {
			FallingBlock blok = p.getWorld().spawnFallingBlock(p.getLocation(), Bukkit.createBlockData(status.typ));
			blok.setGravity(false);
			blok.setDropItem(false);
			blok.setHurtEntities(false);
			status.blok = blok;
		}
		
		
		
		// util
		
		static final String metaStatusId = "mimiMinigraChowanyStatus";
		static Status status(Player p) {
			return (Status) p.getMetadata(metaStatusId).get(0).value();
		}
		
		
		
		
		// Override
		
		Chowany inst;
		@Override Minigra getInstMinigra() { return inst; }
		@Override <M extends Minigra> void setInst(M inst) { this.inst = (Chowany) inst; }
		@Override Supplier<? extends Minigra.Statystyki> noweStaty() { return Statystyki::new; }
		@Override int policzGotowych() { return gracze.size(); }
		
	}
	public static class Statystyki extends Minigra.Statystyki {

		@Override void sprawdzTopke(Player p) {}
		
	}

	
	
	
	
	// EventHandler
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void ruch(PlayerMoveEvent ev) {
		Arena arena = arena(ev.getPlayer());
		if (arena == null) return;
		
		Location skąd  = ev.getFrom();
		Location gdzie = ev.getTo();
		
		Predicate<Function<Location, Double>> zmiana = func -> Math.abs(func.apply(skąd) - func.apply(gdzie)) > .15;
		
		if (	zmiana.test(Location::getX) &&
				zmiana.test(Location::getY) &&
				zmiana.test(Location::getZ)
				) {
			arena.ruch(ev.getPlayer());
		}
		
	}
	
	
	
	
	// util
	
	@Override @SuppressWarnings("unchecked") Arena		arena(Entity p) { return super.arena(p); }
	@Override @SuppressWarnings("unchecked") Statystyki staty(Entity p) { return super.staty(p); }
	
	
	
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
	final Config configAreny = new Config("Chowany Areny");
	@Override String getPrefix() { return prefix; }
	@Override Config getConfigAreny() { return configAreny; }
	@Override String getMetaStatystyki() { return "mimiMinigraChowanyStaty"; }
	@Override String getMetaId() { return "mimiMinigraChowany"; }
	

}

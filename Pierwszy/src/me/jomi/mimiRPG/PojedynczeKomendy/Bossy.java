package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Chat.Party.API.OpuszczaniePartyEvent;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDespawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

@Moduł
public class Bossy extends Komenda implements Listener, Przeładowalny {
	public static boolean warunekModułu() {
		return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
	}
	public static class ArenaDane extends Mapowany {
		@Mapowane Location róg1;
		@Mapowane Location róg2;
		@Mapowane Location spawnGraczy;
		@Mapowane Location spawnBossa;
		@Mapowane String nazwaBossa;
		@Mapowane int minutyWalki = 30; // -1 dla braku limitu
		@Mapowane int życiaGraczy = 3; // -1 dla braku limitu
		@Mapowane boolean keepInv;
		@Mapowane int minGracze = 1;
		@Mapowane int maxGracze = -1; // -1 dla braku limitu
		@Mapowane boolean broadcastZwycięstwa;
		@Mapowane boolean broadcastPorażki;
		@Mapowane boolean broadcastStartu;
		
		public final Set<Integer> zajęte = new HashSet<>();

		@Override
		protected void Init() {
			BiConsumer<Function<Location, Integer>, BiConsumer<Location, Integer>> krotka = (getter, setter) -> {
				Krotka<Integer, Integer> k = Func.minMax(getter.apply(róg1), getter.apply(róg2), Math::min, Math::max);
				setter.accept(róg1, k.a);
				setter.accept(róg2, k.b);
			};
			
			krotka.accept(Location::getBlockX, Location::setX);
			krotka.accept(Location::getBlockY, Location::setY);
			krotka.accept(Location::getBlockZ, Location::setZ);
		}
		
		public int getPrzesunięcieAren() {
			return Math.abs(róg1.getBlockX() - róg2.getBlockX()) + 25;
		}
		
		public boolean wczytajZ(ArenaDane dane) {
			if (!możnaWczytać(dane))
				return false;
			Func.dajFields(this.getClass()).forEach(field -> {
				if (field.isAnnotationPresent(Mapowane.class))
					try {
						field.set(this, field.get(dane));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
			});
			return true;
		}
		boolean możnaWczytać(ArenaDane dane) {
			return róg1.equals(dane.róg1) && róg2.equals(dane.róg2);
		}
	}
	public static class Arena {
		static final String meta = "mimiBossArena";
		
		final int przesunięcieLoc;
		final ArenaDane arenaDane;
		final List<Player> gracze;
		ActiveMob boss;
		
		BossBar bbCzas;
		int czas;
		
		boolean wystartowana = false;
		boolean zakończona = false;
		
		final String graczeStr;
		
		Arena(ArenaDane arenaDane, List<Player> gracze, MythicMob boss, int przesunięcieLoc) throws AssertionError {
			this.przesunięcieLoc = przesunięcieLoc;
			this.arenaDane = arenaDane;
			this.gracze = gracze;
			
			graczeStr = Func.listToString(Func.wykonajWszystkim(gracze, p -> "§e" + p.getDisplayName()));
			
			assert gracze.size() < arenaDane.minGracze : ("Potrzeba conajmniej " + arenaDane.minGracze + " graczy"); 
			assert gracze.size() > arenaDane.maxGracze : ("Nie może być więcej niż " + arenaDane.maxGracze + "graczy");
			if (arenaDane.minutyWalki > 0) {
				czas = arenaDane.minutyWalki * 60 + 1;
				bbCzas = Bukkit.createBossBar("Czas", BarColor.YELLOW, BarStyle.SOLID);
				bbCzas.setVisible(true);
				bbCzas.setProgress(1);
				gracze.forEach(bbCzas::addPlayer);
			}
			arenaDane.spawnGraczy.clone().add(0, -1, 0).getBlock().setType(Material.BEDROCK, false);
			gracze.forEach(p -> {
				Func.ustawMetadate(p, meta, this);
				p.teleport(arenaDane.spawnGraczy);
			});
			
			World world = arenaDane.róg1.getWorld();
			timerMove();
			generuj(new Func.IteratorBloków<>(arenaDane.róg1, arenaDane.róg2,
					(x, y, z) -> new Krotka<>(world.getBlockAt(x, y, z), world.getBlockAt(x + przesunięcieLoc, y, z))), () -> {
				this.boss = boss.spawn(BukkitAdapter.adapt(arenaDane.spawnBossa), 1);
				wystartowana = true;
				if (arenaDane.broadcastStartu)
					Bukkit.broadcastMessage(prefix + Func.msg("%s zaatakowli wyzwali na pojedynek %s!",
							graczeStr, boss.getDisplayName()));
				if (arenaDane.minutyWalki > 0)
					timer();
			});
		}
		private void timerMove() {
			if (wystartowana)
				return;
			gracze.forEach(p -> {
				if (p.getLocation().distance(arenaDane.spawnGraczy) > .4) {
					p.teleport(arenaDane.spawnGraczy);
					p.sendMessage(prefix + "Nie ruszaj się, Arena za moment zostanie wygenerowana");
				}
			});
			Func.opóznij(4, this::timerMove);
		}
		private void generuj(Iterator<Krotka<Block, Block>> it, Runnable taskNaKoniec) {
			int licz = 0;
			while (it.hasNext() && licz < Baza.BudowanieAren.maxBloki) {
				Krotka<Block, Block> krotka = it.next();
				if (krotka.a.getType() != krotka.b.getType()) {
					krotka.b.setType(krotka.a.getType(), false);
					licz++;
				}
			}
			if (it.hasNext())
				Func.opóznij(Baza.BudowanieAren.tickiPrzerw, () -> generuj(it, taskNaKoniec));
			else
				taskNaKoniec.run();
		}
		private void timer() {
			if (zakończona)
				return;
			
			bbCzas.setProgress(--czas / (arenaDane.minutyWalki * 60));
			
			if (czas <= 0) {
				porażka();
			} else
				Func.opóznij(20, this::timer);
			
		}
		
		public void zabił(LivingEntity killer) {
			if (killer != null)
				for (Player p : gracze)
					if (p.getName().equals(killer.getName())) {
						powiadomGraczy("%s zadał finalny cios bossowi %s!", p.getDisplayName(), boss.getDisplayName());
						return;
					}
			powiadomGraczy("pokonaliście Bossa %s!", boss.getDisplayName());
		}
		public void zwycięstwo() {
			if (arenaDane.broadcastZwycięstwa)
				Bukkit.broadcastMessage(prefix + Func.msg("Drużyna szaleńców %s udowodniła swą wyższość przeciwko Bossowi %s!",
						graczeStr, boss.getDisplayName()));
			zakończ();
		}
		public void porażka() {
			if (arenaDane.broadcastPorażki)
				Bukkit.broadcastMessage(prefix + Func.msg("Drużyna szaleńców %s poległa na polu chwały przeciwko Bossowi %s",
						graczeStr, boss.getDisplayName()));
			zakończ();
		}
		public void zakończ() {
			zakończona = true;
			areny.remove(boss);
			boss.setDespawned();
			gracze.forEach(p -> zapomnij(p, false));
		}
		

		Map<String, Integer> mapaŚmierci = new HashMap<>();
		void śmierć(Player p) {
			int zgony = mapaŚmierci.getOrDefault(p.getName(), 0) + 1;
			mapaŚmierci.put(p.getName(), zgony);
			if (arenaDane.życiaGraczy > 0 && zgony >= arenaDane.życiaGraczy)
				odpada(p);
		}
		Location respawn(Player p) {
			return arenaDane.spawnGraczy;
		}
		
		void odpada(Player p) {
			powiadomGraczy("%s odpadł z Areny!", p.getDisplayName());
			zapomnij(p, true);
			if (gracze.size() <= 0)
				porażka();
		}
		void zapomnij(Player p, boolean usuńZGraczy) {
			if (usuńZGraczy)
				for (int i=0; i < gracze.size(); i++)
					if (gracze.get(i).getName().equals(p.getName())) {
						gracze.remove(i);
						break;
					}
			p.removeMetadata(meta, Main.plugin);
			Func.spawn(p);
		}

		
		public boolean powiadomGraczy(String format, Object... args) {
			String msg = prefix + Func.msg(format, args);
			gracze.forEach(p -> p.sendMessage(msg));
			return true;
		}

		public static Arena get(Player p) {
			return p.hasMetadata(meta) ? (Arena) p.getMetadata(meta).get(0).value() : null;
		}
	}
	
	public static final String prefix = Func.prefix("Bossy");
	public static final String permEdytor = Func.permisja("bossy.edytor");
	
	public Bossy() {
		super("bossy", "/bossy dołącz <arena>");
		Main.dodajPermisje(permEdytor);
	}
	
	static Map<String, ArenaDane> mapaArenDanych;
	static Map<ActiveMob, Arena> areny;
	
	@EventHandler
	public void opuszczanieParty(OpuszczaniePartyEvent ev) {
		Func.wykonajDlaNieNull(Arena.get(ev.getPlayer()), arena -> arena.odpada(ev.getPlayer()));
	}
	@EventHandler
	public void śmierćGracza(PlayerDeathEvent ev) {
		Func.wykonajDlaNieNull(Arena.get(ev.getEntity()), arena -> arena.śmierć(ev.getEntity()));
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void respawn(PlayerRespawnEvent ev) {
		Func.wykonajDlaNieNull(Arena.get(ev.getPlayer()), arena -> ev.setRespawnLocation(arena.respawn(ev.getPlayer())));
	}
	
	@EventHandler
	public void śmiećBossa(MythicMobDeathEvent ev) {
		Func.wykonajDlaNieNull(areny.remove(ev.getMob()), arena -> {
			arena.zabił(ev.getKiller());
			arena.zwycięstwo();
		});
	}
	@EventHandler
	public void despawnBossa(MythicMobDespawnEvent ev) {
		Func.wykonajDlaNieNull(areny.remove(ev.getMob()), arena -> {
			arena.zakończ();
		});
	}
	
	// TODO dozwolone komendy
	
	
	EdytorOgólny<ArenaDane> edytor = new EdytorOgólny<>("bossy", ArenaDane.class);
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> lista = new ArrayList<>();
		if (args.length <= 1) {
			lista.add("dołącz");
			if (sender.hasPermission(permEdytor))
				lista.add("edytor");
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor))
				return utab(args, "-t", "-u");
		}
		if (lista.isEmpty())
			return null;
		return utab(args, lista);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		
		switch (args[0].toLowerCase()) {
		case "edytor":
			if (sender.hasPermission(permEdytor))
				return edytor.wymuśConfig_onCommand(prefix, "configi/Bossy", sender, label, args);
			break;
		case "dolacz":
		case "dolącz":
		case "dołacz":
		case "dołącz":
			//bossy dołącz <arena>
			//TODO dołączanie
			break;
		}
		
		return false;
	}
	
	
	@Override
	public void przeładuj() {
		Map<String, ArenaDane> stare = new HashMap<>();
		mapaArenDanych.forEach(stare::put);
		mapaArenDanych.clear();
		Config config = new Config("configi/Bossy");
		config.mapa(false).forEach((str, obj) -> {
			ArenaDane dane = (ArenaDane) obj;
			Func.wykonajDlaNieNull(stare.remove(str), stary -> {
				if (!stary.wczytajZ(dane))
					stare.put(str, stary);
			}, () -> mapaArenDanych.put(str, dane));
		});
		stare.values().forEach(dane -> {
			Lists.newArrayList(areny.values()).forEach(arena -> {
				if (arena.arenaDane.equals(dane))
					arena.zakończ();
			});
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Areny Bossów", mapaArenDanych.size());
	}
}

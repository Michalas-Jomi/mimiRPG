package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Chat.Party;
import me.jomi.mimiRPG.Chat.Party.API.OpuszczaniePartyEvent;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.PojedynczeKomendy.Bossy.API.PrzegranaBossArenaEvent;
import me.jomi.mimiRPG.PojedynczeKomendy.Bossy.API.WygranaBossArenaEvent;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.TriKrotka;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

import io.lumine.xikage.mythicmobs.MythicMobs;
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
	
	public static class API {
		public static abstract class BossArenaEvent extends Event {
			public final Arena arena;
			public final String nazwaBossa;
			
			public BossArenaEvent(Arena arena) {
				this.arena = arena;
				this.nazwaBossa = arena.arenaDane.nazwaBossa;
			}
		}
		public static class WygranaBossArenaEvent extends BossArenaEvent {
			public WygranaBossArenaEvent(Arena arena) {
				super(arena);
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
		public static class PrzegranaBossArenaEvent extends BossArenaEvent {
			public PrzegranaBossArenaEvent(Arena arena) {
				super(arena);
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
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
		@Mapowane int minutyCooldownu = 30;
		@Mapowane List<String> wymaganeBossy;
		@Mapowane int priorytet = 1;
		@Mapowane ItemStack ikona = Func.stwórzItem(Material.GOLD_INGOT, "", "Boss");
		@Mapowane ItemStack przepustka; // null dla braku przepustki
		
		public final Set<Integer> zajęte = new HashSet<>();

		@Override
		protected void Init() {
			if (róg1 == null || róg2 == null)
				return;
			
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
			return Math.abs(róg1.getBlockX() - róg2.getBlockX()) + 50;
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

		public boolean maWstęp(Player p) {
			return maWstęp(Gracz.wczytaj(p));
		}
		public boolean maWstęp(Gracz g) {
			for (String wymagany : wymaganeBossy)
				if (!g.bossyLicznik.containsKey(wymagany))
					return false;
			return true;
		}
		public boolean minąłCooldown(Player p) {
			return minąłCooldown(Gracz.wczytaj(p));
		}
		public boolean minąłCooldown(Gracz g) {
			return minąłCooldown(g.bossyCooldown.getOrDefault(nazwaBossa, 0L));
		}
		public boolean minąłCooldown(long ostatnieWejście) {
			return ostatnieWejście + minutyCooldownu * 60 * 1000 <= System.currentTimeMillis();
		}
		
		public MythicMob dajTypBossa() {
			return MythicMobs.inst().getMobManager().getMythicMob(nazwaBossa);
		}
		
		public Arena nowaArena(List<Player> gracze) {
			int p = getPrzesunięcieAren();
			
			int akt = p;
			
			while (zajęte.contains(akt))
				akt += p;
			
			try {
				return new Arena(this, gracze, dajTypBossa(), akt);
			} catch (AssertionError e) {//TODO sprawdzić -dsa -esa
				gracze.forEach(gracz -> gracz.sendMessage(prefix + e.getMessage()));
				return null;
			}
		}
	}
	public static class Arena {
		static final String meta = "mimiBossArena";
		
		final int przesunięcieLoc;
		final ArenaDane arenaDane;
		final List<Player> graczeStart = new ArrayList<>();
		final List<Player> gracze;
		ActiveMob boss;
		
		final Location spawnGraczy;
		
		BossBar bbCzas;
		int czas;
		
		boolean wystartowana = false;
		boolean zakończona = false;
		
		final String graczeStr;
		
		Arena(ArenaDane arenaDane, List<Player> gracze, MythicMob boss, int przesunięcieLoc) throws AssertionError {
			this.przesunięcieLoc = przesunięcieLoc;
			this.arenaDane = arenaDane;
			this.gracze = gracze;
			
			gracze.forEach(graczeStart::add);
			
			graczeStr = Func.listToString(Func.wykonajWszystkim(gracze, p -> "§e" + Func.getDisplayName(p)));
			
			assert gracze.size() < arenaDane.minGracze : ("Potrzeba conajmniej " + arenaDane.minGracze + " graczy"); 
			assert gracze.size() > arenaDane.maxGracze : ("Nie może być więcej niż " + arenaDane.maxGracze + "graczy");
			if (arenaDane.minutyWalki > 0) {
				czas = arenaDane.minutyWalki * 60 + 1;
				bbCzas = Bukkit.createBossBar("Czas", BarColor.YELLOW, BarStyle.SOLID);
				bbCzas.setVisible(true);
				bbCzas.setProgress(1);
				gracze.forEach(bbCzas::addPlayer);
			}
			spawnGraczy = przesuń(arenaDane.spawnGraczy);
			
			spawnGraczy.clone().add(0, -1, 0).getBlock().setType(Material.BEDROCK, false);
			gracze.forEach(p -> {
				p.addScoreboardTag(Main.tagBlokowanieKomendy);
				Func.ustawMetadate(p, meta, this);
				p.teleport(spawnGraczy);
				
				Gracz g = Gracz.wczytaj(p);
				g.bossyCooldown.put(arenaDane.nazwaBossa, System.currentTimeMillis());
				g.zapisz();
			});
			
			World world = arenaDane.róg1.getWorld();
			timerMove();
			generuj(new Func.IteratorBloków<>(arenaDane.róg1, arenaDane.róg2,
					(x, y, z) -> new Krotka<>(world.getBlockAt(x, y, z), world.getBlockAt(x + przesunięcieLoc, y, z))), () -> {
				this.boss = boss.spawn(BukkitAdapter.adapt(przesuń(arenaDane.spawnBossa)), 1);
				
				areny.put(this.boss, this);
				wystartowana = true;
				if (arenaDane.broadcastStartu)
					Func.broadcast(prefix + Func.msg("%s wyzwali na pojedynek %s!",
							graczeStr, boss.getDisplayName()));
				if (arenaDane.minutyWalki > 0)
					timer();
			});
			
			arenaDane.zajęte.add(przesunięcieLoc);
		}
		private void timerMove() {
			if (wystartowana)
				return;
			gracze.forEach(p -> {
				if (p.getLocation().distance(spawnGraczy) > .4) {
					p.teleport(spawnGraczy);
					p.sendMessage(prefix + "Nie ruszaj się, Arena za moment zostanie wygenerowana");
				}
			});
			Func.opóznij(4, this::timerMove);
		}
		private void generuj(Iterator<Krotka<Block, Block>> it, Runnable taskNaKoniec) {
			int licz = 0;
			while (it.hasNext() && licz < Baza.BudowanieAren.maxBloki) {
				Krotka<Block, Block> krotka = it.next();
				if (krotka.a.getType() != krotka.b.getType() || !krotka.a.getBlockData().getAsString().equals(krotka.b.getBlockData().getAsString())) {
					//krotka.b.setType(krotka.a.getType(), false);
					krotka.b.setBlockData(krotka.a.getBlockData(), false);
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
			
			bbCzas.setProgress(--czas / (double) (arenaDane.minutyWalki * 60));
			
			if (czas <= 0) {
				porażka();
			} else
				Func.opóznij(20, this::timer);
			
		}
		
		public void zabił(LivingEntity killer) {
			if (killer != null)
				for (Player p : gracze)
					if (p.getName().equals(killer.getName())) {
						powiadomGraczy("%s zadał finalny cios bossowi %s!", Func.getDisplayName(p), boss.getDisplayName());
						return;
					}
			powiadomGraczy("pokonaliście Bossa %s!", boss.getDisplayName());
		}
		public void zwycięstwo() {
			if (arenaDane.broadcastZwycięstwa)
				Func.broadcast(prefix + Func.msg("Drużyna szaleńców %s udowodniła swą wyższość przeciwko Bossowi %s!",
						graczeStr, boss.getDisplayName()));
			graczeStart.forEach(p -> {
				Gracz g = Gracz.wczytaj(p);
				g.bossyLicznik.put(arenaDane.nazwaBossa, g.bossyLicznik.getOrDefault(arenaDane.nazwaBossa, 0) + 1);
				g.zapisz();
			});
			Bukkit.getPluginManager().callEvent(new WygranaBossArenaEvent(this));
			Func.opóznij(20 * 20, this::zakończ);
		}
		public void porażka() {
			if (arenaDane.broadcastPorażki)
				Func.broadcast(prefix + Func.msg("Drużyna szaleńców %s poległa na polu chwały przeciwko Bossowi %s",
						graczeStr, boss.getDisplayName()));
			Bukkit.getPluginManager().callEvent(new PrzegranaBossArenaEvent(this));
			zakończ();
		}
		public void zakończ() {
			Main.log(prefix + Func.msg("arena graczy %s przeciwko %s zakończyła się", graczeStr, boss.getDisplayName()));
			zakończona = true;
			areny.remove(boss);
			boss.setDespawned();
			Func.wykonajDlaNieNull(boss.getEntity(), e -> {
				e.remove();
				Func.wykonajDlaNieNull(e.getBukkitEntity(), Entity::remove);
			});
			gracze.forEach(p -> zapomnij(p, false));
			arenaDane.zajęte.remove(przesunięcieLoc);
		}
		

		Map<String, Integer> mapaŚmierci = new HashMap<>();
		boolean śmierć(Player p) {
			int zgony = mapaŚmierci.getOrDefault(p.getName(), 0) + 1;
			mapaŚmierci.put(p.getName(), zgony);
			
			powiadomGraczy("%s umarł! życia %s/%s", Func.getDisplayName(p), arenaDane.życiaGraczy - zgony, arenaDane.życiaGraczy);
			
			if (arenaDane.życiaGraczy > 0 && zgony >= arenaDane.życiaGraczy)
				odpada(p);
			return arenaDane.keepInv;
		}
		Location respawn(Player p) {
			return spawnGraczy;
		}
		
		void odpada(Player p) {
			powiadomGraczy("%s odpadł z Areny!", Func.getDisplayName(p));
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
			p.removeScoreboardTag(Main.tagBlokowanieKomendy);
			p.removeMetadata(meta, Main.plugin);
			Func.tpSpawn(p);
			if (bbCzas != null)
				bbCzas.removePlayer(p);
		}

		public Location przesuń(Location loc) {
			return loc.clone().add(przesunięcieLoc, 0, 0);
		}
		
		public void wykonajGraczom(Consumer<? super Player> cons) {
			gracze.forEach(cons);
		}
		
		public boolean powiadomGraczy(String format, Object... args) {
			String msg = prefix + Func.msg(format, args);
			gracze.forEach(p -> p.sendMessage(msg));
			return true;
		}

		public static Arena get(Player p) {
			return p.hasMetadata(meta) ? (Arena) p.getMetadata(meta).get(0).value() : null;
		}
		public void zdespawnowany(MythicMobDespawnEvent ev) {
			Main.log(prefix + Func.msg("boss %s z areny graczy %s został zdespawnowany!", ev.getMob().getDisplayName(), graczeStr));
			zakończ();
			graczeStart.forEach(p -> {
				Gracz g = Gracz.wczytaj(p);
				g.bossyCooldown.remove(arenaDane.nazwaBossa);
				g.zapisz();
			});
		}
	}
	
	
	public static final String prefix = Func.prefix("Bossy");
	public static final String permEdytor = Func.permisja("bossy.edytor");

	public Bossy() {
		super("bossy", "/bossy dołącz <arena>");
		Main.dodajPermisje(permEdytor);
		
		edytor.zarejestrujPoZatwierdz((dane1, dane2) -> przeładuj());
		edytor.zarejestrujOnInit((dane, ścieżka) -> dane.nazwaBossa = ścieżka);
		edytor.zarejestrujOnZatwierdzZEdytorem((dane, edytor) -> edytor.ścieżka = dane.nazwaBossa);
		
		
		panel.ustawClick(ev -> {
			if (ev.getCurrentItem().isSimilar(Baza.pustySlotCzarny))
				return;
			if (Func.multiEquals(ev.getCurrentItem().getType(), Material.YELLOW_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE) &&
					ev.getCurrentItem().getItemMeta().hasCustomModelData() && ev.getCurrentItem().getItemMeta().getCustomModelData() == 441441)
				return;
			if (!stwórzPanel((Player) ev.getWhoClicked()).getItem(ev.getRawSlot()).isSimilar(ev.getCurrentItem())) {
				ev.getWhoClicked().closeInventory();
				return;
			}
			
			String boss = Func.getDisplayName(ev.getCurrentItem().getItemMeta()).substring(2);
			try {
				Player p = (Player) ev.getWhoClicked();
				ArenaDane arena = mapaArenDanych.get(boss);
				if (arena.przepustka != null) {
					if (!p.getInventory().contains(arena.przepustka))
						return;
					p.getInventory().remove(arena.przepustka);
				}
				arena.nowaArena(Party.dajGraczyParty(p));
			} catch (AssertionError e) {
				ev.getWhoClicked().sendMessage(prefix + e.getMessage());
			}
		});
	}
	
	static final Map<String, ArenaDane> mapaArenDanych = new HashMap<>();
	static final Map<ActiveMob, Arena> areny = new HashMap<>();
	
	@EventHandler
	public void opuszczanieParty(OpuszczaniePartyEvent ev) {
		Func.wykonajDlaNieNull(Arena.get(ev.getPlayer()), arena -> arena.odpada(ev.getPlayer()));
	}
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		Func.wykonajDlaNieNull(Arena.get(ev.getPlayer()), arena -> arena.odpada(ev.getPlayer()));
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void śmierćGracza(PlayerDeathEvent ev) {
		Func.wykonajDlaNieNull(Arena.get(ev.getEntity()), arena -> {
			boolean keepinv = arena.śmierć(ev.getEntity());
			ev.setKeepInventory(keepinv);
			ev.setKeepLevel(keepinv);
		});
	}
	@EventHandler(priority = EventPriority.HIGHEST)
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
			arena.zdespawnowany(ev);
		});
	}
	
	static Panel panel = new Panel(true);
	
	private Inventory stwórzPanel(Player p) {
		List<Gracz> gracze = Func.wykonajWszystkim(Party.dajGraczyParty(p), Gracz::wczytaj);
		
		List<TriKrotka<String, ArenaDane, Long>> dostępne = new ArrayList<>();
		
		mapaArenDanych.forEach((str, dane) -> {
			for (Gracz g : gracze)
				if (!dane.maWstęp(g))
					return;
			AtomicLong cooldown = new AtomicLong(0L);
			
			gracze.forEach(g -> cooldown.set(Math.max(cooldown.get(), g.bossyCooldown.getOrDefault(dane.nazwaBossa, 0L))));
			
			Func.insort(new TriKrotka<>(str, dane, cooldown.get()), dostępne, krotka -> (double) krotka.b.priorytet);
		});
		
		Inventory inv = panel.stwórz(null, Func.potrzebneRzędy(dostępne.size()), "&4&lBossy", Baza.pustySlotCzarny);
		
		int[] slot = new int[] {0};
		dostępne.forEach(krotka -> {
			ItemStack item = krotka.b.ikona.clone();
			
			int zabito = Gracz.wczytaj(p).bossyLicznik.getOrDefault(krotka.b.nazwaBossa, 0);
			Func.dodajLore(item, "&aZabito &e" + zabito + "&a razy");
			
			Func.nazwij(item, "&6" + krotka.b.nazwaBossa);
			
			if (!krotka.b.minąłCooldown(krotka.c)) {
				Func.dodajLore(item, "&cNiedostępne do &e" + Func.data(krotka.c + krotka.b.minutyCooldownu * 60 * 1000L));
				Func.typ(item, Material.YELLOW_STAINED_GLASS_PANE);
				Func.customModelData(item, 441441);
			}
			if (krotka.b.przepustka != null)
				if (p.getInventory().contains(krotka.b.przepustka)) {
					Func.dodajLore(item, "&czużywa przepustkę");
				} else {
					Func.typ(item, Material.YELLOW_STAINED_GLASS_PANE);
					Func.customModelData(item, 441441);
					Func.dodajLore(item, "&4Wymaga przepustki");
				}
			if (!(krotka.b.minGracze <= gracze.size() && (krotka.b.maxGracze == -1 || krotka.b.maxGracze >= gracze.size()))) {
				Func.dodajLore(item, Func.msg("&cWymagane /party w ilości %s-%s &cosób", krotka.b.minGracze, krotka.b.maxGracze));
				Func.typ(item, Material.RED_STAINED_GLASS_PANE);
				Func.customModelData(item, 441441);
			}
			
			inv.setItem(slot[0]++, item);
		});
		return inv;
	}
	public void otwórzPanel(Player p) {
		p.openInventory(stwórzPanel(p));
	}
	
	EdytorOgólny<ArenaDane> edytor = new EdytorOgólny<>("bossy", ArenaDane.class);
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(permEdytor))
			return null;
		
		if (args.length <= 1)
			return utab(args, "edytor");
		else if (args[0].equalsIgnoreCase("edytor"))
			if (args.length == 2)
				return utab(args, "-t", "-u");
			else if (args.length >= 3 && args[1].equalsIgnoreCase("-t"))
				return utab(args, mapaArenDanych.keySet());
		
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor))
			return edytor.wymuśConfig_onCommand(prefix, "configi/Bossy", sender, label, args);
		
		if (!(sender instanceof Player))
			throwMsg("tylkoGracz");

		otwórzPanel((Player) sender);
		
		return true;
	}
	
	@Override
	public void przeładuj() {
		Map<String, ArenaDane> stare = new HashMap<>();
		mapaArenDanych.forEach(stare::put);
		mapaArenDanych.clear();
		Config config = new Config("configi/Bossy");
		config.mapa(false).forEach((__, obj) -> {
			ArenaDane dane = (ArenaDane) obj;
			
			Func.wykonajDlaNieNull(stare.remove(dane.nazwaBossa), stary -> {
				if (stary.wczytajZ(dane))
					mapaArenDanych.put(dane.nazwaBossa, stary);
				else
					stare.put(dane.nazwaBossa, stary);
			}, () -> mapaArenDanych.put(dane.nazwaBossa, dane));
			
			if (dane.dajTypBossa() == null)
				Main.warn(prefix + "Nie odnaleziono bossa " + dane.nazwaBossa);
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

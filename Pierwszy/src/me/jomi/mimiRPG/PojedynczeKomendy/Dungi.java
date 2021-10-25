package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import me.jomi.mimiRPG.PojedynczeKomendy.Dungi.API.PrzegranaDungArenaEvent;
import me.jomi.mimiRPG.PojedynczeKomendy.Dungi.API.WygranaDungArenaEvent;
import me.jomi.mimiRPG.PojedynczeKomendy.Dungi.ArenaDane.Pokój;
import me.jomi.mimiRPG.PojedynczeKomendy.Dungi.ArenaDane.Pokój.Mob;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.TriKrotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDespawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

@Moduł
public class Dungi extends Komenda implements Listener, Przeładowalny, Zegar {
	public static boolean warunekModułu() {
		return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
	}
	
	public static class API {
		public static abstract class DungArenaEvent extends Event {
			public final Arena arena;
			public final String nazwaBossa;
			
			public DungArenaEvent(Arena arena) {
				this.arena = arena;
				this.nazwaBossa = arena.arenaDane.nazwaBossa;
			}
		}
		public static class WygranaDungArenaEvent extends DungArenaEvent {
			public WygranaDungArenaEvent(Arena arena) {
				super(arena);
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
		public static class PrzegranaDungArenaEvent extends DungArenaEvent {
			public PrzegranaDungArenaEvent(Arena arena) {
				super(arena);
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
	}
	
	public static class ArenaDane extends Mapowany {
		public static class Pokój extends Mapowany {
			public static class Mob extends Mapowany {
				@Mapowane String nazwaMoba;
				@Mapowane Location loc;
				
				public ActiveMob zresp(Arena arena) {
					MythicMob mob = dajMythicMob(nazwaMoba);
					if (mob == null)
						throw new Error(prefix + "Niepoprawny MythicMob \"" + nazwaMoba + "\" w Dungeonach");
					ActiveMob amob = mob.spawn(BukkitAdapter.adapt(arena.przesuń(loc)), 1);
					Func.ustawMetadate(amob.getEntity().getBukkitEntity(), metaArenaMob, arena);
					return amob;
				}
			}
			@Mapowane List<Location> drzwi;
			@Mapowane List<Mob> moby;
			
			public void otwórzDrzwi(Arena arena) {
				drzwi.forEach(loc -> {
					loc = arena.przesuń(loc);
					loc.getBlock().setType(Material.AIR);
					Func.particle(Particle.CLOUD, loc.add(.5, .5, .5), 15, .3, .3, .3, 0);
				});
			}
			public void zrespMoby(Arena arena) {
				moby.forEach(mob -> arena.moby.add(mob.zresp(arena)));
			}
		}
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
		@Mapowane List<String> wymaganeDungi;
		@Mapowane int priorytet = 1;
		@Mapowane ItemStack ikona = Func.stwórzItem(Material.GOLD_INGOT, "", "Dungeon");
		@Mapowane ItemStack przepustka; // null dla braku przepustki
		
		@Mapowane List<Pokój> pokoje;
		
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
			for (String wymagany : wymaganeDungi)
				if (!g.dungiLicznik.containsKey(wymagany))
					return false;
			return true;
		}
		public boolean minąłCooldown(Player p) {
			return minąłCooldown(Gracz.wczytaj(p));
		}
		public boolean minąłCooldown(Gracz g) {
			return minąłCooldown(g.dungiCooldown.getOrDefault(nazwaBossa, 0L));
		}
		public boolean minąłCooldown(long ostatnieWejście) {
			return ostatnieWejście + minutyCooldownu * 60 * 1000 <= System.currentTimeMillis();
		}
		
		public Arena nowaArena(List<Player> gracze) {
			int p = getPrzesunięcieAren();
			
			int akt = p;
			
			while (zajęte.contains(akt))
				akt += p;
			
			try {
				return new Arena(this, gracze, akt);
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
		List<ActiveMob> moby = new ArrayList<>();
		int pokój = -1;
		ActiveMob boss;
		
		final Location spawnGraczy;
		
		BossBar bbCzas;
		int czas;
		
		boolean wystartowana = false;
		boolean zakończona = false;
		boolean walkaZBossem = false;
		
		final String graczeStr;
		
		Arena(ArenaDane arenaDane, List<Player> gracze, int przesunięcieLoc) throws AssertionError {
			this.przesunięcieLoc = przesunięcieLoc;
			this.arenaDane = arenaDane;
			this.gracze = gracze;
			
			gracze.forEach(graczeStart::add);
			
			graczeStr = Func.listToString(Func.wykonajWszystkim(gracze, p -> "§e" + Func.getDisplayName(p)));

			dajMythicMob(arenaDane.nazwaBossa);
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
				g.dungiCooldown.put(arenaDane.nazwaBossa, System.currentTimeMillis());
				g.zapisz();
			});
			
			World world = arenaDane.róg1.getWorld();
			timerMove();
			generuj(new Func.IteratorBloków<>(arenaDane.róg1, arenaDane.róg2,
					(x, y, z) -> new Krotka<>(world.getBlockAt(x, y, z), world.getBlockAt(x + przesunięcieLoc, y, z))), () -> {
				
				wystartowana = true;
				if (arenaDane.broadcastStartu)
					Func.broadcast(prefix + Func.msg("%s wyzwali na pojedynek %s!", graczeStr, dajMythicMob(arenaDane.nazwaBossa).getDisplayName()));
				if (arenaDane.minutyWalki > 0)
					timer();
			});
			
			arenaDane.zajęte.add(przesunięcieLoc);
			areny.add(this);
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
			
			if (!walkaZBossem)
				sprawdzNastępnyPokój();
			
			if (czas <= 0) {
				porażka();
			} else
				Func.opóznij(20, this::timer);
			
		}
		
		public void sprawdzNastępnyPokój() {
			for (ActiveMob mob : moby)
				if (!mob.isDead())
					return;
			następnyPokój();
		}
		private void następnyPokój() {
			moby.clear();
			if (pokój >= 0) {
				arenaDane.pokoje.get(pokój).otwórzDrzwi(this);
				powiadomGraczy("Pokój %s zaliczony", pokój + 1);
			}
			
			if (++pokój >= arenaDane.pokoje.size())
				walkaZBossem();
			else
				arenaDane.pokoje.get(pokój).zrespMoby(this);
		}
		private void walkaZBossem() {
			this.boss = dajMythicMob(arenaDane.nazwaBossa).spawn(BukkitAdapter.adapt(przesuń(arenaDane.spawnBossa)), 1);
			Func.ustawMetadate(this.boss.getEntity().getBukkitEntity(), metaArenaMob, this);
			powiadomGraczy("Boss Dungeonu został przywołany");
			walkaZBossem = true;
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
				g.dungiLicznik.put(arenaDane.nazwaBossa, g.dungiLicznik.getOrDefault(arenaDane.nazwaBossa, 0) + 1);
				g.zapisz();
			});
			Bukkit.getPluginManager().callEvent(new WygranaDungArenaEvent(this));
			Func.opóznij(20 * 20, this::zakończ);
		}
		public void porażka() {
			if (arenaDane.broadcastPorażki)
				Func.broadcast(prefix + Func.msg("Drużyna szaleńców %s poległa na polu chwały przeciwko Bossowi %s",
						graczeStr, boss.getDisplayName()));
			Bukkit.getPluginManager().callEvent(new PrzegranaDungArenaEvent(this));
			zakończ();
		}
		public void zakończ() {
			Main.log(prefix + Func.msg("arena graczy %s przeciwko %s zakończyła się", graczeStr, boss.getDisplayName()));
			
			zakończona = true;
			
			areny.remove(this);
			
			usuńMoba(boss);
			moby.forEach(Arena::usuńMoba);
			
			gracze.forEach(p -> zapomnij(p, false));
			
			arenaDane.zajęte.remove(przesunięcieLoc);
		}
		private static void usuńMoba(ActiveMob mob) {
			mob.setDespawned();
			Func.wykonajDlaNieNull(mob.getEntity(), e -> {
				e.remove();
				Func.wykonajDlaNieNull(e.getBukkitEntity(), Entity::remove);
			});
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
				g.dungiCooldown.remove(arenaDane.nazwaBossa);
				g.zapisz();
			});
		}
	
	
		@Override
		public int hashCode() {
			return Objects.hash(przesunięcieLoc, arenaDane.nazwaBossa, graczeStart);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (!(obj instanceof Arena)) return false;
			return this.hashCode() == obj.hashCode();
		}
	}

	public static class ArenaEdit {
		static final Map<String, ArenaEdit> edytory = new HashMap<>();
		
		final ArenaDane dane;
		final List<Entity> moby = new ArrayList<>();
		int wybranyPokój = -1;
		final Player p;
		
		public ArenaEdit(Player p, String init) {
			this.p = p;

			if (init != null) {
				Object preDane = new Config("configi/Dungi").wczytaj(init);
				if (preDane != null && preDane instanceof ArenaDane) {
					dane = (ArenaDane) preDane;
					return;
				} else
					dane = Func.utwórz(ArenaDane.class);
			} else
				dane = Func.utwórz(ArenaDane.class);

			dane.róg1 = p.getLocation().clone().add(-3, -3, -3);
			dane.róg2 = p.getLocation().clone().add( 3,  3,  3);
			dane.spawnGraczy = p.getLocation().clone().add(-1, 0, 0);
			dane.spawnBossa  = p.getLocation().clone().add( 1, 0, 0);
		}
		
		public Pokój pokój() {
			if (dane.pokoje.isEmpty())
				dane.pokoje.add(Func.utwórz(Pokój.class));
			wybranyPokój = Math.min(wybranyPokój, dane.pokoje.size() - 1);
			return dane.pokoje.get(wybranyPokój);
		}
		
		
		public void czas() {
			if (!p.isOnline()) 
				return;
			oznaczRogi();
			oznaczDrzwi();
		}
		public void oznaczRogi() {
			oznaczRogi(dane.róg1, dane.róg2, Color.LIME);
			oznaczRogi(dane.róg2, dane.róg1, Color.RED);
 		}
		private void oznaczRogi(Location loc1, Location loc2, Color kolor) {
			for (int i=0; i < 3; i++) {
				Location loc = loc2.clone();
				if (i != 0) loc.setX(loc1.getX());
				if (i != 1) loc.setY(loc1.getY());
				if (i != 2) loc.setZ(loc1.getZ());
				Func.particle(loc1, loc, .3, (sloc, __) -> Func.particle(p, sloc, 1, 0, 0, 0, 0, kolor, 1));
			}
		}
		public void oznaczDrzwi() {
			if (wybranyPokój != -1)
				pokój().drzwi.forEach(loc -> {
					BlockData blok = loc.getBlock().getBlockData().clone();
					loc.getBlock().setType(Material.GLASS, false);
					Func.particle(p, loc, 5, .1, .1, .1, 0, Color.GRAY, 2);
					Func.opóznij(5, () -> loc.getBlock().setBlockData(blok, false));
				});
		}
		
		public void zainicjujMoby() {
			pokój().moby.forEach(this::zainicjujMoba);
		}
		public void zainicjujMoba(Mob mob) {
			MythicMob mm = dajMythicMob(mob.nazwaMoba);
			if (mm == null)
				p.sendMessage(prefix + Func.msg("MythicMob %s nie istnieje", mob.nazwaMoba));
			else {
				Entity e = mob.loc.getWorld().spawnEntity(mob.loc, Func.StringToEnum(EntityType.class, mm.getEntityType()));
				e.setInvulnerable(true);
				e.setGlowing(true);
				if (e instanceof Attributable) {
					Attributable ae = (Attributable) e;
					ae.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
					ae.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0);
				}
				if (e instanceof LivingEntity) {
					LivingEntity le = (LivingEntity) e;
					le.getEquipment().setHelmet(new ItemStack(Material.STONE_BUTTON));
					le.setCanPickupItems(false);
					le.setAI(false);
				}
			}
		}
		public void usuńMoby() {
			moby.forEach(Entity::remove);
			moby.clear();
		}
		
		public void odświeżPokój() {
			wybierzPokój(wybranyPokój);
		}
		public void wybierzPokój(int pokój) {
			usuńMoby();
			wybranyPokój = pokój;
			if (pokój >= 0)
				zainicjujMoby();
		}
		
		public void wyświetl() {
			Napis n = new Napis("\n\n\n§9§lDungeon\n\n");
			
			n.dodaj("§6Rogi§8: ");
			n.dodaj(new Napis("§a" + Func.locBlockToString(dane.róg1), "§bKliknij aby ustawić", "/dungi edytor róg1"));
			n.dodaj(" ");
			n.dodajEnd(new Napis("§c" + Func.locBlockToString(dane.róg2), "§bKliknij aby ustawić", "/dungi edytor róg2"));

			n.dodaj("§6Spawn graczy§8: ");
			n.dodajEnd(new Napis("§e" + Func.locToString(dane.spawnGraczy), "§bKliknij aby ustawić", "/dungi edytor sprawnGracze"));
			n.dodaj("§6Spawn Bossa§8: ");
			n.dodajEnd(new Napis("§e" + Func.locToString(dane.spawnBossa), "§bKliknij aby ustawić", "/dungi edytor spawnBoss"));
			
			n.dodaj("§6Nazwa Bossa§8: ");
			n.dodajEnd(new Napis(dane.nazwaBossa == null ? "§cbrak" : ("§e" + dane.nazwaBossa), "§bKliknij aby ustawić", "/dungi edytor nazwa >> "));
			
			n.dodaj("§6keep inv §9/ §6życia graczy§8: ");
			n.dodaj(new Napis(dane.keepInv ? "§aTak" : "§cNie", "§bKliknij aby zmienić", "/dungi edytor keepInv"));
			n.dodaj("§9 / ");
			n.dodajEnd(new Napis("§e" + String.valueOf(dane.życiaGraczy), "§bKliknij aby ustawić", "/dungi edytor życia >> "));
			
			n.dodaj("§6Limit graczy§8: ");
			n.dodaj(new Napis("§e" + String.valueOf(dane.minGracze), "§bKliknij aby ustawić", "/dungi edytor minGracze >> "));
			n.dodaj("§7 - ");
			n.dodajEnd(new Napis(String.valueOf(dane.maxGracze == -1 ? ("§eBrak górnego limitu §7(-1)") : dane.maxGracze), "§bKliknij aby ustawić", "/dungi edytor maxGracze >> "));
			
			n.dodaj("§6Broadcasty Zwycięstwo§9/§6Porażka§9/§6Start§8: ");
			n.dodaj(new Napis(dane.broadcastZwycięstwa ? "§aTak" : "§cNie", "§bKliknij aby zmienić", "/dungi edytor bcz"));
			n.dodaj("§9/");
			n.dodaj(new Napis(dane.broadcastPorażki    ? "§aTak" : "§cNie", "§bKliknij aby zmienić", "/dungi edytor bcp"));
			n.dodaj("§9/");
			n.dodajEnd(new Napis(dane.broadcastStartu  ? "§aTak" : "§cNie", "§bKliknij aby zmienić", "/dungi edytor bcs"));
			
			n.dodaj("§6Cooldown §7(minuty)§8: ");
			n.dodajEnd(new Napis("§e" + String.valueOf(dane.minutyCooldownu), "§bKliknij aby ustawić", "/dungi edytor cooldown >> "));
			
			n.dodaj("§6Wymagane Dungeony§8: §a[§e");
			for (int i=0; i < dane.wymaganeDungi.size(); i++) {
				if (i > 0)
					n.dodaj("§d, ");
				n.dodaj(new Napis("§e" + dane.wymaganeDungi.get(i), "§bKliknij aby edytować", "/dungi edytor dungedit " + i + " >> "));
				n.dodaj(new Napis("§c{X}", "§bKliknij aby usunąć element", "/dungi edytor dungrem " + i));
			}
			n.dodaj("§a]");
			n.dodajEnd(new Napis("§a[+]", "§bKliknij aby dodać wymagany Dungeon", "/dungi edytor dungadd >> "));
			
			n.dodaj("§6Priorytet§8: ");
			n.dodajEnd(new Napis("§e" + String.valueOf(dane.priorytet), "§bKliknij aby ustawić", "/dungi edytor priorytet >> "));
			
			n.dodaj("§6Ikona§8: §e");
			n.dodajEnd(Napis.item(dane.ikona).hover("§bKliknij aby ustawić").clickEvent("/dungi edytor ikona"));
			
			n.dodaj("§6Przepustka§8: ");
			n.dodajEnd(Napis.item(dane.przepustka).hover("§bKliknij aby ustawić").clickEvent("/dungi edytor przepustka"));
			
			n.dodaj("§6Pokój§8: §a[");
			for (int i = 0; i < dane.pokoje.size(); i++) {
				if (i > 0)
					n.dodaj("§d, §e");
				n.dodaj(new Napis((wybranyPokój == i ? "§5" : "§e") + (i + 1), "§bKliknij aby wybrać pokój", "/dungi edytor roomsel " + i));
				n.dodaj(" ");
				n.dodaj(new Napis("§c{X}", "§bKliknij aby usunąć", "/dungi edytor roomrem " + i));
			}
			n.dodaj("§a] ");
			n.dodajEnd(new Napis("§a[+]", "§bKliknij aby dodać pokój", "/dungi edytor roomadd"));
			
			if (wybranyPokój != -1) {

				n.dodaj("§6Drzwi§8: §a[");
				for (int i=0; i < pokój().drzwi.size(); i++) {
					if (i > 0)
						n.dodaj("§d, ");
					n.dodaj(new Napis(Func.locBlockToString(pokój().drzwi.get(i)), "§bKliknij aby namierzyć", "/dungi edytor mobradar " + i));
					n.dodaj(" ");
					n.dodaj(new Napis("§c{X}", "§bKliknij aby usunąć drzwi", "/dungi edytor drzwirem " + i));
				}
				n.dodaj("§a] §8(" + pokój().drzwi.size() + ")");
				n.dodajEnd(new Napis("§a[+]", "§bKliknij aby dodać drzwi", "/dungi edytor drzwiadd"));
				

				n.dodaj("§6Moby§8: §a[");
				for (int i=0; i < pokój().moby.size(); i++) {
					if (i > 0)
						n.dodaj("§d, ");
					n.dodaj(new Napis(pokój().moby.get(i).nazwaMoba, "§bKliknij aby namierzyć", "/dungi edytor drzwiradar " + i));
					n.dodaj(" ");
					n.dodaj(new Napis("§c{X}", "§bKliknij aby usunąć moba", "/dungi edytor mobrem " + i));
				}
				n.dodaj("§a] §8(" + pokój().moby.size() + ")");
				n.dodajEnd(new Napis("§a[+]", "§bKliknij aby dodać moba", "/dungi edytor mobadd >> "));
			}
			
			n.dodaj("\n\n");
			n.dodaj(new Napis("§a[zatwierdz]", "§bKliknij aby zapisać i zakończyć edycje Dungeonu", "/dungi edytor zatwierdz"));
			n.dodaj("§8 ----- ");
			n.dodaj(new Napis("§4[Anuluj]", "§bKliknij aby zakończyć edycje Dungeonu bez zapisywania", "/dungi edytor anuluj"));
			n.dodajEnd("\n");
			
			n.wyświetl(p);
		}
	
		public void onCommand(String[] args) {
			// args[0] == edytor
			
			Supplier<ItemStack> hand = () -> p.getEquipment().getItemInMainHand();
			
			if (args.length > 1)
				switch (args[1]) {
				case "mobradar": p.teleport(pokój().moby.get(Func.Int(args[2])).loc); break;
				case "mobrem": pokój().moby.remove(Func.Int(2)); odświeżPokój(); break;
				case "mobadd": 
					if (args.length == 5) {
						Mob mob = Func.utwórz(Mob.class);
						mob.nazwaMoba = args[4];
						mob.loc = p.getLocation();
						pokój().moby.add(mob);
						zainicjujMoba(mob);
					}
					break;
				case "drzwiradar": p.teleport(pokój().drzwi.get(Func.Int(args[2]))); break;
				case "drzwiadd": pokój().drzwi.add(p.getLocation()); break;
				case "drzwirem": pokój().drzwi.remove(Func.Int(2)); break;
				
				case "róg1": 		 dane.róg1 		  = p.getLocation(); 												break;
				case "róg2": 		 dane.róg2 		  = p.getLocation(); 												break;
				case "spawnBoss": 	 dane.spawnBossa  = p.getLocation(); 												break;
				case "sprawnGracze": dane.spawnGraczy = p.getLocation(); 												break;
				case "keepInv": dane.keepInv 		 	 = !dane.keepInv; 												break;
				case "bcz": 	dane.broadcastZwycięstwa = !dane.broadcastZwycięstwa; 									break;
				case "bcp": 	dane.broadcastPorażki 	 = !dane.broadcastPorażki; 										break;
				case "bcs": 	dane.broadcastStartu 	 = !dane.broadcastStartu; 										break;
				case "przepustka": dane.przepustka = hand.get().getType().isAir() ? null : hand.get().clone(); 			break;
				case "ikona": 	  if (!hand.get().getType().isAir()) dane.ikona = hand.get().clone(); 					break;
				case "nazwa": 	  if (args.length == 4) dane.nazwaBossa = args[3]; 										break;
				case "życia": 	  if (args.length == 4) dane.życiaGraczy 	 = Func.Int(args[3], dane.życiaGraczy); 	break;
				case "minGracze": if (args.length == 4) dane.minGracze 		 = Func.Int(args[3], dane.minGracze); 		break;
				case "maxGracze": if (args.length == 4) dane.maxGracze 		 = Func.Int(args[3], dane.maxGracze); 		break;
				case "priorytet": if (args.length == 4) dane.priorytet 		 = Func.Int(args[3], dane.priorytet); 		break;
				case "cooldown":  if (args.length == 4) dane.minutyCooldownu = Func.Int(args[3], dane.minutyCooldownu); break;
				case "dungedit":  if (args.length == 5) dane.wymaganeDungi.set(Func.Int(args[2]), args[4]); 			break;
				case "dungadd":   if (args.length == 4) dane.wymaganeDungi.add(args[3]); 								break;
				case "dungrem": dane.wymaganeDungi.remove(Func.Int(args[2])); 											break;
				case "roomsel": wybierzPokój(Func.Int(args[2], wybranyPokój)); 											break;
				case "roomadd": dane.pokoje.add(Func.utwórz(Pokój.class)); 												break;
				case "roomrem":
					int room = Func.Int(args[2]);
					dane.pokoje.remove(room);
					if (room <= wybranyPokój)
						if (room == wybranyPokój || dane.pokoje.size() == 1)
							wybierzPokój(-1);
						else
							wybierzPokój(wybranyPokój - 1);
					break;
				case "zatwierdz":
					try {
						if (dane.nazwaBossa == null) 
							throw new Error("Nazwa bossa musi być wpisana");
						if (dane.maxGracze < dane.minGracze)
							throw new Error("min gracze muszą być większe od max gracze");
						if (dane.róg1.distance(dane.róg2) < 5)
							throw new Error("Arena jest za mała");
						if (dane.minutyWalki < 1)
							throw new Error("Za którki czas walki");
						if (dane.ikona == null || dane.ikona.getType().isAir())
							throw new Error("Ikona jest wymagana");
						
						for (int i=0; i < dane.pokoje.size(); i++)
							if (dane.pokoje.get(i).moby.isEmpty())
								throw new Error(Func.msg("W pokoju nr %s nie ma żadnych mobów", i));
						
						
						new Config("configi/Dungi").ustaw_zapisz(dane.nazwaBossa, dane);
					} catch (Error e) {
						p.sendMessage(prefix + e.getMessage());
						return;
					}
					break;
				case "anuluj":
					edytory.remove(p.getName());
					break;
				}
			wyświetl();
		}
	}
	
	public static final String prefix = Func.prefix("Dungi");
	public static final String permEdytor = Func.permisja("dungi.edytor");
	static final String metaArenaMob = "mimiDungeonyMob";

	public Dungi() {
		super("dungi", "/dungi dołącz <arena>");
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
	static final Set<Arena> areny = new HashSet<>();
	
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
		Func.wykonajDlaNieNull(arena(ev.getMob().getEntity().getBukkitEntity()), arena -> {
			if (arena.walkaZBossem) {
				arena.zabił(ev.getKiller());
				arena.zwycięstwo();
			} else
				arena.sprawdzNastępnyPokój();
		});
	}
	@EventHandler
	public void despawnBossa(MythicMobDespawnEvent ev) {
		Func.wykonajDlaNieNull(arena(ev.getMob().getEntity().getBukkitEntity()), arena -> {
			if (arena.walkaZBossem)
				arena.zdespawnowany(ev);
			else
				arena.sprawdzNastępnyPokój();
		});
	}
	
	static Panel panel = new Panel(true);
	
	public void otwórzPanel(Player p) {
		List<Gracz> gracze = Func.wykonajWszystkim(Party.dajGraczyParty(p), Gracz::wczytaj);
		
		List<TriKrotka<String, ArenaDane, Long>> dostępne = new ArrayList<>();
		
		mapaArenDanych.forEach((str, dane) -> {
			for (Gracz g : gracze)
				if (!dane.maWstęp(g))
					return;
			AtomicLong cooldown = new AtomicLong(0L);
			
			gracze.forEach(g -> cooldown.set(Math.max(cooldown.get(), g.dungiCooldown.getOrDefault(dane.nazwaBossa, 0L))));
			
			Func.insort(new TriKrotka<>(str, dane, cooldown.get()), dostępne, krotka -> (double) krotka.b.priorytet);
		});
		
		Inventory inv = panel.stwórz(null, Func.potrzebneRzędy(dostępne.size()), "&4&lDungi", Baza.pustySlotCzarny);
		
		int[] slot = new int[] {0};
		dostępne.forEach(krotka -> {
			ItemStack item = krotka.b.ikona.clone();
			
			int zabito = Gracz.wczytaj(p).dungiLicznik.getOrDefault(krotka.b.nazwaBossa, 0);
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
		
		p.openInventory(inv);
	}
	
	public static Arena arena(Entity mob) {
		if (mob.hasMetadata(metaArenaMob))
			return (Arena) mob.getMetadata(metaArenaMob).get(0).value();
		return null;
	}
	public static MythicMob dajMythicMob(String mob) {
		return MythicMobs.inst().getMobManager().getMythicMob(mob);
	}
	
	
	EdytorOgólny<ArenaDane> edytor = new EdytorOgólny<>("dungi", ArenaDane.class);
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
		if (!(sender instanceof Player))
			throwMsg("tylkoGracz");
		
		if (args.length >= 1 && args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor)) {
			//return edytor.wymuśConfig_onCommand(prefix, "configi/Dungi", sender, label, args);
			if (!ArenaEdit.edytory.containsKey(sender.getName()))
				ArenaEdit.edytory.put(sender.getName(), new ArenaEdit((Player) sender, args.length >= 2 ? args[1] : null));
			ArenaEdit.edytory.get(sender.getName()).onCommand(args);
			return true;
		}
		

		otwórzPanel((Player) sender);
		
		return true;
	}
	
	@Override
	public int czas() {
		ArenaEdit.edytory.values().forEach(ArenaEdit::czas);
		return 10;
	}
	
	@Override
	public void przeładuj() {
		Map<String, ArenaDane> stare = new HashMap<>();
		mapaArenDanych.forEach(stare::put);
		mapaArenDanych.clear();
		Config config = new Config("configi/Dungi");
		config.mapa(false).forEach((__, obj) -> {
			ArenaDane dane = (ArenaDane) obj;
			
			Func.wykonajDlaNieNull(stare.remove(dane.nazwaBossa), stary -> {
				if (stary.wczytajZ(dane))
					mapaArenDanych.put(dane.nazwaBossa, stary);
				else
					stare.put(dane.nazwaBossa, stary);
			}, () -> mapaArenDanych.put(dane.nazwaBossa, dane));
			
			if (dajMythicMob(dane.nazwaBossa) == null)
				Main.warn(prefix + "Nie odnaleziono bossa " + dane.nazwaBossa);
		});
		stare.values().forEach(dane -> {
			Lists.newArrayList(areny).forEach(arena -> {
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

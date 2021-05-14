package me.jomi.mimiRPG.MineZ;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.api._WorldGuard;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Bazy extends Komenda implements Listener, Przeładowalny, Zegar {
	public static class Gildia extends Mapowany {
		public static final String prefix = Func.prefix("Gildia");
		static final Config config = new Config("configi/Gildie");
		@Mapowane List<String> gracze;
		@Mapowane String przywódca;
		@Mapowane String nazwa;
		@Mapowane String tag;

		void zapisz() {
			config.ustaw_zapisz(nazwa, this);
		}
		static Gildia wczytaj(String nazwa) {
			if (nazwa == null) return null;
			return (Gildia) config.wczytaj(nazwa);
		}
		static Gildia stwórz(String nazwa, String tag, Player p) {
			String przywódca = p.getName();
			Gracz g = Gracz.wczytaj(przywódca);
			
			if (g.gildia != null) {
				p.sendMessage(prefix + "Nie możesz utworzyć nowej gildi puki nie opuścisz aktualnej");
				return null;
			}
			if (nazwa.contains(".")) {
				p.sendMessage(prefix + "Nie możesz utworzyć gildi o tej nazwie");
				return null;
			}
			Gildia gildia = Func.utwórz(Gildia.class);
			gildia.przywódca = przywódca;
			gildia.nazwa = nazwa;
			gildia.ustawTag(p, tag);
			gildia.zapisz();
			
			g.gildia = nazwa;
			g.zapisz();
			
			return gildia;
		}
			
		static boolean istnieje(String nazwa) {
			for (String klucz : config.klucze())
				if (klucz.equalsIgnoreCase(nazwa))
					return true;
			
			return false;
		}
		static boolean istniejeTag(String tag) {
			for (String klucz : config.klucze()) {
				try {
					Gildia g = (Gildia) config.wczytaj(klucz);
					if (tag.equals(g.tag))
						return true;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return false;
		}
		
		boolean ustawTag(Player p, String tag) {
			if (istniejeTag(tag)) {
				p.sendMessage(prefix + "Ten tag już jest zajęty");
				return false;
			}
			
			this.tag = tag;
			zapisz();
			
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, this::odświeżTag);
			
			wyświetlCzłonkom(prefix + Func.msg("%s ustawił tag gildi %s na %s", p.getName(), nazwa, tag()));
			return true;
		}
		
		String tag() {
			if (tag == null)
				return "";
			return Func.koloruj(Main.ust.wczytaj("Gildie.tag", "§0[§2<tag>§0]").replace("<tag>", tag));
		}
		
		// wymagane używanie asynchroniczne
		void odświeżTag() {
			String tag = tag();
			Consumer<String> cons = nick -> Main.chat.setPlayerSuffix(null, Func.graczOffline(nick), tag);
			cons.accept(przywódca);
			for (String gracz : gracze)
				cons.accept(gracz);
		}
		
		
		static void przeładuj() {
			if (Main.chat == null) {
				Main.warn("Brak ekonomi, nie można przeładować suffixów gildi");
				return;
			}
			new Thread(() -> {
				for (String nazwa : config.klucze())
					try {
						Gildia g = (Gildia) config.wczytaj(nazwa);
						g.odświeżTag();
					} catch (Throwable e) {
						e.printStackTrace();
					}
			}).start();
		}
		
		void dołącz(Player p) {
			Gracz g = Gracz.wczytaj(p.getName());
			
			dodajRegiony(g);
			
			gracze.add(p.getName());
			zapisz();
			
			g.gildia = nazwa;
			g.zapisz();
			
			if (Main.chat != null)
				Main.chat.setPlayerSuffix(null, p, tag());
		}
		void opuść(String nick) {
			Gracz g = Gracz.wczytaj(nick);
			
			gracze.remove(nick);
			zapisz();
			
			g.gildia = null;
			g.zapisz();
			
			zapomnijRegiony(g);
			
			if (nick.equals(przywódca))
				if (gracze.size() == 0)
					config.ustaw_zapisz(nazwa, null);
				else {
					przywódca = gracze.remove(0);
					zapisz();
				}
			
			
			if (Main.chat != null)
				new Thread(() -> Main.chat.setPlayerSuffix(null, Func.graczOffline(g.nick), "")).start();;
		}
		
		void przekażLidera(String członek) {
			if (!gracze.remove(członek))
				throw new Error("Członek nie należy do gildi");
			wyświetlCzłonkom(prefix + Func.msg("%s przekazał dowodzenie %s", przywódca, członek));
			gracze.add(przywódca);
			przywódca = członek;
			zapisz();
		}
		
		void dodajRegiony(Gracz g) {
			wykonajNaRegionach(g, DefaultDomain::addPlayer);
		}
		void zapomnijRegiony(Gracz g) {
			wykonajNaRegionach(g, DefaultDomain::removePlayer);
		}
				
 		private void wykonajNaRegionach(Gracz g, BiConsumer<DefaultDomain, String> bic) {
			Consumer<String> cons = członek -> {
				Gracz gracz = Gracz.wczytaj(członek);
				if (gracz.baza == null) return;
				ProtectedRegion region = gracz.baza.region;
				DefaultDomain members = region.getMembers();
				bic.accept(members, g.nick);
				region.setMembers(members);			
			};
			for (String członek : gracze)
				cons.accept(członek);
			cons.accept(przywódca);
			
			
			if (g.baza == null) return;
			ProtectedRegion region = g.baza.region;
			DefaultDomain members = region.getMembers();
			for (String członek : gracze)
				bic.accept(members, członek);
			bic.accept(members, przywódca);
			region.setMembers(members);
		}
		
		void napiszDoCzłonków(Player kto, String msg) {
			Set<Player> set = Sets.newConcurrentHashSet();
			
			Consumer<String> dodaj = nick -> {
				Player p = Bukkit.getPlayer(nick);
				if (p != null) 
					set.add(p);
			};
			
			for (String nick : gracze) 
				dodaj.accept(nick);
			dodaj.accept(przywódca);
			
			String imie = "§7" + kto.getName();
			if (kto.getName().equalsIgnoreCase(przywódca))
				imie = "§e^" + imie + "§e^";
			msg = "§3[§5Chat §d" + nazwa + "§3] " + imie + "§8: §f" + Func.koloruj(msg);
			Main.log(msg);
			wyświetlCzłonkom(msg);
		}
		void wyświetlCzłonkom(String msg) {
			Consumer<String> wyświetl = nick -> {
				Player p = Bukkit.getPlayer(nick);
				if (p != null) 
					p.sendMessage(msg);
			};
			
			for (String nick : gracze) 
				wyświetl.accept(nick);
			wyświetl.accept(przywódca);
			
		}
	}
	public static class Baza extends Mapowany {
		ProtectedCuboidRegion region;
		World świat;
		
		@Mapowane String nazwa;
		@Mapowane int poziom = -1;
		@Mapowane String nazwaŚwiata;
		
		
		static int getMinY() {
			return Bazy.config.wczytaj("ustawienia.najniższyh poziom bazy", 50);
		}
		
		static int odświeżConfigiGraczy() {
			int licz = 0;

			Pattern patern = Pattern.compile("baza-?\\d+x-?\\d+y-?\\d+z");
			for (World world : Bukkit.getWorlds())
				for (ProtectedRegion region : regiony.get(BukkitAdapter.adapt(world)).getRegions().values())
					try {
						if (patern.matcher(region.getId()).find()) {
							Baza baza = new Baza();
							baza.nazwa = region.getId();
							baza.nazwaŚwiata = world.getName();
							baza.poziom = config.wczytaj("Naprawa.p" + (region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()), -12345);
							if (baza.poziom == -12345)
								Main.error("Brak odpowiednika dla " + region.getMaximumPoint().getBlockX() + " - " + region.getMinimumPoint().getBlockX() + " id:" + region.getId());
							for (String owner : region.getOwners().getPlayers()) {
								Gracz g = Gracz.wczytaj(owner);
								g.baza = baza;
								g.zapisz();
							}
							licz++;
						}
					} catch (Throwable e) {
						Main.error(region.getId());
						e.printStackTrace();
					}
			
			for (Entry<String, Object> en : Gildia.config.mapa(false).entrySet()) {
				try {
					Gildia gildia = (Gildia) en.getValue();
					
					 Gracz g = Gracz.wczytaj(gildia.przywódca);
					 g.gildia = gildia.nazwa;
					 g.zapisz();
					for (String nick : gildia.gracze) {
						g = Gracz.wczytaj(nick);
						g.gildia = gildia.nazwa;
						g.zapisz();
					}
				} catch (Throwable e) {
					Main.error(en, en.getKey(), en.getValue());
					e.printStackTrace();
				}
			}
			
			
			
			return licz;
		}
		
		Baza(int x, int y, int z, int dx, int dy, int dz, World świat, Player właściciel) {
			Player p = właściciel;
			this.świat = świat;
					
			String nazwaBazy = String.format("baza%sx%sy%sz", x, y, z);
			
			region = new ProtectedCuboidRegion(
					nazwaBazy,
					BlockVector3.at(x+dx, y+dy, z+dz),
					BlockVector3.at(x-dx, Math.max(getMinY(), y-Bazy.config.wczytaj("ustawienia.kraki w dół baz", 1)), z-dz)
					);
			Bazy.regiony.get(BukkitAdapter.adapt(świat)).addRegion(region);
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(p.getName());
			region.setOwners(owners);
			region.setPriority(Bazy.config.wczytajInt("ustawienia.prority baz"));
			region.setFlag(_WorldGuard.flagaCustomoweMoby, "brak");
			region.setFlag(_WorldGuard.flagaStawianieBaz, StateFlag.State.DENY);
			region.setFlag(_WorldGuard.flagaC4, 		  StateFlag.State.ALLOW);
			String msgWejścia = config.wczytaj("ustawienia.msg wejścia", "§6Wszedłeś na teren bazy gracza {gracz}");
			region.setFlag(Flags.GREET_MESSAGE, Func.koloruj(msgWejścia.replace("{gracz}", p.getName())));
			String msgWyjścia = config.wczytaj("ustawienia.msg wyjścia", "§6Wyszedłeś z terenu bazy gracza {gracz}");
			region.setFlag(Flags.FAREWELL_MESSAGE, Func.koloruj(msgWyjścia.replace("{gracz}", p.getName())));
			
			
			nazwaŚwiata = świat.getName();
			nazwa = nazwaBazy;
			
			Gracz g = Gracz.wczytaj(p.getName());
			
			g.baza = this;
			
			Func.wykonajDlaNieNull(Gildia.wczytaj(g.gildia), gildia -> gildia.dodajRegiony(g));
			
			g.zapisz();
			
			// ognisko to rdzeń bazy, zniszczenie ogniska = usunięcie bazy
			Func.opóznij(1, () -> new Location(świat, x, y, z).getBlock().setType(Material.CAMPFIRE));
		}		
		static Baza wczytaj(Player p, int x, int y, int z, World świat, ItemStack item, BlockPlaceEvent ev, Map<String, Object> mapa) {
			if (mapa == null) return null;
			Gracz g = Gracz.wczytaj(p.getName());
			if (g.baza != null) {
				p.sendMessage(prefix + "Nie możesz postawić więcej baz");
				Bazy.inst.blokuj = true;
				return null;
			}
			if (y < getMinY()) {
				p.sendMessage(prefix + "Nie możesz postawić bazy tak nisko");
				Bazy.inst.blokuj = true;
				return null;
			}
			if (!bypass.contains(p.getName())) {
				int czas = Main.ust.wczytaj("Bazy.deley stawiania baz", 12*60*60);
				czas = czas - (int) ((System.currentTimeMillis() / 1000) - g.BazaOstatnieStawianie);
				if (czas > 0) {
					Func.powiadom(p, prefix + "Musisz poczekać jeszcze " + Func.czas(czas) + " zanim postawisz następną bazę");
					Bazy.inst.blokuj = true;
					return null;
				}
			}
			
			int dx = (int) mapa.get("dx");
			int dy = (int) mapa.get("dy");
			int dz = (int) mapa.get("dz");
			ProtectedCuboidRegion region = new ProtectedCuboidRegion(
					"mimiBazaTestowana",
					BlockVector3.at(x+dx, y+dy, z+dz),
					BlockVector3.at(x-dx, y-1,  z-dz)
					);
			if (Bazy.regiony.get(BukkitAdapter.adapt(świat))
					.getApplicableRegions(region)
					.testState(null, _WorldGuard.flagaStawianieBaz)) {
				g.BazaOstatnieStawianie = (int) (System.currentTimeMillis() / 1000);
				g.zapisz();
				return new Baza(x, y, z, dx, dy, dz, świat, ev.getPlayer());
			}
			Bazy.inst.blokuj = true;
			ev.getPlayer().sendMessage(Bazy.prefix + "Nie możesz tu postawić swojej bazy");
			return null;
		}
		@SuppressWarnings("unchecked")
		boolean możnaUlepszyć() {
			int xz = (int) ((List<Map<String, Object>>) config.wczytaj("ulepszenia bazy")).get(poziom + 1).get("kratki");
			ProtectedCuboidRegion regionKontrolny = new ProtectedCuboidRegion(
					"mimiBazaUlepszeniowoTestowana",
					region.getMaximumPoint().add(xz, xz, xz),
					region.getMinimumPoint().add(-xz, 0, -xz)
					);
			region.setFlag(_WorldGuard.flagaStawianieBaz, StateFlag.State.ALLOW);
			boolean w = Bazy.regiony.get(BukkitAdapter.adapt(świat))
					.getApplicableRegions(regionKontrolny)
					.testState(null, _WorldGuard.flagaStawianieBaz);
			region.setFlag(_WorldGuard.flagaStawianieBaz, StateFlag.State.DENY);
			return w;
		}

		public Baza() {}// konstruktor dla Mapowanego
		@Override
		protected void Init() throws NiepoprawneDemapowanieException {
			świat = Bukkit.getWorld(nazwaŚwiata);
			region = (ProtectedCuboidRegion) Func.regiony(świat).getRegion(nazwa);
			if (region == null)
				throw new NiepoprawneDemapowanieException();
		}
		
		static Baza wczytaj(World świat, ProtectedRegion region) {
			if (świat == null) return null;
			if (!(region instanceof ProtectedCuboidRegion)) return null;
			Pattern patern = Pattern.compile("baza-?\\d+x-?\\d+y-?\\d+z");
			if (patern.matcher(region.getId()).find())
				for (String owner : region.getOwners().getPlayers()) {
					Baza baza = Gracz.wczytaj(owner).baza;
					if (baza != null)
						return baza;
				}
			return null;
		}
		
		static final HashMap<String, Integer> mapaRaidów = new HashMap<>();
		void rajdowana(BlockBreakEvent ev, Baza baza) {
			if (mapaRaidów.containsKey(nazwa))
				zrajdowana(ev, baza);
			else {
				Func.opóznij(1, () -> ev.getBlock().setType(Material.SOUL_CAMPFIRE));
				mapaRaidów.put(nazwa, Func.opóznij(config.wczytaj("czas rajdowania", 5) * 20 + 1, () -> {
					mapaRaidów.remove(nazwa);
					ev.getBlock().setType(Material.CAMPFIRE);
				}));
			}
		}
		private void zrajdowana(BlockBreakEvent ev, Baza baza) {
			Bukkit.getScheduler().cancelTask(mapaRaidów.remove(nazwa));
		
			Func.opóznij(1, () -> ev.getBlock().setType(Material.AIR));
			
			ev.getPlayer().sendMessage(prefix + Func.msg("Zniszczyłeś baze gracza %s", "§e" + Func.listToString(
					baza.region.getOwners().getPlayers(), 0, "§6, §e")));
			for (String owner : baza.region.getOwners().getPlayers()) {
				Player p = Bukkit.getPlayer(owner);
				if (p != null) Func.powiadom(prefix, p, "%s zniszczył twoją baze!", ev.getPlayer().getDisplayName());
				Main.log(prefix + Func.msg("%s zniszczył baze gracza %s", ev.getPlayer().getName(), p == null ? owner : p.getName()));
			}
			
			usuń();
		}
		
		void usuń() {
			for (String owner : region.getOwners().getPlayers()) {
				Gracz g = Gracz.wczytaj(owner);
				
				Func.wykonajDlaNieNull(Gildia.wczytaj(g.gildia), gildia -> gildia.zapomnijRegiony(g));
				
				if (g.baza != null && g.baza.nazwa.equals(nazwa)) {
					g.baza = null;
					g.zapisz();
				}
			}
			
			Matcher mat = Pattern.compile("baza(-?\\d+)x(-?\\d+)y(-?\\d+)z").matcher(region.getId());
			if (!mat.find())
				Main.warn("Nieprawidłowe id regionu Bazy: " + region.getId());
			UnaryOperator<Integer> func = i -> Func.Int(mat.group(i));
			Bukkit.getWorld(nazwaŚwiata).getBlockAt(func.apply(1), func.apply(2), func.apply(3)).setType(Material.AIR);
				
				
			ProtectedCuboidRegion nowy = new ProtectedCuboidRegion(
					"zniszczona" + nazwa,
					region.getMinimumPoint(),
					region.getMaximumPoint()
					);
			Bazy.regiony.get(BukkitAdapter.adapt(świat)).addRegion(nowy);
			
			nowy.setFlag(Flags.BLOCK_BREAK, 	StateFlag.State.ALLOW);
			nowy.setFlag(_WorldGuard.flagaC4, 			StateFlag.State.ALLOW);
			nowy.setFlag(_WorldGuard.flagaStawianieBaz, StateFlag.State.DENY);
			nowy.setPriority(region.getPriority() - 1);
			
			Func.regiony(świat).removeRegion(region.getId());
			
			if (atakowana)
				Bukkit.getScheduler().cancelTask(idTasku);
		}
		
		void ulepsz(int ile) {
			ulepsz(region::getMaximumPoint, region::setMaximumPoint, ile, ile);
			ulepsz(region::getMinimumPoint, region::setMinimumPoint, -ile, 0);
		}
		private void ulepsz(Supplier<BlockVector3> supplier, Consumer<BlockVector3> consumer, int xz, int y) {	
			consumer.accept(supplier.get().add(xz, y, xz));
		}
	
		boolean atakowana = false;
		int idTasku;
		void atak() {
			if (atakowana)
				Bukkit.getScheduler().cancelTask(idTasku);
			else
				for (String nick : region.getOwners().getPlayers())
					Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(prefix + "Twoja baza jest §cAtakowana!"));
			
			idTasku = Func.opóznij(config.wczytaj("ustawienia.długość rajdów", 120) * 20, () -> {
					atakowana = false;
					for (String nick : region.getOwners().getPlayers())
						Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(prefix + "Atak na twoją bazę §aminął"));
					});
			
			atakowana = true;
		}
	}
		
	final String permBypass = Func.permisja("bazy.bypass");
	public static final String prefix = Func.prefix("Baza");
	static RegionContainer regiony;
	static Config config = new Config("Bazy");
	
	public static Bazy inst;
	
	public Bazy() {
		super("gildia", null, "g");
		ustawKomende("baza", "/baza [usuń | ulepsz]", null);
		Main.dodajPermisje(permBypass);
		inst = this;
		regiony = WorldGuard.getInstance().getPlatform().getRegionContainer();
	}
	public static boolean warunekModułu() {
		return _WorldGuard.rg != null;
	}
	
	static class Explozja {
		static int _id = 0;
		final int id;
		final List<Block> bloki = Lists.newArrayList();
		final List<Krotka<Block, Material>> kolejka = Lists.newArrayList();
		final List<String> niezniszczalne = config.wczytajListe("c4.niezniszczalne");
		final double zasięg = config.wczytaj("ustawienia.zaiśieg c4", 5d);
		final Location loc;
		Explozja(Location loc) {
			id = _id++;
			this.loc = loc;
		}
		
		private void zniszcz(Block blok, RegionManager regiony) {
			final String mat = blok.getType().toString();
			final String str = config.sekcja("c4").getString(mat);
			
			if (!regiony.getApplicableRegions(Func.locToVec3(blok.getLocation())).testState(null, _WorldGuard.flagaC4))
				return;
			
			if (str != null) {
				final String data = dajDate(blok);
				try {
					blok.setBlockData(Bukkit.createBlockData(Func.StringToEnum(Material.class, str), data), false);
				} catch (IllegalArgumentException e) {
					blok.setType(Func.StringToEnum(Material.class, str));
				}
				
				if (mat.endsWith("_DOOR"))
					kolejka.add(new Krotka<>(
							blok.getLocation().add(0, data.contains("half=upper") ? -1 : 1, 0).getBlock(),
							Material.valueOf(str))
							);
				else if (mat.endsWith("_BED")) {
					UnaryOperator<String> znajdz = co -> {
						String w = data.substring(data.indexOf(co + "=") + co.length() + 1);
						int i = w.indexOf(",");
						if (i == -1) i = w.length()-2;
						return w.substring(0, i);
					};
					
					int x = 0;
					int z = 0;
					switch (znajdz.apply("facing")) {
					case "north": z = -1; break;
					case "south": z = 1;  break;
					case "west":  x = -1; break;
					case "east":  x = 1;  break;
					}

					int i = data.contains("part=foot") ? 1 : -1;
					
					kolejka.add(new Krotka<>(
							blok.getLocation().add(x*i, 0, z*i).getBlock(),
							Material.valueOf(str))
							);
				}
			} else if (!niezniszczalne.contains(mat))
				blok.setType(Material.AIR);
		}
		
		void zakończ() {
			RegionManager regiony = Bazy.regiony.get(BukkitAdapter.adapt(loc.getWorld()));
			ProtectedCuboidRegion _region = new ProtectedCuboidRegion("mimiChwilowaBazaC4",
					Func.locToVec3(loc.clone().add(zasięg, zasięg, zasięg)), Func.locToVec3(loc.clone().add(-zasięg, -zasięg, -zasięg)));
			
			Set<String> wybuchnięte = Sets.newConcurrentHashSet();
			
			for (Block blok : bloki) {
				String str = blok.getLocation().toString();
				if (wybuchnięte.contains(str)) continue;
				wybuchnięte.add(str);
				zniszcz(blok, regiony);
			}
			
			for (Snowball sniezka : loc.getWorld().getEntitiesByClass(Snowball.class))
				if (sniezka.hasMetadata("mimiChwilowaBazaC4"))
					sniezka.remove();
			
			
			for (Krotka<Block, Material> krotka : kolejka)
				if (krotka.a.getType() != krotka.b)
					krotka.a.setBlockData(Bukkit.createBlockData(krotka.b, dajDate(krotka.a)), false);
			
			
			for (ProtectedRegion region : regiony.getApplicableRegions(_region).getRegions())
				Func.wykonajDlaNieNull(Baza.wczytaj(loc.getWorld(), region), Baza::atak);
		}
		
	}
	static String dajDate (Block blok) {
		return blok.getBlockData().getAsString(false).substring(10 + blok.getType().toString().length());
	}	
	@EventHandler
	public void explozja(ExplosionPrimeEvent ev) {
		if (!ev.getEntity().getScoreboardTags().contains("mimiC4"))
			return;
		ev.setCancelled(true);
		
		final ConfigurationSection mapa = config.sekcja("c4");
		if (mapa == null)
			return;
		
		Location loc = ev.getEntity().getLocation();
		final float zasięg = ev.getRadius();
		
		float r = zasięg/3*2;
		loc.getWorld().spawnParticle(Particle.CLOUD, 		loc, (int) zasięg*50, r, r, r, 0);
		loc.getWorld().spawnParticle(Particle.SMOKE_LARGE,	loc, (int) zasięg*20, r, r, r, 0);
		r *= .4;
		loc.getWorld().spawnParticle(Particle.CLOUD, 		loc, (int) zasięg*20, r, r, r, 0);
		loc.getWorld().spawnParticle(Particle.SMOKE_LARGE,	loc, (int) zasięg*15, r, r, r, 0);
		r *= 1.5;
		loc.getWorld().spawnParticle(Particle.FLAME, loc, (int) zasięg*20, r, r, r, .1);
		
		loc.getWorld().playSound(loc, Sound.ENTITY_RAVAGER_STEP, 100, 0);
		
		BiConsumer<Float, Double> uderz = (Zasięg, dmg) -> {
			for (Entity mob : loc.getWorld().getNearbyEntities(loc, Zasięg, Zasięg, Zasięg))
				if (mob instanceof Damageable && !mob.isInvulnerable())
					((Damageable) mob).damage(dmg);
		};
		uderz.accept(zasięg+3,   	4d);
		uderz.accept(zasięg/3*2.5f, 8d);
		uderz.accept(zasięg/3, 		8d);
		
		int ile = config.wczytaj("ustawienia.śnieżki w C4", 50);
		
		Supplier<Double> los = () -> Math.random() * (Func.losuj(.5) ? 1 : -1);
		
		Explozja explozja = new Explozja(loc);
		World w = ev.getEntity().getWorld();
		for (int i=0; i<ile; i++) {
			Snowball s = (Snowball) w.spawnEntity(loc, EntityType.SNOWBALL);
			s.setVelocity(new Vector(los.get(), los.get(), los.get()).multiply(5));
			Func.ustawMetadate(s, "mimiC4Sniezka", explozja);
		}
		
		Func.opóznij(config.wczytaj("ustawienia.ticki snieżek c4", 10), explozja::zakończ);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void __(ProjectileHitEvent ev) {
		if (!ev.getEntity().hasMetadata("mimiC4Sniezka")) return;
		Func.wykonajDlaNieNull(ev.getHitBlock(), b -> {
			Explozja ex = (Explozja) ev.getEntity().getMetadata("mimiC4Sniezka").get(0).value();
			if (ex.zasięg >= b.getLocation().distance(ex.loc))
				ex.bloki.add(b);
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (!(ev.getEntity() instanceof Player)) return;
		Player uderzony = (Player) ev.getEntity();
		
		Player uderzający;
		if (ev.getDamager() instanceof Player)
			uderzający = (Player) ev.getDamager();
		else if (ev.getDamager() instanceof Projectile && ((Projectile) ev.getDamager()).getShooter() instanceof Player)
			uderzający = (Player) ((Projectile) ev.getDamager()).getShooter();
		else
			return;
		
		String g1 = Gracz.wczytaj(uderzony).gildia;
		String g2 = Gracz.wczytaj(uderzający).gildia;
		
		if (g1 != null && g1.equals(g2))
			ev.setCancelled(true);
	}
	
	
	int min() {
		return config.wczytajInt("ustawienia.godzinyRajdów.min godz safe");
	}
	int max() {
		return config.wczytajInt("ustawienia.godzinyRajdów.max godz safe");
	}
	boolean możnaRajdować() {
		int aktH = ZonedDateTime.now().getHour();
		int min = min();
		int max = max();
		return !(aktH >= min && aktH < max);
	}
	String rajdowanieMsg() {
		return "Nie możesz rajdować baz w godzinach " + min() + "-" + max();
	}
	
	boolean blokuj;
	@EventHandler(priority = EventPriority.LOWEST)
	public void stawianie(BlockPlaceEvent ev) {
		World świat = ev.getBlock().getWorld();
		ItemStack item = ev.getPlayer().getEquipment().getItemInMainHand();
		int x = ev.getBlock().getX();
		int y = ev.getBlock().getY();
		int z = ev.getBlock().getZ();
		
		if (config.klucze().contains("bazy"))
			for (Entry<String, Object> en : config.sekcja("bazy").getValues(false).entrySet()) {
				Map<String, Object> mapa = ((ConfigurationSection) en.getValue()).getValues(false);
				if (Func.porównaj(Config.item(mapa.get("item")), item)) {
					ev.setCancelled(true);
					
					Runnable zabierzItem = () -> {
						item.setAmount(item.getAmount()-1);
						ev.getPlayer().getEquipment().setItemInMainHand(item);
					};

					// C4
					if (mapa.containsKey("c4")) {
						if (!możnaRajdować()) {
							ev.getPlayer().sendMessage(prefix + rajdowanieMsg());
							return;
						}
						Map<String, Object> mapaC4 = ((ConfigurationSection) mapa.get("c4")).getValues(false);
						
						float zasięg = (float) (double) mapaC4.getOrDefault("zasięg", 1f);
						int czas   	 = (int)   			mapaC4.getOrDefault("czas",   1);
						Location loc = ev.getBlock().getLocation().add(.5, 0, .5);
						
						TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
						tnt.addScoreboardTag("mimiC4");
						tnt.setFuseTicks(czas);
						tnt.setGravity(false);
						tnt.setYield(zasięg);
						tnt.setVelocity(new Vector());
						try { 
							tnt.setCustomName(ev.getItemInHand().getItemMeta().getDisplayName());
						} catch (Exception e) {}
						
						Main.log(prefix + Func.msg("%s postawił c4 na koordynatach %sx %sy %sz", ev.getPlayer().getName(), ev.getBlock().getX(), ev.getBlock().getY(), ev.getBlock().getZ()));
						
						zabierzItem.run();
						return;
					}

					if (Func.multiEquals(ev.getBlockReplacedState().getType(), Material.WATER, Material.LAVA)) return;
					
					// Baza/Schemat
					// jeśli baza nie może być postawiona przez flage -> blokuj = true
					blokuj = false;
					boolean zabierz = Baza.wczytaj(ev.getPlayer(), x, y, z, świat, item, ev,
							((ConfigurationSection) mapa.get("baza")).getValues(false)) != null;
					
					boolean warn = false;
					if (mapa.containsKey("schemat") && !blokuj && 
							Bazy.regiony.get(BukkitAdapter.adapt(świat))
								.getApplicableRegions(BlockVector3.at(x, y, z))
								.testState(_WorldGuard.rg.wrapPlayer(ev.getPlayer()), Flags.BUILD) &&
							(warn = Func.wklejSchemat(Main.path + mapa.get("schemat"), świat, x, y, z)))
								zabierz = true;
					if (warn)
						Main.warn("Nie odnaleziono pliku " + Main.path + mapa.get("schemat") + " schemat z Bazy.yml nie został wybudowany.");
					
					if (zabierz) 
						zabierzItem.run();
					return;
				}
			}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void niszczenie(BlockBreakEvent ev) {
		if (!Func.multiEquals(ev.getBlock().getType(), Material.CAMPFIRE, Material.SOUL_CAMPFIRE)) return;
		
		Baza baza = znajdzBaze(ev.getBlock().getLocation());
		if (baza == null) return;
		
		ev.setCancelled(true);
		
		if (baza.region.getOwners().contains(ev.getPlayer().getName())) {
			Func.powiadom(prefix, ev.getPlayer(), "Nie możesz zniszczyć własnej bazy, jeśli musisz użyj /baza usuń");
			return;
		}
		if (baza.region.getMembers().contains(ev.getPlayer().getName())) {
			Func.powiadom(prefix, ev.getPlayer(), "Nie możesz zniszczyć bazy członka twojej gildi");
			return;
		}
		
		if (!możnaRajdować()) {
			Func.powiadom(prefix, ev.getPlayer(), rajdowanieMsg());
			return;
		}
		
		// spradza czy blok jest solidny (np. ironBlock, dirt, glass, nie kraty, plotki itp)
		// zwraca true jeśli przez blok nie da sie zniszczyć
		Predicate<Location> solidny = loc -> {
			Material mat = loc.getBlock().getType();
			return mat.isOccluding() || Func.multiEquals(mat, Material.GLASS, Material.GLOWSTONE) ||
					mat.toString().contains("_LEAVES");
		};
		
		Location loc = ev.getBlock().getLocation();
		if (
				solidny.test(loc.clone().add(1, 0, 0)) &&
				solidny.test(loc.clone().add(0, 1, 0)) &&
				solidny.test(loc.clone().add(0, 0, 1)) &&
				solidny.test(loc.clone().add(-1, 0, 0)) &&
				solidny.test(loc.clone().add(0, -1, 0)) &&
				solidny.test(loc.clone().add(0, 0, -1))
				)
			podejrzany(ev.getPlayer());
		else
			baza.rajdowana(ev, baza);
	}
	void podejrzany(Player p) {
		for (OfflinePlayer op : Bukkit.getOperators())
			try {
				if (!op.isOnline())
					return;
				Player oop = (Player) op;
				oop.sendMessage(prefix + "§4" + p.getName() + " podejrzany o cheaty/bugi podczas rajdu");
			} catch (Throwable e) {
				Main.error("Bazy.podejrzany()", e, e.getMessage());
			}
		Main.log(prefix + "§4" + p.getName() + " podejrzany o cheaty/bugi podczas rajdu");
		Config config = new Config("Podejrzani o cheaty");
		List<String> lista = config.wczytajListe("podejrzani");
		if (!lista.contains(p.getName())) {
			lista.add(p.getName());
			config.ustaw_zapisz("podejrzani", lista);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void preNiszczenieStawianie(PlayerInteractEvent ev) {
		if (bypass.contains(ev.getPlayer().getName())) return;
		if (!Func.multiEquals(ev.getAction(),
				org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, org.bukkit.event.block.Action.LEFT_CLICK_BLOCK)) return;
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		Baza baza = znajdzBaze(blok.getLocation());
		boolean jego = false;
		if (baza != null) {
			jego = baza.region.getOwners() .contains(ev.getPlayer().getName()) ||
				   baza.region.getMembers().contains(ev.getPlayer().getName());
			ev.setCancelled(!jego);
		}
		
		
		switch (ev.getAction()) {
		case LEFT_CLICK_BLOCK:
			if (Func.multiEquals(blok.getType(), Material.CAMPFIRE, Material.SOUL_CAMPFIRE))
				ev.setCancelled(false);
			break;
		case RIGHT_CLICK_BLOCK:
			if (blok.getType().toString().contains("_BED")) {
				ev.setCancelled(true);
				if (jego) {
					Gracz g = Gracz.wczytaj(ev.getPlayer());
					if (g.łóżkoBazowe == null || g.łóżkoBazowe.distance(blok.getLocation()) > 2)
						ev.getPlayer().sendMessage(prefix + "Ustawiono punkt respawnu");
					g.łóżkoBazowe = blok.getLocation();
					g.zapisz();
				}
			}
			if (baza != null && jego && baza.atakowana && (!blok.getType().isInteractable() || ev.getPlayer().isSneaking())) {
				ev.setCancelled(true);
				ev.getPlayer().sendMessage(prefix + "Nie buduj, teraz Jesteś §cAtakowany!");
				return;
			}
			ItemStack item = ev.getItem();
			if (item == null) return;
			if (config.klucze().contains("bazy"))
				for (Entry<String, Object> en : config.sekcja("bazy").getValues(false).entrySet()) {
					Map<String, Object> mapa = ((ConfigurationSection) en.getValue()).getValues(false);
					if (Func.porównaj(Config.item(mapa.get("item")), item)) {
						if (!blok.getType().isInteractable() && !blok.getType().toString().contains("LEAVES"))
							ev.setCancelled(false);
						break;
					}
				}
			break;
		default:
			break;
		}
	}

	
	public Baza znajdzBaze(Location loc) {
		for (ProtectedRegion region : Func.regiony(loc.getWorld()).getApplicableRegions(Func.locToVec3(loc))) {
			Baza baza = Baza.wczytaj(loc.getWorld(), region);
			if (baza != null)
				return baza;
		}
		return null;
	}
	
	final static Set<String> bypass = Sets.newConcurrentHashSet();
	
	// nick zapraszającego: (zaproszony, czas)
	private final HashMap<String, Krotka<String, Integer>> mapaZaproszeń = new HashMap<>();
	private final int czasZaproszeń = 2*60; // max czas zaproszeń w sekundach
	boolean komendaGildia(Player sender, String[] args) {
		if (args.length < 1) return edytor(sender);
		
		Gracz g = Gracz.wczytaj(sender.getName());
		Gildia gildia = Gildia.wczytaj(g.gildia);
		
		BooleanSupplier maGildie = () -> {
			if (g.gildia == null || g.gildia.isEmpty()) {
				sender.sendMessage(Gildia.prefix + "Nie należysz do żadnej gildi");
				return false;
			}
			return true;
		};
		BooleanSupplier przywódca = () -> {
			if (gildia.przywódca.equals(sender.getName()))
				return true;
			sender.sendMessage(Gildia.prefix + "Tylko przywódca gildi może to zrobić");
			return false;
		};
			
		switch (args[0].toLowerCase()) {
		case "z":
		case "zaproś":
		case "zapros":
			if (args.length < 2) 
				return Func.powiadom(sender, Gildia.prefix + "/gildia zaproś <nick>");
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;
			if (mapaZaproszeń.containsKey(sender.getName()))
				return Func.powiadom(sender, Gildia.prefix + "Poczekaj aż minie poprzednie zaproszenie zanim wyślesz kolejne");
			if (gildia.gracze.size() >= config.wczytaj("max osób w gildi", 4))
				return Func.powiadom(sender, Gildia.prefix + "Osiągnięto już limit członków gildi");
			
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) return Func.powiadom(sender, Gildia.prefix + "Gracz nie jeste online");
			
			Gracz zaproszony = Gracz.wczytaj(p.getName());
			if (!(zaproszony.gildia == null || zaproszony.gildia.isEmpty()))
				return Func.powiadom(sender, Gildia.prefix + Func.msg("%s nalezy już do gildi %s", args[1], zaproszony.gildia));

			if (gildia.gracze.size() >= Main.ust.wczytaj("Gildie.max członkowie", 8))
				return Func.powiadom(sender, Gildia.prefix + "Twoja gildia jest już przepełniona, nie możesz zaprosić więcej osób");
			
			mapaZaproszeń.put(sender.getName(), new Krotka<>(p.getName(), czasZaproszeń));
			
			Napis n = new Napis();
			n.dodaj(Func.msg("%s zaprasza cię do gildi %s ", sender.getName(), gildia.nazwa));
			n.dodaj(new Napis("§a[dołącz]", "§9Kliknij aby dołączyć", "/gildia dołącz " + sender.getName() + " " + gildia.nazwa));
			n.wyświetl(p);
			
			sender.sendMessage(Gildia.prefix + Func.msg("Wysłano zaproszenie dla gracza %s, które wygaśnie za %s", args[1], Func.czas(czasZaproszeń)));
			
			break;
		case "w":
		case "wyrzuć":
		case "wyrzuc":
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;
			if (args.length < 2) 
				return Func.powiadom(sender, Gildia.prefix + "/gildia wyrzuć <nick>");
			if (!gildia.gracze.contains(args[1])) 
				return Func.powiadom(sender, Gildia.prefix + Func.msg("%s nie należy do twojej gildii", args[1]));
			gildia.opuść(args[1]);
			gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s wyrzucił %s z gildi", gildia.nazwa, sender.getName(), args[1]));
			Func.wykonajDlaNieNull(Bukkit.getPlayer(args[1]), gracz ->
					gracz.sendMessage(Gildia.prefix + Func.msg("Zostałeś wyrzucony z gildi %s przez %s", gildia.nazwa, sender.getName())));
			break;
		case "o":
		case "opuść":
		case "opuśc":
		case "opusć":
		case "opusc":
			if (!maGildie.getAsBoolean()) break;
			gildia.opuść(sender.getName());
			gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s opuścił gildię", gildia.nazwa, sender.getName()));
			sender.sendMessage(Gildia.prefix + Func.msg("Opuściłeś gildię %s", gildia.nazwa));
			break;
		case "s":
		case "stwórz":
		case "stworz":
			if (args.length < 3)
				return Func.powiadom(sender, Gildia.prefix + "/gildia stwórz <nazwa> <tag>");
			
			if (args[1].length() > Main.ust.wczytaj("Gildia.nazwa.maksymalna długość", 30))
				return Func.powiadom(sender, Gildia.prefix + "Za długa nazwa gildi");
			
			if (Gildia.istnieje(args[1]))
				return Func.powiadom(sender, Gildia.prefix + Func.msg("gildia %s już istnieje", args[1]));
			
			if (Gildia.istniejeTag(args[2]))
				return Func.powiadom(prefix, sender, "Tag %s jest już zajęty", args[2]);
			
			if (args[2].length() > Main.ust.wczytaj("Gildia.tag.maksymalna długość", 4))
				return Func.powiadom(sender, Gildia.prefix + "Ten tag jest za długi");
			
			Gildia.stwórz(args[1], args[2], sender);
			sender.sendMessage(Gildia.prefix + Func.msg("Gildia %s została utworzona", args[1]));
			break;
		case "n":
		case "napisz":
			if (args.length < 2)
				return Func.powiadom(sender, Gildia.prefix + "Nie podano żadnej wiadomości");
			gildia.napiszDoCzłonków(sender, Func.listToString(args, 1));
			break;
		case "t":
		case "tag":
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;
			
			if (args.length < 2)
				return Func.powiadom(Gildia.prefix, sender, "Nie podano żadnego tagu");
			
			String tag = args[1];
			
			if (tag.length() > Main.ust.wczytaj("Gildia.tag.maksymalna długość", 4))
				return Func.powiadom(sender, Gildia.prefix + "Ten tag jest za długi");
			
			gildia.ustawTag(sender, tag);
			break;
		case "l":
		case "lider":
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;

			if (args.length < 2)
				return Func.powiadom(Gildia.prefix, sender, "Nie podano gracza");
			
			try {
				gildia.przekażLidera(args[1]);
			} catch (Error e) {
				sender.sendMessage(Gildia.prefix + Func.msg("%s nie nalezy do twojej gildi", args[1]));
			}
			break;
		case "dołącz": // musi byc na dole bo nie ma bezpośrednio break
			try {
				String zapraszający = args[1];
				String nazwaGildi = args[2];
				if (!mapaZaproszeń.containsKey(zapraszający))
					return Func.powiadom(sender, Gildia.prefix + "To zaproszenie już nie jest aktualne");
				if (!(g.gildia == null || g.gildia.isEmpty()))
					return Func.powiadom(sender, Gildia.prefix + "Należysz już do gildi");
				Gildia _gildia = Gildia.wczytaj(nazwaGildi);
				if (_gildia == null)
					return Func.powiadom(sender, Gildia.prefix + "Ta gildia już nie istnieje");
				if (_gildia.gracze.size() >= Main.ust.wczytaj("Gildie.max członkowie", 8))
					return Func.powiadom(sender, Gildia.prefix + "Ta gildia jest już pełna");
				_gildia.dołącz(sender);
				_gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s na mocy %s dołączył do gildi", nazwaGildi, zapraszający, sender.getName()));

				mapaZaproszeń.remove(zapraszający);
			} catch (Throwable e) {}
		default:
			return edytor(sender);
		}
		return true;
	}	
	boolean edytor(Player p) {
		Napis n = new Napis();
		
		Gracz g = Gracz.wczytaj(p.getName());
		if (Func.nieNull(g.gildia).isEmpty()) {
			n.dodaj(Gildia.prefix);
			n.dodaj(new Napis("§a[stwórz gildie]\n", "§bWymagana nazwa gildi", "/gildia stwórz ", Action.SUGGEST_COMMAND));
		} else {
			Function<String, Napis> dajNick = nick -> {
				Player gracz = Bukkit.getPlayer(nick);
				if (gracz == null)
					return new Napis("§c" + nick, "§coffline");
				Block b = p.getLocation().getBlock();
				return new Napis("§a" + nick, String.format("§6Koordynaty: §e%sx %sy %sz", b.getX(), b.getY(), b.getZ()));
			};
			Gildia gildia = Gildia.wczytaj(g.gildia);
			n.dodaj("\n\n\n\n\n§9Gracze gildi " + gildia.nazwa + " " + gildia.tag() + ":\n");
			n.dodaj("§e§l- §e^§b");
			n.dodaj(dajNick.apply(gildia.przywódca));
			n.dodaj("§e^\n");
			for (String nick : gildia.gracze) {
				n.dodaj("§e§l- ");
				n.dodaj(dajNick.apply(nick));
				n.dodaj(" ");
				if (p.getName().equals(gildia.przywódca))
					n.dodaj(new Napis("§c[x]", "§cWyrzuć", "/gildia wyrzuć " + nick));
				n.dodaj("\n");
			}
			n.dodaj(new Napis("§c[opuść]", "§cKliknij aby opuść gildie", "/gildia opuść"));
			n.dodaj("§a §b §c §d ");
			n.dodaj(new Napis("§6[msg]", "§bKliknij aby wysłać wiadomość do gildi", ClickEvent.Action.SUGGEST_COMMAND, "/gildia n "));
			n.dodaj("\n\n");
		}
		
		n.wyświetl(p);
		
		return true;
	}
	boolean komendaBaza(Player p, String[] args) {
		if (args.length < 1) return false;
		if (args[0].equalsIgnoreCase("bypass")) {
			if (!p.hasPermission(permBypass))
				return Func.powiadom(p, prefix + "Nie możesz tego użyć");
			if (bypass.remove(p.getName()))
				return Func.powiadom(p, prefix + "Wyłączyłeś bypass");
			else {
				bypass.add(p.getName());
				return Func.powiadom(p, prefix + "Włączyłeś bypass");
			}
		}
		Gracz g = Gracz.wczytaj(p.getName());
		if (g.baza == null)
			return Func.powiadom(p, prefix + "Nie masz bazy");
		switch (args[0].toLowerCase()) {
		case "usuń":
			if (args.length < 2 || !g.baza.nazwa.equals(args[1]))
				return Func.powiadom(p, prefix + "Jesteś pewny że chcesz usunąć swoją baze? Jeśli tak wpisz /baza usuń " + g.baza.nazwa);
			g.baza.usuń();
			Main.log(prefix + Func.msg("%s usunął swoją bazę", p.getDisplayName()));
			return Func.powiadom(p, prefix + "Usunąłeś swoją bazę");
		case "ulepsz":
			if (((List<?>) config.wczytaj("ulepszenia bazy")).size() > g.baza.poziom + 1)
				if (g.baza.możnaUlepszyć())
					p.openInventory(stwórzInvUlepszenia(g.baza.poziom + 1));
				else
					p.sendMessage(prefix + "W tym miejscu nie możesz bardziej ulepszyć swojej bazy");
			else
				p.sendMessage(prefix + "Twoja baza jest już na maksymalnym poziomie");
			break;
		default:
			return Func.powiadom(p, prefix + "Niepoprawne argumenty użyj /baza ulepsz /baza usuń");
		}
		return true;
	}
	
	final String nazwaEqUlepszania = "§1§lUlepszanie Bazy";
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (!(ev.getView().getTitle().equals(nazwaEqUlepszania))) return;
		
		int slot = ev.getRawSlot();
		
		if (slot < ev.getInventory().getSize() && slot >= 0 && !(slot >= 3*9 + 1 && slot < 3*9 + 8))	
			ev.setCancelled(true);
	}
	@EventHandler
	@SuppressWarnings("unchecked")
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (!ev.getView().getTitle().equals(nazwaEqUlepszania)) return;
		
		Player p = (Player) ev.getPlayer();
		Gracz g = Gracz.wczytaj(p);
		
		if (!g.baza.możnaUlepszyć()) {
			p.sendMessage(prefix + "Nie możesz ulepszyć bazy w tym miejscu");
			return;
		}
		
		List<ItemStack> zwrot = zwrotneItemy(ev.getInventory());
		if (zwrot == null) {
			for (int i=3*9 + 1; i<3*9 + 8; i++)
				Func.wykonajDlaNieNull(ev.getInventory().getItem(i), p.getInventory()::addItem);
			return;
		}
		for (ItemStack item : zwrot)
			p.getInventory().addItem(item);
		g.baza.ulepsz((int) ((List<Map<String, Object>>) config.wczytaj("ulepszenia bazy")).get(++g.baza.poziom).get("kratki"));
		g.zapisz();
		p.sendMessage(prefix + "Ulepszyłeś swoją baze");
	}
	
	
	final Set<String> setUmierających = Sets.newConcurrentHashSet();
	@EventHandler(priority = EventPriority.NORMAL)
	public void śmierć(PlayerRespawnEvent ev) {
		Gracz g = Gracz.wczytaj(ev.getPlayer());
		if (g.łóżkoBazowe != null) {
			if (setUmierających.contains(ev.getPlayer().getName())) {
				ev.getPlayer().sendMessage(prefix + "Za szybko umierasz. Twoje łóżko nie nadąrza");
				return;
			}
			if (g.łóżkoBazowe.getBlock().getType().toString().contains("_BED")) {
				Location resp = g.łóżkoBazowe.clone().add(0, .5, 0);
				ev.setRespawnLocation(resp);
				Func.opóznij(1, () -> ev.getPlayer().teleport(resp));
				setUmierających.add(ev.getPlayer().getName());
				Func.opóznij(20 * 60 * config.wczytaj("łóżkoCooldownRespuMinuty", 5), () -> setUmierających.remove(ev.getPlayer().getName()));
			} else {
				g.łóżkoBazowe = null;
				g.zapisz();
				ev.getPlayer().sendMessage(prefix + "Twoje łóżko uległo awarii");
			}
		}
	}
	
	final ItemStack pustyZablokowanySlot = Func.stwórzItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§1§l §2§o");
	@SuppressWarnings("unchecked")
	Inventory stwórzInvUlepszenia(int poziom) {
		ItemStack szybka = pustyZablokowanySlot.clone();
		szybka.setType(Material.BLACK_STAINED_GLASS_PANE);
		Inventory inv = Bukkit.createInventory(null, 9*5, nazwaEqUlepszania);
		for (int i=0; i< 9*5; i++)
			inv.setItem(i, szybka);
		
		List<Map<String, Object>> lista = (List<Map<String, Object>>) config.wczytaj("ulepszenia bazy");
		if (lista.size() <= poziom) return null;
		int i = 1*9 + 1;
		for (ItemStack item : Config.itemy((List<?>) lista.get(poziom).get("itemy")))
			inv.setItem(i++, Func.customModelData(item, 2020));
		while (i < 1*9 + 8)
			inv.setItem(i++, pustyZablokowanySlot);;
		
		for (i = 3*9 + 1; i< 3*9 + 8; i++)
			inv.setItem(i, null);
		
		return inv;
	}
	List<ItemStack> zwrotneItemy(Inventory inv) {
		List<ItemStack> lista = Lists.newArrayList();
		for (int i=1; i<8; i++) {
			ItemStack potrzebny = inv.getItem(1*9 + i);
			ItemStack item = inv.getItem(3*9 + i);
			if (potrzebny.isSimilar(pustyZablokowanySlot)) {
				Func.wykonajDlaNieNull(item, lista::add);
				continue;
			}
			if (item == null) return null;
			if (!Func.customModelData(potrzebny, null).isSimilar(item)) return null;
			int zwrot = item.getAmount() - potrzebny.getAmount();
			if (zwrot < 0) return null;
			if (zwrot > 0) {
				ItemStack _item = item.clone();
				_item.setAmount(zwrot);
				lista.add(_item);
			}
		}
		return lista;
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		switch (cmd.getName()) {
		case "gildia":
			if (args.length <= 1)
				return utab(args, "zaproś", "wyrzuć", "opuść", "stwórz", "tag", "lider");
			break;
		case "baza":
			if (args.length <= 1)
				if (sender.hasPermission(permBypass))
					return utab(args, "usuń", "ulepsz", "bypass");
				else
					return utab(args, "usuń", "ulepsz");
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, "Ta komenda jest zarezerwowana tylko dla graczy");
		Player p = (Player) sender;
		switch (cmd.getName()) {
		case "gildia":
			return komendaGildia(p, args);
		case "baza":
			return komendaBaza(p, args);
		}
		return true;
	}

	final HashMap<String, Krotka<Integer, Player>> mapaTp = new HashMap<>();
	@Override
	public int czas() {
		Set<String> doUsunięcia = Sets.newConcurrentHashSet();
		for (Entry<String, Krotka<String, Integer>> en : mapaZaproszeń.entrySet())
			if ((en.getValue().b -= 1) <= 0) {
				Func.wykonajDlaNieNull(Bukkit.getPlayer(en.getKey()), p ->
						p.sendMessage(Gildia.prefix + Func.msg("Zaproszenie do gildi dla %s wygasło", en.getValue().a)));
				Func.wykonajDlaNieNull(Bukkit.getPlayer(en.getValue().a), p ->
						p.sendMessage(Gildia.prefix + Func.msg("Zaproszenie do gildi od %s wygasło", en.getKey())));
				doUsunięcia.add(en.getKey());
			}
		for (String nick : doUsunięcia)
			mapaZaproszeń.remove(nick);
		return 20;
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		ConfigurationSection sekcja = config.sekcja("bazy");
		return Func.r("Itemy dla Baz/schematów/C4", sekcja == null ? "Nieaktywne" : "Aktywne");
	}

	// dla /citem
	public static Set<String> getBazy() {
		ConfigurationSection sekcja = config.sekcja("bazy");
		if (sekcja == null) return Sets.newConcurrentHashSet();
		return sekcja.getKeys(false);
	}
	public static ItemStack getBaze(String nazwa) {
		return config.wczytajItem("bazy." + nazwa + ".item");
	}
}


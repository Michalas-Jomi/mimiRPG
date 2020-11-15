package me.jomi.mimiRPG.MineZ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;
import net.md_5.bungee.api.chat.ClickEvent.Action;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

// TODO title w actionbarze przy wchodzeniu/wychodzeniu z bazy
@Moduł
public class Bazy extends Komenda implements Listener, Przeładowalny, Zegar {
	public static class Gildia extends Mapowany {
		public static final String prefix = Func.prefix("Gildia");
		static final Config config = new Config("configi/Gildie");
		@Mapowane List<String> gracze;
		@Mapowane String przywódca;
		@Mapowane String nazwa;
		
		
		void zapisz() {
			config.ustaw_zapisz(nazwa, this);
		}
		static Gildia wczytaj(String nazwa) {
			if (nazwa == null) return null;
			return (Gildia) config.wczytaj(nazwa);
		}
		static Gildia stwórz(String nazwa, String przywódca) {
			Gracz g = Gracz.wczytaj(przywódca);
			
			if (g.gildia != null) {
				Player p = Bukkit.getPlayer(przywódca);
				p.sendMessage(prefix + "Nie możesz utworzyć nowej gildi puki nie opuścisz aktualnej");
				return null;
			}
			
			Gildia gildia = new Gildia();
			gildia.przywódca = przywódca;
			gildia.nazwa = nazwa;
			gildia.zapisz();;
			
			g.gildia = nazwa;
			g.zapisz();
			
			return gildia;
		}
			
		static boolean istnieje(String nazwa) {
			for (String klucz : config.klucze(false))
				if (klucz.equalsIgnoreCase(nazwa))
					return true;
			return false;
		}
		
		void dołącz(Player p) {
			Gracz g = Gracz.wczytaj(p.getName());
			
			dodajRegiony(g);
			
			gracze.add(p.getName());
			zapisz();
			
			g.gildia = nazwa;
			g.zapisz();
			
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
			
			// TODO sprawdzić czy sie wyświetli, czy trzeba samodzielnie
			Event event = new AsyncPlayerChatEvent(true, kto, msg, set);
			Bukkit.getServer().getPluginManager().callEvent(event);
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
		
		
		Baza(int x, int y, int z, int dx, int dy, int dz, World świat, Player właściciel) {
			Player p = właściciel;
			this.świat = świat;
					
			String nazwaBazy = String.format("baza%sx%sy%sz", x, y, z);
			
			region = new ProtectedCuboidRegion(
					nazwaBazy,
					BlockVector3.at(x+dx, y+dy, z+dz),
					BlockVector3.at(x-dx, y-1, z-dz)
					);
			Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).addRegion(region);
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(p.getName());
			region.setOwners(owners);
			region.setPriority(Bazy.config.wczytajInt("ustawienia.prority baz"));
			region.setFlag(Main.flagaCustomoweMoby, "brak");
			region.setFlag(Main.flagaStawianieBaz, StateFlag.State.DENY);
			region.setFlag(Main.flagaC4, 		   StateFlag.State.ALLOW);
			String msgWejścia = config.wczytajLubDomyślna("ustawienia.msg wejścia", "§6Wszedłeś na teren bazy gracza {gracz}");
			region.setFlag(Flags.GREET_MESSAGE, Func.koloruj(msgWejścia.replace("{gracz}", p.getName())));
			String msgWyjścia = config.wczytajLubDomyślna("ustawienia.msg wyjścia", "§6Wyszedłeś z terenu bazy gracza {gracz}");
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
			int dx = (int) mapa.get("dx");
			int dy = (int) mapa.get("dy");
			int dz = (int) mapa.get("dz");
			ProtectedCuboidRegion region = new ProtectedCuboidRegion(
					"mimiBazaTestowana",
					BlockVector3.at(x+dx, y+dy, z+dz),
					BlockVector3.at(x-dx, y-1,  z-dz)
					);
			if (Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
					.getApplicableRegions(region)
					.testState(null, Main.flagaStawianieBaz))
				return new Baza(x, y, z, dx, dy, dz, świat, ev.getPlayer());
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
			region.setFlag(Main.flagaStawianieBaz, StateFlag.State.ALLOW);
			boolean w = Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
					.getApplicableRegions(regionKontrolny)
					.testState(null, Main.flagaStawianieBaz);
			region.setFlag(Main.flagaStawianieBaz, StateFlag.State.DENY);
			return w;
		}

		public Baza() {}// konstruktor dla Mapowanego
		public void Init() {
			świat = Bukkit.getWorld(nazwaŚwiata);
			region = (ProtectedCuboidRegion) Bazy.inst.regiony(świat).getRegion(nazwa);
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
		
		Integer idRajd = null;
		void rajdowana(BlockBreakEvent ev, Baza baza) {
			if (idRajd != null)
				zrajdowana(ev, baza);
			else {
				Func.opóznij(1, () -> ev.getBlock().setType(Material.SOUL_CAMPFIRE));
				idRajd = Func.opóznij(config.wczytajLubDomyślna("czas rajdowania", 5) * 20 + 1, () -> {
					idRajd = null;
					ev.getBlock().setType(Material.CAMPFIRE);
				});
			}
		}
		private void zrajdowana(BlockBreakEvent ev, Baza baza) {
			Bukkit.getScheduler().cancelTask(idRajd);
		
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

			ProtectedCuboidRegion nowy = new ProtectedCuboidRegion(
					"zniszczona" + nazwa,
					region.getMinimumPoint(),
					region.getMaximumPoint()
					);
			Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).addRegion(nowy);
			
			nowy.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
			nowy.setFlag(Main.flagaC4, 		StateFlag.State.ALLOW);
			nowy.setPriority(region.getPriority() - 1);
			
			Bazy.inst.regiony(świat).removeRegion(region.getId());
			
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
				Bukkit.getScheduler().cancelTask(idTasku); // TODO uzupełnić szablon
			else
				for (String nick : region.getOwners().getPlayers())
					Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(prefix + "Twoja baza jest §cAtakowana!"));
			
			idTasku = Func.opóznij(config.wczytajLubDomyślna("ustawienia.długość rajdów", 120) * 20, () -> {
					atakowana = false;
					for (String nick : region.getOwners().getPlayers())
						Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(prefix + "Atak na twoją bazę §aminął"));
					});
			
			atakowana = true;
		}
	}
		
	final String permBypass = Func.permisja("bazy.bypass");
	public static final String prefix = Func.prefix("Baza");
	RegionContainer regiony;
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
		return Main.rg != null;
	}
	
	
	@EventHandler
	public void explozja(ExplosionPrimeEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains("mimiC4")) {
			ev.setCancelled(true);
			
			final ConfigurationSection mapa = config.sekcja("c4");
			if (mapa == null)
				return;
			
			final List<String> niezniszczalne = config.wczytajListe("c4.niezniszczalne");
			final List<Krotka<Block, Material>> kolejka = Lists.newArrayList();
			Function<Block, String> dajDate = blok -> {
				return blok.getBlockData().getAsString(false).substring(10 + blok.getType().toString().length());
			};
			Consumer<Block> zniszcz = blok -> {
				final String mat = blok.getType().toString();
				final String str = (String) mapa.get(mat);
				
				if (str != null) {
					final String data = dajDate.apply(blok);
					blok.setBlockData(Bukkit.createBlockData(Material.valueOf(str), data), false);
					
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
				}
				else if (!niezniszczalne.contains(mat))
					blok.setType(Material.AIR);
			};
			
			Location loc = ev.getEntity().getLocation();
			Location _loc = loc.clone();
			RegionManager regiony = Bazy.inst.regiony.get(BukkitAdapter.adapt(loc.getWorld()));
			final float zasięg = ev.getRadius();
			int mx = (int) (zasięg*2+1);
			loc.add(-zasięg, -zasięg, -zasięg);
			
			float r = zasięg/3*2;
			_loc.getWorld().spawnParticle(Particle.CLOUD, 		_loc, (int) zasięg*50, r, r, r, 0);
			_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, _loc, (int) zasięg*20, r, r, r, 0);
			r *= .4;
			_loc.getWorld().spawnParticle(Particle.CLOUD, 		_loc, (int) zasięg*20, r, r, r, 0);
			_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, _loc, (int) zasięg*15, r, r, r, 0);
			r *= 1.5;
			_loc.getWorld().spawnParticle(Particle.FLAME, _loc, (int) zasięg*20, r, r, r, .1);
			
			_loc.getWorld().playSound(_loc, Sound.ENTITY_RAVAGER_STEP, 100, 0);
			
			BiConsumer<Float, Double> uderz = (Zasięg, dmg) -> {
				for (Entity mob : _loc.getWorld().getNearbyEntities(_loc, Zasięg, Zasięg, Zasięg))
					if (mob instanceof Damageable && !mob.isInvulnerable())
						((Damageable) mob).damage(dmg);
			};
			uderz.accept(zasięg+3,   	4d);
			uderz.accept(zasięg/3*2.5f, 8d);
			uderz.accept(zasięg/3, 		8d);
			
			
			for (int y=0; y<mx; y++) {
				for (int z=0; z<mx; z++) {
					for (int x=0; x<mx; x++) {
						if (!loc.getBlock().getType().isAir()) {
							double dystans = _loc.distance(loc);
							if (dystans <= zasięg) {
								int szansa = dystans < zasięg/3*1 ? 90 : (dystans < zasięg/3*2 ? 60 : 30);
								if (Func.losuj(1, 100) <= szansa &&
										regiony.getApplicableRegions(locToVec3(loc)).testState(null, Main.flagaC4))
									zniszcz.accept(loc.getBlock());
							}
						}
						loc.add(1, 0, 0);
					}
					loc.add(-mx, 0, 1);
				}
				loc.add(0, 1, -mx);
			}
			for (Krotka<Block, Material> krotka : kolejka)
				if (krotka.a.getType() != krotka.b)
					krotka.a.setBlockData(Bukkit.createBlockData(krotka.b, dajDate.apply(krotka.a)), false);
			
			ProtectedCuboidRegion _region = new ProtectedCuboidRegion("mimiChwilowaBazaC4",
					locToVec3(_loc.clone().add(zasięg, zasięg, zasięg)), locToVec3(_loc.clone().add(-zasięg, -zasięg, -zasięg)));
			for (ProtectedRegion region : regiony.getApplicableRegions(_region).getRegions())
				Func.wykonajDlaNieNull(Baza.wczytaj(_loc.getWorld(), region), baza -> {
					boolean atakowana = baza.atakowana;
					baza.atak();
					if (atakowana) return;
				});
		}
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
		
	boolean blokuj;
	@EventHandler(priority = EventPriority.LOWEST)
	public void stawianie(BlockPlaceEvent ev) {
		World świat = ev.getBlock().getWorld();
		ItemStack item = ev.getPlayer().getEquipment().getItemInMainHand();
		int x = ev.getBlock().getX();
		int y = ev.getBlock().getY();
		int z = ev.getBlock().getZ();
		
		if (config.klucze(false).contains("bazy"))
			for (Entry<String, Object> en : config.sekcja("bazy").getValues(false).entrySet()) {
				Map<String, Object> mapa = ((ConfigurationSection) en.getValue()).getValues(false);
				if (Func.porównaj((ItemStack) Config.item(mapa.get("item")), item)) {
					ev.setCancelled(true);
					
					if (Func.multiEquals(ev.getBlockReplacedState().getType(), Material.WATER, Material.LAVA)) return;
					
					Runnable zabierzItem = () -> {
						item.setAmount(item.getAmount()-1);
						ev.getPlayer().getEquipment().setItemInMainHand(item);
					};

					// C4
					if (mapa.containsKey("c4")) {
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
						
						zabierzItem.run();
						return;
					}
					
					// Baza/Schemat
					// jeśli baza nie może być postawiona przez flage -> blokuj = true
					blokuj = false;
					boolean zabierz = Baza.wczytaj(ev.getPlayer(), x, y, z, świat, item, ev,
							((ConfigurationSection) mapa.get("baza")).getValues(false)) != null;
					
					if (mapa.containsKey("schemat") && !blokuj && 
							Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
								.getApplicableRegions(BlockVector3.at(x, y, z))
								.testState(Main.rg.wrapPlayer(ev.getPlayer()), Flags.BUILD) &&
							wklejSchemat((String) mapa.get("schemat"), świat, x, y, z))
								zabierz = true;
					
					if (zabierz) 
						zabierzItem.run();
					return;
				}
			}
	}
	boolean wklejSchemat(String schematScieżka, World świat, int x, int y, int z) {
		String scieżka = Main.path + schematScieżka;
		File file = new File(scieżka);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file));
				EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(świat))) {
			Operations.complete(
					new ClipboardHolder(reader.read())
		            .createPaste(editSession)
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(true)
		            .build()
		            );
		} catch (IOException  e) {
			Main.warn("Nie odnaleziono pliku " + scieżka + " schemat z Bazy.yml nie został wybudowany.");
			return false;
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		return true;
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
		
		baza.rajdowana(ev, baza);
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
			if (config.klucze(false).contains("bazy"))
				for (Entry<String, Object> en : config.sekcja("bazy").getValues(false).entrySet()) {
					Map<String, Object> mapa = ((ConfigurationSection) en.getValue()).getValues(false);
					if (Func.porównaj((ItemStack) Config.item(mapa.get("item")), item)) {
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
		for (ProtectedRegion region : regiony(loc.getWorld()).getApplicableRegions(locToVec3(loc))) {
			Baza baza = Baza.wczytaj(loc.getWorld(), region);
			if (baza != null)
				return baza;
		}
		return null;
	}
	RegionManager regiony(World świat) {
		return regiony.get(BukkitAdapter.adapt(świat));
	}
	BlockVector3 locToVec3(Location loc) {
		return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
	}
	
	final Set<String> bypass = Sets.newConcurrentHashSet();
	
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
		case "zaproś":
		case "zapros":
			if (args.length < 2) 
				return Func.powiadom(sender, Gildia.prefix + "/gildia zaproś <nick>");
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;
			if (mapaZaproszeń.containsKey(sender.getName()))
				return Func.powiadom(sender, Gildia.prefix + "Poczekaj aż minie poprzednie zaproszenie zanim wyślesz kolejne");
			
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) return Func.powiadom(sender, Gildia.prefix + "Gracz nie jeste online");
			
			Gracz zaproszony = Gracz.wczytaj(p.getName());
			if (!(zaproszony.gildia == null || zaproszony.gildia.isEmpty()))
				return Func.powiadom(sender, Gildia.prefix + Func.msg("%s nalezy już do gildi %s", args[1], zaproszony.gildia));
			
			mapaZaproszeń.put(sender.getName(), new Krotka<>(p.getName(), czasZaproszeń));
			
			Napis n = new Napis();
			n.dodaj(Func.msg("%s zaprasza cię do gildi %s ", sender.getName(), gildia.nazwa));
			n.dodaj(new Napis("§a[dołącz]", "§9Kliknij aby dołączyć", "/gildia dołącz " + sender.getName() + " " + gildia.nazwa));
			n.wyświetl(p);
			
			sender.sendMessage(Gildia.prefix + Func.msg("Wysłano zaproszenie dla gracza %s, które wygaśnie za %s", args[1], Func.czas(czasZaproszeń)));
			
			break;
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
			Func.napisz(args[1], Gildia.prefix + Func.msg("Zostałeś wyrzucony z gildi %s przez %s", gildia.nazwa, sender.getName()));
			break;
		case "opuść":
		case "opuśc":
		case "opusć":
		case "opusc":
			if (!maGildie.getAsBoolean()) break;
			gildia.opuść(sender.getName());
			gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s opuścił gildię", gildia.nazwa, sender.getName()));
			sender.sendMessage(Gildia.prefix + Func.msg("Opuściłeś gildię %s", gildia.nazwa));
			break;
		case "stwórz":
		case "stworz":
			if (args.length < 2)
				return Func.powiadom(sender, Gildia.prefix + "/gildia stwórz <nazwa>");
			
			if (args[1].length() > 30)
				return Func.powiadom(sender, Gildia.prefix + "Zbyt długa nazwa gildi");
			
			if (Gildia.istnieje(args[1]))
				return Func.powiadom(sender, Gildia.prefix + Func.msg("gildia %s już istnieje", args[1]));
			
			Gildia.stwórz(args[1], sender.getName());
			sender.sendMessage(Gildia.prefix + Func.msg("Gildia %s została utworzona", args[1]));
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
				_gildia.dołącz(sender);
				_gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s na mocy %s dołączył do gildi", nazwaGildi, zapraszający, sender.getName()));

				mapaZaproszeń.remove(zapraszający);
				break;
			} catch (Throwable e) {}
		default:
			return edytor(sender);
		}
		return true;
	}	
	boolean edytor(Player p) {
		Napis n = new Napis();
		
		Gracz g = Gracz.wczytaj(p.getName());
		if (Func.nieNullStr(g.gildia).isEmpty()) {
			n.dodaj(Gildia.prefix);
			n.dodaj(new Napis("§a[stwórz gildie]\n", "§bWymagana nazwa gildi", "/gildia stwórz ", Action.SUGGEST_COMMAND));
		} else {
			Gildia gildia = Gildia.wczytaj(g.gildia);
			n.dodaj("\n\n\n\n\n§9Gracze gildi " + gildia.nazwa + ":\n");
			n.dodaj("§e§l- §e^§b" + gildia.przywódca + "§e^\n");
			for (String nick : gildia.gracze) {
				n.dodaj("§e§l- §b" + nick + " ");
				if (p.getName().equals(gildia.przywódca))
					n.dodaj(new Napis("§c[x]", "§cWyrzuć", "/gildia wyrzuć " + nick));
				n.dodaj("\n");
			}
			n.dodaj(new Napis("§c[opuść]", "§cKliknij aby opuść gildie", "/gildia opuść"));
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
				Func.opóznij(20 * 60 * config.wczytajLubDomyślna("łóżkoCooldownRespuMinuty", 5), () -> setUmierających.remove(ev.getPlayer().getName()));
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
				return utab(args, "zaproś", "wyrzuć", "opuść", "stwórz");
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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
				Func.napisz(en.getKey(), Gildia.prefix + Func.msg("Zaproszenie do gildi dla %s wygasło", en.getValue().a));
				Func.napisz(en.getValue().a, Gildia.prefix + Func.msg("Zaproszenie do gildi od %s wygasło", en.getKey()));
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


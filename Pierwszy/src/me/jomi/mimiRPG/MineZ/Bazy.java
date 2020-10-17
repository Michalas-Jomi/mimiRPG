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
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
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
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Krotka;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import me.jomi.mimiRPG.Gracze.Gracz;
import net.md_5.bungee.api.chat.ClickEvent.Action;

// TODO title w actionbarze przy wchodzeniu/wychodzeniu z bazy
@Moduł
public class Bazy extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Baza");
	RegionContainer regiony;
	static Config config = new Config("Bazy");
	
	static Bazy inst;
	public Bazy() {
		super("gildia", null, "g");
		ustawKomende("usuńbaze", null, null);
		ustawKomende("ulepszbaze", null, null);
		inst = this;
		regiony = WorldGuard.getInstance().getPlatform().getRegionContainer();
	}
	public static boolean warunekModułu() {
		return Main.rg != null;
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public String raport() {
		ConfigurationSection sekcja = config.sekcja("bazy");
		return "§6Itemy dla Baz/schematów/C4: §e" + (sekcja == null ? 0 : sekcja.getKeys(false).size());
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
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void niszczenie(BlockBreakEvent ev) {
		if (!ev.getBlock().getType().equals(Material.CAMPFIRE)) return;
		
		Location loc = ev.getBlock().getLocation();
		for (ProtectedRegion region : regiony(loc.getWorld()).getApplicableRegions(locToVec3(loc))) {
			Baza baza = Baza.wczytaj(loc.getWorld(), region);
			if (baza != null) {
				ev.setCancelled(true);
				if (baza.region.getOwners().contains(ev.getPlayer().getName())) {
					Func.powiadom(prefix, ev.getPlayer(), "Nie możesz zniszczyć własnej bazy, jeśli musisz użyj /usuńbaze");
					return;
				}
				if (baza.region.getMembers().contains(ev.getPlayer().getName())) {
					Func.powiadom(prefix, ev.getPlayer(), "Nie możesz zniszczyć bazy członka twojej gildi");
					return;
				}
				baza.usuń();
				Func.opóznij(1, () -> ev.getBlock().setType(Material.AIR));
				ev.getPlayer().sendMessage(prefix + Func.msg("Zniszczyłeś baze gracza %s", Func.listToString(
						baza.region.getOwners().getPlayers(), 0, "§6, §e")));
				for (String owner : baza.region.getOwners().getPlayers()) {
					Player p = Bukkit.getPlayer(owner);
					if (p != null) Func.powiadom(prefix, p, "%s zniszczył twoją baze!", ev.getPlayer().getDisplayName());
				}
				return;
			}
		}
	}
	
	boolean blokuj = false;
	@EventHandler
	public void stawianie(BlockPlaceEvent ev) {
		World świat = ev.getBlock().getWorld();
		ItemStack item = ev.getPlayer().getEquipment().getItemInMainHand();
		int x = ev.getBlock().getX();
		int y = ev.getBlock().getY();
		int z = ev.getBlock().getZ();
		
		Consumer<Map<String, Object>> wejście = mapa -> {
			ev.setCancelled(true);
			
			Supplier<Integer> zabierzItem = () -> {
				item.setAmount(item.getAmount()-1);
				ev.getPlayer().getEquipment().setItemInMainHand(item);
				return 0;
			};
			// FIXME me.jomi.mimiRPG.Func.zdemapuj(Func.java:587) 
			// C4
			Map<String, Object> mapaC4 = ((ConfigurationSection) mapa.get("c4")).getValues(false);
			if (mapaC4 != null) {
				
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
				
				zabierzItem.get();
				return;
			}
			
			// Baza/Schemat
			boolean zabierz = false;
			
			if (mapa.containsKey("schemat") && !blokuj && 
					Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
						.getApplicableRegions(BlockVector3.at(x, y, z))
						.testState(Main.rg.wrapPlayer(ev.getPlayer()), Flags.BUILD) &&
					wklejSchemat((String) mapa.get("schemat"), świat, x, y, z))
						zabierz = true;
			
			if (Baza.wczytaj(x, y, z, świat, item, ev, ((ConfigurationSection) mapa.get("baza")).getValues(false)) != null)
				zabierz = true;
			
			blokuj = false;
			
			if (zabierz) 
				zabierzItem.get();
		};
		if (config.klucze(false).contains("bazy"))
			for (Entry<String, Object> en : config.sekcja("bazy").getValues(false).entrySet()) {
				Map<String, Object> mapa = ((ConfigurationSection) en.getValue()).getValues(false);
				if (Func.porównaj((ItemStack) Config.item(mapa.get("item")), item)) {
					wejście.accept(mapa);
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
		
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "zaproś", "wyrzuć", "opuść", "stwórz");
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, "Ta komenda jest zarezerwowana tylko dla graczy");
		Player p = (Player) sender;
		switch (cmd.getName()) {
		case "gildia":
			return komendaGildia(p, args);
		case "usuńbaze":
			for (ProtectedRegion region : regiony(p.getWorld()).getApplicableRegions(locToVec3(p.getLocation()))) {
				Baza baza = Baza.wczytaj(p.getWorld(), region);
				if (baza != null) {
					if (!baza.region.getOwners().contains(sender.getName()))
						return Func.powiadom(prefix, sender, "To nie twoja baza");
					baza.usuń();
					sender.sendMessage(prefix + "Usunięto baza z pod twoich nóg");
					return true;
				}
			}
			sender.sendMessage(prefix + "W tym miejscu nie ma żadnej bazy");
			break;
		case "ulepszbaze":
			for (ProtectedRegion region : regiony(p.getWorld()).getApplicableRegions(locToVec3(p.getLocation()))) {
				Baza baza = Baza.wczytaj(p.getWorld(), region);
				if (baza != null) {
					if (!baza.region.getOwners().contains(sender.getName()))
						return Func.powiadom(prefix, sender, "To nie twoja baza");
					baza.ulepsz(2);
					sender.sendMessage(prefix + "Ulepszono baze z pod twoich nóg");
					return true;
				}
			}
			sender.sendMessage(prefix + "Tu nie ma żadnej bazy");
			break;
		}
		return true;
	}
	
	@Override
	public int czas() {
		Set<String> doUsunięcia = Sets.newConcurrentHashSet();
		for (Entry<String, Krotka<String, Integer>> en : mapaZaproszeń.entrySet()) {
			if ((en.getValue().b -= 1) <= 0) {
				Func.napisz(en.getKey(), Gildia.prefix + Func.msg("Zaproszenie do gildi dla %s wygasło", en.getValue().a));
				Func.napisz(en.getValue().a, Gildia.prefix + Func.msg("Zaproszenie do gildi od %s wygasło", en.getKey()));
				doUsunięcia.add(en.getKey());
			}
		}
		for (String nick : doUsunięcia)
			mapaZaproszeń.remove(nick);
		return 20;
	}
	
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
			if (args.length < 2) return Func.powiadom(sender, Gildia.prefix + "/gildia stwórz <nazwa>");
			
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
				if (maGildie.getAsBoolean())
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
		if (!g.posiadaGildie()) {
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

	RegionManager regiony(World świat) {
		return regiony.get(BukkitAdapter.adapt(świat));
	}
	BlockVector3 locToVec3(Location loc) {
		return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public static Set<String> getBazy() {
		ConfigurationSection sekcja = config.sekcja("bazy");
		if (sekcja == null) return Sets.newConcurrentHashSet();
		return sekcja.getKeys(false);
	}
	public static ItemStack getBaze(String nazwa) {
		return config.wczytajItem("bazy." + nazwa + ".item");
	}
}


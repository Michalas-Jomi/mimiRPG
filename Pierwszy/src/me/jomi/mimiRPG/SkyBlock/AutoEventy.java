package me.jomi.mimiRPG.SkyBlock;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.NowyEkwipunek;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;
import net.md_5.bungee.api.chat.ClickEvent.Action;

@Moduł
public class AutoEventy extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Event");
	
	final String permEdytuj = Func.permisja("autoevent.edytuj");
	final String permBypass = Func.permisja("autoevent.bypass");
	
	final Config config = new Config("configi/eventy");
	
	final Set<String> dozwoloneKomendy = Sets.newHashSet("/autoevent", "/event", "/msg", "/t", "/m", "tell", "/me", "/afk", "/komendyinfo", "/cg", "/vote", "");
	
	static AutoEventy inst;
	public AutoEventy() {
		super("autoevent", null, "event");
		Main.dodajPermisje(permEdytuj, permBypass);
		inst = this;
	}
	
	public static boolean warunekModułu() {
		return Main.ekonomia;
	}

	public static void wyłącz() {
		if (inst.event == null) return;
		Main.log("Wyłączanie AutoEventu");
		inst.event.koniec();
	}
	
	int czas;
	int maxCzas;
	@Override
	public int czas() {
		if (event != null) 
			event.czas();
		else if (czas-- <= 0) {
			czas = maxCzas;
			rozpocznij(Func.losuj(Lists.newArrayList(dajEventy())));
		}
		return 20;
	}
	@Override
	public void przeładuj() {
		config.przeładuj();
		maxCzas = config.wczytajInt("czas") * 60;
		czas = maxCzas;
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane Eventy", dajEventy().size());
	}
	
	final HashMap<String, EventEdytor> mapaEdytorów = new HashMap<>();
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(permEdytuj)) return null;
		if (args.length == 2 && args[0].equals("edytuj"))
			return utab(args, dajEventy());
		else if (args.length <= 1)
			return utab(args, "edytuj", "odśwież");
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Tylko gracz może korzystać z autoeventów");
		Player p = (Player) sender;
		if (args.length >= 1 && p.hasPermission(permEdytuj))
			switch (args[0]) {
			case "edytuj":	
				if (args.length < 2) return Func.powiadom(sender, prefix + "/" + label + " edytuj <nazwa>");
				mapaEdytorów.put(p.getName(), new EventEdytor(p, args[1]));
				break;
			case "odśwież":
				EventEdytor ee = mapaEdytorów.get(p.getName());
				if (ee != null)
					ee.info();
				else
					p.sendMessage(prefix + "Aktualnie nie edytujesz żadnego eventu");
				break;
			case "-e":
				EventEdytor _e = mapaEdytorów.get(p.getName());
				if (_e != null)
					try {
						_e.onCommand(p, args);
					} catch (Throwable e) {
						p.sendMessage(EventEdytor.prefix + "§cNie zmieniaj nic przed znakiem >> ");
					}
				else
					p.sendMessage(prefix + "Aktualnie nie edytujesz żadnego eventu");
				
				break;
			}
		else if (event != null)
			event.dołącz(p);
		else
			p.sendMessage(prefix + "Aktuanie nie trwa żaden event");
		
		return true;
	}
	
	Event event;
	void rozpocznij(String nazwa) {
		if (nazwa == null || nazwa.isEmpty()) return;
		ConfigurationSection sekcja = config.sekcja("eventy." + nazwa);
		if (sekcja == null || event != null) return;
		
		event = new Event(
				Config.itemy(Func.nieNullList(sekcja.getList("itemy"))),
				RodzajEventu.valueOf(sekcja.getString("rodzaj")),
				sekcja.getLocation("róg mety 1"),
				sekcja.getLocation("róg mety 2"),
				GameMode.valueOf(sekcja.getString("gamemode").toUpperCase()),
				sekcja.getDouble("nagroda"),
				sekcja.getInt("min gracze"),
				sekcja.getInt("max gracze"),
				nazwa,
				sekcja.getInt("czas zbiórki"),
				sekcja.getInt("czas gry"),
				sekcja.getLocation("start"),
				sekcja.getLocation("zbiórka")
				);
		infoStart();
	}
	
	@EventHandler
	public void śmierć(PlayerDeathEvent ev) {
		if (event != null)
			event.śmierć(ev);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void poruszanie(PlayerMoveEvent ev) {
		if (event != null)
			event.poruszanie(ev);
	}
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		if (event != null)
			for (Player p : event.gracze)
				if (p.getName().equals(ev.getPlayer().getName())) {
					event.opuść(p);
					return;
				}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void komendy(PlayerCommandPreprocessEvent ev) {
		if (event != null) {
			if (dozwoloneKomendy.contains(ev.getMessage().split(" ")[0])) return;
			if (ev.getPlayer().hasPermission(permBypass)) return;
			String nick = ev.getPlayer().getName();
			for (Player p : event.gracze)
				if (p.getName().equals(nick)) {
					ev.setCancelled(true);
					p.sendMessage(prefix + "W trakcie eventów nie można używać komend");
					return;
				}
		}
	}
	
	void infoStart() {
		Bukkit.broadcastMessage(prefix + Func.msg("Za %s rozpocznie się Event %s", Func.czas(event.zbiórka), event.nazwa));
	}
	
	Set<String> dajEventy(){
		ConfigurationSection sekcja = config.sekcja("eventy");
		if (sekcja != null)
			return sekcja.getKeys(false);
		return Sets.newConcurrentHashSet();
	}
}

class Event {
	static String prefix = AutoEventy.prefix;
	
	List<ItemStack> itemy;
	RodzajEventu rodzaj;
	Location meta1;
	Location meta2;
	GameMode gamemode;
	double nagroda;
	int min_gracze;
	int max_gracze;
	String nazwa;
	int zbiórka;
	int czas;
	Location loc_start;
	Location loc_zbiórka;
	
	final List<Player> gracze = Lists.newArrayList();
	boolean koniec = false;
	
	Event(List<ItemStack> itemy, RodzajEventu rodzaj, Location meta1, Location meta2, GameMode gamemode, double nagroda,
			int min_gracze, int max_gracze, String nazwa, int zbiórka, int czas, Location loc_start, Location loc_zbiórka) {
		this.itemy = itemy;
		this.rodzaj = rodzaj;
		this.meta1 = meta1;
		this.meta2 = meta2;
		this.gamemode = gamemode;
		this.nagroda = nagroda;
		this.min_gracze = min_gracze;
		this.max_gracze = max_gracze;
		this.nazwa = nazwa;
		this.zbiórka = zbiórka;
		this.czas = czas;
		this.loc_start = loc_start;
		this.loc_zbiórka = loc_zbiórka;
	}
	
	void dołącz(Player p) {
		if (zbiórka <= 0) {
			p.sendMessage(prefix + "Event już wystartował");
			return;
		}
		if (gracze.size() >= max_gracze) {
			p.sendMessage(prefix + "Wszystkie miejsca w evencie są już zajęte");
			return;
		}
		for (Player gracz : gracze)
			if (gracz.getName().equals(p.getName())) {
				opuść(p);
				return;
			}
		
		gracze.add(p);
		NowyEkwipunek.dajNowy(p, loc_zbiórka, gamemode);
		for (Player gracz : gracze)
			gracz.sendMessage(prefix + Func.msg("%s Dołącza do Eventu! %s/%s", p.getDisplayName(), gracze.size(), max_gracze));
		if (gracze.size() >= max_gracze)
			zbiórka = Math.max(zbiórka, 5);
		return;
	}
	void opuść(Player p) {
		for (int i=0; i<gracze.size(); i++)
			if (gracze.get(i).getName().equals(p.getName())) {
				if (!koniec)
					for (Player gracz : gracze)
						gracz.sendMessage(prefix + "§e" + p.getDisplayName() + " §6" + (zbiórka <= 0 ? "odpada!" : "opuścił event") + "§e" + gracze.size() + "§6/§e" + max_gracze);
				gracze.remove(i);
				NowyEkwipunek.wczytajStary(p);
				if (rodzaj.equals(RodzajEventu.OstatniNaArenie) && gracze.size() == 1)
					wygrał(gracze.get(0));
				return;
			}
	}
	void wygrał(Player p) {
		Main.econ.depositPlayer(p, nagroda);
		p.sendMessage(prefix + Func.msg("Na twoje konto wpłyneło %s§e$", nagroda));
		Bukkit.broadcastMessage(prefix + Func.msg("%s wygrał event %s!", p.getName(), nazwa));
		koniec();
	}
	void koniec() {
		koniec = true;
		for (Player p : Lists.newCopyOnWriteArrayList(gracze))
			opuść(p);
		AutoEventy.inst.event = null;
	}

	void start() {
		if (gracze.size() < min_gracze) {
			Bukkit.broadcastMessage(Func.msg("Event %s nie wystartował przez małą ilość graczy %s/%s", nazwa, gracze.size(), max_gracze));
			koniec();
			return;
		}
		for (Player p : gracze) {
			p.teleport(loc_start);
			int i=0;
			for (ItemStack item : itemy)
				p.getInventory().setItem(i++, item);
		}
		Bukkit.broadcastMessage(Func.msg("Event %s rozpoczął się %s/%s", nazwa, gracze.size(), max_gracze));
	}
	
	// wykonuje się co sekunde
	void czas() {
		if (zbiórka-- > 0) {
			// czas zbiórki
			if (zbiórka % 60 == 0 || zbiórka == 30 || zbiórka == 10)
				AutoEventy.inst.infoStart();
			else if (zbiórka % 30 == 0)
				for (Player p : gracze)
					p.sendMessage(prefix + "Start za §e" + Func.czas(zbiórka));
			if (zbiórka < 5 && zbiórka > 0)
				for (Player p : gracze)
					p.sendTitle("§bStart za", "§a" + zbiórka, 20, 50, 30);
			else if (zbiórka <= 0)
				start();
		} else if (--czas <= 0) {
			// czas gry
			Bukkit.broadcastMessage(prefix + Func.msg("Event %s dobiegł końca, nikt nie wygrał", nazwa));
			koniec();
		} else if (czas % 60 == 0 || (czas <= 60 && czas % 10 == 0))
				for (Player p : gracze)
					p.sendMessage(prefix + "Koniec za §e" + Func.czas(czas));
	}

	void śmierć(PlayerDeathEvent ev) {
		switch (rodzaj) {
		case OstatniNaArenie:
			opuść(ev.getEntity());
			break;
		case PierwszyNaMecie:
			for (Player p : gracze)
				if (p.getName().equals(ev.getEntity().getName())) {
					ev.getEntity().teleport(loc_start);
					ev.setKeepInventory(true);
					ev.setKeepLevel(true);
					return;
				}
			break;
		}
	}
	void poruszanie(PlayerMoveEvent ev) {
		if (rodzaj.equals(RodzajEventu.PierwszyNaMecie))
			for (Player p : gracze)
				if (p.getName().equals(ev.getPlayer().getName())) {
					if (Func.zawiera(ev.getTo(), meta1, meta2))
						wygrał(p);
					return;
				}
	}
}


enum RodzajEventu {
	OstatniNaArenie,
	PierwszyNaMecie;
}

class EventEdytor {
	static final String prefix = Func.prefix("Edytor AutoEventów");
	Player p;

	final String wybierz = "§8Wybierz";
	final String ustaw = "§bKliknij aby ustawić";
	final String cmd = "/autoevent -e ";
	
	public String nazwa;
	public RodzajEventu rodzaj;
	public GameMode gamemode = GameMode.ADVENTURE;
	public int czas = 10;
	public int zbiórka = 5;
	public double nagroda = 100;
	public int min_gracze = 2;
	public int max_gracze = 10;
	public Location loc_start;
	public Location loc_zbiórka;
	public Location loc_meta1;
	public Location loc_meta2;
	public List<ItemStack> itemy = Lists.newArrayList();
	
	
	EventEdytor(Player p, String nazwa) {
		this.nazwa = nazwa;
		this.p = p;
		boolean b = AutoEventy.inst.dajEventy().contains(nazwa);
		if (b) {
			ConfigurationSection sekcja = AutoEventy.inst.config.sekcja("eventy." + nazwa);
			
			czas = sekcja.getInt("czas gry");
			nagroda = sekcja.getDouble("nagroda");
			zbiórka = sekcja.getInt("czas zbiórki");
			loc_start = sekcja.getLocation("start");
			min_gracze = sekcja.getInt("min gracze");
			max_gracze = sekcja.getInt("max gracze");
			loc_zbiórka = sekcja.getLocation("zbiórka");
			loc_meta1 = sekcja.getLocation("róg mety 1");
			loc_meta2 = sekcja.getLocation("róg mety 2");
			rodzaj = RodzajEventu.valueOf(sekcja.getString("rodzaj"));
			itemy = Config.itemy(Func.nieNullList(sekcja.getList("itemy")));
			gamemode  = GameMode.valueOf(sekcja.getString("gamemode").toUpperCase());
			
		}
		info();
		if (b) p.sendMessage(prefix + "Edytujesz już istniejący event");
	}
	
	void info() {
		Function<Location, String> locString = loc -> {
			if (loc == null) return wybierz;
			StringBuilder w = new StringBuilder();
			
			w.append(loc.getBlockX()).append("x ")
				.append(loc.getBlockY()).append("y ")
				.append(loc.getBlockZ()).append("z ")
				.append(Func.zaokrąglij(loc.getPitch(), 2))
				.append('/')
				.append(Func.zaokrąglij(loc.getYaw(), 2));
			
			return w.toString();
		};
		
		Napis n = new Napis("\n\n\n\n\n\n\n§1§lEvent ");
		n.dodajEnd(
				new Napis("§9§l§o" + nazwa,
						ustaw, cmd + "nazwa >> "),
				new Napis("\n"),
				new Napis("§6Typ: §e" 						+ (rodzaj != null ? rodzaj : wybierz),
						ustaw, cmd + "typ"),
				new Napis("§6Gamemode: §e" 					+ (gamemode != null ? gamemode : wybierz),
						ustaw, cmd + "gamemode"),
				new Napis("§6Czas gry: §e" 					+ (czas > 0 ? czas : wybierz) + "§e minut",
						ustaw, cmd + "-p czas >> "),
				new Napis("§6Czas zbiórki: §e" 				+ (zbiórka > 0 ? zbiórka : wybierz) + "§e minut",
						ustaw, cmd + "-p zbiórka >> "),
				new Napis("§6Nagroda: §e" 					+ (nagroda >= 0 ? nagroda : wybierz) + "§e$",
						ustaw, cmd + "-p nagroda >> "),
				new Napis("§6Wymagana Ilość Graczy: §e" 	+ (min_gracze > 0 ? min_gracze : wybierz),
						ustaw, cmd + "-p min_gracze >> "),
				new Napis("§6Maksymalna Ilość Graczy: §e" 	+ (max_gracze > 0 && max_gracze >= min_gracze ? max_gracze : wybierz),
						ustaw, cmd + "-p max_gracze >> "),
				new Napis("§6Pozycja Startu: §e" 			+ locString.apply(loc_start),
						ustaw, cmd + "-loc start"),
				new Napis("§6Pozycja Zbiórki: §e" 			+ locString.apply(loc_zbiórka),
						ustaw, cmd + "-loc zbiórka")
				); 
		if (RodzajEventu.PierwszyNaMecie.equals(rodzaj)) {
			n.dodajEnd(
					new Napis("§61 Róg mety: §e" + locString.apply(loc_meta1),
							ustaw, cmd + "-loc meta1"),
					new Napis("§62 Róg mety: §e" + locString.apply(loc_meta2),
							ustaw, cmd + "-loc meta2")
					);
		}
		n.dodaj(new Napis("§6Itemy graczy: §e", "§bKlikając item, usuwasz go z listy"));
		for (int i=0; i<itemy.size(); i++)
			n.dodaj(Napis.item(itemy.get(i)).clickEvent(Action.RUN_COMMAND, cmd + "-item usuń " + i)).dodaj(" ");
		n.dodajEnd(new Napis("§a[dodaj]", "§aKliknij aby dodać", cmd + "-item dodaj"));
	
		n.dodajEnd(new Napis("§a[Zatwierdz]", "§bKliknij aby " + 
					(AutoEventy.inst.dajEventy().contains(nazwa) ? "przeedytować" : "stworzyć") + " Event", cmd + "zatwierdz"));
		
		n.wyświetl(p);
	}
	
	void onCommand(Player p, String[] args) throws Throwable {
		if (this.p.getName() != p.getName()) return;
		if (!args[0].equals("-e")) return;
		switch (args[1]) {
		case "-loc":
			this.getClass().getField("loc_" + args[2]).set(this, p.getLocation());
			break;
		case "-p":
			if (args.length >= 4) {
				Field f = this.getClass().getField(args[2]);
				if (args[2].equals("nagroda"))
					f.set(this, Func.Double(args[4], -1));
				else
					f.set(this, Func.Int(args[4], -1));
			}
			break;
		case "-item":
			if 		(args[2].equals("dodaj"))
				itemy.add(p.getInventory().getItemInMainHand().clone());
			else if (args[2].equals("usuń"))
				itemy.remove(Func.Int(args[3], -1));
			break;
		case "typ":
		case "gamemode":
			Napis typ = new Napis("\n\n\n\n§6Rodzaj Eventu: ");
			Enum<?>[] tab = args[1].equals("typ") ? RodzajEventu.values() : GameMode.values();
			int i=tab.length;
			for (Enum<?> r : tab)
				typ.dodaj(new Napis("§e" + r.toString(), "§9Kliknij aby wybrać", cmd + "enum" + args[1] + " " + r.name()))
					.dodaj(--i > 0 ? "§6, " : "");
			typ.wyświetl(p);
			return;
		case "enumtyp":
			rodzaj = RodzajEventu.valueOf(args[2]);
			break;
		case "enumgamemode":
			gamemode = GameMode.valueOf(args[2]);
			break;
		case "nazwa":
			if (args.length >= 4 && !args[3].isEmpty()) {
				if (AutoEventy.inst.dajEventy().contains(args[3]))
					Func.opóznij(1, () 
							-> p.sendMessage(prefix + "§cUWAGA §6Event o tej nazwie już istnieje, zapis spowoduje utracenie go!"));
				this.nazwa = args[3];
			}
			break;
		case "zatwierdz":
			zapisz();
			return;
		default:
			throw new Throwable();
		}
		info();
	}
	
	void zapisz() {
		if (!sprawdz()) {
			p.sendMessage(prefix + "§cNie wszystko zostało poprawnie ustawione");
			return;
		}
		
		Config config = AutoEventy.inst.config;
		String sc = "eventy." + nazwa + ".";
		
		config.ustaw(sc + "rodzaj", rodzaj.name());
		config.ustaw(sc + "gamemode", gamemode.name());
		config.ustaw(sc + "nagroda", nagroda);
		config.ustaw(sc + "min gracze", min_gracze);
		config.ustaw(sc + "max gracze", max_gracze);
		config.ustaw(sc + "czas zbiórki", zbiórka * 60);
		config.ustaw(sc + "czas gry", czas * 60);
		config.ustaw(sc + "start", loc_start);
		config.ustaw(sc + "zbiórka", loc_zbiórka);
		if (!itemy.isEmpty())
			config.ustaw(sc + "itemy", itemy);
		if (RodzajEventu.PierwszyNaMecie.equals(rodzaj)) {
			config.ustaw(sc + "róg mety 1", loc_meta1);
			config.ustaw(sc + "róg mety 2", loc_meta2);
		}
		
		config.zapisz();
		p.sendMessage(prefix + "Zapisano AutoEvent §e" + nazwa);
		AutoEventy.inst.mapaEdytorów.remove(p.getName());
	}
	
	boolean sprawdz() {
		return 	!nazwa.isEmpty() &&
				rodzaj != null &&
				gamemode != null &&
				czas > 0 &&
				zbiórka > 0 &&
				nagroda >= 0 &&
				min_gracze > 0 &&
				max_gracze >= min_gracze &&
				loc_start != null &&
				loc_zbiórka != null &&
				(!RodzajEventu.PierwszyNaMecie.equals(rodzaj) || (loc_meta1 != null && loc_meta2 != null))
				;
	}
}



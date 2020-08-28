package me.jomi.mimiRPG.Maszyny;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Prze�adowalny;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import net.md_5.bungee.api.chat.ClickEvent;

public class JednorekiBandyta extends Komenda implements Listener, Prze�adowalny {
	public static final String prefix = Func.prefix("Jednor�ki Bandyta");
	public static final Config config = new Config("configi/jednoreki bandyta");
	
	protected static JednorekiBandyta inst;
	public JednorekiBandyta() {
		super("automat");
		Main.dodajPermisje("automat.graj");
		inst = this;
	}
	
	private static final List<Automat> automaty = Lists.newArrayList();
	public void prze�aduj() {
		Automat.prze�aduj();
		config.prze�aduj();
		automaty.clear();
		
		Automat ost = null;
		for(String klucz : config.klucze(true))
			if (!klucz.contains(".")) {
				if (ost != null) ost.wczytany();
				ost = new Automat(klucz);
				automaty.add(ost);
			} else
				ost.wczytaj(config, klucz);
		if (ost != null)
			ost.wczytany();
	}
	public String raport() {
		return "�6Automaty Jednor�kiego Bandyty: �e" + automaty.size();
	}
	
	private static final HashMap<String, AutomatTworzony> mapaTworzycieli = new HashMap<>();
	
	public static void anuluj(Player p) {
		mapaTworzycieli.remove(p.getName());
		p.sendMessage(prefix + "Nie tworzysz ju� �adnego automatyu");
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=false)
	public void uderzenie(BlockBreakEvent ev) {
		Player p = ev.getPlayer();
		if (!p.hasPermission("mimiRPG.automat") || p.getInventory().getItemInMainHand() == null ||
			!p.getInventory().getItemInMainHand().getType().equals(Material.STICK) ||
			!mapaTworzycieli.containsKey(p.getName())) return;
		mapaTworzycieli.get(p.getName()).zaznacz(ev.getBlock().getLocation());
		ev.setCancelled(true);
	}
	
	@EventHandler
	public void interakcja(PlayerInteractEvent ev) {
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		if (ev.getAction().equals(Action.LEFT_CLICK_BLOCK))
			for (Automat automat : automaty)
				if (automat.blokAktywacyjny.equals(blok.getLocation())) {
					automat.graj(ev.getPlayer());
					ev.setCancelled(true);
					return;
				}
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupe�nijTabComplete(args, Arrays.asList("stw�rz", "anuluj"));
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Hazard to z�ooo, nawet nie pr�buj ziom");
		Player p = (Player) sender;
		if (args.length < 2) return Main.powiadom(p, prefix + "/automat stworz <nazwa>");
		switch (args[0].toLowerCase()) {
		case "s":
		case "stworz":
		case "stw�rz":
			for (Automat automat : automaty)
				if (automat.nazwa.equals(args[1])) {
					p.sendMessage(prefix + "Ta nazwa autoatu jest ju� zaj�ta");
					return true;
				}
			mapaTworzycieli.put(p.getName(), new AutomatTworzony(p, args[1]));
			break;
		case "anuluj":
			anuluj(p);
			break;
		default:
			mapaTworzycieli.get(p.getName()).komenda(args);
			break;
		}
		return true;
	}
	
}

class Automat {
	public static final String prefix = JednorekiBandyta.prefix;
	
	private static int slotyG�ra = 1;
	private static int slotyD� = 1;
	private static int wczesneRolle = 20;
	private static int czekanieKrok = 2;
	private static int czekanieMin = 5;
	private static int czekanieMax = 20;
	private static int licznikMax = 5;
	public static void prze�aduj() {
		slotyG�ra = (int) Main.ust.wczytaj("JednorekiBandyta.slotyG�ra");
		slotyD� = (int) Main.ust.wczytaj("JednorekiBandyta.slotyD�");
		wczesneRolle = (int) Main.ust.wczytaj("JednorekiBandyta.wczesneRolle");
		czekanieKrok = (int) Main.ust.wczytaj("JednorekiBandyta.czekanieKrok");
		czekanieMax = (int) Main.ust.wczytaj("JednorekiBandyta.czekanieMax");
		czekanieMin = (int) Main.ust.wczytaj("JednorekiBandyta.czekanieMin");
		licznikMax = (int) Main.ust.wczytaj("JednorekiBandyta.licznikMax");
	}
	
	public String nazwa;
	public Automat(String nazwa) {
		this.nazwa = nazwa;
	}
	
	private int koszt = 0;
	private Player gracz = null;
	private boolean broadcastWygrana = false;
	private boolean broadcastPrzegrana = false;
	public Location blokAktywacyjny;
	private List<Location> spiny;
	private List<Wygrana> wygrane = Lists.newArrayList();
	@SuppressWarnings("unchecked")
	public void wczytaj(Config config, String klucz) {
		if (klucz.contains("spiny"))
			spiny = (List<Location>) config.wczytaj(klucz);
		else if (klucz.contains("koszt"))
			this.koszt = (int) config.wczytaj(klucz);
		else if (klucz.contains("broadcastWygrana"))
			this.broadcastWygrana = (boolean) config.wczytaj(klucz);
		else if (klucz.contains("broadcastPrzegrana"))
			this.broadcastPrzegrana = (boolean) config.wczytaj(klucz);
		else if (klucz.contains("blokAktywacyjny"))
			this.blokAktywacyjny = (Location) config.wczytaj(klucz);
		else
			wygrane.add(Wygrana.wczytaj(config, klucz));
	}
	int mx = 0;
	public void wczytany() {
		wygrane = Lists.reverse(wygrane);
		for (Wygrana wygrana : wygrane) {
			wygrana.szansa += mx;
			mx = wygrana.szansa;
		}
	}
	
	public void graj(Player p) {
		if (!p.hasPermission("mimiRPG.automat.graj")) {
			p.sendMessage(prefix + "Nie masz uprawnie� do gry na automatach");
			return;
		}
		if (gracz != null) {
			if (!gracz.getName().equals(p.getName()))
				p.sendMessage(prefix + "Aktualnie gra tu �e" + gracz.getDisplayName());
			return;
		}
		double kasa = Main.econ.getBalance(p);
		if (kasa < koszt) {
			p.sendMessage(prefix + "Nie sta� ci� na ten automat");
			return;
		}
		Main.econ.withdrawPlayer(p, koszt);
		
		gracz = p;
		zakr��();
	}
	private void koniec() {
		gracz = null;
	}
	
	private boolean podlicz() {
		Material ost = spiny.get(0).getBlock().getType();
		
		for (Location spin: spiny)
			if (!spin.getBlock().getType().equals(ost))
				return przegrana();
		return wygrana(ost);
	}
	private boolean wygrana(Material mat) {
		gracz.sendMessage(prefix + "�aWygra�e�");
		
		for (Wygrana wygrana : wygrane)
			if (wygrana.blok.equals(mat)) {
				final String msg = gracz.getDisplayName() + "�a wygra� �6w kasynie �e" + Func.DoubleToString(koszt) + "$";
				if (broadcastWygrana)
					Bukkit.broadcastMessage(prefix + msg);
				else {
					Main.log("[Automat] " + msg);
					gracz.sendMessage(prefix + "Wygra�e� �e" + Func.DoubleToString(wygrana.wygrana) + "$");
				}
				gracz.getWorld().playSound(gracz.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1);
				Main.econ.depositPlayer(gracz, wygrana.wygrana).transactionSuccess();
				koniec();
				return true;
			}
		koniec();
		return false;
	}
	private boolean przegrana() {
		gracz.sendMessage(prefix + "�cPrzegra�e�");
		
		final String msg = gracz.getDisplayName() + "�c przegra� �6w kasynie �e" + Func.DoubleToString(koszt) + "$";
		if (broadcastPrzegrana)
			Bukkit.broadcastMessage(prefix + msg);
		else
			Main.log("[Automat] " + msg);
		
		gracz.getWorld().playSound(gracz.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 1);
		koniec();
		return false;
	}
	
	private Material losujBlok() {
		int liczba = Func.losuj(0, wygrane.get(wygrane.size()-1).szansa);
		
		for (int i=0; i < wygrane.size(); i++)
			if (liczba <= wygrane.get(i).szansa)
				return wygrane.get(i).blok;
		
		Main.log(prefix + "Jest b��d w dzia�aniach, Jednor�kiBandyta -> Automat -> losujBlok\n wylosowana: " + liczba);
		return null;
	}
	private void zakr��() {
		gracz.sendMessage(prefix + "Grasz o �e" + koszt + "$");
		zakr��Wszystkie(wczesneRolle);
	}
	// zakr�ca kilka razy na maksymalnej szybko�ci
	private void zakr��Wszystkie(int licznik) {
		zakr��Kilka(spiny.size());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (licznik <= 0)
		    		_zakr��Wszystkie(czekanieMin);
		    	else
		    		zakr��Wszystkie(licznik - 1);
		    }
		}, czekanieMin);
	}
	// zakr�canie coraz to wolniej
	private void _zakr��Wszystkie(int czekanie) {
		zakr��Kilka(spiny.size());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (czekanie >= czekanieMax)
		    		zakr��(spiny.size(), czekanie, licznikMax);
		    	else
		    		_zakr��Wszystkie(czekanie + czekanieKrok);
		    }
		}, czekanie);
		
	}
	// ostateczne losowanie
	private void zakr��(int ile, int czekanie, int licznik) {
		if (ile <= 0) {
			podlicz();
			return;
		}
		
		zakr��Kilka(ile);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (licznik <= 0) {
		    		gracz.getWorld().playSound(gracz.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1);
			    	zakr��(ile - 1, czekanie, licznikMax);
		    	}
		    	else
		    		zakr��(ile, czekanie, licznik - 1);
		    }
		}, czekanie);
	}
	private void zakr��Kilka(int ile) {
		for (int i=0; i<spiny.size(); i++) {
			if (i >= ile) break;
			zakr��Jeden(spiny.get(i));
			gracz.getWorld().playSound(blokAktywacyjny, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
		}
	}
	private void zakr��Jeden(Location spin) {
		spin = spin.clone();
		spin.add(0, -slotyD�, 0);
		
		for (int i=0; i < slotyD� + slotyG�ra; i++) {
			Block b1 = spin.getBlock();
			Block b2 = spin.add(0, 1, 0).getBlock();
			b1.setType(b2.getType());
		}
		
		spin.getBlock().setType(losujBlok());
	}
}

class AutomatTworzony {
	public static final String prefix = JednorekiBandyta.prefix;
	
	private Player gracz;
	private String nazwa;
	public AutomatTworzony(Player p, String nazwa) {
		this.nazwa = nazwa;
		this.gracz = p;
		status();
	}

	private int koszt = 100;
	private boolean broadcastWygrana = false;
	private boolean broadcastPrzegrana = false;
	private List<Location> spiny = Lists.newArrayList();
	private List<Wygrana> wygrane = Lists.newArrayList();
	private Location blokAktywacyjny;
	private Location wybrany;
	public void status() {
		Napis n = new Napis("�a------- �lAutomat �a-------");
		
		n.dodaj(new Napis("\n�6Koszt gry: �e" + koszt, "�bKliknij aby ustawi�", ClickEvent.Action.SUGGEST_COMMAND, "/automat koszt " + koszt));
		n.dodaj(new Napis("\n�6Info po wygraniu: " 		+ (broadcastWygrana    ? "�aWy�wietlaj" : "�cNie wy�wietlaj"), "�bKliknij aby ustawi�", ClickEvent.Action.SUGGEST_COMMAND, "/automat broadcastWygrana zmien"));
		n.dodaj(new Napis("\n�6Info po przegraniu: " 	+ (broadcastPrzegrana  ? "�aWy�wietlaj" : "�cNie wy�wietlaj"), "�bKliknij aby ustawi�", ClickEvent.Action.SUGGEST_COMMAND, "/automat broadcastPrzegrana zmien"));
		n.dodaj(new Napis("\n�6Blok aktywacyjny: �e" + koordy(blokAktywacyjny), "�bKliknij aby ustawi� wybrany", ClickEvent.Action.SUGGEST_COMMAND, "/automat blokAktywacyjny " + koordy(wybrany)));
		n.dodaj(new Napis("\n�6spiny: ", "�bKliknij aby doda�", ClickEvent.Action.SUGGEST_COMMAND, "/automat dspin auto"));
		if (spiny.isEmpty()) n.dodaj("�6puste");
		else for (Location spin : spiny)
			n.dodaj(new Napis("\n�e " + koordy(spin), "�bKliknij Aby usun��", 
					ClickEvent.Action.SUGGEST_COMMAND, "/automat uspin " + koordy(spin)));
		n.dodaj(new Napis("\n�6wygrane: ", "�bKliknij aby doda� zaznaczony", ClickEvent.Action.SUGGEST_COMMAND, "/automat wygrana " + ((wybrany != null && wybrany.getBlock() != null) ? wybrany.getBlock().getType().toString() : "<blok>") + " "));
		if (wygrane.isEmpty()) n.dodaj("�6puste");
		else for (int i=0; i < wygrane.size(); i++)
			n.dodaj(new Napis("\n�e- " + wygrane.get(i), "�bKliknij Aby usun��",
					ClickEvent.Action.SUGGEST_COMMAND, "/automat uwygrana " + i));
		n.dodaj("\n");
		n.dodaj(new Napis("\n�6Wybrany Blok: �d" + blokLokacji(wybrany), "�bUderz, u�ywaj�c patyka, w chciany blok aby go wybra�"));
		n.dodaj(new Napis("\n�a[Zapisz] ", "�bKliknij aby zapisa�, �cnie b�dzie powrotu", ClickEvent.Action.SUGGEST_COMMAND, "/automat zapisz potwierdz"));
		n.dodaj(new Napis("-------"));
		n.dodaj(new Napis("�c [Anuluj]", "�bKliknij aby anulowa�, �cnie b�dzie powrotu", ClickEvent.Action.SUGGEST_COMMAND, "/automat anuluj potwierdz"));
		n.dodaj("\n");
		
		n.wy�wietl(gracz);
	}
	public void zaznacz(Location loc) {
		wybrany = loc;
		gracz.sendMessage(prefix + "Wybrano blok�d " + blokLokacji(loc));
		status();
	}
	public void komenda(String[] args) {
		switch(args[0]) {
		case "zapisz":
			if (spiny.isEmpty()) 	{gracz.sendMessage(prefix + "Nie ustawiono �adnego spina");   return;}
			if (wygrane.isEmpty()) 	{gracz.sendMessage(prefix + "Nie ustawiono �adnej wygranej"); return;}
			if (blokAktywacyjny == null) {gracz.sendMessage(prefix + "Nie ustawiono BLoku Aktywacyjnego"); return;}
			JednorekiBandyta.config.ustaw(nazwa + ".broadcastPrzegrana", broadcastPrzegrana);
			JednorekiBandyta.config.ustaw(nazwa + ".broadcastWygrana", broadcastWygrana);
			JednorekiBandyta.config.ustaw(nazwa + ".blokAktywacyjny", blokAktywacyjny);
			JednorekiBandyta.config.ustaw(nazwa + ".spiny", spiny);
			JednorekiBandyta.config.ustaw(nazwa + ".koszt", koszt);
			for (Wygrana wygrana : wygrane)
				JednorekiBandyta.config.ustaw(nazwa + "." + wygrana.blok, Arrays.asList(wygrana.szansa, wygrana.wygrana));
			JednorekiBandyta.config.zapisz();
			JednorekiBandyta.anuluj(gracz);
			JednorekiBandyta.inst.prze�aduj();
			return;
		case "koszt":
			int ile = EdytujItem.sprawdz_liczbe(args[1], -1);
			if (ile < 0) {gracz.sendMessage(prefix + "Niepoprawna liczba"); return;}
			koszt = ile;
			break;
		case "broadcastWygrana":
			broadcastWygrana = !broadcastWygrana;
			break;
		case "broadcastPrzegrana":
			broadcastPrzegrana = !broadcastPrzegrana;
			break;
		case "blokAktywacyjny":
			if (wybrany != null) {
				blokAktywacyjny = wybrany;
				break;
			} else {
				gracz.sendMessage(prefix + "Nie wybra�e� �adnego bloku");
				return;
			}
		case "dspin":
			if (spiny.contains(wybrany))
				{gracz.sendMessage(prefix + "Ten spin jest ju� dodany (" + koordy(wybrany) + ")"); return;}
			spiny.add(wybrany);
			break;
		case "uspin":
			Location loc = null;
			try {
				int x = Integer.parseInt(args[1].trim());
				int y = Integer.parseInt(args[2].trim());
				int z = Integer.parseInt(args[3].trim());
				loc = new Location(gracz.getWorld(), x, y, z);
			} catch(NumberFormatException nfe) {
				gracz.sendMessage(prefix + "Nieprawid�owe koordynaty " + args[1] + " " + args[2] + " " + args[3]);
				return;
			}
			spiny.remove(loc);
			break;
		case "wygrana":
			if (args.length < 4) {
				gracz.sendMessage(prefix + "/automat wygrana <blok> <szansa> <wygrana>");
				return;
			}
			try {
				Material typ = Material.valueOf(args[1].toUpperCase());
				if (typ == null) {
					gracz.sendMessage(prefix + "/automat wygrana <blok> <szansa> <wygrana>");
					return;
				}
				int szansa = Integer.parseInt(args[2].trim());
				int wygrana = Integer.parseInt(args[3].trim());
				wygrane.add(new Wygrana(typ, szansa, wygrana));
				break;
			} catch(NumberFormatException nfe) {
				gracz.sendMessage(prefix + "/automat wygrana <blok> <szansa> <wygrana>");
				return;
			}
		case "uwygrana":
			int nr = EdytujItem.sprawdz_liczbe(args[1], -1);
			if (nr == -1 || nr > wygrane.size()) {
				gracz.sendMessage(prefix + "Nieprawid�owy nr lini " + args[1]);
				return;
			}
			wygrane.remove(nr);
			break;
		}
		status();
	}
	
	public static String koordy(Location loc) {
		if (loc == null) return "brak lokalizacji";
		return String.format("%s %s %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	public static String blokLokacji(Location loc) {
		if (loc == null || loc.getBlock() == null)
			return "Nie wybrano";
		return String.format("%s %s %s %s", loc.getBlock().getType().toString().toLowerCase(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
}

class Wygrana {
	@SuppressWarnings("unchecked")
	public static Wygrana wczytaj(Config config, String klucz) {
		List<Integer> lista = (List<Integer>) config.wczytaj(klucz);
		return new Wygrana(
				Material.valueOf(klucz.substring(klucz.indexOf('.')+1)),
				lista.get(0), // szansa
				lista.get(1)  // wygrana
				);
	}
	public int szansa;
	public int wygrana;
	public Material blok;
	public Wygrana(Material blok, int szansa, int wygrana) {
		this.blok = blok;
		this.szansa = szansa;
		this.wygrana = wygrana;
	}

	public String toString() {
		return blok.toString().toLowerCase() + " " + szansa + "% " + wygrana + "$";
	}

}
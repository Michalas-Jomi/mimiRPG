package me.jomi.mimiRPG.JednorekiBandyta;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class AutomatTworzony {
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
		Napis n = new Napis("§a------- §lAutomat §a-------");
		
		n.dodaj(new Napis("\n§6Koszt gry: §e" + koszt, "§bKliknij aby ustawiæ", Action.SUGGEST_COMMAND, "/automat koszt " + koszt));
		n.dodaj(new Napis("\n§6Info po wygraniu: " 		+ (broadcastWygrana    ? "§aWyœwietlaj" : "§cNie wyœwietlaj"), "§bKliknij aby ustawiæ", Action.RUN_COMMAND, "/automat broadcastWygrana zmien"));
		n.dodaj(new Napis("\n§6Info po przegraniu: " 	+ (broadcastPrzegrana  ? "§aWyœwietlaj" : "§cNie wyœwietlaj"), "§bKliknij aby ustawiæ", Action.RUN_COMMAND, "/automat broadcastPrzegrana zmien"));
		n.dodaj(new Napis("\n§6Blok aktywacyjny: §e" + koordy(blokAktywacyjny), "§bKliknij aby ustawiæ wybrany", Action.RUN_COMMAND, "/automat blokAktywacyjny " + koordy(wybrany)));
		n.dodaj(new Napis("\n§6spiny: ", "§bKliknij aby dodaæ", Action.RUN_COMMAND, "/automat dspin auto"));
		if (spiny.isEmpty()) n.dodaj("§6puste");
		else for (Location spin : spiny)
			n.dodaj(new Napis("\n§e " + koordy(spin), "§bKliknij Aby usun¹æ", 
					Action.RUN_COMMAND, "/automat uspin " + koordy(spin)));
		n.dodaj(new Napis("\n§6wygrane: ", "§bKliknij aby dodaæ zaznaczony", Action.SUGGEST_COMMAND, "/automat wygrana " + ((wybrany != null && wybrany.getBlock() != null) ? wybrany.getBlock().getType().toString() : "<blok>") + " "));
		if (wygrane.isEmpty()) n.dodaj("§6puste");
		else for (int i=0; i < wygrane.size(); i++)
			n.dodaj(new Napis("\n§e- " + wygrane.get(i), "§bKliknij Aby usun¹æ",
					Action.RUN_COMMAND, "/automat uwygrana " + i));
		n.dodaj("\n");
		n.dodaj(new Napis("\n§6Wybrany Blok: §d" + blokLokacji(wybrany), "§bUderz, u¿ywaj¹c patyka, w chciany blok aby go wybraæ"));
		n.dodaj(new Napis("\n§a[Zapisz] ", "§bKliknij aby zapisaæ, §cnie bêdzie powrotu", Action.SUGGEST_COMMAND, "/automat zapisz potwierdz"));
		n.dodaj(new Napis("-------"));
		n.dodaj(new Napis("§c [Anuluj]", "§bKliknij aby anulowaæ, §cnie bêdzie powrotu", Action.SUGGEST_COMMAND, "/automat anuluj potwierdz"));
		n.dodaj("\n");
		
		n.wyœwietl(gracz);
	}
	public void zaznacz(Location loc) {
		wybrany = loc;
		gracz.sendMessage(prefix + "Wybrano blok§d " + blokLokacji(loc));
		status();
	}
	public void komenda(String[] args) {
		switch(args[0]) {
		case "zapisz":
			if (spiny.isEmpty()) 	{gracz.sendMessage(prefix + "Nie ustawiono ¿adnego spina");   return;}
			if (wygrane.isEmpty()) 	{gracz.sendMessage(prefix + "Nie ustawiono ¿adnej wygranej"); return;}
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
			JednorekiBandyta.inst.prze³aduj();
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
				gracz.sendMessage(prefix + "Nie wybra³eœ ¿adnego bloku");
				return;
			}
		case "dspin":
			if (spiny.contains(wybrany))
				{gracz.sendMessage(prefix + "Ten spin jest ju¿ dodany (" + koordy(wybrany) + ")"); return;}
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
				gracz.sendMessage(prefix + "Nieprawid³owe koordynaty " + args[1] + " " + args[2] + " " + args[3]);
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
				gracz.sendMessage(prefix + "Nieprawid³owy nr lini " + args[1]);
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

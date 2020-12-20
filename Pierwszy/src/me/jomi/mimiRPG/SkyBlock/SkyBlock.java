package me.jomi.mimiRPG.SkyBlock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import net.md_5.bungee.api.chat.ClickEvent.Action;

//TODO /is
//TODO /is booster
//TODO /is coop
//TODO /is upgrade
//TODO tabcompleter do wyboru angielski/polski
//TODO przycisk back w menu wyspy
//TODO limity bloków
//TODO ulepszanie limitów bloków
//TODO tepać na home spadających w przepaść


@Moduł
public class SkyBlock extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Skyblock");
	public static class TopInfo extends Mapowany {
		@Mapowane int pkt;// TODO pamiętać o zmianach
		@Mapowane List<String> opis; // TODO pamiętać o zmianach
		@Mapowane String nick;// TODO pamiętać o zmianach
		@Mapowane int idWyspy;
	}
	public static class Holder implements InventoryHolder {
		private Inventory inv;
		TypInv typ;
		Wyspa wyspa;
		public Holder(Wyspa wyspa, TypInv typ, int rzędy) {
			init(wyspa, typ, rzędy, Func.enumToString(typ));
		}
		public Holder(Wyspa wyspa, TypInv typ, int rzędy, String nazwa) {
			init(wyspa, typ, rzędy, nazwa);
		}
		private final void init(Wyspa wyspa, TypInv typ, int rzędy, String nazwa) {
			inv = Func.stwórzInv(this, rzędy, Func.enumToString(typ));
			Func.ustawPuste(inv);
			this.typ = typ;
			this.wyspa = wyspa;
		}
		
		@Override
		public Inventory getInventory() {
			return inv;
		}
	}
	public static class Wyspa extends Mapowany {
		public Wyspa() {}// Konstruktor dla mapowanego
		public static class Permisje {
			// TODO blokować usuwanie permisje gdy ktoś ma ją przypisaną
			// TODO customowe grupy permisji
			
			// TODO lista priorytetów grup
			
			String grupa;
			
			boolean niszczenie;
			boolean stawianie;
			boolean dostęp_do_spawnerów;
			
			boolean otwieranie_drzwi_i_furtek;
			boolean otwieranie_skrzyń;
			boolean używanie_przycisków_dzwigni_itp;
			
			boolean wyrzucaniec_członków;
			boolean zapraszanie_członków;
			
			boolean wyrzucanie_odwiedzających;
			
			boolean zmiana_prywatności;
			
			boolean używanie_portalu;
			
			boolean coop;
			
			boolean ustawienie_home_wyspy; // v
			
			boolean dodawanie_i_usuwanie_warpów;
			
			boolean dostęp_do_kasy_banku;
			boolean dostęp_do_expa_banku;
			boolean dostęp_do_magazynu;
			
			boolean bicie_mobów;
			
			boolean podnoszenie_itemów;
			
			boolean usuwanie_grup_permisji;
			boolean tworzenie_grup_permisji;
			boolean edytowanie_permisji;
			boolean awansowanie_członków;
			
			boolean zmiana_dropu; // TODO możliwość wyłączenia poszczzególnego dropu z cobla, w tym cobla
			
			boolean zmiana_nazwy_wyspy;
			
			boolean tworzenie_warpów;
			boolean usuwanie_warpów;
			
			
			List<Boolean> warpy = Lists.newArrayList(); // v
		}
		public static class Warp extends Mapowany {
			@Mapowane String nazwa;
			@Mapowane Location loc;
		}
		public static class Poziomy extends Mapowany {
			// TODO ulepszanie
			@Mapowane int członkowie;
			@Mapowane int generator;
			@Mapowane int wielkość;
			@Mapowane int łowienie;
			@Mapowane int warpy;
			@Mapowane int magazyn;
		}
		
		public static Wyspa nowa(Player p, String typ) {
			Main.log(prefix + p.getName() + "utworzył wyspę typu " + typ);
			
			Wyspa wyspa = Func.utwórz(Wyspa.class);
			
			wyspa.id = configData.wczytajInt("id następnej wyspy");
			
			wyspa.członkowie.put(p.getName(), "właściciel"); // TODO permisja domyślna właściciel, (bez możliwości usunięcia, tak jak członek i odwiedzający)
			
			Krotka<Integer, Integer> xz = następnaPozycja();
			wyspa.locŚrodek = new Location(światWysp, xz.a, yWysp, xz.b);
			
			wyspa.locHome = wyspa.locŚrodek.clone();
			
			wyspa.nazwa = p.getName();
			
			wyspa.zapisz();

			
			double dys0 = wyspa.locŚrodek.distance(new Location(wyspa.locŚrodek.getWorld(), 0, 100, 0));
			Func.insort(new Krotka<>(dys0, wyspa.id), listaLokacji, k -> k.a);
			configData.ustaw("wyspy loc." + wyspa.id, dys0);
			
			configData.ustaw("id następnej wyspy", wyspa.id + 1);
			
			configData.zapisz();
			
			p.teleport(wyspa.locHome);
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = wyspa.id;
			g.zapisz();
			
			return wyspa;
		}
		
		// (loc.distance(new Location(loc.getWorld(), 0, 100, 0), id wyspy)
		static final List<Krotka<Double, Integer>> listaLokacji = Lists.newArrayList();
		

		static Wyspa wczytaj(Location loc) {
			// TODO upewnić się że jest poprawne
			int id = Func.wyszukajBinarnieLIndex(loc.distance(new Location(loc.getWorld(), 0, 100, 0)), listaLokacji, k -> k.a);
			Wyspa wyspa;
			for (int i=id; i <= id+1; i++)
				if ((wyspa = wczytaj(i)) != null && wyspa.zawiera(loc))
					return wyspa;
			
			return null;
		}
		static Wyspa wczytaj(Player p) {
			return wczytaj(Gracz.wczytaj(p));
		}
		static Wyspa wczytaj(Gracz g) {
			return g.wyspa == -1 ? null : wczytaj(g.wyspa);
		}
		// id: wyspa
		static final WeakHashMap<Integer, Wyspa> mapaWysp = new WeakHashMap<>();
		static Wyspa wczytaj(int id) {
			Wyspa wyspa = mapaWysp.get(id);
			if (wyspa == null)
				mapaWysp.put(id, wyspa = (Wyspa) getConfig(id).wczytaj("wyspa"));
			return wyspa;
		}
		
 		static Config getConfig(int id) {
			return new Config("configi/Wyspy/" + id);
		}


		@Mapowane HashMap<String, String> członkowie = new HashMap<>(); // nick: nazwaGrupyPermisji
		@Mapowane Poziomy poziomy = Func.utwórz(Poziomy.class);
		@Mapowane Location locŚrodek;
		@Mapowane String nazwa;
		@Mapowane String typ; // TODO wczytywać schematic
		@Mapowane int id;


		private void usuń() {
			double dys0 = locŚrodek.distance(new Location(locŚrodek.getWorld(), 0, 100, 0));
			if (listaLokacji.remove(Func.wyszukajBinarnieLIndex(dys0, listaLokacji, k -> k.a)).b != id) {
				Main.warn("Znaleziony BUG! Niepoprawnie wyszukawana wyspa errorid:1 Wyspa.usuń()");
				inst.przeładuj();
				return;
			}
			configData.ustaw("wyspy loc." + id, null);
			
			członkowie.keySet().forEach(nick -> {
				Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), Func::tpSpawn);
				Gracz g = Gracz.wczytaj(nick);
				g.wyspa = -1;
				g.zapisz();
			});
			
			getConfig(id).usuń();
			
			powiadomCzłonków("Wyspa została usunięta");
			
			Krotka<Location, Location> rogi = rogi();
			Func.wykonajNaBlokach(rogi.a, rogi.b, blok -> {
				if (blok.getType() == Material.AIR)
					return false;
				blok.setType(Material.AIR, false);
				return true;
			});
		}
		
		public boolean zawiera(Location loc) {
			Krotka<Location, Location> rogi = rogi();
			return Func.zawiera(loc, rogi.a, rogi.b);
		}
		
 		
 		
 		// Permisje
 		
 		static final List<String> permsNietykalne = Arrays.asList("właściciel", "odwiedzający"); // TODO domyślne permisje
 		@Mapowane List<String> permsKody; // grupa: Permisje // posortowane według priorytetów
 		final HashMap<String, Permisje> perms = new HashMap<>();
 		void Init() {
 			wczytajPermisjeZKodów();
 		}
 		void wczytajPermisjeZKodów() {
 			perms.clear();
 			for (String nazwaKod : permsKody) {
 				List<String> nazwaIKod = Func.tnij(nazwaKod, " ");
 				Permisje perm = new Permisje();
 				
 				perm.grupa = nazwaIKod.get(0);
 				String kod = nazwaIKod.get(1);
 				
 				Field[] fields = Permisje.class.getDeclaredFields();
				try {
	 				for (int i=1; i < fields.length; i++)
						fields[i].set(perm, kod.length() > i && kod.charAt(i-1) == '1');
	 				for (int i=fields.length; i < fields.length + warpy.size(); i++)
	 					perm.warpy.add(kod.charAt(fields.length + i) == '1');
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
 				perms.put(perm.grupa, perm);
 			}
 			
 		}
 		void odświeżKodPermisji(Permisje perm) {
 			StringBuilder strB = new StringBuilder();
 			strB.append(perm.grupa).append(' ');
 			
 			Field[] fields = Permisje.class.getDeclaredFields();
			for (int i=1; i < fields.length; i++)
				try {
					strB.append(fields[i].getBoolean(perm) ? '1' : '0');
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			for (Boolean warp : perm.warpy)
				strB.append(warp ? '1' : '0');
			
			
			for (int i=0; i < permsKody.size(); i++)
				if (permsKody.get(i).startsWith(perm.grupa)) {
					permsKody.set(i, strB.toString());
					break;
				}
 		}
 		void odświeżKodyWszystkichPermisji() {
 			Lists.newArrayList(perms.values()).forEach(this::odświeżKodPermisji);
 		}
 		
 		
		Permisje permisje(Player p) {
			if (maBypass(p))
				return perms.get("właściciel");
			return permisje(p.getName());
		}
		Permisje permisje(String nick) {
			return perms.get(członkowie.getOrDefault(nick, "odwiedzający"));
		}
		
		

		// /is permissions
		
		public void permisjeInv(Player p) {
			p.openInventory(getInvPermisje());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory getInvPermisje() {
			Inventory invPermisje = new Holder(this, TypInv.PERMISJE_MAIN, (permsKody.size() - 1) / 9 + 1).getInventory();
			
			int i = 0;
			for (String perm : permsKody) {
				String grp = Func.tnij(perm, " ").get(0);
				invPermisje.setItem(i++, Func.stwórzItem(Material.LIME_STAINED_GLASS_PANE, "&a&l" + grp));
			}
			return invPermisje;
		}
		private void klikanyInvPermisje(Player p, int slot) {
			if (slot >= permsKody.size())
				return;
			int i = -1;
			boolean może = false;
			Permisje perm = permisje(p);
			for (String str : permsKody) {
				if (++i == slot) {
					if (może)
						permisjeEdytuj(p, perms.get(Func.tnij(permsKody.get(i), " ").get(0)));
					else
						p.sendMessage(prefix + "Nie możesz edytować tych uprawnień");
					return;
				}
				if (str.startsWith(perm.grupa))
					może = true;
			}
		}
		private void permisjeEdytuj(Player p, Permisje perm) {
			p.openInventory(permisjeEdytujInv(permisje(p), perm));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory permisjeEdytujInv(Permisje p, Permisje perm) {
			Inventory inv = new Holder(this, TypInv.PERMISJE, (permsKody.get(0).length() - "właściciel ".length() - 1) / 9 + 1, "&4Edytuj Uprawnienia &9" + perm.grupa).getInventory();
				
			Field[] pola = Permisje.class.getDeclaredFields();
			try {
				for (int i=1; i < pola.length - 1; i++) {
					boolean możeP	 = pola[i].getBoolean(p);
					boolean możePerm = pola[i].getBoolean(perm);
					
					inv.setItem(i-1, Func.stwórzItem(
							!możeP ? Material.BLUE_STAINED_GLASS_PANE : (możePerm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE),
							"&9" + pola[i].getName().replace("_", " ")
							));
				}
				for (int i=-1; i < warpy.size() - 1; i++) {
					boolean możeP	 = p.warpy.get(i+1);
					boolean możePerm = perm.warpy.get(i+1);
					
					inv.setItem(pola.length + i, Func.stwórzItem(
							!możeP ? Material.BLUE_STAINED_GLASS_PANE : (możePerm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE),
							"&ewarp " + warpy.get(i+1).nazwa
							));
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
			return inv;
		}
		void klikanyPermisjeEdytujInv(Player p, int slot, String invName, ItemStack item) {
			boolean red = false;
			switch (item.getType()) {
			case RED_STAINED_GLASS_PANE:
				red = true;
			case LIME_STAINED_GLASS_PANE:
				break;
			case BLUE_STAINED_GLASS_PANE:
				p.sendMessage(prefix + "Nie masz uprawnień do tych uprawnień");
			default:
				return;
			}
			Permisje perm = perms.get(invName.substring("&4Edytuj Uprawnienia &9".length()));
			try {
				Field[] pola = Permisje.class.getDeclaredFields();
				if (slot < pola.length - 2)
					Permisje.class.getDeclaredField(item.getItemMeta().getDisplayName().substring(2).replace(" ", "_")).set(perm, red);
				else {
					perm.warpy.set(slot - pola.length + 2, red);
					odświeżInvWarpy();
				}
				item.setType(red ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
				odświeżKodPermisji(perm);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		void odświeżEdytoryPermisji() {
			for (Player p : Bukkit.getOnlinePlayers()) {
				Inventory inv = p.getOpenInventory().getTopInventory();
				Func.wykonajDlaNieNull(inv.getHolder(), Holder.class, holder -> {
					if (holder.wyspa.equals(this))
						if (holder.typ == TypInv.PERMISJE_MAIN)
							permisjeInv(p);
						else if (holder.typ == TypInv.PERMISJE)
							Func.wykonajDlaNieNull(perms.get(p.getOpenInventory().getTitle().substring("&4Edytuj Uprawnienia &9".length())), perm ->
									permisjeEdytuj(p, perm));
				});
			}
		}
		
		
		// /is permsedit
		
		public void edytorPermisji(Player p, String[] args) {
			try {
				Permisje perm = perms.get(args[2]);
				Permisje perm2;
				int mnZwiększ = 1;
				switch (args[1]) {
				case "zwiększ":
					mnZwiększ = -1;
				case "zmniejsz":
					if (permsNietykalne.contains(perm.grupa))
						break;
					for (int i = 0; i < permsKody.size(); i++)
						if (permsKody.get(i).startsWith(perm.grupa)) {
							perm2 = kodToPerm(i + mnZwiększ);
							if (permsNietykalne.contains(perm2.grupa))
								break;
							String s1 = permsKody.get(i);
							String s2 = permsKody.get(i + mnZwiększ);
							permsKody.set(i, s2);
							permsKody.set(i + mnZwiększ, s1);
							odświeżEdytoryPermisji();
							zapisz();
							break;
						}
					break;
				case "usuń":
					Matcher mat = Pattern.compile("Tak, chcę usunąć grupe permisji (\\w+)").matcher(Func.listToString(args, 3));
					if (mat.find())
						Func.wykonajDlaNieNull(perms.get(mat.group(1)), permisja -> {
							if (permsNietykalne.contains(permisja.grupa)) {
								p.sendMessage(prefix + "Nie można usunąć tej grupy permisji");
								return;
							}
							for (String permisjaCzłonka : członkowie.values())
								if (permisjaCzłonka.equals(permisja.grupa)) {
									p.sendMessage(prefix + "Nie można usunąć grupy w której są jacyś gracze!");
									return;
								}
							for (int i=0; i < permsKody.size(); i++)
								if (permsKody.get(i).startsWith(permisja.grupa)) {
									permsKody.remove(i);
									break;
								}
							perms.remove(permisja.grupa);
							odświeżEdytoryPermisji();
							zapisz();
						});
					break;
				case "dodaj":
					if (args.length <= 3) {
						p.sendMessage(prefix + "po znaku >> wpisz nazwe grupy");
						return;
					}
					if (args.length >= 5) {
						p.sendMessage(prefix + "Nazwa grupy musi być pojedyńczym słowem");
						return;
					}
					String nazwa = args[3];
					if (perms.containsKey(nazwa)) {
						p.sendMessage(prefix + "Grupa permisji o tej nazwie już istnieje");
						return;
					}
					StringBuilder strB = new StringBuilder();
					try {
						for (int i=0; i < Permisje.class.getDeclaredFields().length; i++)
							strB.append('0');
						for (int i=0; i < warpy.size(); i++)
							strB.append('0');
						
					} catch (Throwable e) {
						e.printStackTrace();
					}
					permsKody.add(permsKody.size() - 1, strB.toString());
					wczytajPermisjeZKodów();
					odświeżEdytoryPermisji();
					zapisz();
					break;
				}
			} catch (Throwable e) {}
			edytorPermisjiWyświetl(p);
		}
		void edytorPermisjiWyświetl(Player p) {
			Napis n = new Napis("\n\n§9Permisje wyspy: \n\n");
			for (String grp : permsKody) {
				grp = grp.substring(0, grp.indexOf(' '));
				n.dodaj("§e- §d" + grp + " ");
				if (!permsNietykalne.contains(grp)) {
					n.dodaj(new Napis("§6↑", "§bKliknij aby zwiększyć priorytet",  "/is permsedit zwiększ "  + grp));
					n.dodaj(" ");
					n.dodaj(new Napis("§6↓", "§bKliknij aby zmniejszyć priorytet", "/is permsedit zmniejsz " + grp));
					n.dodaj("   ");
					n.dodaj(new Napis("§[x]", "§cKliknij aby usunąć\ngrupe §4" + grp, Action.SUGGEST_COMMAND, "/is permsedit usuń >> Tak, chcę usunąć grupe permisji " + grp));
				}
				n.dodaj("\n");
				n.dodaj("§a[nowa]", "§bKliknij aby dodać nową grupe", "/is permsedit dodaj >> ");
				n.dodaj("\n\n");
			}
			n.wyświetl(p);
		}
		
		
		// /is warps
		
		@Mapowane List<Warp> warpy; 
		public void otwórzWarpy(Player p) {
			p.openInventory(getInvWarpy(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory getInvWarpy(Player p) {
			Inventory invWarpy = new Holder(this, TypInv.WARPY, (warpy.size() - 1) / 9 + 1).getInventory();
			
			Permisje perms = permisje(p);
			
			int i = -1;
			for (Warp warp : warpy)
				invWarpy.setItem(++i,
						Func.stwórzItem(
								perms.warpy.get(i) ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS_PANE,
								"&9&l" + warp.nazwa
						)
				);
			
			return invWarpy;
		}
		private void klikanyInvWarp(Player p, int slot, ItemStack item) {
			if (slot >= warpy.size() || slot < 0)
				return;
			if (item.getType() == Material.LIME_STAINED_GLASS_PANE)
				tpToWarp(p, warpy.get(slot));
			else
				p.sendMessage(prefix + "Nie masz uprawnień na korzystanie z tego warpa");
		}
		public void tpToWarp(Player p, Warp warp) {
			p.teleport(warp.loc);
			p.sendMessage(prefix + Func.msg("Zostałeś przeteleportowany na warp %s", warp.nazwa));
		}
		
		void odświeżInvWarpy() {
			for (Player p : Bukkit.getOnlinePlayers())
				Func.wykonajDlaNieNull(p.getOpenInventory().getTopInventory().getHolder(), Holder.class, holder -> {
					if (holder.wyspa.equals(this))
						if (holder.typ == TypInv.WARPY)
							otwórzWarpy(p);
						else if (holder.typ == TypInv.DEL_WARP)
							otwórzInvDelWarp(p);
						
				});
		}
		
		// /is addwarp
		
		public void dodajWarp(Player p, String nazwa) {
			// TODO limit warpów
			
			if (!zawiera(p.getLocation())) {
				p.sendMessage(prefix + "Nie możesz tu ustawić warpa wyspy");
				return;
			}
			
			Warp warp = new Warp();
			warp.loc = p.getLocation();
			warp.nazwa = Func.koloruj(nazwa);
			warpy.add(warp);
			
			perms.values().forEach(perm -> perm.warpy.add(true));
			

			odświeżInvWarpy();
			
			odświeżKodyWszystkichPermisji();
			odświeżEdytoryPermisji();

			zapisz();
		}
		
		// /is delwarp
		
		public void otwórzInvDelWarp(Player p) {
			p.openInventory(dajInvDelWarp(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		Inventory dajInvDelWarp(Player p) {
			Inventory inv = new Holder(this, TypInv.DEL_WARP, Func.potrzebneRzędy(warpy.size())).getInventory();
			
			Permisje perm = permisje(p);
			
			for (int i=0; i < warpy.size(); i++)
				inv.setItem(i, Func.stwórzItem(perm.warpy.get(i) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "&b&l" + warpy.get(i).nazwa));
			
			return inv;
		}
		void klikanieInvDelWarp(Player p, int slot, Material mat) {
			if (mat == Material.RED_STAINED_GLASS_PANE)
				p.sendMessage(prefix + "Nie masz dostępu do tego warpu");
			else if (mat == Material.LIME_STAINED_GLASS_PANE)
				usuńWarp(p, slot);
		}
		void usuńWarp(Player p, int index) {
			warpy.remove(index);
			
			perms.values().forEach(perm -> perm.warpy.remove(index));
			
			
			odświeżInvWarpy();

			odświeżKodyWszystkichPermisji();
			odświeżEdytoryPermisji();
			
			zapisz();
		}
		
		
		// /is bank
		
		@Mapowane int exp;
		@Mapowane int kasa;
		public void otwórzBank(Player p) {
			p.openInventory(getInvBank());
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		static int slotBankKasa = 11; 	   // TODO wczytywać
		static int slotBankExp = 13; 	  // ^
		static int slotBankMagazyn = 15; // ^
		private Inventory invBank = null;
		private Inventory getInvBank() {
			if (invBank == null) {
				invBank = new Holder(this, TypInv.BANK, 3).getInventory();
				invBank.setItem(slotBankKasa,	Func.stwórzItem(Material.PAPER, "&6Pieniądze", ""));
				invBank.setItem(slotBankExp,	Func.stwórzItem(Material.EXPERIENCE_BOTTLE, "&6Exp", ""));
				invBank.setItem(slotBankMagazyn,Func.stwórzItem(Material.CHEST, "&6Magazyn"));
				odświeżInvBank();
			}
			
			return invBank;
		}
		private void odświeżInvBank() {
			Func.ustawLore(invBank.getItem(slotBankKasa), "&7Dostępne środki: &a" + kasa + "$", 0);
			Func.ustawLore(invBank.getItem(slotBankExp),  "&7Zgromadzony Exp: &a" + exp, 0);
		}
		public void wpłaćExp(Player p, int ile) {
			if (ile == 0 || (ile > 0 && Poziom.policzCałyExp(p) < ile))
				return;
			p.giveExp(-ile);
			exp += ile;
			odświeżInvBank();
			zapisz();
			Main.log(prefix + p.getName() + "wpłacił " + ile + " expa do banku, całkowita ilość: " + exp);
		}
		public void wpłaćPieniądze(Player p, int ile) {
			if (ile == 0 || (ile > 0 && Main.econ.getBalance(p) < ile))
				return;
			Main.econ.withdrawPlayer(p, ile);
			kasa += ile;
			odświeżInvBank();
			zapisz();
			Main.log(prefix + p.getName() + "wpłacił " + ile + "$ do banku, całkowita ilość: " + kasa);	
		}
		private void klikanyBank(Player p, int slot, ClickType clickType) {
			int min;
			int prawy;
			Supplier<Integer> lewy;
			
			BiConsumer<Player, Integer> bic = null;
			if (slot == slotBankKasa) {
				lewy = () -> Math.abs((int) Main.econ.getBalance(p));
				bic = this::wpłaćPieniądze;
				min = 1000;
				prawy = kasa;
			} else if (slot == slotBankExp) {
				lewy = () -> Poziom.policzCałyExp(p);
				bic = this::wpłaćExp;
				min = 1725;
				prawy = exp;
			} else if (slot == slotBankMagazyn) {
				otwórzMagazyn(p);
				return;
			} else
				return;

			int ile;
			switch (clickType) {
			// wpłacanie
			case LEFT:		 ile = Math.min(min, lewy.get());	break;
			case SHIFT_LEFT: ile = lewy.get();					break;
			// wypłacanie
			case RIGHT:		  ile = -Math.min(min, prawy);		break;
			case SHIFT_RIGHT: ile = -prawy;						break;
			default:
				return;
			}
				
			bic.accept(p, ile);
			
		}
		
		
		// /is storage

		@Mapowane List<ItemStack> magazynItemów;
		public void otwórzMagazyn(Player p) {
			p.openInventory(getInvMagazyn());
		}
		private Inventory invMagazyn = null;
		private Inventory getInvMagazyn() {
			if (invMagazyn == null) {
				invMagazyn = new Holder(this, TypInv.MAGAZYN, poziomy.magazyn + 1).getInventory();
				invMagazyn.clear();
				int i=0;
				for (ItemStack item : magazynItemów)
					invMagazyn.setItem(i++, item);
			}
			return invMagazyn;
		}
		private void zamknięcieMagazynu() {
			magazynItemów.clear();
			invMagazyn.forEach(magazynItemów::add);
			zapisz();
		}
		
		
		// /is sethome

		@Mapowane Location locHome;
		public void ustawHome(Player p) {
			if (!permisje(p).ustawienie_home_wyspy)
				p.sendMessage(prefix + "Nie masz uprawnień na ustawnie home wyspy");
			else {
				locHome = p.getLocation();
				zapisz();
				p.sendMessage(prefix + "Ustawiono nowy home wyspy");
			}
		}
		
		
		// /is home
		
		public void tpHome(Player p) {
			p.teleport(locHome);
			p.sendMessage(prefix + "Zostałeś przeleportowany na swoją wyspę");
		}
		
		
		// /is public /is private

		@Mapowane boolean prywatna = true;
		public void ustawPubliczna(Player p) {
			ustawPubliczność(p, true);
		}
		public void ustawPrywatna(Player p) {
			if (ustawPubliczność(p, false)) {
				// TODO wyrzucić wszystkich z wyspy
			}
		}
		private boolean ustawPubliczność(Player p, boolean publiczna) {
			String msg = publiczna ? "publiczn" : "prywatn";
			if (prywatna == publiczna) {
				prywatna = !prywatna;
				zapisz();
				p.sendMessage(prefix + "Ustawiono wyspę na " + msg + "ą");
				return true;
			} else
				p.sendMessage(prefix + "Twoja wyspa jest już " + msg + "a");
			return false;
		}
		
		
		// /is value

		@Mapowane int pkt;
		int ostatnieLiczenie = 0;
		public void wartość(Player p) {
			int teraz = (int) (System.currentTimeMillis() / 1000);
			
			int mineło = teraz - ostatnieLiczenie;
			
			if (mineło >= czasCooldownuLiczeniaWartości || maBypass(p)) {
				ostatnieLiczenie = teraz;
				p.sendMessage(prefix + "Liczenie wartości wyspy");
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					policzWartość();
					if (Gracz.wczytaj(p).wyspa != id)
						p.sendMessage(prefix + Func.msg("Aktualna wartość ich wyspy: %s", pkt));
					członkowie.keySet().forEach(nick -> Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), gracz ->
							gracz.sendMessage(prefix + Func.msg("Aktualna wartość twojej wyspy: %s", pkt))));
				});
			} else
				p.sendMessage(prefix + Func.msg("musisz jeszcze poczekać %s żeby ponownie przeliczyć wartość wyspy",
						Func.czas(czasCooldownuLiczeniaWartości - mineło)));
			
		}
		public int policzWartość() {
			int ile = 0;
			Set<Material> omijane = Sets.newConcurrentHashSet();
			
			Krotka<Location, Location> rogi = rogi();
			
			for (Block blok : Func.bloki(rogi.a, rogi.b)) {
				Material mat = blok.getType();
				if (omijane.contains(mat))
					continue;
				int pkt = punktacja.get(mat);
				if (pkt == 0)
					omijane.add(mat);
				else
					ile += pkt;
			}
			
			if (ile != pkt) {
				pkt = ile;
				zapisz();
			}
			return ile;
		}
		
		
		// /is invite /is join
		
		private static final String metaZaproszenie = "mimiSkyblockZaproszenie";
		public void zaproś(Player p, Player kogo) {
			// TODO spradzanie limitu członków wyspy
			// TODO jakiś cooldown na tą samą osobe
			
			if (Gracz.wczytaj(kogo).wyspa != -1) {
				Func.powiadom(prefix, p, "%s ma już wyspę", kogo.getDisplayName());
				return;
			}
			
			Func.ustawMetadate(kogo, metaZaproszenie, p);
			
			Main.panelTakNie(kogo,
					"&4Zaproszenie na wyspe " + p.getDisplayName(),
					"&aDołącz do wyspy &7" + p.getDisplayName(),
					"&cOdrzuć zaproszenie do wyspy &7" + p.getDisplayName(),
					() -> przyjmijZaproszenie(kogo),
					() -> odrzućZaproszenie(kogo));
			
			p.sendMessage(prefix + "Wysłano zaproszenie na wyspy do " + kogo.getDisplayName());
		}
		void przyjmijZaproszenie(Player p) {
			// TODO ponowne spradzanie limitu członków wyspy
			
			członkowie.put(p.getName(), "członek");
			zapisz();
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = id;
			g.zapisz();
			
			powiadomCzłonków("%s dołączył wyspy", p.getName());
			
			p.removeMetadata(metaZaproszenie, Main.plugin);
			
			odświeżInvMembers();
		}
		void odrzućZaproszenie(Player p) {
			Player zapraszający = (Player) p.getMetadata(metaZaproszenie).get(0).value();
			
			Func.powiadom(prefix, p, "Odrzuciłeś zaproszenie do wyspy %s", zapraszający.getDisplayName());
			Func.powiadom(prefix, p, "%s odrzucił twoje zaproszenie do wyspy", zapraszający.getDisplayName());
			
			p.removeMetadata(metaZaproszenie, Main.plugin);
		}

		
		// /is leave
		
		public void opuść(Player p) {
			powiadomCzłonków("%s opuścił wyspę", p.getDisplayName());

			członkowie.remove(p.getName());
			zapisz();
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = -1;
			g.zapisz();
			
			odświeżInvMembers();
			
			Func.tpSpawn(p);
		}
		
		
		// /is visit
		
		public void odwiedz(Player p) {
			if (członkowie.containsKey(p.getName()))
				tpHome(p);
			else if (prywatna)
				p.sendMessage(prefix + "Ta wyspa jest prywatna");
			else {
				p.teleport(locHome);
				powiadomCzłonków("%s Odwiedza twoją wyspę", p.getDisplayName());
				p.sendMessage(prefix + Func.msg("Odwiedzasz wyspę %s", nazwa));
			}
		}
		
		
		// /is delete //TODO tylko własciciel
		
		public void usuń(Player p) {
			Main.panelTakNie(p, "&4Usunąć wyspe?", "&aTak, usuń wyspę", "&4Nie, nie usuwał wyspy", this::usuń, null);
		}
		
		
		// /is members
		
		public void otwórzMembers(Player p) {
			p.openInventory(getInvMembers(p));
			p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
		}
		private Inventory getInvMembers(Player p) {
			Inventory inv = new Holder(this, TypInv.CZŁONKOWIE, Func.potrzebneRzędy(członkowie.size())).getInventory();
			
			Permisje perm = permisje(p);
			
			int i = 0;
			boolean był = false;
			String[] nicki = new String[członkowie.size()];
			for (String grupa : permsKody) {
				grupa = kodToStrPerm(grupa);
				for (Entry<String, String> en : członkowie.entrySet())
					if (grupa.equals(en.getValue())) {
						nicki[i] = en.getKey();
						ItemStack item = Func.stwórzItem(Material.PLAYER_HEAD, "&9&l" + en.getKey(), "&6Ranga: &a" + grupa);
						if (był)
							Func.dodajLore(Func.dodajLore(item, "&8LPM aby awansować"), "&8PPM aby degradować");
						inv.setItem(i++, item);
					}
				if (!był && grupa.equals(perm.grupa))
					był = true;
			}
			
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
				for (int j=0; j < nicki.length; j++)
					if (inv.getViewers().isEmpty())
						return;
					else
						Func.ustawGłowe(Func.graczOffline(nicki[j]), inv.getItem(j));
			});
			
			return inv;
		}
		void klikanyInvMembers(Player p, ItemStack item, ClickType typ) {
			if (!item.getType().equals(Material.PLAYER_HEAD) || !Func.multiEquals(typ, ClickType.RIGHT, ClickType.LEFT))
				return;
			String nick2 = item.getItemMeta().getDisplayName().substring(4);
			
			Permisje permP = permisje(p);
			Permisje perm2 = permisje(nick2);
			
			
			int mn = typ == ClickType.LEFT ? -1 : 1;
			int indexPerm2 = indexPerm(perm2);
			
			if (indexPerm(permP) >= indexPerm2 + mn || permsNietykalne.contains(kodToStrPerm(indexPerm2 + mn))) {
				p.sendMessage(prefix + "Nie możesz tego zrobić");
				return;
			}
			
			permsKody.add(indexPerm2 + mn, permsKody.remove(indexPerm2));
			odświeżInvMembers();
			zapisz();
			
			p.sendMessage(prefix + Func.msg("%s gracza %s!", mn == -1 ? "Awansowałeś" : "Zdegradowałeś", nick2));
			Func.wykonajDlaNieNull(Bukkit.getPlayer(nick2), p2 -> p.sendMessage(prefix + Func.msg("%s %s cię!", p.getDisplayName(), mn == -1 ? "Awansował" : "Zdegradował")));
		}
		
		void odświeżInvMembers() {
			for (Player p : Bukkit.getOnlinePlayers()) {
				Inventory inv = p.getOpenInventory().getTopInventory();
				Func.wykonajDlaNieNull(inv.getHolder(), Holder.class, holder -> {
					if (holder.wyspa.equals(this) && holder.typ == TypInv.CZŁONKOWIE)
						otwórzMembers(p);
				});
			}
		}
		
		
		// /is kick
		
		public void wyrzuć(Player p, String kogo) {
			if (p.getName().equalsIgnoreCase(kogo)) {
				p.sendMessage(prefix + "zamiast tego użyć /is opuść");
				return;
			}
			if (!członkowie.containsKey(kogo)) {
				for (String nick : członkowie.keySet())
					if (nick.equalsIgnoreCase(kogo)) {
						kogo = nick;
						break;
					}
				if (!członkowie.containsKey(kogo)) {
					Player kogoP = Bukkit.getPlayer(kogo);
					if (kogoP == null) {
						p.sendMessage(prefix + Func.msg("%s nie jest online i nie należy do twojej wyspy", kogo));
						return;
					}
					if (zawiera(kogoP.getLocation())) {
						Func.tpSpawn(kogoP);
						p.sendMessage(prefix + Func.msg("Wyprosiłeś %s ze swojej wyspy", kogoP.getDisplayName()));
						kogoP.sendMessage(prefix + Func.msg("%s wyprosił cie ze swojej wyspy", p.getDisplayName()));
					}
					return;
				}
			}
			powiadomCzłonków("%s wyrzucił %s z wyspy!", p.getName(), kogo);
			
			członkowie.remove(kogo);

			zapisz();
			
			Gracz g = Gracz.wczytaj(kogo);
			g.wyspa = -1;
			g.zapisz();
			
			odświeżInvMembers();
			
			Func.wykonajDlaNieNull(Bukkit.getPlayer(kogo), Func::tpSpawn);
		}
		
		
		// /is biome
		
		public void zmieńBiom(Player p, String biom) {
			Krotka<Location, Location> rogi = rogi();
			World świat = locŚrodek.getWorld();
			// TODO obsługa komendy
			// TODO panel
			// TODO dostępne biomy
			Biome biome = Biome.valueOf(biom.toUpperCase()); // TODO obsługa błedu
			Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> 
				Func.wykonajNaBlokach(rogi.a, rogi.b, (x, y, z) -> {
						świat.setBiome(x, y, z, biome);
						return true;
			}));
		}
		
		
		// /is name
		
		public void zmieńNazwe(Player p, String nazwa) {
			this.nazwa = Func.koloruj(nazwa);
			p.sendMessage(prefix + Func.msg("Zmieniono nazwę wyspy na %s", this.nazwa));
			zapisz();
		}
		
		
		
		// ogólne odniesienia
		
		void zamykany(Player p, TypInv typ, InventoryCloseEvent ev) {
			if (typ == TypInv.MAGAZYN)
				zamknięcieMagazynu();
		}
		
		public Krotka<Location, Location> rogi() {
			return null; // TODO napisać
		}
		
		
		// zapis
		
		private boolean zapisano = false;
		private boolean doZapisania = false;
		public void zapisz() {
			doZapisania = true;
			if (!zapisano) {
				zapiszNatychmiast();
				zapisano = true;
				doZapisania = false;
				Func.opóznij(20*60, () -> {
					zapisano = false;
					if (doZapisania)
						zapisz();
				});
			}
		}
		public void zapiszNatychmiast() {
			getConfig(id).ustaw_zapisz("wyspa", this);	
		}
		
		
		// util
		
		void powiadomCzłonków(String msg, Object... uzupełnienia) {
			msg = Func.msg(msg, uzupełnienia);
			if (!msg.startsWith(prefix))
				msg = prefix + msg;
			String _msg = msg;
			członkowie.keySet().forEach(nick -> Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(_msg)));
		}

		Permisje kodToPerm(int i) {
			return kodToPerm(permsKody.get(i));
		}
		Permisje kodToPerm(String kod) {
			return perms.get(kodToStrPerm(kod));
		}
		String kodToStrPerm(int i) {
			return kodToStrPerm(permsKody.get(i));
		}
		String kodToStrPerm(String kod) {
			return kod.substring(0, kod.indexOf(' '));
		}
		int indexPerm(Permisje perm) {
			return indexPerm(perm.grupa);
		}
		int indexPerm(String perm) {
			for (int i=0; i < permsKody.size(); i++)
				if (kodToStrPerm(i).equals(perm))
					return i;
			throw new Error("Permisja " + perm + " nie została odnaleziona");
		}
		
		
		// Override
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			
			return obj instanceof Wyspa && ((Wyspa) obj).id == this.id;
		}		
	}
	static enum TypInv {
		TOP				((wyspa, p, typ, ev) -> {}),
		MAGAZYN			((wyspa, p, typ, ev) -> {}),
		BANK			((wyspa, p, typ, ev) -> wyspa.klikanyBank(p, ev.getRawSlot(), ev.getClick())),
		WARPY			((wyspa, p, typ, ev) -> wyspa.klikanyInvWarp(p, ev.getRawSlot(), ev.getCurrentItem())),
		CZŁONKOWIE		((wyspa, p, typ, ev) -> wyspa.klikanyInvMembers(p, ev.getCurrentItem(), ev.getClick())),
		DEL_WARP		((wyspa, p, typ, ev) -> wyspa.klikanieInvDelWarp(p, ev.getRawSlot(), ev.getCurrentItem().getType())),
		PERMISJE		((wyspa, p, typ, ev) -> wyspa.klikanyPermisjeEdytujInv(p, ev.getRawSlot(), ev.getView().getTitle(), ev.getCurrentItem())),
		PERMISJE_MAIN	((wyspa, p, typ, ev) -> wyspa.klikanyInvPermisje(p, ev.getRawSlot()));
		private static interface TypInvConsumer {
			void wykonaj(Wyspa wyspa, Player p, TypInv typ, InventoryClickEvent ev);
		}
		
		TypInvConsumer cons;
		TypInv(TypInvConsumer cons) {
			this.cons = cons;
		}
	}
	
	static SkyBlock inst;
	public SkyBlock() {
		super("is");
		inst = this;
	}
	

	private static final Set<String> zBypassem = Sets.newConcurrentHashSet();;
	public static boolean maBypass(Player p) {
		return zBypassem.contains(p.getName());
	}
	public static void ustawBypass(Player p, boolean stan) {
		Consumer<String> cons = (stan ? zBypassem::add : zBypassem::remove);
		cons.accept(p.getName());
	}
	
	
	
	// Event Handler
	
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			if (holder.wyspa == null) {
				// TODO /is visit <- InvTyp == TOP
			} else
				holder.typ.cons.wykonaj(holder.wyspa, (Player) ev.getWhoClicked(), holder.typ, ev);
		});
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder ->
				holder.wyspa.zamykany((Player) ev.getPlayer(), holder.typ, ev));
	}
	
	
	
	// /is top
	
	public void otwórzTopke(Player p) {
		p.openInventory(dajInvTop());
		p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
	}
	private Inventory invTop;
	private Inventory dajInvTop() {
		if (invTop == null) {
			invTop = new Holder(null, TypInv.TOP, rzędyTopki).getInventory();
			odświeżInvTop();
		}
		return invTop;
	}
	private BukkitTask taskInvTop;
	void odświeżInvTop() {
		if (taskInvTop != null) {
			taskInvTop.cancel();
			taskInvTop = null;
		}
		for (int i=0; i < slotyTopki.size() && i < topInfo.size(); i++) {
			TopInfo top = topInfo.get(i);
			invTop.setItem(slotyTopki.get(i), Func.stwórzItem(Material.PLAYER_HEAD, "&1&l" + top.nick, top.opis));
		}
		// TODO zapis topki
		taskInvTop = Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			for (int i=0; i < slotyTopki.size() && i < topInfo.size(); i++) {
				TopInfo top = topInfo.get(i);
				int slot = slotyTopki.get(i);
				invTop.setItem(slot, Func.ustawGłowe(Func.graczOffline(top.nick), invTop.getItem(slot)));
			}
			taskInvTop = null;
		});
	}
	
	
	
	// I/O
	
	static final Config configData = new Config("configi/SkyBlock Data");
	
	static final HashMap<Material, Integer> punktacja = new HashMap<>(); // TODO wczytywać
	
	static int rzędyTopki = 3;
	static List<Integer> slotyTopki;
	static List<TopInfo> topInfo;// TODO przechowuje troche więcej niż potrzeba powiedzmy 1.5 raza
	static World światWysp;
	static World światNether;
	static int yWysp;
	static int czasCooldownuLiczeniaWartości;
	
	
	// Następna pozycja wyspy
	
	public static class NastępnaPozycja extends Mapowany {
		@Mapowane int faza;
		@Mapowane int mx;
		@Mapowane int x;
		@Mapowane int y;
	}
	static Krotka<Integer, Integer> następnaPozycja() {
		NastępnaPozycja next = configData.wczytajLubDomyślna("następna", () -> Func.utwórz(NastępnaPozycja.class));
		Krotka<Integer, Integer> w = new Krotka<>(next.x, next.y);
		
		boolean r = false;
		
		switch(next.faza) {
		case 0:
			if (r = (--next.y < -next.mx))
				next.y++;
			break;
		case 1:
			if (r = (--next.x < -next.mx))
				next.x++;
			break;
		case 2:
			if (r = (++next.y > next.mx))
				next.y--;
			break;
		case 3:
			if (r = (++next.x > next.mx)) {
				next.x = next.y = ++next.mx;
				next.faza = -1;
			}
			break;
		}
		
		if (r)
			next.faza++;
		configData.ustaw_zapisz("następna", next);
		return r ? następnaPozycja() : w;
	}
	
	
	// Override
	
	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		Wyspa.listaLokacji.clear();
		Func.wykonajDlaNieNull(configData.sekcja("wyspy loc"), sekcja ->
				sekcja.getValues(false).entrySet().forEach(en ->
						Func.insort(new Krotka<>((double) en.getValue(), Func.Int(en.getKey())), Wyspa.listaLokacji, k -> k.a))
		);
		
		
		rzędyTopki = Math.max(1, Math.min(6, Main.ust.wczytajInt("Skyblock.topka.rzędy")));
		slotyTopki = Func.nieNullList((List<Integer>) Main.ust.wczytaj("Skyblock.topka.sloty"));
		topInfo = Func.nieNullList((List<TopInfo>) Main.ust.wczytaj("SkyBlock.topka.gracze"));
		światWysp = Bukkit.getWorld(Main.ust.wczytajLubDomyślna("Skyblock.świat.zwykły", "SkyblockNormalny"));
		światNether = Bukkit.getWorld(Main.ust.wczytajLubDomyślna("Skyblock.świat.nether", "SkyblockNether"));
		yWysp = Main.ust.wczytajLubDomyślna("Skyblock.y wysp", 100);
		czasCooldownuLiczeniaWartości = Main.ust.wczytajLubDomyślna("Skyblock.cooldown.liczenie punktów", 60*30);
	}
	@Override
	public Krotka<String, Object> raport() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p2;
		Gracz g2;

		Gracz g = null;
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
			g = Gracz.wczytaj(p);
		}
		
		// gracz nie musi mieć wyspy
		
		switch(args[0]) {
		case "visit":
		case "odwiedz":
			if (args.length < 2)
				return Func.powiadom(sender, prefix + "/is visit <nick>");
			g2 = Gracz.wczytaj(args[1]);
			if (g2.wyspa == -1)
				return Func.powiadom(sender, prefix + Func.msg("%s nie posiada wyspy", g2.nick));
			Wyspa.wczytaj(g2).odwiedz(p);
			return true;
		case "bypass": // TODO pamiętać o permisjach
			ustawBypass(p, !maBypass(p));
			return Func.powiadom(sender, prefix + (maBypass(p) ? "w" : "wy") + "łączono bypass");
		}
		
		
		Wyspa wyspa = g == null ? null : Wyspa.wczytaj(g.wyspa);
		if (p == null)
			return Func.powiadom(prefix, sender, "Tylko gracz może zarządzać wyspą");
		if (g.wyspa == -1)
			return Func.powiadom(prefix, sender, "Problem jest następujący: Brak wyspy");
		
		switch (args[0].toLowerCase()) {
		case "bank":
			wyspa.otwórzBank(p);
			break;
		case "storage":
		case "magazyn":
			wyspa.otwórzMagazyn(p);
			break;
		case "sethome":
		case "ustawdom":
			wyspa.ustawHome(p);
			break;
		case "home":
		case "dom":
			wyspa.tpHome(p);
			break;
		case "public":
		case "publiczna":
			wyspa.ustawPubliczna(p);
			break;
		case "private":
		case "prywatna":
			wyspa.ustawPrywatna(p);
			break;
		case "warp":
			if (args.length > 1) {
				String nazwa = Func.listToString(args, 1);
				for (Wyspa.Warp warp : wyspa.warpy)
					if (warp.nazwa.equalsIgnoreCase(nazwa)) {
						wyspa.tpToWarp(p, warp);
						return true;
					}
			}
		case "warps":
		case "warpy":
			wyspa.otwórzWarpy(p);
			break;
		case "value":
		case "wartość":
			wyspa.wartość(p);
			break;
		case "invite":
		case "zaproś":
			p2 = Bukkit.getPlayer(args[0]);
			if (p2 == null)
				return Func.powiadom(sender, prefix + Func.msg("%s nie jest online", args[0]));
			wyspa.zaproś(p, p2);
			break;
		case "leave":
		case "opuść":
			wyspa.opuść(p);
			break;
		case "delete":
		case "usuń":
			wyspa.usuń(p);
			break;
		case "permissions":
		case "permisje":
		case "uprawnienia":
			wyspa.permisjeInv(p);
			break;
		case "permsedit":
		case "edytujpermisje":
			wyspa.edytorPermisji(p, args);
			break;
		case "members":
		case "członkowie":
			wyspa.otwórzMembers(p);
			break;
		case "kick":
		case "wyrzuć":
			if (args.length < 2)
				return Func.powiadom(sender, prefix + "/is wyrzuć <nick>");
			wyspa.wyrzuć(p, args[1]);
			break;
		case "biom":
		case "biome":
			if (args.length < 2)
				return Func.powiadom(sender, prefix + "/is biome biom");
			wyspa.zmieńBiom(p, args[1]);
			break;
		case "addwarp":
		case "dodajwarp":
			if (args.length < 2)
				return Func.powiadom(sender, prefix + "/is dodajwarp <nazwa>");
			wyspa.dodajWarp(p, args[1]);
			break;
		case "delwarp":
		case "remwarp":
		case "deletewarp":
		case "removewarp":
		case "usuńwarp":
			wyspa.otwórzInvDelWarp(p);
			break;
		case "name":
		case "nazwa":
			if (args.length < 2)
				return Func.powiadom(sender, prefix + "/is nazwa <nazwa>");
			wyspa.zmieńNazwe(p, args[1]);
			break;
		}
		return true;
	}
}

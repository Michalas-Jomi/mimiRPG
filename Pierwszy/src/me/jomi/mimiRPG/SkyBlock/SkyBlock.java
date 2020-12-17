package me.jomi.mimiRPG.SkyBlock;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import me.jomi.mimiRPG.util.Przeładowalny;

//TODO /is
//TODO /is kick/wyrzuć - wyrzucenie z wyspy na spawn, nie jako członka
//TODO /is delete
//TODO /is regen
//TODO /is booster
//TODO /is members
//TODO /is permissions
//TODO /is coop
//TODO /is upgrade
//TODO /is visit
//TODO tabcompleter do wyboru angielski/polski
//TODO przycisk back w menu wyspy
//TODO limity bloków
//TODO ulepszanie limitów bloków
//TODO /is biome
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
	public static class Wyspa extends Mapowany{
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
			
			boolean reset_wyspy;
			
			boolean zmiana_prywatności;
			
			boolean używaniePortalu;
			
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
			
			
			double dys0 = wyspa.locŚrodek.distance(new Location(wyspa.locŚrodek.getWorld(), 0, 100, 0));
			Func.insort(new Krotka<>(dys0, wyspa.id), listaLokacji, k -> k.a);
			configData.ustaw("wyspy loc." + wyspa.id, dys0);
			
			configData.ustaw("id następnej wyspy", wyspa.id + 1);
			
			configData.zapisz();
			
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = wyspa.id;
			g.zapisz();
			
			wyspa.zapisz();
			
			return wyspa;
		}
		
		// (loc.distance(new Location(loc.getWorld(), 0, 100, 0), id wyspy)
		static final List<Krotka<Double, Integer>> listaLokacji = Lists.newArrayList();
		

		static Wyspa wczytaj(Location loc) {
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

 		
 		// Permisje
 		
 		@Mapowane List<String> permsKody; // grupa: Permisje // posortowane według priorytetów
 		final HashMap<String, Permisje> perms = new HashMap<>();
 		void Init() {
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
 		void odświeżWszystkiePermisje() {
 			Lists.newArrayList(perms.values()).forEach(this::odświeżKodPermisji);
 		}
 		
 		
		@Mapowane HashMap<String, String> członkowie = new HashMap<>(); // nick: nazwaGrupyPermisji
		@Mapowane Poziomy poziomy = Func.utwórz(Poziomy.class);
		@Mapowane Location locŚrodek;
		@Mapowane String typ; // TODO wczytywać schematic
		@Mapowane int id;

		
		public void usuń() {
			double dys0 = locŚrodek.distance(new Location(locŚrodek.getWorld(), 0, 100, 0));
			if (listaLokacji.remove(Func.wyszukajBinarnieLIndex(dys0, listaLokacji, k -> k.a)).b != id) {
				Main.warn("Znaleziony BUG! Niepoprawnie wyszukawana wyspa errorid:1 Wyspa.usuń()");
				inst.przeładuj();
				return;
			}
			configData.ustaw("wyspy loc." + id, null);
			
			członkowie.keySet().forEach(nick -> {
				// TODO tepać ich na spawn jeśli są online
				Gracz g = Gracz.wczytaj(nick);
				g.wyspa = -1;
				g.zapisz();
			});
			
			getConfig(id).usuń();
			
			// TODO usunąć bloki
			
		}
		
		public boolean zawiera(Location loc) {
			return false; // TODO napisać
		}
		
		Permisje permisje(Player p) {
			if (maBypass(p))
				return perms.get("właściciel"); // TODO domyślne permisje
			try {
				return perms.get(członkowie.get(p.getName()));
			} catch (Throwable e) {
				return perms.get("odwiedzający"); // TODO domyślne permisje
			}
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
			
			if (Gracz.wczytaj(kogo).wyspa != -1) {
				Func.powiadom(prefix, p, "%s ma już wyspę", kogo.getDisplayName());
				return;
			}
			
			Func.ustawMetadate(kogo, metaZaproszenie, p);
			
			Inventory inv = new Holder(this, TypInv.ZAPROSZENIE, 3, "&4Zaproszenie na wyspe " + p.getDisplayName()).getInventory();
			inv.setItem(12, Func.stwórzItem(Material.LIME_STAINED_GLASS_PANE, "&aDołącz do wyspy &7" + p.getDisplayName()));
			inv.setItem(14, Func.stwórzItem(Material.RED_STAINED_GLASS_PANE, "&aOdrzuć zaproszenie do wyspy &7" + p.getDisplayName()));
			kogo.openInventory(inv);
			kogo.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			
			
			
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
		}
		void odrzućZaproszenie(Player p) {
			Player zapraszający = (Player) p.getMetadata(metaZaproszenie).get(0).value();
			
			Func.powiadom(prefix, p, "Odrzuciłeś zaproszenie do wyspy %s", zapraszający.getDisplayName());
			Func.powiadom(prefix, p, "%s odrzucił twoje zaproszenie do wyspy", zapraszający.getDisplayName());
			
			p.removeMetadata(metaZaproszenie, Main.plugin);
		}
		void klikanieZaproszenia(Player p, int slot) {
			Consumer<Player> cons = null;
			
			switch (slot) {
			case 12: cons = this::przyjmijZaproszenie;	break;
			case 14: cons = this::odrzućZaproszenie;	break;
			}
			
			Func.wykonajDlaNieNull(cons, _cons -> {
				p.closeInventory();
				_cons.accept(p);
			});
			
		}
		
		
		// /is leave
		
		public void opuść(Player p) {
			powiadomCzłonków("%s opuścił wyspę", p.getDisplayName());

			członkowie.remove(p.getName());
			zapisz();
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = -1;
			g.zapisz();
			
			// TODO tepać na spawn
		}
		
		
		// ogólne odniesienia
		
		void klikany(Player p, TypInv typ, InventoryClickEvent ev) {
			switch (typ) {
			case MAGAZYN: return;
			case TOP: return;
			case BANK:
				klikanyBank(p, ev.getRawSlot(), ev.getClick());
				break;
			case WARPY:
				klikanyInvWarp(p, ev.getRawSlot(), ev.getCurrentItem());
				break;
			case ZAPROSZENIE:
				klikanieZaproszenia(p, ev.getRawSlot());
				break;
			}
		}
		void zamykany(Player p, TypInv typ, InventoryCloseEvent ev) {
			if (typ == TypInv.MAGAZYN)
				zamknięcieMagazynu();
			else if (typ == TypInv.ZAPROSZENIE)
				odrzućZaproszenie(p);
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
		
		
		// Override
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			
			return obj instanceof Wyspa && ((Wyspa) obj).id == this.id;
		}		
	}
	static enum TypInv {
		BANK,
		MAGAZYN,
		TOP,
		WARPY,
		ZAPROSZENIE;
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
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), holder -> {
			if (holder instanceof Holder)
				if (((Holder) holder).wyspa == null) {
					// TODO /is visit <- InvTyp == TOP
				} else
					((Holder) holder).wyspa.klikany((Player) ev.getWhoClicked(), ((Holder) holder).typ, ev);
		});
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), holder -> {
			if (holder instanceof Holder)
				((Holder) holder).wyspa.zamykany((Player) ev.getPlayer(), ((Holder) holder).typ, ev);
		});
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
	BukkitTask taskInvTop;
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
	
	// TODO Do wczytania
	static int rzędyTopki = 3; // "topka.rzędy" 1..6
	static List<Integer> slotyTopki; // "topka.sloty"
	static List<TopInfo> topInfo;// "topka.gracze" // przechowuje troche więcej niż potrzeba powiedzmy 1.5 raza
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
	public void przeładuj() {
		Wyspa.listaLokacji.clear();
		
		Func.wykonajDlaNieNull(configData.sekcja("wyspy loc"), sekcja ->
				sekcja.getValues(false).entrySet().forEach(en ->
						Func.insort(new Krotka<>((double) en.getValue(), Func.Int(en.getKey())), Wyspa.listaLokacji, k -> k.a))
		);
		
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

		Gracz g = null;
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
			g = Gracz.wczytaj(p);
		}
		
		Wyspa wyspa = g == null ? null : Wyspa.wczytaj(g.wyspa);
		
		// zakładam poprawność sender jest playerem
		// zakładam g.wyspa != -1 | gracz ma wyspe
		
		switch (args[0]) {
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
		case "bypass": // TODO pamiętać o permisjach
			ustawBypass(p, !maBypass(p));
			sender.sendMessage(prefix + (maBypass(p) ? "w" : "wy") + "łączono bypass");
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
		}
		return true;
	}
}

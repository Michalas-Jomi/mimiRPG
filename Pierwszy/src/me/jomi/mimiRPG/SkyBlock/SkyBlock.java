package me.jomi.mimiRPG.SkyBlock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import net.minecraft.server.v1_16_R2.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R2.WorldBorder;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.PojedynczeKomendy.Poziom;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.Ulepszenia.Ulepszenie;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

//TODO /is
//TODO /is booster
//TODO /is border
//TODO /is coop
//TODO tabcompleter do wyboru angielski/polski
//TODO przycisk back w menu wyspy
//TODO limity bloków
//TODO ulepszanie limitów bloków
//TODO tepać na home spadających w przepaść
//TODO limity warpów i grup permisji, aby nie przekroczyły 6*9
//TODO zablokować przepychanie bloków pistonami przez barriery

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
	static class Ulepszenia {
		public static class Ulepszenie {
			public Ulepszenie(int wartość, double cena) {
				this.wartość = wartość;
				this.cena = cena;
			}
			public final int wartość;
			public final double cena;
		}
		public static Ulepszenie[] limityBloków;
		public static Ulepszenie[] członkowie;
		public static Ulepszenie[] generator;
		public static Ulepszenie[] wielkość;
		public static Ulepszenie[] magazyn;
		public static Ulepszenie[] warpy;
	}
	public static class Wyspa extends Mapowany {
		public Wyspa() {}// Konstruktor dla mapowanego
		public static class Permisje {
			String grupa;
			
			boolean niszczenie; // v
			boolean stawianie; // v
			boolean dostęp_do_spawnerów;
			
			boolean otwieranie_drzwi_i_furtek; // v
			boolean otwieranie_skrzyń; // v
			boolean używanie_przycisków_dzwigni; // v
			boolean używanie_armorstandów_itemframów; // PlayerArmorStandManipulateEvent
			
			boolean wyrzucaniec_członków; // v
			boolean zapraszanie_członków; // v
			
			boolean wyrzucanie_odwiedzających; // v
			
			boolean zmiana_prywatności; // v
			
			boolean używanie_portalu;
			
			boolean liczenie_wartości_wyspy; // v
			
			boolean coop;
			
			boolean kupowianie_ulepszeń; // v
			
			boolean ustawienie_home_wyspy; // v
			
			boolean dodawanie_i_usuwanie_warpów;
			
			boolean dostęp_do_kasy_banku; // v
			boolean dostęp_do_expa_banku; // v
			boolean dostęp_do_magazynu; // v
			
			boolean bicie_mobów; // v
			
			boolean podnoszenie_itemów; // v
			
			boolean usuwanie_grup_permisji; // v
			boolean tworzenie_grup_permisji; // v
			boolean edytowanie_hierarhi_grup_permisji; // v
			boolean edytowanie_permisji; // v
			boolean awansowanie_i_degradowanie_członków; // v
			
			boolean zmiana_dropu; // TODO możliwość wyłączenia poszczzególnego dropu z cobla, w tym cobla
			
			boolean zmiana_nazwy_wyspy; // v
			
			boolean zmiana_biomu; // v
			
			boolean tworzenie_warpów; // v
			boolean usuwanie_warpów; // v
			
			
			List<Boolean> warpy = Lists.newArrayList(); // v
		}
		public static class Warp extends Mapowany {
			@Mapowane String nazwa;
			@Mapowane Location loc;
		}
		public static class Poziomy extends Mapowany {
			// TODO ulepszanie
			@Mapowane int limityBloków;
			@Mapowane int członkowie; // v // wartość - maksymalna ilość osób na wyspe  // Przy dodawaniu nowego pamiętać aby dodać też w klasie Ulepszenia
			@Mapowane int generator;
			@Mapowane int wielkość; // wartość - długość boku wyspy
			@Mapowane int magazyn; // \ // wartość - ilość rządków magazynu
			@Mapowane int warpy; // v // wartość - maksymalna ilość warpów
			/*
			 * # Ulepszenia
			 * <nazwa>:
			 * #- wartość cena
			 * - 0 100
			 * - 2 200
			 * - 5 300
			 * 
			 * 
			 */
		}
		
		public static Wyspa nowa(Player p, String typ) {
			Main.log(prefix + p.getName() + "utworzył wyspę typu " + typ);
			
			Wyspa wyspa = Func.utwórz(Wyspa.class);
			
			wyspa.id = configData.wczytajInt("id następnej wyspy");
			
			wyspa.członkowie.put(p.getName(), "właściciel"); // TODO permisja domyślna właściciel, (bez możliwości usunięcia, tak jak członek i odwiedzający)
			
			Krotka<Integer, Integer> xz = następnaPozycja();
			wyspa.locŚrodek = new Location(Światy.overworld, xz.a * odstęp, yWysp, xz.b * odstęp);
			
			wyspa.locHome = wyspa.locŚrodek.clone();
			
			wyspa.nazwa = p.getName();
			
			wyspa.zapisz();

			
			configData.ustaw("wyspy loc." + dolnyRóg(wyspa.locŚrodek), wyspa.id);
			
			configData.ustaw("id następnej wyspy", wyspa.id + 1);
			
			configData.zapisz();
			
			p.teleport(wyspa.locHome);
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = wyspa.id;
			g.zapisz();
			
			return wyspa;
		}
		
		static String dolnyRóg(Location loc) {
			int x = ((int) (loc.getX() / odstęp)) * odstęp;
			int z = ((int) (loc.getZ() / odstęp)) * odstęp;
			
			return x + "_" + z;
		}
		public static Wyspa wczytaj(Location loc) {
			String locWorldName = loc.getWorld().getName();
			if (!(locWorldName.equals(Światy.nazwaOverworld) || (Światy.dozwolonyNeter && locWorldName.equals(Światy.nazwaNether))))
				return null;
			
			Wyspa wyspa = wczytaj(configData.wczytajLubDomyślna("wyspy loc." + dolnyRóg(loc), -1));
			return wyspa == null ? null : wyspa.zawieraIgnorujŚwiat(loc) ? wyspa : null;
		}
		public static Wyspa wczytaj(Player p) {
			return wczytaj(Gracz.wczytaj(p));
		}
		public static Wyspa wczytaj(Gracz g) {
			return g.wyspa == -1 ? null : wczytaj(g.wyspa);
		}
		// id: wyspa
		static final WeakHashMap<Integer, Wyspa> mapaWysp = new WeakHashMap<>();
		public static Wyspa wczytaj(int id) {
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
		@Mapowane String typ; // TODO wczytywać schematic
		@Mapowane int id;


		private void usuń() {
			configData.ustaw("wyspy loc." + dolnyRóg(locŚrodek), null);
			
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
			String locWorldName = loc.getWorld().getName();
			if (!(locWorldName.equals(Światy.nazwaOverworld) || (Światy.dozwolonyNeter && locWorldName.equals(Światy.nazwaNether))))
				return false;
			return zawieraIgnorujŚwiat(loc);
		}
		public boolean zawieraIgnorujŚwiat(Location loc) {
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
			if (!permisje(p).edytowanie_permisji) {
				p.sendMessage(prefix + "Nie możesz tego zrobić");
				return;
			}
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
			Permisje permP = permisje(p);
			if (!(permP.edytowanie_hierarhi_grup_permisji || permP.tworzenie_grup_permisji || permP.usuwanie_grup_permisji)) {
				p.sendMessage(prefix + "Nie masz wystarczających uprawnień aby zarządzać uprawnieniami");
				return;
			}
			try {
				Permisje perm = perms.get(args[2]);
				Permisje perm2;
				int mnZwiększ = 1;
				switch (args[1]) {
				case "zwiększ":
					mnZwiększ = -1;
				case "zmniejsz":
					if (!permP.edytowanie_hierarhi_grup_permisji) {
						p.sendMessage(prefix + "Nie masz wystarczających uprawnień aby edytować hierarchie grup permisji");
						return;
					}
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
					if (!permP.usuwanie_grup_permisji) {
						p.sendMessage(prefix + "Nie masz wystarczających uprawnień aby usuwać grupy permisji");
						return;
					}
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
					if (!(permP.tworzenie_grup_permisji)) {
						p.sendMessage(prefix + "Nie masz wystarczających uprawnień aby dodawać grupy permisji");
						return;
					}
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
			if (permisje(p).warpy.get(slot))
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
		
		public boolean dodajWarp(Player p, String nazwa) {
			if (warpy.size() >= Ulepszenia.warpy[poziomy.warpy].wartość)
				return Func.powiadom(p, prefix + "Wyspa osiągneła już limit warpów");
			
			if (!permisje(p).tworzenie_warpów) 
				return Func.powiadom(p, prefix + "Nie masz uprawnień na dodawanie warpów");
			
			if (!zawiera(p.getLocation()))
				return Func.powiadom(p, prefix + "Nie możesz tu ustawić warpa wyspy");
			
			Warp warp = new Warp();
			warp.loc = p.getLocation();
			warp.nazwa = Func.koloruj(nazwa);
			warpy.add(warp);
			
			perms.values().forEach(perm -> perm.warpy.add(true));
			

			odświeżInvWarpy();
			
			odświeżKodyWszystkichPermisji();
			odświeżEdytoryPermisji();

			zapisz();
			return false;
		}
		
		// /is delwarp
		
		public void otwórzInvDelWarp(Player p) {
			if (!permisje(p).usuwanie_warpów) {
				p.sendMessage(prefix + "Nie masz uprawnień aby usuwać warpy");
				return;
			}
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
				usuńWarp(slot);
		}
		void usuńWarp(int index) {
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
			Permisje perm = permisje(p);
			if (!(perm.dostęp_do_expa_banku || perm.dostęp_do_kasy_banku || perm.dostęp_do_magazynu)) {
				p.sendMessage(prefix + "Nie masz uprawnień do banku wyspy");
				return;
			}
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
		private boolean klikanyBank(Player p, int slot, ClickType clickType) {
			int min;
			int prawy;
			Supplier<Integer> lewy;
			
			BiConsumer<Player, Integer> bic = null;
			if (slot == slotBankKasa) {
				if (!permisje(p).dostęp_do_kasy_banku) 
					return Func.powiadom(p, prefix + "Nie masz uprawnień do środków wyspy");
				lewy = () -> Math.abs((int) Main.econ.getBalance(p));
				bic = this::wpłaćPieniądze;
				min = 1000;
				prawy = kasa;
			} else if (slot == slotBankExp) {
				if (!permisje(p).dostęp_do_expa_banku)
					return Func.powiadom(p, prefix + "Nie masz uprawnień do expa wyspy");
				lewy = () -> Poziom.policzCałyExp(p);
				bic = this::wpłaćExp;
				min = 1725;
				prawy = exp;
			} else if (slot == slotBankMagazyn) {
				otwórzMagazyn(p);
				return true;
			} else
				return true;

			int ile;
			switch (clickType) {
			// wpłacanie
			case LEFT:		 ile = Math.min(min, lewy.get());	break;
			case SHIFT_LEFT: ile = lewy.get();					break;
			// wypłacanie
			case RIGHT:		  ile = -Math.min(min, prawy);		break;
			case SHIFT_RIGHT: ile = -prawy;						break;
			default:
				return true;
			}
				
			bic.accept(p, ile);
			return false;
		}
		
		
		// /is storage

		@Mapowane List<ItemStack> magazynItemów;
		public void otwórzMagazyn(Player p) {
			if (permisje(p).dostęp_do_magazynu)
				p.openInventory(getInvMagazyn());
			else
				p.sendMessage(prefix + "Nie masz uprawnień do magazynu wyspy");
		}
		private Inventory invMagazyn = null;
		private Inventory getInvMagazyn() {
			if (invMagazyn == null) {
				invMagazyn = new Holder(this, TypInv.MAGAZYN, Ulepszenia.magazyn[poziomy.magazyn].wartość).getInventory();
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
		
		void odświeżMagazyn() {
			Func.wykonajDlaNieNull(invMagazyn, inv -> {
				Lists.newArrayList(inv.getViewers()).forEach(HumanEntity::closeInventory);
				invMagazyn = null;
			});
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
			if (ustawPubliczność(p, false))
				for (Player gracz : Bukkit.getOnlinePlayers())
					if (zawiera(gracz.getLocation()))
						Func.tpSpawn(gracz);
		}
		private boolean ustawPubliczność(Player p, boolean publiczna) {
			if (!permisje(p).zmiana_prywatności)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do zmiany prywatności wyspy", false);
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
		public boolean wartość(Player p) {
			if (!permisje(p).liczenie_wartości_wyspy)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do przeliczania wartości wyspy");
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
			return false;
			
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
		public boolean zaproś(Player p, Player kogo) {
			if (członkowie.size() >= Ulepszenia.członkowie[poziomy.członkowie].wartość)
				return Func.powiadom(p, prefix + "Wyspa osiągneła już limit członków");
			// TODO jakiś cooldown na tą samą osobe
			
			if (!permisje(p).zapraszanie_członków)
				return Func.powiadom(p, prefix + "Nie masz uprawnień na zapraszanie ludzi na wyspe");
			if (Gracz.wczytaj(kogo).wyspa != -1)
				return Func.powiadom(prefix, p, "%s ma już wyspę", kogo.getDisplayName());
			
			Func.ustawMetadate(kogo, metaZaproszenie, p);
			
			Main.panelTakNie(kogo,
					"&4Zaproszenie na wyspe " + p.getDisplayName(),
					"&aDołącz do wyspy &7" + p.getDisplayName(),
					"&cOdrzuć zaproszenie do wyspy &7" + p.getDisplayName(),
					() -> przyjmijZaproszenie(kogo),
					() -> odrzućZaproszenie(kogo));
			
			return Func.powiadom(p, prefix + "Wysłano zaproszenie na wyspy do " + kogo.getDisplayName(), false);
		}
		boolean przyjmijZaproszenie(Player p) {
			if (członkowie.size() >= Ulepszenia.członkowie[poziomy.członkowie].wartość)
				return Func.powiadom(p, prefix + "Wyspa osiągneła już limit członków");
			
			członkowie.put(p.getName(), "członek");
			zapisz();
			
			Gracz g = Gracz.wczytaj(p);
			g.wyspa = id;
			g.zapisz();
			
			powiadomCzłonków("%s dołączył wyspy", p.getName());
			
			p.removeMetadata(metaZaproszenie, Main.plugin);
			
			odświeżInvMembers();
			return false;
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
		
		
		// /is delete
		
		public void usuń(Player p) {
			if (permisje(p).grupa.equals("właściciel"))
				Main.panelTakNie(p, "&4Usunąć wyspe?", "&aTak, usuń wyspę", "&4Nie, nie usuwał wyspy", this::usuń, null);
			else
				p.sendMessage(prefix + "Tylko właściciel może usunąć wyspe");
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
		boolean klikanyInvMembers(Player p, ItemStack item, ClickType typ) {
			if (!item.getType().equals(Material.PLAYER_HEAD) || !Func.multiEquals(typ, ClickType.RIGHT, ClickType.LEFT))
				return true;
			if (permisje(p).awansowanie_i_degradowanie_członków)
				return Func.powiadom(p, prefix + "Nie możesz tego zrobić");
			
			String nick2 = item.getItemMeta().getDisplayName().substring(4);
			
			Permisje permP = permisje(p);
			Permisje perm2 = permisje(nick2);
			
			
			int mn = typ == ClickType.LEFT ? -1 : 1;
			int indexPerm2 = indexPerm(perm2);
			
			if (indexPerm(permP) >= indexPerm2 + mn || permsNietykalne.contains(kodToStrPerm(indexPerm2 + mn)))
				return Func.powiadom(p, prefix + "Nie możesz tego zrobić");
			
			permsKody.add(indexPerm2 + mn, permsKody.remove(indexPerm2));
			odświeżInvMembers();
			zapisz();
			
			Func.wykonajDlaNieNull(Bukkit.getPlayer(nick2), p2 -> p.sendMessage(prefix + Func.msg("%s %s cię!", p.getDisplayName(), mn == -1 ? "Awansował" : "Zdegradował")));
			return Func.powiadom(p, prefix + Func.msg("%s gracza %s!", mn == -1 ? "Awansowałeś" : "Zdegradowałeś", nick2), false);
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
		
		public boolean wyrzuć(Player p, String kogo) {
			if (p.getName().equalsIgnoreCase(kogo))
				return Func.powiadom(p, prefix + "zamiast tego użyć /is opuść");
			if (!członkowie.containsKey(kogo)) {
				for (String nick : członkowie.keySet())
					if (nick.equalsIgnoreCase(kogo)) {
						kogo = nick;
						break;
					}
				if (!członkowie.containsKey(kogo)) {
					if (!permisje(p).wyrzucanie_odwiedzających)
						return Func.powiadom(p, prefix + "Nie masz uprawnień aby wyrzucać odwiedzających z wyspy");
					Player kogoP = Bukkit.getPlayer(kogo);
					if (kogoP == null)
						return Func.powiadom(p, prefix + Func.msg("%s nie jest online i nie należy do twojej wyspy", kogo));
					if (zawiera(kogoP.getLocation())) {
						Func.tpSpawn(kogoP);
						p.sendMessage(prefix + Func.msg("Wyprosiłeś %s ze swojej wyspy", kogoP.getDisplayName()));
						kogoP.sendMessage(prefix + Func.msg("%s wyprosił cie ze swojej wyspy", p.getDisplayName()));
						return false;
					}
					return true;
				}
			}
			if (!permisje(p).wyrzucaniec_członków)
				return Func.powiadom(p, prefix + "Nie masz uprawnień aby wyrzucać członków z wyspy");
			powiadomCzłonków("%s wyrzucił %s z wyspy!", p.getName(), kogo);
			
			członkowie.remove(kogo);

			zapisz();
			
			Gracz g = Gracz.wczytaj(kogo);
			g.wyspa = -1;
			g.zapisz();
			
			odświeżInvMembers();
			
			Func.wykonajDlaNieNull(Bukkit.getPlayer(kogo), Func::tpSpawn);
			return false;
		}
		
		
		// /is biome
		
		public boolean zmieńBiom(Player p, String biom) {
			if (!permisje(p).zmiana_biomu)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do zmiany biomu");
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
			return false;
		}
		
		
		// /is name

		@Mapowane String nazwa;
		public boolean zmieńNazwe(Player p, String nazwa) {
			if (!permisje(p).zmiana_nazwy_wyspy)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do zmiany nazwy wyspy");
			this.nazwa = Func.koloruj(nazwa);
			p.sendMessage(prefix + Func.msg("Zmieniono nazwę wyspy na %s", this.nazwa));
			zapisz();
			return false;
		}
		
		
		// /is upgrade
		
		public void otwórzUlepszenia(Player p) {
			if (permisje(p).kupowianie_ulepszeń) {
				p.openInventory(dajInvUlepszenia());
				p.addScoreboardTag(Main.tagBlokWyciąganiaZEq);
			} else
				p.sendMessage(prefix + "Nie masz uprawnień do kupowania ulepszeń wyspy");
		}
		Inventory dajInvUlepszenia() {
			Inventory inv = new Holder(this, TypInv.ULEPSZENIA, 4).getInventory();
			// TODO info o cenie i parametrach
			
			inv.setItem(12, dajItemekInvUlepszenia(
					"członkowie",
					Material.PLAYER_HEAD,
					"Członkowie",
					"&aZwiększa limit członków wyspy",
					ile -> "&aAktualnie dostępne &3" + ile + " &aczłonków",
					ile -> "&aNastępny poziom: &3" + ile
					));
			
			inv.setItem(13, dajItemekInvUlepszenia(
					"wielkość",
					Material.BARRIER,
					"Wielkość",
					"&aRozszerza granice wyspy",
					w -> "&aAktualna wielkość: &3" + w + "m",
					w -> "&aNastępny poziom: &3" + w + "m"
					));
			
			
			// TODO ulepszanie generatora
			inv.setItem(14, Func.stwórzItem(Material.DIAMOND_ORE, "&9&lGenerator", "", "&aUlepsza drop z generatorów"));
			
			
			inv.setItem(21, dajItemekInvUlepszenia(
					"magazyn",
					Material.CHEST,
					"Magazyn",
					"&aZwiększa pojemność magazynu wyspy",
					rzędy -> "&aAktualna pojemność: &3" + (rzędy*9)   + "&a slotów",
					rzędy -> "&aNastępny poziom: &3"    + (rzędy * 9) + "&a slotów"
					));
			
			inv.setItem(22, dajItemekInvUlepszenia(
					"warpy",
					Material.NETHER_PORTAL,
					"Warpy",
					"&aZwiększa limit warpów wyspy",
					warpy -> "&aAktualna ilość warpów: &e" + warpy,
					warpy -> "&aNastępna ilość warpów: &3" + warpy
					));
			
			inv.setItem(23, dajItemekInvUlepszenia(
					"limityBloków",
					Material.HOPPER,
					"Limity Bloków",
					"&aZwiększa limity bloków na wyspie",
					limit -> String.format("Aktualny limit: &3%s &askrzyń &3%s &aspawnerów", limit, limit / 2),
					limit -> String.format("Następny limit: &3%s &askrzyń &3%s &aspawnerów", limit, limit / 2)
					));
			
			return inv;
		}
		private ItemStack dajItemekInvUlepszenia(String nazwaPola, Material mat, String nazwa, String krótkiOpis, Function<Integer, String> akt, Function<Integer, String> następny) {
			int poziom;
			try {
				poziom = Poziomy.class.getDeclaredField(nazwaPola).getInt(poziomy);
				Ulepszenia.Ulepszenie[] ulepszenia = (Ulepszenia.Ulepszenie[]) Ulepszenia.class.getDeclaredField(nazwaPola).get(null);
				ItemStack item = Func.stwórzItem(mat,
						"&9&l" + nazwa,
						"",
						"&a" + krótkiOpis,
						"&a" + akt.apply(ulepszenia[poziom].wartość)
						);
				if (ulepszenia.length > poziom + 1)
					Func.dodajLore(Func.dodajLore(item,
							"&a" + następny.apply(ulepszenia[poziom + 1].wartość)),
							"&aCena ulepszenia: &e" + ulepszenia[poziom + 1].cena + "$");
				return item;
			} catch (Throwable e) {
				e.printStackTrace();
				return new ItemStack(Material.CREEPER_HEAD);
			}
		}
		boolean klikanyInvUlepszenia(Player p, int slot) {
			if (!permisje(p).kupowianie_ulepszeń)
				return Func.powiadom(p, prefix + "Nie masz uprawnień do kupowania ulepszeń wyspy");
			
			Func.wykonajDlaNieNull(((Supplier<String>) () -> {
				if		(slot == 12) return "członkowie";
				else if (slot == 13) return "wielkość";
				else if (slot == 14) return "generator";
				else if (slot == 21) return "warpy";
				else if (slot == 22) return "magazyn";
				else if (slot == 23) return "limityBloków";
				return null;
			}).get(), nazwaPola -> {
				try {
					Ulepszenia.Ulepszenie[] ulepszenia = (Ulepszenie[]) Ulepszenia.class.getDeclaredField(nazwaPola).get(null);
					Field pole = Poziomy.class.getDeclaredField(nazwaPola);
					
					// TODO obsługa wujątku z indexowaniem gdy poziom jest maksymalny
					double cena = ulepszenia[pole.getInt(poziomy) + 1].cena;
					
					double fundusz = Math.max(0, kasa);
					if (Main.econ != null) {
						double bal = Main.econ.getBalance(p);
						if (bal > 0)
							fundusz += bal;
					}
					
					if (cena > fundusz) {
						p.sendMessage(prefix + "Nie posiadasz wystarczająco dużo pieniędzy");
						return;
					}
					
					kasa -= cena;
					if (kasa < 0) {
						Main.econ.withdrawPlayer(p, -kasa);
						kasa = 0;
					}					
					
					pole.setInt(poziomy, pole.getInt(poziomy) + 1);
					
					zapisz();

					if 		(slot == 13) odświeżBorder();
					else if (slot == 22) odświeżMagazyn();
					
					powiadomCzłonków("%s zakupił ulepszenie %s", p.getDisplayName(), nazwaPola);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
			});
			
			return false;
		}
		
		
		
		// ogólne odniesienia
		
		void ustawBorder(Player p) {
			WorldBorder wb = new WorldBorder();
			wb.world = ((CraftWorld) p.getWorld()).getHandle();
			wb.setCenter(locŚrodek.getX(), locŚrodek.getZ());
			
			wb.setSize(Ulepszenia.wielkość[poziomy.wielkość].wartość);
			// TODO kolor //wb.transitionSizeBetween(d0, d1, i);
			
			wb.setWarningDistance(0);
			wb.setWarningTime(0);
			
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
		}
		
		void odświeżBorder() {
			Bukkit.getOnlinePlayers().forEach(p -> {
				if (zawiera(p.getLocation()))
					ustawBorder(p);
			});
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
		
		public Krotka<Location, Location> rogi() {
			double a = Ulepszenia.wielkość[poziomy.wielkość].wartość / 2.0;
			return new Krotka<>(
					locŚrodek.clone().add(-a, -locŚrodek.getY(), -a),
					locŚrodek.clone().add(a, 256 - locŚrodek.getY(), a)
					);
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
		ULEPSZENIA		((wyspa, p, typ, ev) -> wyspa.klikanyInvUlepszenia(p, ev.getRawSlot())),
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
	
	
	// Generowanie pustych Światów
	
	private static final GeneratorChunków generatorChunków = new GeneratorChunków();
	public static GeneratorChunków worldGenerator(String worldName) {
		return worldName.equals(Światy.nazwaOverworld) || (Światy.dozwolonyNeter && worldName.equals(Światy.nazwaNether)) ? generatorChunków : null;
	}
	public static class GeneratorChunków extends ChunkGenerator {
	    @Override
	    public List<BlockPopulator> getDefaultPopulators(World world) {
	        return Lists.newArrayList();
	    }
	    @Override
	    public boolean canSpawn(World world, int x, int z) {
	        return true;
	    }
	    
	    @Override
	    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biomeGrid) {
	        ChunkData chunkData = createChunkData(world);
	        
	        Biome biom;
	        if (world.getName().equals(Światy.nazwaOverworld))
	            biom = Biome.PLAINS;
	        else if (Światy.dozwolonyNeter && world.getName().equals(Światy.nazwaNether))
	            biom = Biome.NETHER_WASTES;
	        else
	            return chunkData;
	        
	        for (int x = 0; x < 16; x++)
				for (int z = 0; z < 16; z++)
					for (int y = 0; y < world.getMaxHeight(); y++)
	            		biomeGrid.setBiome(x, y, z, biom);

	        return chunkData;
	    }
	    
	    public byte[][] blockSections;
	    public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
	        if (blockSections == null)
				blockSections = new byte[world.getMaxHeight() / 16][];
	        return blockSections;
	    }
	}
	private World stwórzŚwiat(Environment env, String nazwa) {
		WorldCreator wc = new WorldCreator(nazwa);
		wc.generator(Main.plugin.getName() + ":skyblock");
		wc.generator(generatorChunków);
		wc.generateStructures(false);
		wc.type(WorldType.FLAT);
		wc.environment(env);
		World w = wc.createWorld();
		
		if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
	        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + nazwa + " " + env + " -g " + Main.plugin.getName() + ":skyblock");
	        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set generator " + Main.plugin.getName() + ":skyblock " + nazwa);
		}
		
		return w;
	}
	private void stwórzŚwiaty() {
		Światy.overworld = stwórzŚwiat(Environment.NORMAL, Światy.nazwaOverworld);
		if (Światy.dozwolonyNeter)
			Światy.nether = stwórzŚwiat(Environment.NETHER, Światy.nazwaNether);
	}
	
	
	// bypass
	
	private static final Set<String> zBypassem = Sets.newConcurrentHashSet();
	public static boolean maBypass(Player p) {
		return zBypassem.contains(p.getName());
	}
	public static void ustawBypass(Player p, boolean stan) {
		Consumer<String> cons = (stan ? zBypassem::add : zBypassem::remove);
		cons.accept(p.getName());
	}
	
	
	
	// Event Handler
	
	/// obsługa inv
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
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {	
			if (holder.typ == TypInv.MAGAZYN)
				holder.wyspa.zamknięcieMagazynu();
		});
	}

	/// permisje
	@EventHandler
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (ev.getEntity() instanceof Player)
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getItem().getLocation()), wyspa -> {
				if (!wyspa.permisje((Player) ev.getEntity()).podnoszenie_itemów)
					ev.setCancelled(true);
			});
	}
	@EventHandler
	public void interakjca(PlayerInteractEvent ev) {
		Func.wykonajDlaNieNull(ev.getClickedBlock(), blok -> 
			Func.wykonajDlaNieNull(Wyspa.wczytaj(blok.getLocation()), wyspa -> {
				if (blok.getState() instanceof Container) {
					if (!wyspa.permisje(ev.getPlayer()).otwieranie_skrzyń)
						ev.setCancelled(true);
				} else if (blok.getType().isInteractable())
					if (blok.getType().equals(Material.LEVER) || blok.getType().toString().contains("_BUTTON")) {
						if (!wyspa.permisje(ev.getPlayer()).używanie_przycisków_dzwigni)
							ev.setCancelled(true);
					} else if (!wyspa.permisje(ev.getPlayer()).otwieranie_drzwi_i_furtek)
							ev.setCancelled(true);
			})
		);
	}
	@EventHandler
	public void stawianieBloków(BlockPlaceEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
			if (!wyspa.permisje(ev.getPlayer()).stawianie)
				ev.setCancelled(true);
		});
	}
	@EventHandler
	public void niszczenieBloków(BlockBreakEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getBlock().getLocation()), wyspa -> {
			if (!wyspa.permisje(ev.getPlayer()).niszczenie)
				ev.setCancelled(true);
		});
	}
	@EventHandler
	public void bicieMobów(EntityDamageByEntityEvent ev) {
		boolean dp = ev.getDamager() instanceof Player;
		boolean ep = ev.getEntity() instanceof Player;
		if ((dp || ep) && !(dp && ep))
			Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getDamager().getLocation()), wyspa -> {
				if (!wyspa.permisje((Player) (dp ? ev.getDamager() : ev.getEntity())).bicie_mobów)
					ev.setCancelled(true);
			});
	}
	
	/// border
	@EventHandler(priority = EventPriority.MONITOR)
	public void dołączanieDoGry(PlayerJoinEvent ev) {
		Player p = ev.getPlayer();
		Func.wykonajDlaNieNull(Wyspa.wczytaj(p.getLocation()), wyspa -> {
			if (wyspa.członkowie.containsKey(p.getName()))
				Func.tpSpawn(p);
			else
				wyspa.ustawBorder(p);
		});
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void tepanie(PlayerTeleportEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getTo()), wyspa -> wyspa.ustawBorder(ev.getPlayer()));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void respawn(PlayerRespawnEvent ev) {
		Func.wykonajDlaNieNull(Wyspa.wczytaj(ev.getRespawnLocation()), wyspa -> wyspa.ustawBorder(ev.getPlayer()));
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
	static class Światy {
		static World overworld;
		static World nether;
		static String nazwaOverworld;
		static String nazwaNether;
		static boolean dozwolonyNeter;
	}
	static int yWysp;
	static int czasCooldownuLiczeniaWartości;
	static int odstęp;
	
	
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
		odstęp 							= Main.ust.wczytajLubDomyślna						("Skyblock.odstęp między wyspami", 150);
		rzędyTopki 						= Math.max(1, Math.min(6, Main.ust.wczytajInt		("Skyblock.topka.rzędy")));
		slotyTopki 						= Func.nieNullList((List<Integer>) Main.ust.wczytaj	("Skyblock.topka.sloty"));
		topInfo 						= Func.nieNullList((List<TopInfo>) Main.ust.wczytaj	("Skyblock.topka.gracze"));
		//światWysp 						= Bukkit.getWorld(Main.ust.wczytajLubDomyślna		("Skyblock.świat.zwykły", "SkyblockNormalny"));
		//światNether 					= Bukkit.getWorld(Main.ust.wczytajLubDomyślna		("Skyblock.świat.nether", "SkyblockNether"));
		yWysp 							= Main.ust.wczytajLubDomyślna						("Skyblock.y wysp", 100);
		czasCooldownuLiczeniaWartości 	= Main.ust.wczytajLubDomyślna						("Skyblock.cooldown.liczenie punktów", 60*30);
		
		Światy.dozwolonyNeter = Main.ust.wczytajLubDomyślna("Skyblock.nether", true);
		Światy.nazwaOverworld = Main.ust.wczytajLubDomyślna("Skyblock.świat.zwykły", "mimiSkyblock");
		Światy.nazwaNether	  = Main.ust.wczytajLubDomyślna("Skyblock.świat.nether", "mimiSkyblockNether");
		Func.opóznij(1, this::stwórzŚwiaty);
		
		// Ulepszenia
		for (Field field : Ulepszenia.class.getDeclaredFields())
			try {
				List<String> linie = Main.ust.wczytajListe("Skyblock.ulepszenia." + field.getName());
				Ulepszenia.Ulepszenie[] tab = new Ulepszenia.Ulepszenie[linie.size()];
				
				for (int i=0; i < tab.length; i++) {
					List<String> połówki = Func.tnij(linie.get(i), " ");
					tab[i] = new Ulepszenia.Ulepszenie(Func.Int(połówki.get(0)), Func.Double(połówki.get(1)));
				}
				
				field.set(null, tab);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				Main.warn("Nieprawidłowe ustawienia.yml \"Skyblock.ulepszenia." + field.getName() + "\"");
			}
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

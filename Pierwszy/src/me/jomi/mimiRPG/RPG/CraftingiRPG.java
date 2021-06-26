package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.QuadKrotka;
import me.jomi.mimiRPG.util.MultiConfig;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CraftingiRPG implements Przeładowalny, Listener {
	public static enum Typ {
		SHAPED,
		SHAPELESS;
	}
	
	public static final String prefix = Func.prefix(CraftingiRPG.class);
	private static final Multimap<String, Recepta> mapaZRezultu = HashMultimap.create();
	private static final Map<String, Recepta> mapaZId = new HashMap<>();
	private static final ItemStack itemBrakuRezultu = Func.stwórzItem(Material.STRUCTURE_VOID, "§cUłóż itemy po lewej");
	private static final ItemStack itemSzybkiegoCraftowania = Func.stwórzItem(Material.GRAY_STAINED_GLASS_PANE, "§6Szybkie Craftowanie", "§4Dostępne niebawem!");
	private static final ItemStack itemKsięgaReceptór = Func.stwórzItem(Material.BOOK, "§aKsięga receptów", "§4Dostępne niebawem!");
	private static final int slotKsięgaReceptór = 41;
	// TODO szybkie craftowanie
	// TODO księga receptór;
	public static abstract class Recepta {
		public final boolean domyślna; // false oznacza że musi być w pamięci gracza aby być wycratowaną
		public final String id;
		
		public final String rezult;
		public final int ilość;
		public Recepta(String id, boolean domyślna, String rezult, int ilość) {
			this.domyślna = domyślna;
			this.id = id;
			this.rezult = rezult;
			this.ilość = ilość;

			mapaZId.put(id, this);
			mapaZRezultu.put(rezult, this);
		}
		

		public boolean maDostęp(GraczRPG gracz) {
			return domyślna ? true : gracz.dataPamięć.getCompound("craftingi").hasKey(id);
		}
		public void nadajDostęp(GraczRPG gracz) {
			RPG.dataDajUtwórz(gracz.dataPamięć, "craftingi").setBoolean(id, true);
		}
		public void zabierzDostęp(GraczRPG gracz) {
			RPG.dataDajUtwórz(gracz.dataPamięć, "craftingi").remove(id);
		}
	
		
		public ItemStack rezult() {
			try {
				return Func.ilość(ZfaktoryzowaneItemy.dajItem(rezult), ilość);
			} catch (Throwable e) {
				Main.warn(prefix + "brak itemkarpg w bazie: " + rezult);
				throw Func.throwEx(e);
			}
		}
		
		
		@Override
		public boolean equals(Object o) {
			return 	o != null &&
					o.getClass().equals(this.getClass()) &&
					rezult.equals(((Recepta) o).rezult);
		}
		@Override
		public int hashCode() {
			return Objects.hash(id, rezult, ilość, domyślna);
		}
	}
	public static class ReceptaShaped extends Recepta {
		private static final Map<Integer, ReceptaShaped> mapaZId = new HashMap<>();
		
		public final String[][] matrix;
		public final int[][] ilości;
		
		public ReceptaShaped(String id, boolean domyślna, String rezult, int ilość, String[][] matrix, int[][] ilości) {
			super(id, domyślna, rezult, ilość);
			this.matrix = matrix;
			this.ilości = ilości;
			
			mapaZId.put(id(matrix), this);
		}
		
		public boolean możnaWycraftować(int[][] ilości) {
			if (ilości.length != this.ilości.length || ilości[0].length != this.ilości[0].length)
				throw new IllegalArgumentException("Niepoprawna tablica na wejściu ilości w craftingach RPG");
			
			for (int y=0; y < ilości.length; y++)
				for (int x=0; x < ilości[0].length; x++)
					if (ilości[y][x] < this.ilości[y][x])
						return false;
			return true;
		}
	
		public static int id(String[][] matrix) {
			StringBuilder strB = new StringBuilder();
			for (String[] Xmatrix : matrix) {
				for (String id : Xmatrix)
					strB.append(id == null ? "-" : id).append(" ");
				strB.append('\n');
			}
			return strB.toString().hashCode();
		}
	}
	public static class ReceptaShapeless extends Recepta {
		public static final Map<Integer, ReceptaShapeless> mapaZId = new HashMap<>();
		
		public final List<Krotka<String, Integer>> potrzebne;
		
		public ReceptaShapeless(String id, boolean domyślna, String rezult, int ilość, List<Krotka<String, Integer>> potrzebne) {
			super(id, domyślna, rezult, ilość);
			this.potrzebne = Func.posortuj(potrzebne, k -> Func.stringToDouble(k.a));
			
			mapaZId.put(id(Func.wykonajWszystkim(this.potrzebne, k -> k.a)), this);
		}
		
		public static int id(Collection<String> itemy) {
			int id = 0;
			for (String item : itemy)
				if (item != null)
					id += item.hashCode();
			return id;
		}
		public static int id(String[][] matrix) {
			int id = 0;
			for (String[] linia : matrix)
				for (String item : linia)
					if (item != null)
						id += item.hashCode();
			return id;
		}

		public boolean możeWycraftować(List<Krotka<String, Integer>> ilości) {
			Func.posortuj(ilości, k -> Func.stringToDouble(k.a));
			
			if (potrzebne.size() != ilości.size()) {
				Main.warn(Func.msg(prefix + "Coś poszło nie tak z receptą shapeless id=%s potrzebne=%s ilości=%s", id, potrzebne, ilości));
				return false;
			}
			
			for (int i=0; i < ilości.size(); i++) {
				Krotka<String, Integer> k1 = potrzebne.get(i);
				Krotka<String, Integer> k2 = ilości.get(i);
				
				if (!k1.a.equals(k2.a)) {
					Main.warn(Func.msg(prefix + "Coś poszło nie tak2 z receptą shapeless id=%s potrzebne=%s ilości=%s", id, potrzebne, ilości));
					return false;
				}
				
				if (k1.b > k2.b)
					return false;
			}
			
			return true;
		}
	}
	
	static final Panel panel = new Panel(false);
	public CraftingiRPG() {
		panel.ustawClick(ev -> {
			if (ev.getRawSlot() == 23) {
				switch (ev.getAction()) {
				case DROP_ONE_SLOT:
				case PICKUP_HALF:
					if (ev.getCurrentItem().getAmount() == 1)
						break;
				case DROP_ALL_CURSOR:
				case DROP_ONE_CURSOR:
				case NOTHING:
				case PICKUP_ONE:
				case PICKUP_SOME:
				case PLACE_ALL:
				case PLACE_ONE:
				case PLACE_SOME:
				case SWAP_WITH_CURSOR:
				case UNKNOWN:
				case HOTBAR_MOVE_AND_READD:
				case COLLECT_TO_CURSOR:
					ev.setCancelled(true);
					return;
				case CLONE_STACK:
				case HOTBAR_SWAP:
				case DROP_ALL_SLOT:
				case MOVE_TO_OTHER_INVENTORY:
				case PICKUP_ALL:
					break;
				}
				Inventory inv = ev.getInventory();
				QuadKrotka<Integer, Integer, Object, Recepta> krotka = panel.dajDanePaneluPewny(inv);
				if (krotka == null)
					ev.setCancelled(true);
				else
					krotka.wykonaj((X, Y, ilości, recepta) -> {
						Bukkit.getScheduler().runTask(Main.plugin, () -> {
							if (recepta instanceof ReceptaShaped) {
								ReceptaShaped receptaShaped = (ReceptaShaped) recepta;
								for (int y = Y; y < receptaShaped.ilości.length + Y; y++)
									for (int x = X; x < receptaShaped.ilości[0].length + X; x++) {
										int slot = (y + 1) * 9 + (x + 1);
										ItemStack item = inv.getItem(slot);
										if (item == null && receptaShaped.matrix[y - Y][x - X] == null)
											continue;
										
										int potrzebne = receptaShaped.ilości[y - Y][x - X];
										item.setAmount(item.getAmount() - potrzebne);
										
										if (item.getAmount() < 0)
											Main.error("Błąd w CraftingachRPG, id = 1");
										
										inv.setItem(slot, item.getAmount() <= 0 ? null : item);
									}
							} else if (recepta instanceof ReceptaShapeless) {
								ReceptaShapeless receptaShapeless = (ReceptaShapeless) recepta;
								
								List<Krotka<String, Integer>> lista = Func.wykonajWszystkim(receptaShapeless.potrzebne, k -> k);
								for (int y = 0; y < 3; y++)
									for (int x = 0; x < 3; x++) {
										int slot = (y + 1) * 9 + x + 1;
										ItemStack item = inv.getItem(slot);
										if (item == null) continue;
										
										String id = ZfaktoryzowaneItemy.id(item);
										for (int i=0; i < lista.size(); i++) {
											Krotka<String, Integer> potrzebny = lista.get(i);
											if (potrzebny.a.equals(id) && potrzebny.b <= item.getAmount()) {
												item.setAmount(item.getAmount() - potrzebny.b);
												inv.setItem(slot, item.getAmount() <= 0 ? null : item);
												lista.remove(i);
												break;
											}
										}
									}
								if (!lista.isEmpty())
									Main.warn("Błąd w CraftingachRPG, id = 3, recepta: " + recepta.id + " zostały: " + lista);
								
							} else
								Main.warn("Błąd w CraftingachRPG, id = 2, Nieznana recepta: " + recepta.id);
							odświeżRezult(ev);
						});
						NMS.nms(ev.getCurrentItem()).getTag().remove("mimiCraftingRPGRezult");
						odświeżRezult(ev);
					});
				return;
			} else {
				int r = ev.getRawSlot() / 9;
				int mod = ev.getRawSlot() % 9;
				if (r >= 1 && r <= 3 && mod >= 1 && mod <= 3) {
					odświeżRezult(ev);
					return;
				}
			}
			ev.setCancelled(true);
		});
		panel.ustawClose(ev -> {
			Player p = (Player) ev.getPlayer();
			for (int y = 1; y < 4; y++)
				for (int x = 1; x < 4; x++)
					Func.wykonajDlaNieNull(ev.getInventory().getItem(y * 9 + x), item -> Func.dajItem(p, item));
		});
	}
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		if (Func.multiEquals(ev.getClick(), ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT) &&
				ev.getRawSlot() >= ev.getInventory().getSize() && panel.jestPanelem(ev.getInventory()))
			odświeżRezult(ev);
	}
	private static void odświeżRezult(InventoryInteractEvent ev) {
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			String[][] matrix = new String[3][3];
			Inventory inv = ev.getInventory();
			
			panel.ustawDanePanelu(inv, null);
			
			for (int y=0; y < 3; y++)
				for (int x=0; x < 3; x++)
					matrix[y][x] = ZfaktoryzowaneItemy.id(inv.getItem((y + 1) * 9 + x + 1));
			
			
			boolean x1 = matrix[0][0] == null && matrix[1][0] == null && matrix[2][0] == null;
			boolean x2 = matrix[0][1] == null && matrix[1][1] == null && matrix[2][1] == null;
			boolean x3 = matrix[0][2] == null && matrix[1][2] == null && matrix[2][2] == null;
			
			if (x1 && x2 && x3) {
				inv.setItem(23, itemBrakuRezultu);
				return;
			}
			
			ReceptaShapeless receptaShapeless = ReceptaShapeless.mapaZId.get(ReceptaShapeless.id(matrix));
			if (receptaShapeless != null) {
				List<Krotka<String, Integer>> ilości = new ArrayList<>();
				for (int y=0; y < 3; y++)
					for (int x=0; x < 3; x++) {
						ItemStack item = inv.getItem((y + 1) * 9 + x + 1);
						if (item == null) continue;
						
						ilości.add(new Krotka<>(matrix[y][x], item.getAmount()));
					}
				
				if (receptaShapeless.możeWycraftować(ilości)) {
					ItemStack rezult = receptaShapeless.rezult();
					NMS.nms(rezult).getTag().setBoolean("mimiCraftingRPGRezult", true);
					inv.setItem(23, rezult);
					panel.ustawDanePanelu(inv, new QuadKrotka<>(-1, -1, ilości, receptaShapeless));
					return;
				}
			}
			
			
			
			boolean y1 = matrix[0][0] == null && matrix[0][1] == null && matrix[0][2] == null;
			boolean y2 = matrix[1][0] == null && matrix[1][1] == null && matrix[1][2] == null;
			boolean y3 = matrix[2][0] == null && matrix[2][1] == null && matrix[2][2] == null;
			
			int x = x1 ? (x2 ? 2 : 1) : 0;
			int y = y1 ? (y2 ? 2 : 1) : 0;
			int X = x3 ? (x2 ? 2 : 1) : 0;
			int Y = y3 ? (y2 ? 2 : 1) : 0;
			
			String[][] m = new String[3 - y - Y][3 - x - X];
			for (int my = 0; my < m.length; my++)
				for (int mx = 0; mx < m[0].length; mx++)
					m[my][mx] = matrix[my + y][mx + x];
			
			Func.wykonajDlaNieNull(getReceptaShaped(ReceptaShaped.id(m)), recepta -> {
				if (recepta.maDostęp(GraczRPG.gracz((Player) ev.getWhoClicked()))) {
					int[][] ilości = new int[m.length][m[0].length];
					for (int my = 0; my < m.length; my++)
						for (int mx = 0; mx < m[0].length; mx++)
							if (m[my][mx] != null)
								ilości[my][mx] = inv.getItem((my + y + 1) * 9 + (mx + x + 1)).getAmount();
					
					if (recepta.możnaWycraftować(ilości)) {
						ItemStack rezult = recepta.rezult();
						NMS.nms(rezult).getTag().setBoolean("mimiCraftingRPGRezult", true);
						inv.setItem(23, rezult);
						panel.ustawDanePanelu(inv, new QuadKrotka<>(x, y, ilości, recepta));
						return;
					}
				}
				inv.setItem(23, itemBrakuRezultu);
			}, () -> inv.setItem(23, itemBrakuRezultu));
		});
	}
	@EventHandler
	public void przeciągnie(InventoryDragEvent ev) {
		if (panel.jestPanelem(ev.getInventory()))
			odświeżRezult(ev);
	}
	
	@EventHandler
	public void otwieranieCraftingu(InventoryOpenEvent ev) {
		if (ev.getInventory().getType() == InventoryType.WORKBENCH) {
			ev.setCancelled(true);
			otwórzCrafting((Player) ev.getPlayer());
		}
	}
	
	public static void otwórzCrafting(Player p) {
		Inventory inv = panel.stwórz(null, 6, Func.koloruj("&#8c430e&lCrafting"));
		Func.ustawPuste(inv, Baza.pustySlotCzarny);

		for (int y=0; y < 3; y++)
			for (int x=0; x < 3; x++)
				inv.setItem((y + 1) * 9 + x + 1, null);
		
		inv.setItem(23, itemBrakuRezultu);
		inv.setItem(slotKsięgaReceptór, itemKsięgaReceptór);
		
		for (int i = 16; i < 16 + 4*9; i += 9)
			inv.setItem(i, itemSzybkiegoCraftowania);
		
		p.openInventory(inv);
	}
	
	
	public static ReceptaShaped getReceptaShaped(int id) {
		return ReceptaShaped.mapaZId.get(id);
	}
	public static Collection<Recepta> getRecepta(String rezult) {
		return mapaZRezultu.get(rezult);
	}
	
	@Override
	public void przeładuj() {
		MultiConfig config = new MultiConfig("craftingiRPG");
		mapaZRezultu.clear();
		mapaZId.clear();
		ReceptaShaped.mapaZId.clear();
		ReceptaShapeless.mapaZId.clear();
		
		config.klucze().forEach(klucz -> {
			try {
				ConfigurationSection sekcja = config.sekcja(klucz);
				
				boolean domyślna = sekcja.getBoolean("domyślna", true);
				String rezult = sekcja.getString("rezult");
				int ilośćRezultu = 1;
				List<String> części = Func.tnij(rezult, " ");
				if (części.size() == 2) {
					try {
						ilośćRezultu = Func.Int(części.get(1));
						rezult = części.get(0);
					} catch (Throwable e) {
						Main.warn(prefix + "Niepoprawna ilość w craftinguRPG: " + klucz);
					}
				}
				
				
				Typ typ = Func.StringToEnum(Typ.class, sekcja.getString("typ", "shaped"));
				
				switch (typ) {
				case SHAPED:
					wczytajShaped(sekcja, klucz, domyślna, rezult, ilośćRezultu);
					break;
				case SHAPELESS:
					wczytajShapeless(sekcja, klucz, domyślna, rezult, ilośćRezultu);
					break;
				}
				
			} catch (Throwable e) {
				Main.warn("problem z craftingiem rpg: " + klucz);
			}
		});
		
		sprawdzPoprawność();
	}
	private ReceptaShapeless wczytajShapeless(ConfigurationSection sekcja, String klucz, boolean domyślna, String rezult, int ilośćRezultu) {
		List<Krotka<String, Integer>> lista = new ArrayList<>();
		sekcja.getStringList("itemy").forEach(str -> {
			List<String> części = Func.tnij(str,  " ");
			String item = części.get(0);
			int ile = części.size() >= 1 ? Func.Int(części.get(1)) : 1;
			lista.add(new Krotka<>(item, ile));
		});
		return new ReceptaShapeless(klucz, domyślna, rezult, ilośćRezultu, lista);
	}
	private ReceptaShaped wczytajShaped(ConfigurationSection sekcja, String klucz, boolean domyślna, String rezult, int ilośćRezultu) {
		List<List<Krotka<String, Integer>>> matrixList = new ArrayList<>();
		sekcja.getStringList("matrix").forEach(linia -> {
			List<Krotka<String, Integer>> lista = new ArrayList<>();
			matrixList.add(lista);
			List<String> elementy = Func.tnij(linia, " ");
			for (int i=0; i < elementy.size(); i += 2) {
				int ilość = Func.Int(elementy.get(i + 1));
				String item = elementy.get(i);
				lista.add(new Krotka<>(item, ilość));
			}
		});
		
		String[][] matrix = new String[matrixList.size()][matrixList.get(0).size()];
		int[][]    ilości = new int[matrix.length][matrix[0].length];
		for (int y = 0; y < matrix.length; y++) {
			List<Krotka<String, Integer>> lista = matrixList.remove(0);
			for (int x = 0; x < matrix[0].length; x++) {
				Krotka<String, Integer> krotka = lista.remove(0);
				matrix[y][x] = krotka.a.equals("-") ? null : krotka.a;
				ilości[y][x] = krotka.a.equals("-") ? 0    : krotka.b;
			}
			if (!lista.isEmpty())
				throw new IllegalArgumentException();
		}
		if (!matrixList.isEmpty())
			throw new IllegalArgumentException();
		
		return new ReceptaShaped(klucz, domyślna, rezult, ilośćRezultu, matrix, ilości);
	}
	
	
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("wczytane craftingi RPG", ReceptaShaped.mapaZId.size());
	}

	
	static void sprawdzPoprawność() {
		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			Set<String> zgłoszone = new HashSet<>();
			Set<String> itemy = Sets.newConcurrentHashSet(ZfaktoryzowaneItemy.itemy());
			mapaZId.values().forEach(r -> {
				Consumer<String> sprawdz = item -> {
					if (item != null && !zgłoszone.contains(item) && !itemy.contains(item)) {
						Main.warn(Func.msg(prefix + "Nie odnaleziono itemu %s w craftingu na %s", item, r.rezult));
						zgłoszone.add(item);
					}
				};
				if (r instanceof ReceptaShaped) {
					for (String[] linia : ((ReceptaShaped) r).matrix)
						for (String item : linia)
							sprawdz.accept(item);
				} else if (r instanceof ReceptaShapeless){
					((ReceptaShapeless) r).potrzebne.forEach(k -> sprawdz.accept(k.a));
				} else
					Main.warn("Nieznany typ recepty: " + r.getClass().getSimpleName());
				sprawdz.accept(r.rezult);
			});
		});
	}


	/// DEBUG mdebug RPG.CraftingiRPG generowanieConfigów()
	static void generowanieConfigów() {
		Bukkit.recipeIterator().forEachRemaining(r -> {
			String sc = "craftingiRPG/vanilla/";
			if (r instanceof ShapedRecipe) {
				ShapedRecipe sr = (ShapedRecipe) r;
				String klucz = sr.getKey().getKey();
				Config config = new Config(sc + klucz);
				sc = klucz + ".";
				
				config.ustaw(sc + "rezult", sr.getResult().getType().name().toLowerCase() + " " + sr.getResult().getAmount());
				config.ustaw(sc + "domyślna", true);
				config.ustaw(sc + "typ", "shaped");
				
				List<String> matrix = new ArrayList<>();
				for (String linia : sr.getShape()) {
					 StringBuilder strB = new StringBuilder();
					 for (char c : linia.toCharArray()) {
						 ItemStack item = sr.getIngredientMap().get(c);
						 strB.append(item == null ? "-" : item.getType().name().toLowerCase()).append(" 1 ");
					 }
					 matrix.add(strB.toString().substring(0, strB.toString().length() - 1));
				}
				
				config.ustaw(sc + "matrix", matrix);
				
				config.zapisz();
			} else if (r instanceof ShapelessRecipe) {
				ShapelessRecipe sr = (ShapelessRecipe) r;
				String klucz = sr.getKey().getKey();
				Config config = new Config(sc + klucz);
				sc = klucz + ".";
				
				config.ustaw(sc + "rezult", sr.getResult().getType().name().toLowerCase() + " " + sr.getResult().getAmount());
				config.ustaw(sc + "domyślna", true);
				config.ustaw(sc + "typ", "shapeless");
				
				List<String> itemy = new ArrayList<>();
				sr.getIngredientList().forEach(item -> itemy.add(item.getType().name().toLowerCase() + " " + item.getAmount()));
				config.ustaw(sc + "itemy", itemy);
				
				config.zapisz();
			} else {
				Main.log("Pomijany crafting na %s class %s", Func.enumToString(r.getResult().getType()), r.getClass().getSimpleName());
			}
		});
	}
}

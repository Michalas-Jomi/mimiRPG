package me.jomi.mimiRPG.MineZ;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.ItemCreator;
import me.jomi.mimiRPG.util.ItemCreator.Creator;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class SkinyItemków extends Komenda implements Listener, Przeładowalny {
	public static class Holder extends Func.abstractHolder {
		public Holder(int rzędy) {
			super(rzędy, "&4&lSkiny");
			Func.ustawPuste(inv);
		}
	}

	public static class Grupa {
		// kod itemka: Grupa
		static final HashMap<String, Grupa> mapaGrup = new HashMap<>(); 
		
		final Set<String> kody = Sets.newConcurrentHashSet();
		final String podstawowy;
		
		public Grupa(String kodPodstawowy) {
			this.podstawowy = kodPodstawowy;
			Grupa.mapaGrup.put(kodPodstawowy, this);
		}
		
		List<ItemStack> skiny(Player p, ItemStack item) {
			List<ItemStack> itemy = Lists.newArrayList();
			
			Consumer<String> dodaj = kod ->
					Func.insort(przetwórz(item.clone(), kod), itemy, __ -> (double) (int) mapaNazw.getOrDefault(kod, new Krotka<>(null, 0)).b);
			
			
			dodaj.accept(podstawowy);
			
			kody.forEach(kod -> {
				if (p.hasPermission(kodToPerm(kod)))
					dodaj.accept(kod);
			});
			
			return itemy;
		}
		
		@Override
		public String toString() {
			return String.format(" ||%s%s|| ", podstawowy, kody);
		}
	}
	static ItemStack przetwórz(ItemStack item, String kod) {
		Krotka<Material, Integer> krotka = odkoduj(kod);
		Creator ic = ItemCreator.nowy(
				item)
				.typ(krotka.a)
				.customModelData(krotka.b);
		Func.wykonajDlaNieNull(mapaNazw.get(kod), k -> ic.nazwa(k.a));
		return ic.stwórz();
		
	}
	static String kodToPerm(String kod) {
		return Func.permisja("mimirpg.skinyItemów." + kod);
	}
	static Krotka<Material, Integer> odkoduj(String kod) {
		List<String> części = Func.tnij(kod, "-");
		return new Krotka<>(Func.StringToEnum(Material.class, części.get(0)), Func.Int(części.get(1)));
	}
	static String kod(ItemStack item) {
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData())
			return null;
		return item.getType().toString().toLowerCase() + "-" + item.getItemMeta().getCustomModelData();
	}
	static Grupa wczytaj(String kod) {
		return Grupa.mapaGrup.get(kod);
	}
	static Grupa wczytaj(ItemStack item) {
		return wczytaj(kod(item));
	}
	static boolean podstawowy(ItemStack item) {
		String kod = kod(item);
		Grupa grp = wczytaj(kod);
		
		return grp.podstawowy.equals(kod);
	}
	static void sprawdz(Player p, ItemStack item, Consumer<ItemStack> cons) {
		String kod = kod(item);
		Func.wykonajDlaNieNull(wczytaj(kod), grp -> {
			if (!grp.podstawowy.equals(kod) && !p.hasPermission(kodToPerm(kod)))
				cons.accept(przetwórz(item.clone(), grp.podstawowy));
		});
	}
	
	public SkinyItemków() {
		super("skinyitemków", null, "skiny");
	}
	
	
	// EventHandler
	
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			ev.setCancelled(true);
			
			if (!Func.multiEquals(ev.getClick(), ClickType.LEFT, ClickType.RIGHT))
				return;
			
			int slot = ev.getRawSlot();
			if (ev.getCurrentItem().isSimilar(Baza.pustySlot) || slot < 0 || slot >= ev.getInventory().getSize())
				return;
			
			ev.getWhoClicked().getInventory().setItemInMainHand(ev.getCurrentItem());
			ev.getWhoClicked().closeInventory();
		});
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void wyrzucanie(PlayerDropItemEvent ev) {
		ItemStack item = ev.getItemDrop().getItemStack();
		Func.wykonajDlaNieNull(wczytaj(item), grp -> ev.getItemDrop().setItemStack(przetwórz(item, grp.podstawowy)));
	}
	@EventHandler(priority = EventPriority.LOW)
	public void śmierć(PlayerDeathEvent ev) {
		ev.getDrops().forEach(item -> Func.wykonajDlaNieNull(wczytaj(item), grp -> przetwórz(item, grp.podstawowy)));
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanie2(InventoryClickEvent ev) {
		if (!ev.isCancelled())
			Func.wykonajDlaNieNull(ev.getCurrentItem(), item -> {
				sprawdz((Player) ev.getWhoClicked(), item, ev::setCurrentItem);
			});
	}
	
	
	// util
	
	boolean otwórzPanel(Player p) {
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item == null)
			return Func.powiadom(p, "Musisz trzymać coś w ręce");
		Grupa grp = wczytaj(item);
		if (grp == null)
			return Func.powiadom(p, "Ten item nie ma dodatkowych skinów");
		List<ItemStack> itemy = grp.skiny(p, item);
		Inventory inv = new Holder(Func.potrzebneRzędy(itemy.size())).getInventory();
		int i=0;
		while (!itemy.isEmpty())
			inv.setItem(i++, itemy.remove(0));
		p.openInventory(inv);
		return false;
	}
	
	
	// Override
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, "Dostępne tylko dla Graczy");
		otwórzPanel((Player) sender);
		return true;
	}
	
	
	// kod: (nazwa, priorytet)
	static final HashMap<String, Krotka<String, Integer>> mapaNazw = new HashMap<>();
	
	
	
	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		Grupa.mapaGrup.clear();
		mapaNazw.clear();
		
		File f = new File(Main.ust.wczytajLubDomyślna("Skiny itemków ustawienia.config z losowaniem", "plugins/JbwmMiscellaneous/csgoCase.yml"));
		if (f.exists()) {
			Config configNazw = new Config(f);
			Func.wykonajDlaNieNull(configNazw.sekcja("skiny"), sekcja -> 
				sekcja.getKeys(false).forEach(klucz -> {
					try {
						ItemStack item = sekcja.getItemStack(klucz + ".item");
						mapaNazw.put(kod(item), new Krotka<>(item.getItemMeta().getDisplayName(), Func.Int(klucz)));
					} catch(Throwable e) {
						Main.warn("Nieprawidłowość z " + f.getAbsolutePath() + " skiny." + klucz);
					}
				}));
		}
		
		
		Main.ust.wczytajListeMap("Skiny itemków").forEach(mapa -> {
			try {
				Grupa grp = new Grupa(mapa.get("podstawowy").toString().toLowerCase());
				((Map<String, List<Integer>>) mapa.get("skiny")).forEach((str, lista) -> {
					String _str = Func.StringToEnum(Material.class, str).toString().toLowerCase() + "-";
					lista.forEach(i -> {
						grp.kody.add(_str + i);
						Grupa.mapaGrup.put(_str + i, grp);
					});
				});
			} catch (Throwable e) {
				Main.warn("Nieprawidłowa sekcja ustawienia.yml skinów itemków");
			}
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane Skiny", Grupa.mapaGrup.size());
	}
}

package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Timming;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class WymieniaczItemów extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix(WymieniaczItemów.class);
	public static final class Wymiana extends Mapowany {
		@Mapowane ItemStack co;
		@Mapowane ItemStack wCo;
		@Mapowane boolean info;
		
		public Wymiana() {}
		public Wymiana(ItemStack item1, ItemStack item2, boolean info) {
			Objects.requireNonNull(item1);
			Objects.requireNonNull(item2);
			
			item1.setAmount(1);
			item2.setAmount(1);
			
			this.co = item1;
			this.wCo = item2;
			this.info = info;
		}
	}

	public static final String permBypass = Func.permisja("wymieniaczitemów.bypass");
	private static List<Wymiana> podmiany = new ArrayList<>();
	private static final Panel panel = new Panel(true);
	
	private static final ItemStack itemInfoNie = Func.stwórzItem(Material.RED_STAINED_GLASS_PANE, "&6Informacja o podmianie", "§cWyłączona", "&bGracz nie otrzyma informacji o podmianie itemku");
	private static final ItemStack itemInfoTak = Func.stwórzItem(Material.LIME_STAINED_GLASS_PANE, "&6Informacja o podmianie", "§aWłączona", "&bGracz otrzyma informacje o podmianie itemku");
	private static final ItemStack itemNastępnaStrona = Func.stwórzItem(Material.WRITABLE_BOOK, "&6Następna Strona");
	private static final ItemStack itemPoprzedniaStrona = Func.stwórzItem(Material.WRITABLE_BOOK, "&6Poprzednia Strona");
	private static final ItemStack itemStrzałka = Func.dajGłówkę("&6Podmieni ten z lewej na ten z prawej", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=");
	
	
	public WymieniaczItemów() {
		super("WymieniaczItemów");
		
		Main.dodajPermisje(permBypass);
		
		panel.ustawClick(ev -> {
			ItemStack item = ev.getCurrentItem();
			int slot = ev.getRawSlot();
			Inventory inv = ev.getInventory();
			
			if (slot < 9*5 && (slot % 9 == 2 || slot % 9 == 4))	ev.setCancelled(false);
			else if (item.isSimilar(itemInfoTak))				inv.setItem(slot, itemInfoNie);
			else if (item.isSimilar(itemInfoNie))				inv.setItem(slot, itemInfoTak);
			else if (item.isSimilar(itemNastępnaStrona))		dajPanel((Player) ev.getWhoClicked(), ((int) panel.dajDanePanelu(inv)) + 1);
			else if (item.isSimilar(itemPoprzedniaStrona))		dajPanel((Player) ev.getWhoClicked(), ((int) panel.dajDanePanelu(inv)) - 1);
		});
		
		panel.ustawClose(ev -> zapiszItemy((Player) ev.getPlayer()));
	}

	// strona >= 1
	public static void dajPanel(Player p, int strona) {
		if (strona < 0) return;
		
		Inventory inv = panel.stwórz(strona, 6, "§1§lWymieniacz Itemów", Baza.pustySlotCzarny);
		
		for (int i=0; i < 5 * 9; i += 9) {
			try {
				Wymiana wymiana = podmiany.get(strona*5 + i/9);
				inv.setItem(i + 2, wymiana.co);
				inv.setItem(i + 4, wymiana.wCo);
				inv.setItem(i + 7, wymiana.info ? itemInfoTak : itemInfoNie);
			} catch (IndexOutOfBoundsException e) {
				inv.setItem(i + 2, null);
				inv.setItem(i + 4, null);
				inv.setItem(i + 7, itemInfoNie);
			}
			
			inv.setItem(i + 3, itemStrzałka);
		}
		
		if (strona > 0)
			inv.setItem(5*9+0, itemPoprzedniaStrona);
		inv.setItem(5*9+8, itemNastępnaStrona);
		
		p.openInventory(inv);
	}
	public static void zapiszItemy(Player p) {
		Inventory inv = p.getOpenInventory().getTopInventory();
		if (!panel.jestPanelem(inv)) return;
		
		int strona = panel.dajDanePaneluPewny(inv);
		
		int index = strona * 5;
		
		for (int i=0; i < 5 * 9; i += 9) {
			ItemStack item1 = inv.getItem(i + 2);
			ItemStack item2 = inv.getItem(i + 4);
			ItemStack info  = inv.getItem(i + 7);
			
			if (item1 == null || item2 == null || item1.isSimilar(item2)) {
				Func.dajItem(p, item1);
				Func.dajItem(p, item2);
				
				if (podmiany.size() > index)
					podmiany.remove(index);
				
				index--;
			} else if (podmiany.size() <= index)
				podmiany.add(new Wymiana(item1, item2, info.isSimilar(itemInfoTak)));
			else 
				podmiany.set(index, new Wymiana(item1, item2, info.isSimilar(itemInfoTak)));
			
			index++;
		}
		
		 new Config("configi/WymieniaczItemów").ustaw_zapisz("wymiany", podmiany);
	}

	public static void sprawdz(Player p) {
		if (p.hasPermission(permBypass)) return;
		
		Timming.test("WymieniaczItemów sprawdzanie", () -> {
			PlayerInventory inv = p.getInventory();
			for (int i=0; i < inv.getSize(); i++) {
				ItemStack item = inv.getItem(i);
				if (item == null || item.getType().isAir())
					continue;
				
				final int fi = i;
				podmiany.forEach(wymiana -> {
					if (wymiana.co.isSimilar(item)) {
						ItemStack item2 = wymiana.wCo.clone();
						item2.setAmount(item.getAmount());
						inv.setItem(fi, item2);
						
						if (wymiana.info)
							new Napis(prefix)
								.dodaj("§6Item ")
								.dodaj(Napis.item(item))
								.dodaj(" §6w twoim eq został podmieniony na ")
								.dodaj(Napis.item(item2))
								.wyświetl(p);
						
						Main.log(prefix + "Podmieniono u gracza %s item %s na %s", Func.getDisplayName(p), Func.nazwaItemku(item), Func.nazwaItemku(item2));
					}
				});
			}
		});
	}
	
	
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		sprawdz(ev.getPlayer());
	}
	@EventHandler
	public void tp(PlayerTeleportEvent ev) {
		sprawdz(ev.getPlayer());
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void przeładuj() {
		Config config = new Config("configi/WymieniaczItemów");
		podmiany = Func.nieNull((List<Wymiana>) config.wczytajPewny("wymiany"));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wymieniane Itemy", podmiany.size());
	}


	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (!(sender instanceof Player))
			throwFormatMsg("Musisz być graczem aby tego użyć");
		
		dajPanel((Player) sender, 0);
		
		return true;
	}

}

package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class MenuItemów extends Komenda implements Przeładowalny {
	public static final String prefix = Func.prefix(MenuItemów.class);
	
	Panel panel = new Panel(false);
	Panel panelEdycji = new Panel(false);

	// kategoria : panel
	Map<String, Inventory> kategorie = new HashMap<>();
	Inventory invKategori = panelEdycji.stwórz("główny", 6, "§1§lMenuItemów §7(edytowanie)");
	
	static MenuItemów inst;
	public MenuItemów() {
		super("menuitemów", "/menuitemów (kategoria)");
		ustawKomende("edytujmenuitemów", "/edytujmenuitemów (kategoria)", null);
		inst = this;
		
		panel.ustawClick(ev -> {
			if (Baza.pustySlot.isSimilar(ev.getCurrentItem()))
				ev.setCancelled(true);
			else if ("Menu Itemów".equals(panel.dajDanePanelu(ev.getInventory()))) {
				String kategoria = Func.usuńKolor(Func.nazwaItemku(ev.getCurrentItem()));
				otwórzKategorie((Player) ev.getWhoClicked(), kategorie.get(kategoria), kategoria);
			}
		});
		
		panelEdycji.ustawClose(ev -> {
			Object zapisywane = panelEdycji.dajDanePanelu(ev.getInventory());
			Main.log(prefix + Func.msg("%s zapisał menu %s", ev.getPlayer().getName(), zapisywane));
			zapisz();
			przeładuj();
			ev.getPlayer().sendMessage(prefix + Func.msg("zapisano %s", zapisywane));
		});
	}
	
	void otwórzKategorie(Player p, Inventory inv, String kategoria) {
		int ost = inv.getSize();
		
		while (--ost > 0 && inv.getItem(ost) == null);
		
		Inventory pinv = panel.stwórz(kategoria, Func.potrzebneRzędy(ost), "§4§l" + kategoria);
		Func.ustawPuste(pinv);
		for (int i=0; i < inv.getSize(); i++)
			if (inv.getItem(i) != null)
				pinv.setItem(i, inv.getItem(i));
		
		p.openInventory(pinv);
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, kategorie.keySet());
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (!(sender instanceof Player))
			throwFormatMsg("Musisz być graczem");
		
		Player p = (Player) sender;

		Inventory inv;
		String nazwa = "Menu Itemów";
		if (args.length >= 1) {
			if ((inv = kategorie.get(args[0])) == null)
				throwFormatMsg("Niepoprawna kategoria: %s", args[0]);
			nazwa = args[0];
		} else
			inv = invKategori;
		
		if (cmd.getName().equals("edytujmenuitemów")) {
			p.openInventory(inv);
			return true;
		}
		
		otwórzKategorie(p, inv, nazwa);
		
		
		return true;
	}



	public void zapisz() {
		Config config = new Config("configi/MenuItemów");
		
		Func.backUp(config.f);
		
		config.ustaw("kategorie", null);

		ItemStack item;
		for (int i=0; i < invKategori.getSize(); i++)
			if ((item = invKategori.getItem(i)) != null) {
				String sc = "kategorie." + Func.usuńKolor(Func.nazwaItemku(item)) + ".";
				config.ustaw(sc + "slot", i);
				config.ustaw(sc + "item", item);
			}
		
		kategorie.forEach((kategoria, inv) -> {
			String sc = "kategorie." + kategoria + ".";
			
			if (config.wczytaj(sc + "item") == null) return;
			
			ItemStack item2;
			for (int i=0; i < inv.getSize(); i++)
				if ((item2 = inv.getItem(i)) != null)
					config.ustaw(sc + i, item2);
		});
		
		config.zapisz();
	}
	
	@Override
	public void przeładuj() {
		Config config = new Config("configi/MenuItemów");
		
		invKategori.clear();
		kategorie.clear();
		Func.wykonajDlaNieNull(config.sekcja("kategorie"), sekcja ->
			sekcja.getKeys(false).forEach(kategoria -> {
				try {
					ConfigurationSection sekcjaKategori = sekcja.getConfigurationSection(kategoria);
					
					invKategori.setItem(Func.Int(sekcjaKategori.get("slot")), Func.nazwij(Func.nieNull(Config.item(sekcjaKategori.get("item"))), "§9" + kategoria));
					
					Map<Integer, ItemStack> mapa = new HashMap<>();
					
					sekcjaKategori.getValues(false).forEach((slot, item) -> {
						if (slot.equals("slot") || slot.equals("item"))
							return;
						mapa.put(Func.Int(slot), Config.item(item));
					});
					
					Inventory inv = panelEdycji.stwórz("kat. " + kategoria, 6, "§c§l" + kategoria + " §7(edytowanie)");
					
					mapa.forEach(inv::setItem);
					
					kategorie.put(kategoria, inv);
				} catch (Throwable e) {
					Main.warn(prefix + "problem z kategorią " + kategoria);
					e.printStackTrace();
				}
			}));
	}
	@Override
	public Krotka<String, Object> raport() {
		int ile = 0;
		
		for (Inventory inv : kategorie.values())
			for (ItemStack item : inv)
				if (item != null && !item.isSimilar(Baza.pustySlot))
					ile++;
		
		return Func.r("Itemy w menuItemów", ile);
	}
}

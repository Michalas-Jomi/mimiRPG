package me.jomi.mimiRPG.Customizacja;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.MineZ.AbstractKarabiny;
import me.jomi.mimiRPG.MineZ.Bazy;
import me.jomi.mimiRPG.MineZ.Karabiny;
import me.jomi.mimiRPG.RPG.ZfaktoryzowaneItemy;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Funkcje.TriPredicate;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Panel;

@Moduł
public class CustomoweItemy extends Komenda {
	public static final String prefix = Func.prefix("Customowe Itemy");
	
	public CustomoweItemy() {
		super("customowyitem", prefix + "/citem [[baza | custom] <nick> <item> | bazy <item> | karabin [broń | ammo] <item> | rpg <item> | panel]", "citem");
		panel.ustawClick(ev -> {
			if (ev.getCurrentItem() == null)
				return;
			if (ev.getCurrentItem().isSimilar(Baza.pustySlot)) {
				ev.setCancelled(true);
				return;
			}
			
			int plus;
			if (ev.getRawSlot() == 45)
				plus = -1;
			else if (ev.getRawSlot() == 53)
				plus = 1;
			else
				return;
			ev.setCancelled(true);

			Krotka<Integer, List<ItemStack>> krotka = panel.dajDanePaneluPewny(ev.getInventory());
			if (krotka.a + plus >= 0) {
				panel.ustawDanePanelu(ev.getInventory(), new Krotka<>(krotka.a + plus, krotka.b));
				odświeżPanel(ev.getInventory());
			}
		});
	}
	
	
	public static final HashMap<String, ItemStack> customoweItemy = new HashMap<>();
	
	static final Panel panel = new Panel(false);
	public static void otwórzPanel(Player p) {
		List<ItemStack> itemy = Lists.newArrayList(Baza.itemy.values());
		Func.posortuj(itemy, item -> {
			try {
				return Func.stringToDouble(Func.nazwaItemku(item));
			} catch (Throwable e) {
				return Double.MAX_VALUE;
			}
		});
		
		Inventory inv = panel.stwórz(new Krotka<>(0, itemy), 6, "§1Baza Itemów");
		Func.ustawPuste(inv);
		
		odświeżPanel(inv);
		
		p.openInventory(inv);
	}
	static void odświeżPanel(Inventory inv) {
		Krotka<Integer, List<ItemStack>> krotka = panel.dajDanePaneluPewny(inv);
		krotka.wykonaj((strona, itemy) -> {
			inv.setItem(45, Func.stwórzItem(Material.WRITTEN_BOOK, "§cStrona " + (strona - 1)));
			inv.setItem(53, Func.stwórzItem(Material.WRITTEN_BOOK, "§cStrona " + (strona + 1)));

			int start = (9 * 6 - 2) * strona;
			for (int i = 0; i < 9 * 6 - 1; i++) {
				if (i == 45) continue;
				
				inv.setItem(i, itemy.size() <= i + start ? Baza.pustySlot : itemy.get(i + start));
			}
		});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) {
			List<String> lista = Lists.newArrayList("baza", "panel");
			if (!customoweItemy.isEmpty())
				lista.add("custom");
			if (Main.włączonyModół(Bazy.class))
				lista.add("bazy");
			if (Main.włączonyModół(Karabiny.class))
				lista.add("karabin");
			if (Main.włączonyModół(ZfaktoryzowaneItemy.class))
				lista.add("rpg");
			lista.add("ustaw");
			return utab(args, lista);
		}
		switch (args[0].toLowerCase()) {
		case "baza":
			if (args.length == 2)
				return null;
			return uzupełnijTabComplete(Func.listToString(args, 2), Baza.itemy.keySet());
		case "custom":
			if (args.length == 2)
				return null;
			return uzupełnijTabComplete(Func.listToString(args, 2), customoweItemy.keySet());
		case "bazy":
			if (args.length == 2)
				return utab(args, Bazy.getBazy());
			break;
		case "rpg":
			if (args.length == 2)
				return utab(args, ZfaktoryzowaneItemy.itemy());
			break;
		case "karabin":
			if (args.length == 2)
				return utab(args, "broń", "ammo");
			if (args.length == 3)
				return utab(args, AbstractKarabiny.getKarabiny());
			break;
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase("panel") && sender instanceof Player) {
			otwórzPanel((Player) sender);
			return true;
		}
		if (args.length < 2) return false;
		
		if (sender instanceof Player && args.length >= 2 && args[0].equalsIgnoreCase("ustaw")) {
			Player p = (Player) sender;
			if (p.getInventory().getItemInMainHand() == null)
				throwFormatMsg("Musisz trzymać coś w łapce aby tego użyć");
			String ścieżka = Func.listToString(args, 1);
			new Config("Customowe Itemy").ustaw_zapisz(ścieżka, p.getInventory().getItemInMainHand());
			Baza.przeładuj();
			throwFormatMsg("Wyeksportowałeś item pod nazwą %s", ścieżka);
		}
		
		
		TriPredicate<CommandSender, String, String> tric = null;
		
		switch (args[0].toLowerCase()) {
		case "baza":	tric = this::dajzBazy;	break;
		case "custom":	tric = this::dajCustom;	break;
		}
		
		if (tric != null)
			return tric.apply(sender, args[1], Func.listToString(args, 2));

		
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		
		Player p = (Player) sender;
		ItemStack item;
		switch (args[0].toLowerCase()) {
		case "bazy":
			// /citem bazy <nazwa>
			if (!Main.włączonyModół(Bazy.class))
				return Func.powiadom(p, prefix + "Ta funkcja aktualnie jest wyłączona w configu");
			item = Bazy.getBaze(args[1]);
			if (item == null)
				return Func.powiadom(prefix, p, "Niepoprawna nazwa %s", args[1]);
			break;
		case "rpg":
			if (!Main.włączonyModół(ZfaktoryzowaneItemy.class))
				return Func.powiadom(p, prefix + "Ta funkcja aktualnie jest wyłączona w configu");
			item = ZfaktoryzowaneItemy.dajItem(args[1]);
			if (item == null)
				return Func.powiadom(prefix, p, "Niepoprawne id %s", args[1]);
			break;
		case "karabin":
			// /citem karabin [broń / ammo] <nazwa>
			if (!Main.włączonyModół(Karabiny.class))
				return Func.powiadom(p, prefix + "Ta funkcja aktualnie jest wyłączona w configu");
			Function<String, ItemStack> func;
			switch (args[1].toLowerCase()) {
			case "broń":
			case "bron":
				func = AbstractKarabiny::getKarabin;
				break;
			case "ammo":
				func = AbstractKarabiny::getAmmunicje;
				break;
			default:
				return Func.powiadom(p, prefix + "/citem karabin [broń / ammo] <karabin>");
			}
			if (args.length < 3)
				return Func.powiadom(p, prefix + "/citem karabin [broń / ammo] <karabin>");
			String nazwa = Func.listToString(args, 2);
			item = func.apply(nazwa);
			if (item == null) 
				return Func.powiadom(prefix, p, "Niepoprawna nazwa karabinu %s", nazwa);
			Func.dajItem(p, item);
			return Func.powiadom(prefix, p, "Otrzymałeś %s %s", args[1], nazwa);
		default:
			return false;
		}
		Func.dajItem(p, item);
		return Func.powiadom(prefix, p, "Otrzymałeś item", args[1]);
	}

	private boolean dajCustom(CommandSender sender, String nick, String item) {
		return dajzMapy(sender, nick, item, customoweItemy);
	}
	private boolean dajzBazy(CommandSender sender, String nick, String item) {
		return dajzMapy(sender, nick, item, Baza.itemy);
	}
	private boolean dajzMapy(CommandSender sender, String nick, String item, Map<String, ItemStack> mapa) {
		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p ->
			Func.wykonajDlaNieNull(mapa.get(item), citem -> {
				Func.dajItem(p, citem);
				sender.sendMessage(prefix + Func.msg("Dano %s customowy item %s.", p.getDisplayName(), item));
			}, () -> sender.sendMessage(prefix + Func.msg("Niepoprawny item: %s.", item))),
		() -> sender.sendMessage(prefix + Func.msg("Niepoprawna nazwa gracza: %s.", nick)));
		return true;
	}
	
	public static String raport() {
		return "§6CustomoweItemy: §e" + Baza.itemy.size();
	}
	
}

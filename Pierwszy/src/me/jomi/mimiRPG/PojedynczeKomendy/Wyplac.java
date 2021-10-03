package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

import net.milkbowl.vault.economy.EconomyResponse;

@Moduł
public class Wyplac extends Komenda implements Listener {

	public Wyplac() {
		super("wypłać", "/wypłać <kwota> <ile>", "withdraw");
		ustawKomende("stworzbanknot", "/stworzbanknot <kwota> [ilość]", null);
	}

	public static String prefix = Func.prefix("Banknot");
	
	private static boolean stworz(Player p, String[] args) {
		double kwota = sprawdz(p, args);
		if (kwota == 0) return Func.powiadom(p, prefix + "/stworzbanknot <kwota> [ilość]");

		kwota = Func.zaokrąglij(kwota, 2);

		int ile = 1;
		if (args.length >= 2)
			ile = Func.Int(args[1], 1);
		if (ile > 64) ile = 64;
		if (ile <= 0) ile = 1;
		
		p.getInventory().addItem(dajBanknot(kwota, ile));
		p.sendMessage(prefix + "Utworzono nowy banknot o wartości §e" + Func.DoubleToString(kwota) + "$§6 w ilości §e" + ile);
		
		return true;
	}
	private static ItemStack dajBanknot(double kwota, int ile) {
		ItemStack item = Func.stwórzItem(Material.PAPER, ile, "§9§lBanknot§2", Arrays.asList("&bUżyj Prawym Przyciskiem Myszy", "&bWartość:&a " + Func.DoubleToString(kwota)));
		return Func.ukryj(Func.enchantuj(item, Enchantment.ARROW_INFINITE, 1), ItemFlag.HIDE_ENCHANTS);
	}
	
	private static double sprawdz(Player p, String[] args) {
		if (!Main.ekonomia)
			return powiadom(p, "Na serwerze nie ma wgranej odpowiedniej ekonomi");
		
		if (args.length <= 0)
			return powiadom(p, "/wypłać <kwota> [ilość]");
		
		if (p.getInventory().firstEmpty() == -1)
			return powiadom(p, "Twój ekwipunek jest pełny");
		
		double kwota = Func.Double(args[0].replace(",", ""), 0);
		if (kwota == 0)
			return powiadom(p, "Niepoprawna liczba: §e" + args[0]);
		
		return kwota;
	}
	private static int powiadom(Player p, String msg) {
		p.sendMessage(prefix + msg);
		return 0;
	}
	
	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if (ev.getAction().toString().startsWith("LEFT")) return;
		Player p = ev.getPlayer();
		ItemStack item = p.getInventory().getItemInMainHand();
		if (!item.hasItemMeta()) return;
		ItemMeta meta = item.getItemMeta();
		if (item.getType().equals(Material.PAPER) && meta.hasDisplayName() && Func.getDisplayName(meta).equals("§9§lBanknot§2") && meta.hasLore()) {
			List<String> lore = Func.getLore(meta);
			if (lore.size() == 2 && lore.get(0).equals("§bUżyj Prawym Przyciskiem Myszy")) {
				double ile = Func.Double(lore.get(1).split(" ")[1].replace(",", ""), 0);
				if (ile == 0) return;
				EconomyResponse r = Main.econ.depositPlayer(p, ile);
				if (r.transactionSuccess()) {
					p.sendMessage(prefix + "Wpłacono §e" + r.amount + "$§6 aktualny stan konta to: §e" + r.balance + "$");
					item.setAmount(item.getAmount() - 1);
					p.getInventory().setItemInMainHand(item);
				}
				else
					p.sendMessage(prefix + "§cWystąpił nieznany błąd, powiadom o tym admina");
			}
		}
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Lists.newArrayList();
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Konsole ma wszystko i nic. Kasa to akurat te \"nic\"");
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("stworzbanknot"))
			return stworz(p, args);
		
		double kwota = sprawdz(p, args);
		if (kwota == 0) return true;
		
		if (kwota < 1)
			return Func.powiadom(p, prefix + "Nie możesz wypłaćić mniej niż §e1$");
		
		kwota = Func.zaokrąglij(kwota, 2);

		int ile = 1;
		if (args.length >= 2)
			ile = Func.Int(args[1], 1);
		if (ile > 64) ile = 64;
		if (ile <= 0) ile = 1;
		
		double kasa = Main.econ.getBalance(p);
		if (kasa < kwota*ile)
			return Func.powiadom(p, prefix + "Nie posiadasz tyle pieniędzy");
		
		Main.econ.withdrawPlayer(p, kwota*ile);
		
		p.getInventory().addItem(dajBanknot(kwota, ile));
		return Func.powiadom(p, prefix + "Utworzono banknot o wartości:§e " + Func.DoubleToString(kwota) + "§6 w ilości §e" + ile);
	}
}

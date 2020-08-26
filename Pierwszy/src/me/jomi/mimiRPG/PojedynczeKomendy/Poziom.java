package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Poziom extends Komenda implements Listener{
	public static String prefix = Func.koloruj("&2[&aPoziom&2]&6 ");
	
	@EventHandler
	public void u¿yj(PlayerInteractEvent ev) {
		Player p = ev.getPlayer();
		ItemStack item = ev.getItem();
		if (item == null) return;
		if (!(item.getType().equals(Material.EXPERIENCE_BOTTLE) && item.getItemMeta().hasCustomModelData()
				&& ev.getAction().toString().split("_")[0].equalsIgnoreCase("RIGHT")))
			return;
		
		int ile = item.getItemMeta().getCustomModelData();
		p.giveExp(ile);
		p.sendMessage(prefix + "Przyznano §e" + Func.IntToString(ile) + "§6 doœwiadczenia");
		
		int x = item.getAmount();
		item.setAmount(x-1);
		ev.setCancelled(true);
	}

	public Poziom() {
		super("poziom", prefix + "/poziom [iloœæ]", "lvl");
	}
	
	private static int sprawdz_poprawnoœæ(Player p, int expAkt, String[] args) {
		int ile;
		try {
			if (args[0].equalsIgnoreCase("ca³y")) {
				if (expAkt <= 0) {
					p.sendMessage(prefix + "Nie posiadasz doœwiadczenia");
					return 0;
				}
				ile = expAkt;
			}
			else 
				ile = Integer.parseInt(args[0].trim());
			
		}
		catch(NumberFormatException nfe) {
			p.sendMessage(prefix + "Iloœæ musi byæ poprawn¹ liczb¹ ca³kowit¹ wiêksz¹ od 0");
			return 0; 
		}
		if (ile > expAkt) {
			p.sendMessage(prefix + "Nie posiadasz wystarczaj¹co du¿o expa");
			return 0;
		}
		return ile;
	}
	private static ItemStack zabutelkuj(int ile) {
		ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(ile);
		meta.setDisplayName("§6§lButelka z Expem");
		meta.setLore(Arrays.asList("§2Posiada §a" + Func.IntToString(ile) + "§2 doœwiadczenia"));
		item.setItemMeta(meta);
		return item;
	}
	public static int policzExp(int lvl) {
		// Liczy potrezbny exp na podany lvl
		if (lvl <= 15)      return (int)(lvl*lvl + 6*lvl);
		else if (lvl <= 31) return (int)(2.5*lvl*lvl - 40.5*lvl + 360);
		else                return (int)(4.5*lvl*lvl - 162.5*lvl + 2220);
	}
	public static int policzCa³yExp(Player p) {
		float xp = p.getExp();
		int  lvl = p.getLevel();
		int expAktLvl  = policzExp(lvl);
		int expNextLvl = policzExp(lvl+1);
		int expAkt = (int)((expNextLvl - expAktLvl) * xp + expAktLvl);
		
		return expAkt;
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupe³nijTabComplete(args, Arrays.asList("100", "1000", "3000", "ca³y"));
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Konsola bez expa, Konsola bez /poziom");
		Player p = (Player) sender;
		int lvl = p.getLevel();
		int expAkt = policzCa³yExp(p);
		
		// Brak argumentów
		if (args.length == 0) {
			p.sendMessage(prefix + "Posiadasz §e" + Func.IntToString(expAkt) + "§6 doœwiadczenia (" + lvl + "lvl)");
			return true;
		}
		
		else {
			// Pe³ne eq
			if (p.getInventory().firstEmpty() == -1) {
				p.sendMessage(prefix + "Twój ekwipunek jest pe³ny");
				return true;
			}
			
			// niepoprawna liczba
			int ile = sprawdz_poprawnoœæ(p, expAkt, args);
			if (ile == 0)
				return true;
			
			// Nadawanie itemka
			if (0 < ile) {
				ItemStack item = zabutelkuj(ile);
				p.giveExp(-ile);
				p.getInventory().addItem(item);
				p.sendMessage(prefix + "Zabutelkowano §e" + Func.IntToString(ile) + "§6 doœwiadczenia");
				return true;
			}
			else {
				p.sendMessage(prefix + "Iloœæ musi byæ poprawn¹ liczb¹ ca³kowit¹ wiêksz¹ od 0");
				return true;
			}
		}
	}
}

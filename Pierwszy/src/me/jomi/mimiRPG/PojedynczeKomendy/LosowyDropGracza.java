package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Gracze.Gracz;
import me.jomi.mimiRPG.Gracze.Gracze;

public class LosowyDropGracza extends Komenda implements Listener {
	public static String prefix = Func.prefix("Drop po úmierci");
	public static ItemStack nic = new ItemStack(Material.AIR);

	public LosowyDropGracza() {
		super("ustawdrop");
		ustawKomende("ustawdropwzwyz", null, null);
	}
	
	private static boolean komenda(CommandSender p, String args[], boolean koniecznieWiÍksza) {
		if (args.length == 0)
			return Main.powiadom(p, prefix + "Twoja szansa na zatrzymanie przedmiotu: ße" + Gracze.gracz(p.getName()).dropPoåmierci + "%");
		else {
			Player p2 = Bukkit.getPlayer(args[0]);
			if (p2 == null || !p2.isOnline())
				return Main.powiadom(p, prefix + "Niepoprawna nazwa gracza: ße" + args[0]);
			if (args.length == 1)
				return Main.powiadom(p, prefix + "Szansa na zatrzymanie przedmiotu gracza ße" + args[0] + " ß6: ße" + Gracze.gracz(p.getName()).dropPoåmierci + "%");
			else {
				int liczba = Func.Int(args[1], -1);
				if (liczba < 0)
					return Main.powiadom(p, prefix + "Niepoprawna liczba: ße" + args[1]);
				else {
					liczba = Math.min(liczba, 100);
					if (koniecznieWiÍksza)
						ustawWiÍkszy(p, args[0], liczba);
					else
						ustaw(p, args[0], liczba);
					return true;
				}
			}
		}
	}

	public static void zwiÍksz(CommandSender p, String nick, int ile) {
		int s = Math.min(100, Gracze.gracz(nick).dropPoåmierci + ile);
		ustaw(p, nick, s);
	}
	public static void ustaw(CommandSender p, String nick, int szansa) {
		Gracz gracz = Gracze.gracz(nick);
		gracz.dropPoåmierci = szansa;
		gracz.config.ustaw_zapisz("dropPoåmierci", szansa);
		Player p2 = Bukkit.getPlayer(nick);
		p2.sendMessage(prefix + "Za poúrednictwem ße" + p.getName() + " ß6 twoja szansa na nie wypadanie itemÛw to teraz: ße" + szansa + "%");
		p.sendMessage(prefix + "Ustawiono graczowi ße" +  p2.getName() + "ß6 szansa na nie wypadania itemÛw na: ße" + szansa + "%");	
	}
	public static void ustawWiÍkszy(CommandSender p, String nick, int szansa) {
		Gracz gracz = Gracze.gracz(nick);
		if (gracz.dropPoåmierci < szansa)
			ustaw(p, nick, szansa);
		else
			p.sendMessage(prefix + Func.msg("Drop gracza %s %s% >= %s%", nick, gracz.dropPoåmierci, szansa));
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void úmierÊGracza(PlayerDeathEvent ev) {
		if (ev.getKeepInventory())
			return;
		
		Player p = ev.getEntity();
		PlayerInventory inv = p.getInventory();
		int szansa = Gracze.gracz(p.getName()).dropPoåmierci;
		if (szansa == 0) return;
		
		ev.setKeepInventory(true);
		ev.getDrops().clear();
		
		for (int i=0; i<4*9+5; i++) {
			ItemStack item = inv.getItem(i);
			if (item != null && !item.getType().isAir())
				if (Func.losuj(1, 100) > szansa) {
					ev.getDrops().add(item);
					inv.setItem(i, nic);
				}
		}
		
		
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return komenda(sender, args, cmd.getName().equalsIgnoreCase("ustawdropwzwyz"));
	}
	
	
}

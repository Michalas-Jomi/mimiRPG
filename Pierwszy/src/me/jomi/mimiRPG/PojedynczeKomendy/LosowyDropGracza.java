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
	public static String prefix = Func.prefix("Drop po �mierci");
	public static ItemStack nic = new ItemStack(Material.AIR);

	public LosowyDropGracza() {
		super("ustawdrop");
		ustawKomende("ustawdropwzwyz", null, null);
	}
	
	private static boolean komenda(CommandSender p, String args[], boolean koniecznieWi�ksza) {
		if (args.length == 0)
			return Main.powiadom(p, prefix + "Twoja szansa na zatrzymanie przedmiotu: �e" + Gracze.gracz(p.getName()).dropPo�mierci + "%");
		else {
			Player p2 = Bukkit.getPlayer(args[0]);
			if (p2 == null || !p2.isOnline())
				return Main.powiadom(p, prefix + "Niepoprawna nazwa gracza: �e" + args[0]);
			if (args.length == 1)
				return Main.powiadom(p, prefix + "Szansa na zatrzymanie przedmiotu gracza �e" + args[0] + " �6: �e" + Gracze.gracz(p.getName()).dropPo�mierci + "%");
			else {
				int liczba = Func.Int(args[1], -1);
				if (liczba < 0)
					return Main.powiadom(p, prefix + "Niepoprawna liczba: �e" + args[1]);
				else {
					liczba = Math.min(liczba, 100);
					if (koniecznieWi�ksza)
						ustawWi�kszy(p, args[0], liczba);
					else
						ustaw(p, args[0], liczba);
					return true;
				}
			}
		}
	}

	public static void zwi�ksz(CommandSender p, String nick, int ile) {
		int s = Math.min(100, Gracze.gracz(nick).dropPo�mierci + ile);
		ustaw(p, nick, s);
	}
	public static void ustaw(CommandSender p, String nick, int szansa) {
		Gracz gracz = Gracze.gracz(nick);
		gracz.dropPo�mierci = szansa;
		gracz.config.ustaw_zapisz("dropPo�mierci", szansa);
		Player p2 = Bukkit.getPlayer(nick);
		p2.sendMessage(prefix + "Za po�rednictwem �e" + p.getName() + " �6 twoja szansa na nie wypadanie item�w to teraz: �e" + szansa + "%");
		p.sendMessage(prefix + "Ustawiono graczowi �e" +  p2.getName() + "�6 szansa na nie wypadania item�w na: �e" + szansa + "%");	
	}
	public static void ustawWi�kszy(CommandSender p, String nick, int szansa) {
		Gracz gracz = Gracze.gracz(nick);
		if (gracz.dropPo�mierci < szansa)
			ustaw(p, nick, szansa);
		else
			p.sendMessage(prefix + Func.msg("Drop gracza %s %s% >= %s%", nick, gracz.dropPo�mierci, szansa));
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void �mier�Gracza(PlayerDeathEvent ev) {
		if (ev.getKeepInventory())
			return;
		
		Player p = ev.getEntity();
		PlayerInventory inv = p.getInventory();
		int szansa = Gracze.gracz(p.getName()).dropPo�mierci;
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

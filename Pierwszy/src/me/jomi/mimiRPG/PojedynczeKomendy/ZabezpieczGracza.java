package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.NowyEkwipunek;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class ZabezpieczGracza extends Komenda implements Listener{
	public static String prefix = Func.prefix("Bezpieczny gm");
	
	public static List<Player> gracze = Lists.newArrayList();

	public ZabezpieczGracza() {
		super("zabezpieczgracza", prefix + "/zabezpieczGracza <nick>");
	}
	
	public static void komenda(Player komendziarz, String nick) {
		Player p = Bukkit.getPlayer(nick);
		if (gracze.contains(p)) {
			odbezpiecz(p);
			komendziarz.sendMessage(prefix + "Gracz §e" + nick + "§6 został odbezpieczony");
			p.sendMessage(prefix + "Zostałeś odbezpieczony przez prawomocnika: §e" + komendziarz.getName());
			return;
		}
		if (p.isOp()) {
			komendziarz.sendMessage(prefix + "Gracz §e" + nick + "§6 jest operatorem serwera");
			return;
		}
		NowyEkwipunek.wczytajStary(p);
		NowyEkwipunek.dajNowy(p);
		gracze.add(p);
		p.teleport(komendziarz);
		p.setGameMode(GameMode.CREATIVE);
		p.sendMessage(prefix + "Zostałeś zabezpieczony przez prawomocnika: §e" + komendziarz.getName());
		komendziarz.sendMessage(prefix + "Gracz §e" + nick + "§6 został zabezpieczony");
	}
	public static void odbezpiecz(Player p) {
		p.getInventory().clear();
		NowyEkwipunek.wczytajStary(p);
		p.setGameMode(GameMode.SURVIVAL);
		gracze.remove(p);
	}
	@EventHandler
	public void netherPortal(PlayerTeleportEvent ev) {
		if (gracze.contains(ev.getPlayer()) && (ev.getCause().equals(TeleportCause.END_PORTAL) || ev.getCause().equals(TeleportCause.NETHER_PORTAL))) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(prefix + "Portal jest zakazany");
		}
	}
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		if (gracze.contains(ev.getPlayer()) && !(ev.getMessage().split(" ")[0].equalsIgnoreCase("/msg") || ev.getMessage().split(" ")[0].equalsIgnoreCase("/r"))) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(prefix + "Nie wolno używać komend podczas trwania tego trybu");
		}
	}
	@EventHandler
	public void wyrzucanieItemów(PlayerDropItemEvent ev) {
		if (gracze.contains(ev.getPlayer())) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(prefix + "Nie wyrzucaj! Może ci sie jecze przyda");
		}
	}
	@EventHandler
	public void inventory(InventoryOpenEvent ev) {
		if (gracze.contains(ev.getPlayer()) && !ev.getInventory().getType().equals(InventoryType.PLAYER)) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(prefix + "O nie, zakazano nawet tego?");
		}
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Player p = ev.getPlayer();
		if (gracze.contains(p))
			odbezpiecz(p);
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, "Tylko gracz może zabezpieczyć gracza");
		Player p = (Player) sender;
		if (args.length < 1) {
			if (ZabezpieczGracza.gracze.size() == 0) 
				{p.sendMessage(ZabezpieczGracza.prefix + "Żaden gracz nie jest zabezpieczony"); return true;}
			p.sendMessage(ZabezpieczGracza.prefix + "Zabezpieczeni gracze:");
			for (Player gracz : ZabezpieczGracza.gracze)
				p.sendMessage("§6- §e" + gracz.getName());
		}
		else
			ZabezpieczGracza.komenda(p, args[0]);
		return true;
	}
}

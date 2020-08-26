package me.jomi.mimiRPG.JednorekiBandyta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Prze³adowalny;

public class JednorekiBandyta extends Komenda implements Listener, Prze³adowalny {
	public static final String prefix = Func.prefix("Jednorêki Bandyta");
	public static final Config config = new Config("configi/jednoreki bandyta", "JednorekiBandyta/jednoreki bandyta");
	
	protected static JednorekiBandyta inst;
	public JednorekiBandyta() {
		super("automat");
		Main.dodajPermisje("automat.graj");
		inst = this;
	}
	
	private static final List<Automat> automaty = Lists.newArrayList();
	public void prze³aduj() {
		Automat.prze³aduj();
		config.prze³aduj();
		automaty.clear();
		
		Automat ost = null;
		for(String klucz : config.klucze(true))
			if (!klucz.contains(".")) {
				if (ost != null) ost.wczytany();
				ost = new Automat(klucz);
				automaty.add(ost);
			} else
				ost.wczytaj(config, klucz);
		if (ost != null)
			ost.wczytany();
	}
	public String raport() {
		return "§6Automaty Jednorêkiego Bandyty: §e" + automaty.size();
	}
	
	private static final HashMap<String, AutomatTworzony> mapaTworzycieli = new HashMap<>();
	
	public static void anuluj(Player p) {
		mapaTworzycieli.remove(p.getName());
		p.sendMessage(prefix + "Nie tworzysz ju¿ ¿adnego automatyu");
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=false)
	public void uderzenie(BlockBreakEvent ev) {
		Player p = ev.getPlayer();
		if (!p.hasPermission("mimiRPG.automat") || p.getInventory().getItemInMainHand() == null ||
			!p.getInventory().getItemInMainHand().getType().equals(Material.STICK) ||
			!mapaTworzycieli.containsKey(p.getName())) return;
		mapaTworzycieli.get(p.getName()).zaznacz(ev.getBlock().getLocation());
		ev.setCancelled(true);
	}
	
	@EventHandler
	public void interakcja(PlayerInteractEvent ev) {
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		if (ev.getAction().equals(Action.LEFT_CLICK_BLOCK))
			for (Automat automat : automaty)
				if (automat.blokAktywacyjny.equals(blok.getLocation())) {
					automat.graj(ev.getPlayer());
					ev.setCancelled(true);
					return;
				}
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupe³nijTabComplete(args, Arrays.asList("stwórz", "anuluj"));
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Hazard to z³ooo, nawet nie próbuj ziom");
		Player p = (Player) sender;
		if (args.length < 2) return Main.powiadom(p, prefix + "/automat stworz <nazwa>");
		switch (args[0].toLowerCase()) {
		case "s":
		case "stworz":
		case "stwórz":
			for (Automat automat : automaty)
				if (automat.nazwa.equals(args[1])) {
					p.sendMessage(prefix + "Ta nazwa autoatu jest ju¿ zajêta");
					return true;
				}
			mapaTworzycieli.put(p.getName(), new AutomatTworzony(p, args[1]));
			break;
		case "anuluj":
			anuluj(p);
			break;
		default:
			mapaTworzycieli.get(p.getName()).komenda(args);
			break;
		}
		return true;
	}
	
}

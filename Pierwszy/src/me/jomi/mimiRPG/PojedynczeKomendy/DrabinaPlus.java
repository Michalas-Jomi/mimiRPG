package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;

public class DrabinaPlus extends Komenda implements Listener {
	public static final String prefix = Func.prefix("Drabina+");
	final ItemStack drabina = Func.połysk(Func.stwórzItem(Material.LADDER, "§6Drabina§l+", "§bPostaw jedną a sięgniesz nieba"));
	public DrabinaPlus() {
		super("drabina+", prefix + "/drabina+ (gracz)");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = null;
		if (args.length >= 1)
			p = Bukkit.getPlayer(args[0]);
		else if (sender instanceof Player)
			p = (Player) sender;
		if (p == null) return false;
		p.getInventory().addItem(drabina);
		return true;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void stawianie(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		if (Func.porównaj(ev.getItemInHand(), drabina)) {
			Consumer<Integer> buduj = przyrost -> {
				Location loc = ev.getBlock().getLocation();
				while (loc.getBlockY() <= 255 && loc.getBlockY() >= 0) {
					loc.add(0, przyrost, 0);
					if (!loc.getBlock().getType().isAir()) return;
					loc.getBlock().setType(Material.LADDER);
					loc.getBlock().setBlockData(ev.getBlock().getBlockData());
				}
			};
			buduj.accept(1);
			buduj.accept(-1);
		}
	}
}

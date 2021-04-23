package me.jomi.mimiRPG.Frakcje;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Sets;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

@Moduł
public class RegionyFrakcyjne extends Komenda {
	public static final String prefix = Func.prefix("Regiony Frakcyjne");
	public static boolean warunekModułu() {
		return Baza.rg != null;
	}

	public RegionyFrakcyjne() {
		super("regionyfrakcyjne", "/regionyfrakcyjne <świat> <region> <prefix> <parent>");
		ustawKomende("usuńRegionyFrakcyjne", "/usuńRegionyFrakcyjne <świat> <wyrażnie regularne>", null);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().contains("regionyfrakcyjne")) {
			RegionManager regiony = Func.regiony(Bukkit.getWorld(args[0]));
			ProtectedRegion region = regiony.getRegion(args[1]);
			ProtectedCuboidRegion nowy = new ProtectedCuboidRegion(
					args[2] + args[1],
					region.getMinimumPoint(),
					region.getMaximumPoint()
					);
			try {
				nowy.setParent(regiony.getRegion(args[3]));
			} catch (CircularInheritanceException e) {
				e.printStackTrace();
			}
	
			nowy.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);
			nowy.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
			nowy.setFlag(Flags.USE, StateFlag.State.ALLOW);
	
			regiony.removeRegion(region.getId());
			regiony.addRegion(nowy);
			sender.sendMessage(prefix + "Podmieniono region");
		} else {
			Pattern patern = Pattern.compile(Func.listToString(args, 1));
			RegionManager regiony = Func.regiony(Bukkit.getWorld(args[0]));
			Set<String> doUsunięcia = Sets.newConcurrentHashSet();
			for (ProtectedRegion region : regiony.getRegions().values())
				if (patern.matcher(region.getId()).find())
					doUsunięcia.add(region.getId());
			for (String region : doUsunięcia)
				regiony.removeRegion(region);
			sender.sendMessage(prefix + "Usunięto " + doUsunięcia.size() + " regionów");
		}
		return true;
	}
}

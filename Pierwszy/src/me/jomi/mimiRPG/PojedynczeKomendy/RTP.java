package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;

public class RTP extends Komenda implements Przeładowalny {
	public static final String prefix = Func.prefix("RTP");
	
	private static final HashMap<String, List<Material>> mapa = new HashMap<>();
	
	public RTP() {
		super("rtp", prefix + "/rtp <zasięg> (selektor) (filtr)");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void przeładuj() {
		mapa.clear();
		ConfigurationSection sekcja = Main.ust.sekcja("rtp");
		if (sekcja != null)
			for (String klucz : sekcja.getKeys(false)) {
				List<Material> lista = Lists.newArrayList();;
				for (String mat : (List<String>) sekcja.getList(klucz))
					lista.add(Material.valueOf(mat.toUpperCase()));
				mapa.put(klucz, lista);
			}
	}
	public String raport() {
		return "§6Filtry rtp: §e" + mapa.size();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> lista = Lists.newArrayList();
		switch(args.length) {
		case 1:
			break;
		case 2:
			return null;
		case 3:
			lista = Lists.newArrayList(mapa.keySet());
		}
		return uzupełnijTabComplete(args[args.length-1], lista);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		int zasięg = 1;
		Entity teleportowany = null;
		if (sender instanceof Entity)
			teleportowany = (Entity) sender;
		List<Material> filtr = null;
		switch(args.length) {
		case 3:
			filtr = mapa.get(args[2]);
			if (filtr == null) {
				filtr = Lists.newArrayList();
				for (String mat : args[2].split(","))
					filtr.add(Material.valueOf(mat));
			}
		case 2:
			List<Entity> en = Bukkit.selectEntities(sender, args[1]);
			if (en.isEmpty())
				return Func.powiadom(sender, prefix + "Żaden byt nie odpowiada podanym kryteriom " + args[1]);
			teleportowany = en.get(0);
		case 1:
			zasięg = Func.Int(args[0], -1);
			if (zasięg <= -1)
				return Func.powiadom(sender, prefix + "Niepoprawny zasięg");
		}
		if (teleportowany == null)
			return Func.powiadom(sender, prefix + "/rtp <zasięg> <selektor> (filtr)");
		teleportowany.teleport(losuj(teleportowany.getLocation(), zasięg, filtr));
		teleportowany.sendMessage(prefix + "Zostałeś przeteleportowany");
		return true;
	}
	
	public static Location losuj(Location środek, int zasięg, List<Material> zablokowane) {
		if (zablokowane == null) zablokowane = Lists.newArrayList();
		Location _środek = środek;
		int przejścia = 0;
		do {
		środek = _środek.clone();
		środek.setX(((int) środek.getX()) + Func.losuj(-zasięg, zasięg));
		środek.setZ(((int) środek.getZ()) + Func.losuj(-zasięg, zasięg));
		środek.setY(255);
		
		while (środek.getBlock().getType().isAir() && środek.getBlockY() > 0)
			środek.add(0, -1, 0);
		
		if (++przejścia >= 2000) {
			Main.plugin.getLogger().warning("Brak bloków w zasięgu rtp" + 
						_środek.getBlockX() + " " + _środek.getBlockY() + " " + _środek.getBlockZ());
			return _środek;
		}
		} while (środek.getBlockY() <= 0 || zablokowane.contains(środek.getBlock().getType()));
		
		return środek.add(0.5, 1, 0.5);
	}

}

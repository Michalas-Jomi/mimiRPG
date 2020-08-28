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
import me.jomi.mimiRPG.Prze³adowalny;

public class RTP extends Komenda implements Prze³adowalny {
	public static final String prefix = Func.prefix("RTP");
	
	private static final HashMap<String, List<Material>> mapa = new HashMap<>();
	
	public RTP() {
		super("rtp", prefix + "/rtp <zasiêg> (selektor) (filtr)");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void prze³aduj() {
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
		return uzupe³nijTabComplete(args[args.length-1], lista);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		int zasiêg = 1;
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
				return Main.powiadom(sender, prefix + "¯aden byt nie odpowiada podanym kryteriom " + args[1]);
			teleportowany = en.get(0);
		case 1:
			zasiêg = Func.Int(args[0], -1);
			if (zasiêg <= -1)
				return Main.powiadom(sender, prefix + "Niepoprawny zasiêg");
		}
		if (teleportowany == null)
			return Main.powiadom(sender, prefix + "/rtp <zasiêg> <selektor> (filtr)");
		teleportowany.teleport(losuj(teleportowany.getLocation(), zasiêg, filtr));
		teleportowany.sendMessage(prefix + "Zosta³eœ przeteleportowany");
		return true;
	}
	
	public static Location losuj(Location œrodek, int zasiêg, List<Material> zablokowane) {
		if (zablokowane == null) zablokowane = Lists.newArrayList();
		Location _œrodek = œrodek;
		int przejœcia = 0;
		do {
		œrodek = _œrodek.clone();
		œrodek.setX(((int) œrodek.getX()) + Func.losuj(-zasiêg, zasiêg));
		œrodek.setZ(((int) œrodek.getZ()) + Func.losuj(-zasiêg, zasiêg));
		œrodek.setY(255);
		
		while (œrodek.getBlock().getType().isAir() && œrodek.getBlockY() > 0)
			œrodek.add(0, -1, 0);
		
		if (++przejœcia >= 2000) {
			Main.plugin.getLogger().warning("Brak bloków w zasiêgu rtp" + 
						_œrodek.getBlockX() + " " + _œrodek.getBlockY() + " " + _œrodek.getBlockZ());
			return _œrodek;
		}
		} while (œrodek.getBlockY() <= 0 || zablokowane.contains(œrodek.getBlock().getType()));
		
		return œrodek.add(0.5, 1, 0.5);
	}

}

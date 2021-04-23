package me.jomi.mimiRPG.Chat;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;

@Moduł
public class MenedżerStatystyk extends Komenda {
	public static final String prefix = Func.prefix("Menedżer Statystyk");
	public MenedżerStatystyk() {
		super("staty", "/staty <Statystyka> [(kategoria) (gracz) | (gracz)]", "statystyki", "menedżerStatystyk");
	}

	final static List<String> strMaterial	= Func.wykonajWszystkim(Material.values(),	e -> e.toString().toLowerCase());
	final static List<String> strStatistic	= Func.wykonajWszystkim(Statistic.values(),	e -> e.toString().toLowerCase());
	final static List<String> strEntityType	= Func.wykonajWszystkim(EntityType.values(),e -> e.toString().toLowerCase());
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, strStatistic);
		if (args.length == 2)
			try {
				Statistic stat = Func.StringToEnum(Statistic.class, args[0]);
				if (stat.isSubstatistic())
					return Func.multiEquals(stat, Statistic.KILL_ENTITY, Statistic.ENTITY_KILLED_BY) ? strEntityType : strMaterial;
			} catch (Throwable e) {}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		
		Statistic stat;
		Object kategoria = null;
		Player p = null;
		int i = -1;
		
		if (sender instanceof Player)
			p = (Player) sender;
		
		try {
			stat = Func.StringToEnum(Statistic.class, args[0]);
		} catch (Throwable e) {
			return Func.powiadom(sender, prefix + "Nieprawidłowa statystyka %s", args[0]);
		}
		
		if (args.length >= 2)
			if (stat.isSubstatistic())
				try {
					kategoria = Func.StringToEnum(EntityType.class, args[1]);
				} catch (Throwable e1) {
					try {
						kategoria = Func.StringToEnum(Material.class, args[1]);
					} catch (Throwable e2) {
						return Func.powiadom(sender, prefix + "Nieprawidłowa kategoria %s", args[1]);
					}
				}
			else {
				p = Bukkit.getPlayer(args[i = 1]);
			}
		else if (stat.isSubstatistic())
			return Func.powiadom(sender, prefix + "Ta statystyka wymaga podania dodatkowo kategori");
		final Object fkategoria = kategoria;
		
		if (args.length >= 3 && i == -1)
			p = Bukkit.getPlayer(args[i = 2]);
		
		Consumer<OfflinePlayer> cons = gracz -> {
			int ile;
			if (fkategoria == null)
				ile = gracz.getStatistic(stat);
			else if (fkategoria instanceof Material)
				ile = gracz.getStatistic(stat, (Material) fkategoria);
			else
				ile = gracz.getStatistic(stat, (EntityType) fkategoria);
			Func.powiadom(sender, prefix + "Statystyka %s %s gracza %s wynosi %s", stat, fkategoria == null ? "" : fkategoria, gracz.getName(), ile);
		};
		
		final int fi = i;
		if (p == null)
			if (i != -1) {
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
					Func.powiadom(sender, prefix + "Wyszukiwania gracza %s poza listą aktywnych graczy", args[fi]);
					OfflinePlayer offlinePlayer = Func.graczOffline(args[fi]);
					if (offlinePlayer == null)
						Func.powiadom(sender, prefix + "Niepoprawny nick gracza %s", args[fi]);
					else
						cons.accept(offlinePlayer);
				});
				return true; 
			}
			else
				return false;
		
		cons.accept(p);
		
		return true;
	}
}

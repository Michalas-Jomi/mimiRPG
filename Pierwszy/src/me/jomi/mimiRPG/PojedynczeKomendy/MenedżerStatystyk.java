package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class MenedżerStatystyk extends Komenda {
	public static final String prefix = Func.prefix("Menedżer Statystyk");
	public MenedżerStatystyk() {
		super("staty", "/staty <Statystyka> [(kategoria) (gracz) | (gracz)]", "statystyki", "menedżerStatystyk");
	}

	final static List<String> strStatistic	= Func.wykonajWszystkim(Statistic.values(), Statistic::toString);
	final static List<String> strMaterial	= Func.wykonajWszystkim(Material.values(), Material::toString);
	final static List<String> strEntityType	= Func.wykonajWszystkim(EntityType.values(), EntityType::toString);
	
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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
		
		if (args.length >= 3 && i == -1)
			p = Bukkit.getPlayer(args[i = 2]);
		
		
		if (p == null)
			if (i != -1)
				return Func.powiadom(sender, prefix + "Niepoprawny nick gracza %s", args[i]);
			else
				return false;
		
		int ile;
		if (kategoria == null)
			ile = p.getStatistic(stat);
		else if (kategoria instanceof Material)
			ile = p.getStatistic(stat, (Material) kategoria);
		else
			ile = p.getStatistic(stat, (EntityType) kategoria);
			
		
		return Func.powiadom(sender, prefix + "Statystyka %s %s gracza %s wynosi %s", stat, kategoria == null ? "" : kategoria, p.getDisplayName(), ile);
	}
}

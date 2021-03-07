package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Samobójstwo extends Komenda {
	public static final String prefix = Func.prefix("Samobójstwo");

	public Samobójstwo() {
		super("samobójstwo");
	}

	final Set<String> zabici = Sets.newConcurrentHashSet();
	
	
	int minuty() {
		return Main.ust.wczytajLubDomyślna("Samobójstwo.Cooldown minuty", 30);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może się samemu unicestwić");
		Player p = (Player) sender;
		
		if (zabici.contains(p.getName()))
			return Func.powiadom(p, prefix + "Możesz tego użyć tylko raz na " + Func.czas(minuty() * 60));
		
		p.setHealth(0);
		p.sendMessage(prefix + "Popełniłeś samobójstwo");
		zabici.add(p.getName());
		Func.opóznij(minuty() * 60 * 20, () -> zabici.remove(p.getPlayer().getName()));
		
		return true;
	}
}

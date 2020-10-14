package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;

public class Ujezdzaj extends Komenda {
	public static final String prefix = Func.prefix("Ujeżdzanie");

	public Ujezdzaj(){
		super("ujeżdzaj");
		Main.dodajPermisje("ujezdzaj.ominzasieg", "ujezdzaj.nakazinnym");
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	private boolean info(CommandSender p, int ile) {
		if (!(p instanceof Player)) {
			if (ile >= 2)
				return false;
			p.sendMessage(prefix + "/ujezdzaj <gracz> <gracz>");
			return true;
		}
		
		if (ile >= 1)
			return false;
		String str = "/ujezdzaj <gracz>";
		if (p.hasPermission("mimiRPG.ujezdzaj.nakazinnym"))
			str += " (gracz)";
		p.sendMessage(prefix + str);
		return true;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (info(sender, args.length)) return true;
		
		Entity p2 = byt(sender, args[0]);
		if (p2 == null) return true;
	
		if (args.length >= 2) {
			if (!sender.hasPermission("mimiRPG.ujezdzaj.nakazinnym") && 
					!sender.getName().equals(p2.getName()))
				return Func.powiadom(sender, prefix + "Nie masz wystarczających uprawnień aby tego użyć");
			Entity p3 = byt(sender, args[1]);
			if (p3 == null) return true;
			usiądz(sender, p2, p3);
			return true;
		}
		usiądz(sender, (Player) sender, p2);
		return true;
	}
	
	private Entity byt(CommandSender p, String nazwa) {
		List<Entity> en = Bukkit.selectEntities(p, nazwa);
		if (en.isEmpty()) {
			p.sendMessage("§cnieprawidłowy selektor " + nazwa);
			return null;
		}
		if (en.isEmpty())
			return null;
		return en.get(0);
	}
	

	private void usiądz(CommandSender p, Entity kto, Entity naKim) {
		if (kto.getUniqueId().equals(naKim.getUniqueId())) {
			p.sendMessage(prefix + "Nie można ujeżdzać samego siebie");
			return;
		}
		if (zawiera(naKim.getPassengers(), kto)) {
			naKim.removePassenger(kto);
			if (p.getName().equals(kto.getName()))
				p.sendMessage(prefix + Func.msg("Już nie ujeżdzasz %s", naKim.getName()));
			else if (p.getName().equals(naKim.getName()))
				p.sendMessage(prefix + Func.msg("%s juz cie nie ujeżdza", kto.getName()));
			else
				p.sendMessage(prefix + Func.msg("%s już nie ujeżdza %s", kto.getName(), naKim.getName()));
			return;
		}
		double odległość = kto.getLocation().distance(naKim.getLocation());
		if (!p.hasPermission("mimiRPG.ujezdzaj.ominzasieg")) {
			if (odległość > (double) Main.ust.wczytajLubDomyślna("ujezdzanie.maxOdległość", 5.0)) {
				p.sendMessage(prefix + "Nie można ujeżdzać graczy na tak daleki dystans");
				return;
			}
		}
		if (zawiera(kto.getPassengers(), naKim))
			kto.removePassenger(naKim);
		if (odległość > 50)
			kto.teleport(naKim);
		naKim.addPassenger(kto);
		if (p.getName().equals(kto.getName()))
			p.sendMessage(prefix + Func.msg("Ujeżdzasz teraz %s", naKim.getName()));
		else if (p.getName().equals(naKim.getName()))
			p.sendMessage(prefix + Func.msg("%s teraz cie ujeżdza", kto.getName()));
		else
			p.sendMessage(prefix + Func.msg("%s ujeżdza teraz %s", kto.getName(), naKim.getName()));
	}
	private boolean zawiera(List<Entity> lista, Entity en) {
		for (Entity e : lista)
			if (e.getUniqueId().equals(en.getUniqueId()))
				return true;
		return false;
	}
}

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
	public static final String prefix = Func.prefix("Uje¿dzanie");

	public Ujezdzaj(){
		super("uje¿dzaj");
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
				return Main.powiadom(sender, prefix + "Nie masz wystarczaj¹cych uprawnieñ aby tego u¿yæ");
			Entity p3 = byt(sender, args[1]);
			if (p3 == null) return true;
			usi¹dz(sender, p2, p3);
			return true;
		}
		usi¹dz(sender, (Player) sender, p2);
		return true;
	}
	
	private Entity byt(CommandSender p, String nazwa) {
		List<Entity> en = Bukkit.selectEntities(p, nazwa);
		if (en.isEmpty()) {
			p.sendMessage("§cnieprawid³owy selektor " + nazwa);
			return null;
		}
		if (en.isEmpty())
			return null;
		return en.get(0);
	}
	

	private void usi¹dz(CommandSender p, Entity kto, Entity naKim) {
		if (kto.getUniqueId().equals(naKim.getUniqueId())) {
			p.sendMessage(prefix + "Nie mo¿na uje¿dzaæ samego siebie");
			return;
		}
		if (zawiera(naKim.getPassengers(), kto)) {
			naKim.removePassenger(kto);
			if (p.getName().equals(kto.getName()))
				p.sendMessage(prefix + Func.msg("Ju¿ nie uje¿dzasz %s", naKim.getName()));
			else if (p.getName().equals(naKim.getName()))
				p.sendMessage(prefix + Func.msg("%s juz cie nie uje¿dza", kto.getName()));
			else
				p.sendMessage(prefix + Func.msg("%s ju¿ nie uje¿dza %s", kto.getName(), naKim.getName()));
			return;
		}
		double odleg³oœæ = kto.getLocation().distance(naKim.getLocation());
		if (!p.hasPermission("mimiRPG.ujezdzaj.ominzasieg")) {
			if (odleg³oœæ > (double) Main.ust.wczytajLubDomyœlna("ujezdzanie.maxOdleg³oœæ", 5.0)) {
				p.sendMessage(prefix + "Nie mo¿na uje¿dzaæ graczy na tak daleki dystans");
				return;
			}
		}
		if (zawiera(kto.getPassengers(), naKim))
			kto.removePassenger(naKim);
		if (odleg³oœæ > 50)
			kto.teleport(naKim);
		naKim.addPassenger(kto);
		if (p.getName().equals(kto.getName()))
			p.sendMessage(prefix + Func.msg("Uje¿dzasz teraz %s", naKim.getName()));
		else if (p.getName().equals(naKim.getName()))
			p.sendMessage(prefix + Func.msg("%s teraz cie uje¿dza", kto.getName()));
		else
			p.sendMessage(prefix + Func.msg("%s uje¿dza teraz %s", kto.getName(), naKim.getName()));
	}
	private boolean zawiera(List<Entity> lista, Entity en) {
		for (Entity e : lista)
			if (e.getUniqueId().equals(en.getUniqueId()))
				return true;
		return false;
	}
}

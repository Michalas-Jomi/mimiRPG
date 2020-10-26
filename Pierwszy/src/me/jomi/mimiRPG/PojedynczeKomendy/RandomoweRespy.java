package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class RandomoweRespy extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Randomowe Respy");
	
	public RandomoweRespy() {
		super("losresp", null, "randresp");
	}

	@Override
	public void przeładuj() {
		Baza.config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		int x = 0;
		try {
			x = dajRespy().size();
		} catch (Exception e) {}
		return Func.r("randomowe respy", x);
	}
	
	final String scieżka = "Randomowe Respy";
	@SuppressWarnings("unchecked")
	List<Location> dajRespy() {
		try {
			return Func.nieNullList((List<Location>) Baza.config.wczytaj(scieżka));
		} catch (Exception e) {
			return Lists.newArrayList();
		}
	}
	void zapiszRespy(List<Location> respy) {
		Baza.config.ustaw_zapisz(scieżka, respy);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) return utab(args, "lista", "dodaj", "los");
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			wyświetlRespy(sender);
			return true;
		}
		List<Location> respy = dajRespy();
		int i;
		switch (args[0]) {
		case "l":
		case "lista":
			break;
		case "d":
		case "dodaj":
			if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Tylko gracz może dodać randomowy resp");
			respy.add(((Player) sender).getLocation());
			zapiszRespy(respy);
			break;
		case "u":
		case "usun":
		case "usuń":
			if (args.length < 2) return Func.powiadom(sender, prefix + "Nie wiadomo co usunąć");
			i = Func.Int(args[1], -1);
			if (i <= -1 || i >= respy.size()) return Func.powiadom(sender, prefix + "Niepoprawne id respu: " + args[1]);
			respy.remove(i);
			zapiszRespy(respy);
			break;
		case "t":
		case "tp":
			if (!(sender instanceof Entity)) return Func.powiadom(sender, prefix + "Nie możesz sie przeteleportować");
			if (args.length < 2) return Func.powiadom(sender, prefix + "Nie gdzie tepnąć");
			i = Func.Int(args[1], -1);
			if (i <= -1 || i >= respy.size()) return Func.powiadom(sender, prefix + "Niepoprawne id respu: " + args[1]);
			if (args.length < 3)
				((Entity) sender).teleport(respy.get(i));
			else {
				List<Entity> moby = Bukkit.selectEntities(sender, args[2]);
				if (!moby.isEmpty())
					moby.get(0).teleport(respy.get(i));
			}
			return true;
		case "los":
			if (args.length < 1)
				return Func.powiadom(sender, prefix + "/losresp <nick>");
			List<Entity> en = Bukkit.selectEntities(sender, Func.listToString(args, 0));
			if (!en.isEmpty())
				en.get(0).teleport(Func.losuj(respy));
			return true;
		default:
			return Func.powiadom(sender, prefix + "Niepoprawne argumenty");
		}
		wyświetlRespy(sender);
		return true;
	}
	
	void wyświetlRespy(CommandSender sender) {
		sender.sendMessage("\n\n\n\n§9§lRandomowe respy:\n");
		int i=0;
		for (Location loc : dajRespy()) {
			Napis n = new Napis();
			n.dodaj(new Napis("§9[tp]", "§bKliknij aby się §9przeteleportować", "/losresp tp " + i));
			n.dodaj(" ");
			n.dodaj(new Napis("§c[x]", "§bKliknij aby §cusunąć resp", "/losresp usuń " + i));
			n.dodaj(" ");
			n.dodaj(String.format("%s) %sx %sy %sz", i, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
			n.wyświetl(sender);
			i++;
		}
		new Napis("§a[dodaj]", "§bKliknij aby §adodać resp", "/losresp dodaj").wyświetl(sender);;
		sender.sendMessage("");
	}
	
	@EventHandler
	public void śmierć(PlayerRespawnEvent ev) {
		Location łóżko = ev.getPlayer().getBedLocation();
		if (łóżko != null)
			ev.setRespawnLocation(łóżko);
		else {
			List<Location> respy = dajRespy();
			if (respy.size() > 0)
				ev.setRespawnLocation(Func.losuj(respy));
		}
	}
}

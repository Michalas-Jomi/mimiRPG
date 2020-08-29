package me.jomi.mimiRPG.Chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Zegar;

public class Glosowanie extends Komenda implements Zegar {
	public static final String prefix = Func.prefix("Głosowanie");

	public static final HashMap<String, _Glosowanie> mapa = new HashMap<>();
	
	public Glosowanie() {
		super("vote");
		Main.dodajPermisje("vote.stworz");
	}
	
	@Override
	public int czas() {
		for (_Glosowanie glosowanie : mapa.values())
			glosowanie.czas();
		return 20;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return uzupełnijTabComplete(Func.listToString(args, 0), Lists.newArrayList(mapa.keySet()));
	}
	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			p.sendMessage("/vote <nazwa>");
			return true;
		}
		switch (args[0]) {
		case "i":
		case "info":
			for (_Glosowanie glosowanie : mapa.values())
				p.sendMessage("" + glosowanie);
			break;
		case "s":
		case "stwórz":
			if (!p.hasPermission("mimiRPG.vote.stworz")) {
				p.sendMessage(prefix + "nie masz uprawnień do stworzenia głosowania");
				break;
			}
			if (args.length < 4) {
				p.sendMessage(prefix + "/vote stwórz <potrzebne głosy> <czas w minutach> <nazwa głosowania>");
				break;
			}
			String nazwa = Func.listToString(args, 3);
			if (Arrays.asList("stwórz", "s").contains(args[3])) {
				p.sendMessage(prefix + nazwa + " nie jest prawidłową nazwą głosowania");
				break;
			}
			int potrzebneGłosy = Func.Int(args[1], -1);
			int czas = Func.Int(args[2], -1);
			if (potrzebneGłosy <= 0 || czas <= 0) {
				p.sendMessage(prefix + "/vote stwórz <potrzebne głosy> <czas w minutach> <nazwa głosowania>");
				break;
			}
			if (mapa.containsKey(nazwa)) {
				p.sendMessage(prefix + "Głosowanie o tej nazwie już istnieje");
				break;
			}
			new _Glosowanie(p, potrzebneGłosy, czas, nazwa);
			break;
		default:
			String nazwaG = Func.listToString(args, 0);
			_Glosowanie glosowanie = mapa.get(nazwaG);
			if (glosowanie != null)
				glosowanie.zagłosuj(p);
			else
				p.sendMessage(prefix + "Niepoprawna nazwa głosowania §e" + nazwaG);
		}
		return true;
	}
}

class _Glosowanie {
	public static final String prefix = Glosowanie.prefix;
	
	private int potrzebneGłosy;
	private int czas;
	private String nazwa;
	private final List<String> głosujący = Lists.newArrayList();
	private boolean zakończone = false;
	
	_Glosowanie(CommandSender p, int potrzebneGłosy, int czas, String nazwa) {

		this.czas = czas * 60;
		this.potrzebneGłosy = potrzebneGłosy;
		this.nazwa = nazwa;
		Glosowanie.mapa.put(nazwa, this);
		Bukkit.broadcastMessage(prefix + "§e" + p.getName() + "§6 Utworzył nowe głosowanie §e" + nazwa +
				"§6, które wygaśnie za "+ _czas() +" użyj §e§o/vote " + nazwa + "§6 aby zagłosować");
	}
	
	private String _czas() {
		int sekundy = czas % 60;
		int minuty = czas / 60;
		int godziny = minuty / 60; minuty %= 60;
		int dni = godziny / 24; godziny %= 24;
		
		String w = "";
		if (dni != 0)	 	w += dni 	+ " dni ";
		if (godziny != 0) 	w += godziny+ " godzin ";
		if (minuty != 0) 	w += minuty + " minut ";
		if (sekundy != 0) 	w += sekundy+ " sekund ";
		return w.equals("") ? "teraz" : w;
	}
	public void czas() {
		czas -= 1;
		if (zakończone) 
			return;
		if (czas <= 0) {
			Bukkit.broadcastMessage(prefix + "Głosowanie §e" + nazwa + "§6 dobiegło końca, §cnie uzyskano potrzebnej liczby głosów§6, " + głosujący.size() + "/" + potrzebneGłosy);
			Glosowanie.mapa.remove(nazwa);
			return;
		}
	}
	public void zagłosuj(CommandSender p) {
		if (głosujący.contains(p.getName())) {
			p.sendMessage(prefix + "Już zagłosowałeś na to");
		} else {
			głosujący.add(p.getName());
			Bukkit.broadcastMessage(prefix + "§e" + p.getName() + "§6 zagłosował na §e" + this);
			if (głosujący.size() >= potrzebneGłosy) {
				Bukkit.broadcastMessage(prefix + "Głosowanie §e" + nazwa + " §6 zostało zakończone §aPomyślnie");
				Glosowanie.mapa.remove(nazwa);
				zakończone = true;
			}
		}
	}
	public String toString() {
		return nazwa + "§6 zostało jeszcze §e" + _czas() + " §6" + głosujący.size() + "/" + potrzebneGłosy;
	}
}

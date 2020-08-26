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
import me.jomi.mimiRPG.Edytory.EdytujItem;

public class Glosowanie extends Komenda implements Zegar {
	public static final String prefix = Func.prefix("G³osowanie");

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
		return uzupe³nijTabComplete(Func.listToString(args, 0), Lists.newArrayList(mapa.keySet()));
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
				p.sendMessage(prefix + "nie masz uprawnieñ do stworzenia g³osowania");
				break;
			}
			if (args.length < 4) {
				p.sendMessage(prefix + "/vote stwórz <potrzebne g³osy> <czas w minutach> <nazwa g³osowania>");
				break;
			}
			String nazwa = Func.listToString(args, 3);
			if (Arrays.asList("stwórz", "s").contains(args[3])) {
				p.sendMessage(prefix + nazwa + " nie jest prawid³ow¹ nazw¹ g³osowania");
				break;
			}
			int potrzebneG³osy = EdytujItem.sprawdz_liczbe(args[1], -1);
			int czas = EdytujItem.sprawdz_liczbe(args[2], -1);
			if (potrzebneG³osy <= 0 || czas <= 0) {
				p.sendMessage(prefix + "/vote stwórz <potrzebne g³osy> <czas w minutach> <nazwa g³osowania>");
				break;
			}
			if (mapa.containsKey(nazwa)) {
				p.sendMessage(prefix + "G³osowanie o tej nazwie ju¿ istnieje");
				break;
			}
			new _Glosowanie(p, potrzebneG³osy, czas, nazwa);
			break;
		default:
			String nazwaG = Func.listToString(args, 0);
			_Glosowanie glosowanie = mapa.get(nazwaG);
			if (glosowanie != null)
				glosowanie.zag³osuj(p);
			else
				p.sendMessage(prefix + "Niepoprawna nazwa g³osowania §e" + nazwaG);
		}
		return true;
	}
}

class _Glosowanie {
	public static final String prefix = Glosowanie.prefix;
	
	private int potrzebneG³osy;
	private int czas;
	private String nazwa;
	private final List<String> g³osuj¹cy = Lists.newArrayList();
	private boolean zakoñczone = false;
	
	_Glosowanie(CommandSender p, int potrzebneG³osy, int czas, String nazwa) {

		this.czas = czas * 60;
		this.potrzebneG³osy = potrzebneG³osy;
		this.nazwa = nazwa;
		Glosowanie.mapa.put(nazwa, this);
		Bukkit.broadcastMessage(prefix + "§e" + p.getName() + "§6 Utworzy³ nowe g³osowanie §e" + nazwa +
				"§6, które wygaœnie za "+ _czas() +" u¿yj §e§o/vote " + nazwa + "§6 aby zag³osowaæ");
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
		if (zakoñczone) 
			return;
		if (czas <= 0) {
			Bukkit.broadcastMessage(prefix + "G³osowanie §e" + nazwa + "§6 dobieg³o koñca, §cnie uzyskano potrzebnej liczby g³osów§6, " + g³osuj¹cy.size() + "/" + potrzebneG³osy);
			Glosowanie.mapa.remove(nazwa);
			return;
		}
	}
	public void zag³osuj(CommandSender p) {
		if (g³osuj¹cy.contains(p.getName())) {
			p.sendMessage(prefix + "Ju¿ zag³osowa³eœ na to");
		} else {
			g³osuj¹cy.add(p.getName());
			Bukkit.broadcastMessage(prefix + "§e" + p.getName() + "§6 zag³osowa³ na §e" + this);
			if (g³osuj¹cy.size() >= potrzebneG³osy) {
				Bukkit.broadcastMessage(prefix + "G³osowanie §e" + nazwa + " §6 zosta³o zakoñczone §aPomyœlnie");
				Glosowanie.mapa.remove(nazwa);
				zakoñczone = true;
			}
		}
	}
	public String toString() {
		return nazwa + "§6 zosta³o jeszcze §e" + _czas() + " §6" + g³osuj¹cy.size() + "/" + potrzebneG³osy;
	}
}

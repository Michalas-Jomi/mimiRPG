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
	public static final String prefix = Func.prefix("G�osowanie");

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
		return uzupe�nijTabComplete(Func.listToString(args, 0), Lists.newArrayList(mapa.keySet()));
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
		case "stw�rz":
			if (!p.hasPermission("mimiRPG.vote.stworz")) {
				p.sendMessage(prefix + "nie masz uprawnie� do stworzenia g�osowania");
				break;
			}
			if (args.length < 4) {
				p.sendMessage(prefix + "/vote stw�rz <potrzebne g�osy> <czas w minutach> <nazwa g�osowania>");
				break;
			}
			String nazwa = Func.listToString(args, 3);
			if (Arrays.asList("stw�rz", "s").contains(args[3])) {
				p.sendMessage(prefix + nazwa + " nie jest prawid�ow� nazw� g�osowania");
				break;
			}
			int potrzebneG�osy = EdytujItem.sprawdz_liczbe(args[1], -1);
			int czas = EdytujItem.sprawdz_liczbe(args[2], -1);
			if (potrzebneG�osy <= 0 || czas <= 0) {
				p.sendMessage(prefix + "/vote stw�rz <potrzebne g�osy> <czas w minutach> <nazwa g�osowania>");
				break;
			}
			if (mapa.containsKey(nazwa)) {
				p.sendMessage(prefix + "G�osowanie o tej nazwie ju� istnieje");
				break;
			}
			new _Glosowanie(p, potrzebneG�osy, czas, nazwa);
			break;
		default:
			String nazwaG = Func.listToString(args, 0);
			_Glosowanie glosowanie = mapa.get(nazwaG);
			if (glosowanie != null)
				glosowanie.zag�osuj(p);
			else
				p.sendMessage(prefix + "Niepoprawna nazwa g�osowania �e" + nazwaG);
		}
		return true;
	}
}

class _Glosowanie {
	public static final String prefix = Glosowanie.prefix;
	
	private int potrzebneG�osy;
	private int czas;
	private String nazwa;
	private final List<String> g�osuj�cy = Lists.newArrayList();
	private boolean zako�czone = false;
	
	_Glosowanie(CommandSender p, int potrzebneG�osy, int czas, String nazwa) {

		this.czas = czas * 60;
		this.potrzebneG�osy = potrzebneG�osy;
		this.nazwa = nazwa;
		Glosowanie.mapa.put(nazwa, this);
		Bukkit.broadcastMessage(prefix + "�e" + p.getName() + "�6 Utworzy� nowe g�osowanie �e" + nazwa +
				"�6, kt�re wyga�nie za "+ _czas() +" u�yj �e�o/vote " + nazwa + "�6 aby zag�osowa�");
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
		if (zako�czone) 
			return;
		if (czas <= 0) {
			Bukkit.broadcastMessage(prefix + "G�osowanie �e" + nazwa + "�6 dobieg�o ko�ca, �cnie uzyskano potrzebnej liczby g�os�w�6, " + g�osuj�cy.size() + "/" + potrzebneG�osy);
			Glosowanie.mapa.remove(nazwa);
			return;
		}
	}
	public void zag�osuj(CommandSender p) {
		if (g�osuj�cy.contains(p.getName())) {
			p.sendMessage(prefix + "Ju� zag�osowa�e� na to");
		} else {
			g�osuj�cy.add(p.getName());
			Bukkit.broadcastMessage(prefix + "�e" + p.getName() + "�6 zag�osowa� na �e" + this);
			if (g�osuj�cy.size() >= potrzebneG�osy) {
				Bukkit.broadcastMessage(prefix + "G�osowanie �e" + nazwa + " �6 zosta�o zako�czone �aPomy�lnie");
				Glosowanie.mapa.remove(nazwa);
				zako�czone = true;
			}
		}
	}
	public String toString() {
		return nazwa + "�6 zosta�o jeszcze �e" + _czas() + " �6" + g�osuj�cy.size() + "/" + potrzebneG�osy;
	}
}

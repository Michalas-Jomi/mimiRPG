package me.jomi.mimiRPG.Chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Przyjaciele extends Komenda implements Przeładowalny {
	public static String prefix = Func.prefix("§d§lPrzyjaciele");
	
	private HashMap<String, String> zaproszenia = new HashMap<>();
	
	public Przyjaciele() {
		super("przyjaciele", null, "p", "kumple", "znajomi");
	}
	
	private Napis ogólne;
	private Napis online;
	private Napis offline;
	private Napis zawsze;
	private int ilość;
	@Override
	public void przeładuj() {
		ilość = 0;
		ogólne  = wczytaj("ogólne");
		online  = wczytaj("online");
		offline = wczytaj("offline");
		zawsze  = wczytaj("zawsze");
	}
	private Napis wczytaj(String sciezka) {
		Napis napis = new Napis();
		for (Napis n : Main.ust.wczytajListeNapisów("Przyjaciele." + sciezka)) {
			napis.dodaj(n);
			ilość++;
		}
		return napis;
	}
	
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Komendy Przyjaciół", ilość);
	}
	
	private void zaproś(Player p1, Player p2) {
		if (p2 == null) {
			p1.sendMessage(prefix + "Nie odnaleiono gracza");
			return;
		}
		String nick1 = p1.getName();
		String nick2 = p2.getName();
		
		if (nick1.equals(nick2)) {
			p1.sendMessage(prefix + "xD");
			return;
		}
		
		if (nick2.equals(zaproszenia.get(nick1))) {
			p1.sendMessage(prefix + "Wysłałeś już zaproszenie do tego gracza");
			return;
		}
		
		if (Gracz.wczytaj(nick1).przyjaciele.contains(nick2)) {
			p1.sendMessage(prefix + "ty i §e" + nick2 + "§6 jesteście już przyjaciółmi");
			return;
		}
		
		if (zaproszenia.get(nick2) != null && zaproszenia.get(nick2).equals(nick1)) {
			zaproszenia.remove(nick1);
			zaproszenia.remove(nick2);
			dodaj(nick1, nick2);
			dodaj(nick2, nick1);
			p1.sendMessage(prefix + "Ty i §e" + nick2 + "§6 jesteście od teraz przyjaciółmi!");
			p2.sendMessage(prefix + "Ty i §e" + nick1 + "§6 jesteście od teraz przyjaciółmi!");
		} else {
			if (zaproszenia.get(nick1) != null)
				p1.sendMessage(prefix + "Anulowano poprzednie zaproszenie");
			zaproszenia.put(nick1, nick2);
			p1.sendMessage(prefix + "Wysłano zaproszenie do przyjaciół do §e" + nick2);
			Napis msg = new Napis(prefix + "§e" + nick1 + "§6 chce być twoim przyjacielem ");
			msg.dodaj(new Napis("§a[zaakceptuj]", "§7kliknij aby zaakceptować", Action.RUN_COMMAND, "/przyjaciele dodaj " + p1.getName()));
			msg.wyświetl(p2);
		}
	}
	private void dodaj(String kto, String kogo) {
		Gracz gracz = Gracz.wczytaj(kto);
		gracz.przyjaciele.add(kogo);
		gracz.zapisz();
	}
	private void usuń(Player kto, Player kogo) {
		if (kogo == null) {
			kto.sendMessage(prefix + "Wskazany gracz nie został odnaleziony");
			return;
		}
		String nick1 = kto.getName();
		String nick2 = kogo.getName();
		
		if (nick1.equals(nick2)) {
			kto.sendMessage(prefix + "xD");
			return;
		}
		
		Gracz g1 = Gracz.wczytaj(nick1);
		Gracz g2 = Gracz.wczytaj(nick2);
		
		if (!g1.przyjaciele.contains(nick2)) {
			kto.sendMessage(prefix + "§e" + nick2 + " §6" + "nie jest twoim przyjacielem");
			return;
		}
		g1.przyjaciele.remove(nick2); g1.zapisz();
		g2.przyjaciele.remove(nick1); g2.zapisz();
		kto.sendMessage(prefix  + "§e" + nick2 + "§6 nie jest już twoim przyjacielem");
		kogo.sendMessage(prefix + "§e" + nick1 + "§6 zerwał z tobą wasze przyjacielskie więzi §d§l:(");
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return uzupełnijTabComplete(Func.ostatni(args), Arrays.asList("dodaj", "usuń", "wszyscy"));
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Każdy jest przyjacielem konsoli!");
		Player p = (Player) sender;
		
		Gracz _gracz = Gracz.wczytaj(p.getName());
		if (args.length > 1) {
			switch(args[0]) {
			case "w":
			case "wszyscy":
				String msg = Func.listToString(args, 1);
				msg = prefix + "§2[§aW§2] §e" + p.getDisplayName() + "§7: §f" + msg;
				for (String gracz : _gracz.przyjaciele) {
					Player p2 = Bukkit.getPlayer(gracz);
					if (p2 != null && p2.isOnline())
						p2.sendMessage(msg);
				}
				p.sendMessage(msg);
				return true;
			case "d":
			case "dodaj":
				Player gracz = Bukkit.getPlayer(args[1]);
				if (gracz == null || !gracz.isOnline()) {
					p.sendMessage(prefix + "Nie odnaleziono gracza");
					return true;
				}
				zaproś(p, gracz);
				return true;
			case "usuń":
			case "u":
				usuń(p, Bukkit.getPlayer(args[1]));
				return true;
			}
		}
		if (_gracz.przyjaciele.size() <= 0) 
			return Func.powiadom(p, prefix + "Nie masz żadnych przyjaciół §d§l:(");
		String nagłówek = "§3§l>——————————→ §d§lPrzyjaciele §3§l←——————————<";
		p.sendMessage(nagłówek);
		for (String nick : _gracz.przyjaciele)
			wyświetl(p, nick);
		ogólne.wyświetl(p);
		p.sendMessage(nagłówek);
		return true;
	}
	private void wyświetl(Player komu, String kogo) {
		Player przyjaciel = Bukkit.getPlayer(kogo);
		String kolor = (przyjaciel == null) ? "§c" : "§a";
		Napis msg = new Napis("§e§l-> " + kolor + kogo + " ");
		msg.dodaj(przyjaciel == null ? offline : online);
		msg.dodaj(zawsze);
		msg.wyświetl(komu);
		
	}
}

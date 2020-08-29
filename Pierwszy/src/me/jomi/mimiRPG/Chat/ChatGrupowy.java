package me.jomi.mimiRPG.Chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Napis;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class ChatGrupowy extends Komenda implements Listener {

	public static String prefix = Func.prefix("Chat Grupowy");
	public HashMap<String, ChatGrupowyInst> mapa = new HashMap<>();
	public List<String> nazwy = Lists.newArrayList();
	public List<CommandSender> podgl�dacze = Lists.newArrayList();
	
	public static ChatGrupowy inst;
	public ChatGrupowy() {
		super("chatGrupowy", null, "cg");
		Main.dodajPermisje("chatGrupowy.podgladaj");
		inst = this;
	}
	
	private void dodaj(Napis doCzego, String tekst, String hover, String sugest) {
		doCzego.dodaj(new Napis(tekst, hover, Action.SUGGEST_COMMAND, sugest));
	}
	
	private boolean info(CommandSender p) {
		Napis msg = new Napis("\n");
		
		msg.dodaj("\n�6�l>>> �eCzat Grupowy �6�l<<");
		dodaj(msg, "\n�e�l-> �b/cg stw�rz <nazwa czatu>", 	"Tworzy nowy czat grupowy", 			"/cg stw�rz ");
		dodaj(msg, "\n�e�l-> �b/cg zapro� <nick>", 			"Zaprasza gracza do czatu", 			"/cg zapro� ");
		dodaj(msg, "\n�e�l-> �b/cg wyrzu� <nick>", 			"Wyrzuca gracza z czatu", 				"/cg wyrzu� ");
		dodaj(msg, "\n�e�l-> �b/cg opu��", 					"Opuszcza czat grupowy", 				"/cg opu��");
		dodaj(msg, "\n�e�l-> �b/cg og�lny <wiadomo��>", 	"Pisze wiadomo�� na czacie serwerowym", "/cg og�lny ");
		dodaj(msg, "\n�e�l-> �b/cg lista", 					"Wy�wietla list� graczy czatu", 		"/cg lista");
		//adminowe
		if (p.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
			dodaj(msg, "\n�e�l-> �b/cg lista w", 			"Wy�wietla list� graczy wszystkich cztat�w", 	  "/cg lista w");
			dodaj(msg, "\n�e�l-> �b/cg podgl�daj", 			"Przechodzi w tryb podgl�dania czat�w grupowych", "/cg podgl�daj");
		}
		msg.dodaj("\n�6�l>>> �eCzat Grupowy �6�l<<");
		msg.wy�wietl(p);
		return true;
	}
	private boolean powiadom(CommandSender p, String msg) {
		p.sendMessage(prefix + msg);
		return true;
	}
	private int znajdzPodgl�dacza(CommandSender p) {
		String nick = p.getName();
		for (int i=0; i<podgl�dacze.size(); i++)
			if (podgl�dacze.get(i).getName().equals(nick))
				return i;
		return -1;
	}
	
	@EventHandler
	public void pisanie(AsyncPlayerChatEvent ev) {
		String msg = ev.getMessage();
		CommandSender p = ev.getPlayer();
		if (mapa.containsKey(p.getName())) {
			if (msg.startsWith("||") && msg.length() > 2) {
				ev.setMessage(msg.substring(2));
				return;
			}
			ev.setCancelled(true);
			mapa.get(p.getName()).napisz(p, msg);
		}
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		CommandSender p = ev.getPlayer();
		if (mapa.containsKey(p.getName()))
			mapa.get(p.getName()).opu��(p, true);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 2) return null;
		List<String> lista = Arrays.asList("stw�rz", "lista", "zapro�", "wyrzu�", "opu��", "og�lny");
		if (sender.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
			lista.add("lista w");
			lista.add("podgl�daj");
		}
		return uzupe�nijTabComplete(Func.listToString(args, 0), lista);
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1) return info(p);
		CommandSender gracz;
		switch(args[0]) {
		case "s":
		case "stw�rz":
			if (args.length < 2) return info(p);
			if (mapa.containsKey(p.getName()))
				return powiadom(p, "Nale�ysz ju� do czatu grupowego");
			String nazwa = Func.listToString(args, 1);
			if (nazwa.length() > 30)
				return powiadom(p, "Zbyt d�uga nazwa czatu");
			if (nazwy.contains(nazwa))
				return powiadom(p, "Grupa o tej nazwie ju� istnieje");
			new ChatGrupowyInst(p, nazwa);
			return true;
		case "z":
		case "zapro�":
			if (args.length < 2) return info(p);
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie nale�ysz do czatu �adnego grupowego");
			if (p.getName().equals(args[1]))
				return powiadom(p, "Nie mo�esz wykonywa� tego na sobie");
			if (mapa.containsKey(args[1]))
				return powiadom(p, "Gracz �e" + args[1] + "�6 nale�y ju� do jakiego� czatu grupowego");
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null)
				return powiadom(p, "Gracz �e" + args[1] + "�6 nie istnieje");
			mapa.get(p.getName()).wy�lij("Gracz �d" + p.getName() + "�f zaprosi� gracza �d" + args[1] + "�f do czatu");
			mapa.get(p.getName()).zapro�(p, gracz);
			return true;
		case "wbijDo":
			if (args.length < 2) return info(p);
			if (p.getName().equals(args[1]))
				return info(p);
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null) return info(p);
			if (!mapa.containsKey(gracz.getName()) || mapa.containsKey(p.getName())) {
				p.sendMessage(prefix + "Nie mo�esz ju� do��czy� z tego zaproszenia");
				return true;
			}
			mapa.get(gracz.getName()).dodaj(p);
			return true;
		case "w":
		case "wyrzu�":
			if (args.length < 2) return info(p);
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie nale�ysz do czatu �adnego grupowego");
			if (p.getName().equals(args[1]))
				return powiadom(p, "Nie mo�esz wykonywa� tego na sobie");
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null)
				return powiadom(p, "Gracz �e" + args[1] + "�6 nie istnieje");
			if (mapa.get(p.getName()).znajdzGracza(gracz) == -1)
				return powiadom(p, "Gracz �e" + args[1] + "�6 nie nale�y do twojego czatu grupowego");
			mapa.get(p.getName()).wyrzu�(p, gracz);
			return true;
		case "opu��":
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie nale�ysz do czatu �adnego grupowego");
			mapa.get(p.getName()).opu��(p, true);
			return true;
		case "o":
		case "og�lny":
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie nale�ysz do czatu �adnego grupowego");
			if (args.length < 2) 
				return powiadom(p, "Nie podano �adnej wiadomo�ci");
			if (p instanceof Player)
				((Player) p).chat("||" + Func.listToString(args, 1));
			else
				p.sendMessage(prefix + "zamiaste tego u�yj /say /me /mi");
			return true;
		case "l":
		case "lista":
			if (args.length >= 2 && args[1].equalsIgnoreCase("w") && p.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
				for (String n : nazwy) 
					for (String g : mapa.keySet()) 
						if (mapa.get(g).nazwa.equals(n)) {
							mapa.get(g).lista(p);
							break;
						}
				return true;
			}
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie nale�ysz do czatu �adnego grupowego");
			mapa.get(p.getName()).lista(p);
			return true;
		case "p":
		case "podgl�daj":
			if (p.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
				int i = znajdzPodgl�dacza(p);
				if (i == -1) {
					podgl�dacze.add(p);
					p.sendMessage(prefix + "Podgl�dacz czaty grupowe");
				}
				else {
					podgl�dacze.remove(i);
					p.sendMessage(prefix + "Nie podgl�dasz ju� czat�w grupowych");
				}
				return true;		
			}
		default:
			return info(p);
		}
	}
}

class ChatGrupowyInst {

	public static String prefix = ChatGrupowy.prefix;
	public List<CommandSender> gracze = Lists.newArrayList();
	public String nazwa;
	
	public ChatGrupowyInst(CommandSender p, String nazwa) {
		this.nazwa = "�3" + nazwa;
		gracze.add(p);
		ChatGrupowy.inst.nazwy.add(this.nazwa);
		ChatGrupowy.inst.mapa.put(p.getName(), this);
		p.sendMessage(prefix + "Utworzy�e� czat grupowy " + this.nazwa);
	}
	
	public void dodaj(CommandSender p) {
		if (znajdzGracza(p) != -1) {
			p.sendMessage(prefix + "Nale�ysz ju� do tego czatu");
		}
		gracze.add(p);
		ChatGrupowy.inst.mapa.put(p.getName(), this);
		wy�lij("Gracz �d" + p.getName() + "�f do��czy� do czatu");
	}
	public void zapro�(CommandSender zapraszaj�cy, CommandSender p) {
		TextComponent msg = new TextComponent(prefix + "Zaproszenie od gracza �e" + zapraszaj�cy.getName() + " ");
		
		TextComponent m = new TextComponent("�a[Do��cz]");
		m.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cg wbijDo " + zapraszaj�cy.getName()));
		msg.addExtra(m);
		
		p.spigot().sendMessage(msg);
	}
	public void opu��(CommandSender p, boolean komunikat) {
		gracze.remove(znajdzGracza(p));
		if (komunikat) {
			p.sendMessage(prefix + "Opu�ci�e� czat grupowy " + nazwa);
			wy�lij("Gracz �d" + p.getName() + "�f opu�ci� czat");
		}
		ChatGrupowy.inst.mapa.remove(p.getName());
		if (gracze.size() == 0)
			ChatGrupowy.inst.nazwy.remove(nazwa);
	}
	public void wyrzu�(CommandSender wyrzucacz, CommandSender p) {
		opu��(p, false);
		wy�lij("Gracz �d" + wyrzucacz.getName() + "�f wyrzuci� gracza �d" + p.getName() + " z czatu");
		p.sendMessage(prefix + "Zosta�e� wyrzucony z czatu " + nazwa + " �6 przez gracza �e" + wyrzucacz.getName());
	}
	public void napisz(CommandSender p, String msg) {
		wy�lij("�d" + p.getName() + "�7: �f" + msg);
	}
	public void wy�lij(String msg) {
		msg = "�e[" + nazwa + "�e] �f" + msg;
		for (CommandSender gracz : gracze)
			gracz.sendMessage(msg);
		for (CommandSender gracz : ChatGrupowy.inst.podgl�dacze)
			if (znajdzGracza(gracz) == -1)
				gracz.sendMessage(msg);
		Bukkit.getConsoleSender().sendMessage(msg);
	}
	public void lista(CommandSender p) {
		p.sendMessage("\n�6�l>>> " + nazwa + " �6�l<<<");
		for (CommandSender gracz : gracze)
			p.sendMessage("�6�l- �d" + gracz.getName());
	}
	public int znajdzGracza(CommandSender p) {
		String nick = p.getName();
		for (int i=0; i<gracze.size(); i++)
			if (gracze.get(i).getName().equals(nick))
				return i;
		return -1;
	}
	
}

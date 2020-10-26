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

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Napis;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

@Moduł
public class ChatGrupowy extends Komenda implements Listener {

	public static String prefix = Func.prefix("Chat Grupowy");
	public HashMap<String, ChatGrupowyInst> mapa = new HashMap<>();
	public List<String> nazwy = Lists.newArrayList();
	public List<CommandSender> podglądacze = Lists.newArrayList();
	
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
		
		msg.dodaj("\n§6§l>>> §eCzat Grupowy §6§l<<");
		dodaj(msg, "\n§e§l-> §b/cg stwórz <nazwa czatu>", 	"Tworzy nowy czat grupowy", 			"/cg stwórz ");
		dodaj(msg, "\n§e§l-> §b/cg zaproś <nick>", 			"Zaprasza gracza do czatu", 			"/cg zaproś ");
		dodaj(msg, "\n§e§l-> §b/cg wyrzuć <nick>", 			"Wyrzuca gracza z czatu", 				"/cg wyrzuć ");
		dodaj(msg, "\n§e§l-> §b/cg opuść", 					"Opuszcza czat grupowy", 				"/cg opuść");
		dodaj(msg, "\n§e§l-> §b/cg ogólny <wiadomość>", 	"Pisze wiadomość na czacie serwerowym", "/cg ogólny ");
		dodaj(msg, "\n§e§l-> §b/cg lista", 					"Wyświetla listę graczy czatu", 		"/cg lista");
		//adminowe
		if (p.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
			dodaj(msg, "\n§e§l-> §b/cg lista w", 			"Wyświetla listę graczy wszystkich cztatów", 	  "/cg lista w");
			dodaj(msg, "\n§e§l-> §b/cg podglądaj", 			"Przechodzi w tryb podglądania czatów grupowych", "/cg podglądaj");
		}
		msg.dodaj("\n§6§l>>> §eCzat Grupowy §6§l<<");
		msg.wyświetl(p);
		return true;
	}
	private boolean powiadom(CommandSender p, String msg) {
		p.sendMessage(prefix + msg);
		return true;
	}
	private int znajdzPodglądacza(CommandSender p) {
		String nick = p.getName();
		for (int i=0; i<podglądacze.size(); i++)
			if (podglądacze.get(i).getName().equals(nick))
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
			mapa.get(p.getName()).opuść(p, true);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 2) return null;
		List<String> lista;
		if (sender.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
			lista = Arrays.asList("podglądaj", "lista w", "stwórz", "lista", "zaproś", "wyrzuć", "opuść", "ogólny");
			return uzupełnijTabComplete(Func.listToString(args, 0), lista);
		}
		lista = Arrays.asList("stwórz", "lista", "zaproś", "wyrzuć", "opuść", "ogólny");
		return uzupełnijTabComplete(Func.listToString(args, 0), lista);
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if (args.length < 1) return info(p);
		CommandSender gracz;
		switch(args[0]) {
		case "s":
		case "stwórz":
			if (args.length < 2) return info(p);
			if (mapa.containsKey(p.getName()))
				return powiadom(p, "Należysz już do czatu grupowego");
			String nazwa = Func.listToString(args, 1);
			if (nazwa.length() > 30)
				return powiadom(p, "Zbyt długa nazwa czatu");
			if (nazwy.contains(nazwa))
				return powiadom(p, "Grupa o tej nazwie już istnieje");
			new ChatGrupowyInst(p, nazwa);
			return true;
		case "z":
		case "zaproś":
			if (args.length < 2) return info(p);
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie należysz do czatu żadnego grupowego");
			if (p.getName().equals(args[1]))
				return powiadom(p, "Nie możesz wykonywać tego na sobie");
			if (mapa.containsKey(args[1]))
				return powiadom(p, "Gracz §e" + args[1] + "§6 należy już do jakiegoś czatu grupowego");
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null)
				return powiadom(p, "Gracz §e" + args[1] + "§6 nie istnieje");
			mapa.get(p.getName()).wyślij("Gracz §d" + p.getName() + "§f zaprosił gracza §d" + args[1] + "§f do czatu");
			mapa.get(p.getName()).zaproś(p, gracz);
			return true;
		case "wbijDo":
			if (args.length < 2) return info(p);
			if (p.getName().equals(args[1]))
				return info(p);
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null) return info(p);
			if (!mapa.containsKey(gracz.getName()) || mapa.containsKey(p.getName())) {
				p.sendMessage(prefix + "Nie możesz już dołączyć z tego zaproszenia");
				return true;
			}
			mapa.get(gracz.getName()).dodaj(p);
			return true;
		case "w":
		case "wyrzuć":
			if (args.length < 2) return info(p);
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie należysz do czatu żadnego grupowego");
			if (p.getName().equals(args[1]))
				return powiadom(p, "Nie możesz wykonywać tego na sobie");
			gracz = Bukkit.getPlayer(args[1]);
			if (gracz == null)
				return powiadom(p, "Gracz §e" + args[1] + "§6 nie istnieje");
			if (mapa.get(p.getName()).znajdzGracza(gracz) == -1)
				return powiadom(p, "Gracz §e" + args[1] + "§6 nie należy do twojego czatu grupowego");
			mapa.get(p.getName()).wyrzuć(p, gracz);
			return true;
		case "opuść":
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie należysz do czatu żadnego grupowego");
			mapa.get(p.getName()).opuść(p, true);
			return true;
		case "o":
		case "ogólny":
			if (!mapa.containsKey(p.getName()))
				return powiadom(p, "Nie należysz do czatu żadnego grupowego");
			if (args.length < 2) 
				return powiadom(p, "Nie podano żadnej wiadomości");
			if (p instanceof Player)
				((Player) p).chat("||" + Func.listToString(args, 1));
			else
				p.sendMessage(prefix + "zamiaste tego użyj /say /me /mi");
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
				return powiadom(p, "Nie należysz do czatu żadnego grupowego");
			mapa.get(p.getName()).lista(p);
			return true;
		case "p":
		case "podglądaj":
			if (p.hasPermission("mimiRPG.chatGrupowy.podgladaj")) {
				int i = znajdzPodglądacza(p);
				if (i == -1) {
					podglądacze.add(p);
					p.sendMessage(prefix + "Podglądacz czaty grupowe");
				}
				else {
					podglądacze.remove(i);
					p.sendMessage(prefix + "Nie podglądasz już czatów grupowych");
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
		this.nazwa = "§3" + nazwa;
		gracze.add(p);
		ChatGrupowy.inst.nazwy.add(this.nazwa);
		ChatGrupowy.inst.mapa.put(p.getName(), this);
		p.sendMessage(prefix + "Utworzyłeś czat grupowy " + this.nazwa);
	}
	
	public void dodaj(CommandSender p) {
		if (znajdzGracza(p) != -1) {
			p.sendMessage(prefix + "Należysz już do tego czatu");
		}
		gracze.add(p);
		ChatGrupowy.inst.mapa.put(p.getName(), this);
		wyślij("Gracz §d" + p.getName() + "§f dołączył do czatu");
	}
	public void zaproś(CommandSender zapraszający, CommandSender p) {
		TextComponent msg = new TextComponent(prefix + "Zaproszenie od gracza §e" + zapraszający.getName() + " ");
		
		TextComponent m = new TextComponent("§a[Dołącz]");
		m.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cg wbijDo " + zapraszający.getName()));
		msg.addExtra(m);
		
		p.spigot().sendMessage(msg);
	}
	public void opuść(CommandSender p, boolean komunikat) {
		gracze.remove(znajdzGracza(p));
		if (komunikat) {
			p.sendMessage(prefix + "Opuściłeś czat grupowy " + nazwa);
			wyślij("Gracz §d" + p.getName() + "§f opuścił czat");
		}
		ChatGrupowy.inst.mapa.remove(p.getName());
		if (gracze.size() == 0)
			ChatGrupowy.inst.nazwy.remove(nazwa);
	}
	public void wyrzuć(CommandSender wyrzucacz, CommandSender p) {
		opuść(p, false);
		wyślij("Gracz §d" + wyrzucacz.getName() + "§f wyrzucił gracza §d" + p.getName() + " z czatu");
		p.sendMessage(prefix + "Zostałeś wyrzucony z czatu " + nazwa + " §6 przez gracza §e" + wyrzucacz.getName());
	}
	public void napisz(CommandSender p, String msg) {
		wyślij("§d" + p.getName() + "§7: §f" + msg);
	}
	public void wyślij(String msg) {
		msg = "§e[" + nazwa + "§e] §f" + msg;
		for (CommandSender gracz : gracze)
			gracz.sendMessage(msg);
		for (CommandSender gracz : ChatGrupowy.inst.podglądacze)
			if (znajdzGracza(gracz) == -1)
				gracz.sendMessage(msg);
		Bukkit.getConsoleSender().sendMessage(msg);
	}
	public void lista(CommandSender p) {
		p.sendMessage("\n§6§l>>> " + nazwa + " §6§l<<<");
		for (CommandSender gracz : gracze)
			p.sendMessage("§6§l- §d" + gracz.getName());
	}
	public int znajdzGracza(CommandSender p) {
		String nick = p.getName();
		for (int i=0; i<gracze.size(); i++)
			if (gracze.get(i).getName().equals(nick))
				return i;
		return -1;
	}
	
}

package me.jomi.mimiRPG.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.ClickEvent;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Chat.Party.API.DołączanieDoPartyEvent;
import me.jomi.mimiRPG.Chat.Party.API.OpuszczaniePartyEvent;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Napis;

@Moduł(priorytet = Moduł.Priorytet.NAJWYŻSZY)
public class Party extends Komenda implements Listener {
	public static class API {
		public static class DołączanieDoPartyEvent extends PlayerEvent {
			public final Ekipa party;
			public DołączanieDoPartyEvent(Player p, Ekipa party) {
				super(p);
				this.party = party;
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
		public static class OpuszczaniePartyEvent extends PlayerEvent {
			public final Ekipa party;
			public OpuszczaniePartyEvent(Player p, Ekipa party) {
				super(p);
				this.party = party;
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
	}
	
	public static final String prefix = Func.prefix("§dParty");
	
	public static class Ekipa {
		public Player przywódca;
		public final List<Player> gracze = Lists.newArrayList();
	
		public Ekipa(Player przywódca, Player drugiGracz) {
			this.przywódca = przywódca;
			dołącz(przywódca, false);
			dołącz(drugiGracz);
		}
		
		public final void dołącz(Player p) {
			dołącz(p, true);
		}
		public void dołącz(Player p, boolean wyślijPowiadomienie) {
			if (dajParty(p) != null)
				throw new IllegalArgumentException("Gracz " + p.getName() + " należy już do jakiegoś party");
			
			gracze.add(p);
			
			if (wyślijPowiadomienie)
				powiadom(prefix + "%s dołączył do Party", p.getDisplayName());
			
			mapaParty.put(p.getName(), this);
			
			Bukkit.getPluginManager().callEvent(new DołączanieDoPartyEvent(p, this));
		}
		public final void opuść(Player p) {
			opuść(p, true);
		}
		public void opuść(Player p, boolean wyślijPowiadomienie) {
			musiNależeć(p);
			
			if (wyślijPowiadomienie)
				powiadom(prefix + "%s opuścił Party", p.getDisplayName());
			
			for (int i=0; i < gracze.size(); i++)
				if (gracze.get(i).getName().equals(p.getName())) {
					gracze.remove(i);
					break;
				}
			
			if (przywódca.getName().equals(p.getName()) && !gracze.isEmpty())
				wyznaczPrzywódcę(gracze.get(0));
			
			mapaParty.remove(p.getName());
			
			Bukkit.getPluginManager().callEvent(new OpuszczaniePartyEvent(p, this));
			
			if (gracze.size() == 1)
				opuść(gracze.get(0), false);
		}
		
		public void wyznaczPrzywódcę(Player p) {
			musiNależeć(p);
			przywódca = p;
			p.sendMessage(prefix + "Otrzymałeś miano przywódcy swojego Party");
		}
		
		private void musiNależeć(Player p) {
			if (!należy(p))
				throw new IllegalArgumentException("Gracz " + p.getName() + " nie należy do party");
		}
		
		
		public boolean należy(Player p) {
			for (int i=0; i < gracze.size(); i++)
				if (gracze.get(i).getName().equals(p.getName()))
					return true;
			return false;
			
		}
		
		
		public void powiadom(String format, Object... args) {
			String msg = Func.msg(format, args);
			gracze.forEach(p -> p.sendMessage(msg));
		}

		
		@Override
		public int hashCode() {
			return Objects.hash(przywódca.getName(), Func.wykonajWszystkim(gracze, Player::getName));
		}
		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof Ekipa && przywódca.equals(((Ekipa) obj).przywódca);
		}
	}
	
	
	static Map<String, Ekipa> mapaParty = new HashMap<>();
	public static Ekipa dajParty(Player p) {
		return mapaParty.get(p.getName());
	}
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		Func.wykonajDlaNieNull(mapaParty.get(ev.getPlayer().getName()), party -> party.opuść(ev.getPlayer()));
	}
	
	public static List<Player> dajGraczyParty(Player p) {
		List<Player> lista = new ArrayList<>();
		
		Func.wykonajDlaNieNull(dajParty(p), party -> party.gracze.forEach(lista::add), () -> lista.add(p));
		
		return lista;
	}
	
	public Party() {
		super("party", null, "team", "drużyna");
	}

	private final Set<String> zaproszenia = new HashSet<>();
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "opuść", "dołącz", "zaproś");
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Musisz być graczem aby tego użyć");
		
		Player p = (Player) sender;
		Player gracz;
		String kod;
		
		
		if (args.length >= 1)
			switch (args[0].toLowerCase()) {
			case "zapros":
			case "zaproś":
				if (args.length < 2)
					return Func.powiadom(sender, prefix + "/" + label + " zaproś <nick>");
				gracz = Bukkit.getPlayer(args[1]);
				if (gracz == null)
					return Func.powiadom(sender, prefix + "Niepoprawny gracz: %s", args[1]);
				if (gracz.getName().equals(p.getName()))
					return Func.powiadom(sender, prefix + "Nie możesz zaprosić sam siebie do party");
				if (dajParty(gracz) != null)
					return Func.powiadom(sender, prefix + "%s posiada już party", gracz.getDisplayName());
				
				kod = p.getName() + " " + gracz.getName();
				
				zaproszenia.add(kod);
				
				Func.powiadom(p, prefix + "Zaprosiłeś gracz %s do party", gracz.getDisplayName());
				new Napis(prefix + "Otrzymano zaproszenie od gracza " + p.getDisplayName() + ", kliknij ")
					.dodaj(new Napis("§a[dołącz]", " §bKliknij aby dołączyć", "/party dołącz " + p.getName()))
					.dodaj(" aby dołączyć do jego party")
					.wyświetl(gracz);
				
				Func.opóznij(20 * 120, () -> {
					 if (zaproszenia.remove(kod)) {
						 Func.powiadom(p, prefix + "Zaproszenie dla gracza %s wygasło", gracz.getDisplayName());
						 Func.powiadom(gracz, prefix + "Zaproszenie od gracza %s wygasło", p.getDisplayName());
					 }
				});
				
				return true;
			case "dolacz":
			case "dolącz":
			case "dołacz":
			case "dołącz":
				if (args.length < 2)
					return Func.powiadom(sender, prefix + "/" + label + " dołącz <nick>");
				
				gracz = Bukkit.getPlayer(args[1]);
				if (gracz == null)
					return Func.powiadom(sender, prefix + "Gracz %s nie jest online", args[1]);
	
				if (dajParty(p) != null)
					return Func.powiadom(sender, prefix + "Posiadasz już party");
					
				kod = gracz.getName() + " " + p.getName();
				
				if (zaproszenia.remove(kod))
					Func.wykonajDlaNieNull(dajParty(gracz),
							party -> party.dołącz(p),
							() -> new Ekipa(gracz, p));
				else
					return Func.powiadom(sender, prefix + "Nie masz zaproszenia od %s", p.getDisplayName());
				return true;
			case "opusc":
			case "opusć":
			case "opuśc":
			case "opuść":
				Func.wykonajDlaNieNull(dajParty(p),
						party -> party.opuść(p),
						() -> Func.powiadom(sender, prefix + "Nie jesteś w żadnym party"));
				return true;
			case "wyrzuc":
			case "wyrzuć":
				Func.wykonajDlaNieNull(dajParty(p),
						party -> {
							if (!party.przywódca.getName().equals(p.getName()))
								sender.sendMessage(prefix + "Musisz być przywódcą party aby to zrobić");
							else {
								if (args.length < 2)
									sender.sendMessage(prefix + "/party wyrzuć <gracz>");
								else 
									Func.wykonajDlaNieNull(Bukkit.getPlayer(args[1]), wyrzucany -> {
										if (!party.należy(wyrzucany))
											sender.sendMessage(prefix + "Gracz nie należy do twojego party");
										else if (wyrzucany.getName().equals(sender.getName())) 
											sender.sendMessage(prefix + "Nie możesz się sam wyrzucić z party");
										else {
											party.powiadom("%s Został wyrzucony z party", wyrzucany.getDisplayName());
											party.opuść(wyrzucany, false);
										}
									}, () -> Func.powiadom(sender, prefix + "Niepoprawny gracz %s", args[1]));
							}
						},
						() -> Func.powiadom(sender, prefix + "Nie jesteś w żadnym party"));
				return true;
			case "przekaz":
			case "przekaż":
				Func.wykonajDlaNieNull(dajParty(p),
						party -> {
							if (args.length < 2)
								Func.powiadom(sender, prefix + "/party przekaż <gracz>");
							else if (!party.przywódca.getName().equals(p.getName()))
								Func.powiadom(sender, prefix + "Musisz być przywódcą aby to zrobić");
							else {
								Player p2 = Bukkit.getPlayer(args[1]);
								if (p2 == null)
									Func.powiadom(sender, prefix + "Niepoprawny gracz %s", args[1]);
								else if (!party.należy(p2))
									Func.powiadom(sender, prefix + "%s nie należy do twojego party", p2.getName());
								else if (p.getName().equals(p2.getName()))
									Func.powiadom(sender, prefix + "Nie możesz przekazać przywódctwa samemu sobie");
								else {
									party.powiadom("%s przekazał przywództwo %s", p.getDisplayName(), p2.getDisplayName());
									party.wyznaczPrzywódcę(p2);
								}
							}
						}, () -> Func.powiadom(sender, prefix + "Nie jesteś w żadnym party"));
				return true;
			}
		
		edytor(p);
		
		return true;
	}
	
	void edytor(Player p) {
		Ekipa party = dajParty(p);
		
		Napis n = new Napis("\n\n\n§dTwoje Party\n");
		
		boolean przyciskDodaj = true;
		
		if (party != null) {
			przyciskDodaj = party.przywódca.getName().equals(p.getName());
			
			party.gracze.forEach(gracz -> {
				n.dodaj("\n§e§l- §" + (gracz.getName().equals(party.przywódca.getName()) ? "6" : "a") + gracz.getName() + " ");
				if (p.getName().equals(party.przywódca.getName()) && !p.getName().equals(gracz.getName()))
					n.dodaj(
							new Napis(
									"§c[x]",
									"§cKliknij aby wyrzucić §4" + gracz.getName() + "§c z party",
									"/party wyrzuć " + gracz.getName()
									).dodaj(" "),
							new Napis(
									"§6[w]",
									"§bKliknij aby przekazać §9" + gracz.getName() + "§b przywódcę party",
									"/party przekaż " + gracz.getName()
									)
							);
			});
		} else
			n.dodaj("§6Jesteś sam w party");
		
		n.dodaj("\n");
		
		if (przyciskDodaj)
			n.dodaj("\n").dodaj(new Napis("§a[dodaj]", "§bKliknij aby zaprosić gracza do party", "/party zaproś ", ClickEvent.Action.SUGGEST_COMMAND)).dodaj(" ");
		
		if (party != null)
			n.dodaj(new Napis("§c[opuść]", "§bKliknij aby opuścić party", "/party opuść"));
		
		n.dodaj("\n").wyświetl(p);
	}
}

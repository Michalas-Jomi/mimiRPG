package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.NowyEkwipunek;
import me.jomi.mimiRPG.PojedynczeKomendy.Antylog;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class Minigra implements Listener, Przeładowalny, Zegar  {
	public static abstract class Arena extends Mapowany {
		@Mapowane List<Integer> sekundyPowiadomien;
		@Mapowane int max_gracze = -1;
		@Mapowane int czasStartu = 60;
		@Mapowane int min_gracze = 2;
		@Mapowane Location zbiorka;
		
		String nazwa = "Minigra";
		List<Player> gracze = Lists.newArrayList();
		boolean grane = false;
		int timer = -1;

		abstract Minigra getInstMinigra();
		abstract <M extends Minigra> void setInst(M inst);
		abstract Supplier<? extends Statystyki> noweStaty();
		abstract int policzGotowych();
		
		// Wykonywane co sekunde dla "zaczynanaArena"
		void czas() {
			if (timer == -1) return;
			
			if (policzGotowych() >= max_gracze)
				timer = Math.min(timer, 11);
			
			String czas = Func.czas(--timer);
			
			if (sekundyPowiadomien.contains(timer))
				Bukkit.broadcastMessage(prefix + Func.msg("Arena %s wystartuje za %s (%s/%s)",
						nazwa, czas, gracze.size(), strMaxGracze()));
			
			if (timer <= 0) {
				start();
				return;
			}
			
			if (timer <= 5)
				for (Player p : gracze)
					p.sendTitle("§a" + czas, "§bStart areny " + nazwa, 30, 40, 30);
			
			for (Player p : gracze)
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aStart za " + czas));
		}
				
		void start() {
			timer = -1;
			grane = true;
			getInstMinigra().zaczynanaArena = null;
			for (Player p : gracze) {
				p.getInventory().clear();
				getInstMinigra().staty(p).rozegraneAreny++;
				p.closeInventory();
				p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
			}
		}
		boolean dołącz(Player p) {
			if (opuść(p)) return false;
			Func.ustawMetadate(p, getInstMinigra().getMetaId(), this);
			NowyEkwipunek.dajNowy(p, zbiorka, GameMode.ADVENTURE);
			gracze.add(p);
			napiszGraczom("%s dołączył do poczekalni %s/%s", p.getDisplayName(), gracze.size(), strMaxGracze());

			Statystyki staty = Gracz.wczytaj(p.getName()).staty.getOrDefault(this.getClass().getName(), noweStaty().get());
			Func.ustawMetadate(p, getInstMinigra().getMetaStatystyki(), staty);
			
			Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			p.setScoreboard(scoreboard);
			Objective obj = scoreboard.registerNewObjective("staty", "dummy", "§6§lStatystyki");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			staty.rozpisz(obj);
			
			Antylog.włączBypass(p);
			
			return true;
		}
		boolean opuść(Player p) {
			String nick = p.getName();
			for (int i=0; i<gracze.size(); i++)
				if (gracze.get(i).getName().equals(nick))
					return opuść(i, true) != null;
			
			Antylog.wyłączBypass(p);
			
			return false;
		}
		Player opuść(int i, boolean info) {
			Player p = gracze.remove(i);

			NowyEkwipunek.wczytajStary(p);
			
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			
			Statystyki staty = getInstMinigra().staty(p);
			if (staty != null) {
				Gracz g = Gracz.wczytaj(p.getName());
				staty.sprawdzTopke(p);
				g.staty.put(this.getClass().getName(), staty);
				g.zapisz();
			}
			
			p.removeMetadata(getInstMinigra().getMetaId(), Main.plugin);
			p.removeMetadata(getInstMinigra().getMetaStatystyki(), Main.plugin);
			
			if (info)
				if (!grane)
					napiszGraczom("%s opuścił pokuj", p.getDisplayName());
				else {
					napiszGraczom("%s opuścił rozgrywkę", p.getDisplayName());
					sprawdzKoniec();
				}
			p.sendMessage(prefix + "Nie jesteś już w Paintballu");
			
			if (timer != -1 && policzGotowych() < min_gracze) {
				timer = -1;
				Bukkit.broadcastMessage(prefix + Func.msg("Wstrzymano odliczanie areny %s , z powodu małej ilości graczy %s/%s",
						nazwa, gracze.size(), strMaxGracze()));
			}
			return p;
		}

		abstract boolean sprawdzKoniec();
		void koniec() {
			grane = false;
			while (!gracze.isEmpty())
				opuść(0, false);
		}
		
		Object strMaxGracze() {
			return max_gracze <= 0 ? min_gracze + "+" : max_gracze;
		}

		void napiszGraczom(String msg, Object... uzupełnienia) {
			if (!msg.startsWith(prefix))
				msg = prefix + msg;
			msg = Func.msg(msg, uzupełnienia);
			for (Player p : gracze)
				p.sendMessage(msg);
		}				
		
		boolean pełna() {
			return max_gracze > 0 && gracze.size() >= max_gracze;
		}
		boolean poprawna() {
			return nazwa != null && !nazwa.isEmpty() &&
					zbiorka != null &&
					(max_gracze < 0 || max_gracze >= min_gracze);
		}
	
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Arena)
				return ((Arena) obj).nazwa.equals(nazwa);
			return false;
		}
	}
	public static abstract class Statystyki extends Mapowany {
		@Mapowane int rozegraneAreny;
		@Mapowane int przegraneAreny;
		@Mapowane int wygraneAreny;
		
		private int licz_linie;
		public void rozpisz(Objective obj) {
			licz_linie = -1;
			
			rozpiska(str -> obj.getScore(str).setScore(licz_linie--), true);
		}
		public String rozpisz() {
			StringBuilder s = new StringBuilder();

			s.append('\n');
			
			rozpiska(str -> s.append(str).append('\n'), false);
			
			s.append('\n');
			
			return s.toString();
		}
		
		String _rozpiska(String info, Object stan) {
			return "§6" + info + "§8: §e" + stan;
		}
		String _rozpiska(int liczone, int wszystkie) {
			if (wszystkie == 0) return "100%";
			return ((int) ( ((double) liczone) / ((double) wszystkie) * 100 ) ) + "%";
		}
		void rozpiska(Consumer<String> cons, boolean usuwaćKolor) {
			cons.accept(_rozpiska("win ratio", _rozpiska(wygraneAreny, rozegraneAreny)));
			cons.accept(_rozpiska("Rozegrane gry", rozegraneAreny));
			cons.accept(_rozpiska("Wygrane gry", wygraneAreny));
			cons.accept(_rozpiska("Przegrane gry", przegraneAreny));
			cons.accept(" ");
			
		}
	
		abstract void sprawdzTopke(Player p);
	}

	static Set<String> dozwoloneKomendy = Sets.newConcurrentHashSet();

	HashMap<String, Arena> mapaAren = new HashMap<String, Arena>();
	static Config configTopki = new Config("configi/minigry/Topki");
		
	// abstract
	public static String prefix = Func.prefix("Minigra");
	abstract Config getConfigAreny();
	abstract String getMetaStatystyki();
	abstract String getMetaId();
	
	public Minigra() {
		Minigry.mapaGier.put(this.getClass().getSimpleName().toLowerCase(), this);
	}
	
	Arena zaczynanaArena;
	
	@Override
	public void przeładuj() {
		configTopki.przeładuj();

		wyłącz("Przeładowywanie pluginu");
		
		dozwoloneKomendy.clear();
		for (String komenda : Main.ust.wczytajListe("Minigry.Dozwolone komendy"))
			dozwoloneKomendy.add(komenda.startsWith("/") ? "/" + komenda : komenda);

		Config configAreny = getConfigAreny();
		configAreny.przeładuj();
		mapaAren.clear();
		for (String klucz : configAreny.klucze(false))
			try {
				Arena arena = (Arena) configAreny.wczytaj(klucz);
				arena.nazwa = klucz;
				arena.setInst(this);
				if (!arena.poprawna())
					throw new Throwable();
				mapaAren.put(klucz, arena);
			} catch (Throwable e) {
				Main.warn("Niepoprawna arena " + this.getClass().getSimpleName() + " " + klucz + " w " + configAreny.path());
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane areny " + this.getClass().getSimpleName(), mapaAren.size());
	}
	
	@Override
	public int czas() {
		if (zaczynanaArena != null)
			zaczynanaArena.czas();
		return 20;
	}
	
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		Player p = ev.getPlayer();
		if (p.hasMetadata(getMetaId())) {
			if (dozwoloneKomendy.contains(Func.tnij(ev.getMessage(), " ").get(0)))
				return;
			else if (p.hasPermission(Minigry.permCmdBypass))
				p.sendMessage(prefix + "pamiętaj że jesteś w trakcie minigry");
			else {
				ev.setCancelled(true);
				p.sendMessage(prefix + "Nie wolno tu uzywać komend");
			}
		}
	}

	Arena zaczynanaArena() {
		if (zaczynanaArena != null) 
			return zaczynanaArena;
		
		if (mapaAren.isEmpty())
			return null;
		
		for (int i=0; i < 10; i++) {
			Arena arena = Func.losuj(mapaAren.values());
			if (arena.grane) continue;
			return zaczynanaArena = arena;
		}
		for (Arena arena : mapaAren.values()) {
			if (arena.grane) continue;
			return zaczynanaArena = arena;
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	static <T> T metadata(Entity p, String meta) {
		if (p == null || !p.hasMetadata(meta)) return null;
		return (T) p.getMetadata(meta).get(0).value();	
	}

	<A extends Arena> A arena(Entity p) {
		return metadata(p, getMetaId());
	}
	<S extends Statystyki> S staty(Player p) {
		return metadata(p, getMetaStatystyki());
	}

	public static void wyłącz() {
		wyłącz("Wyłączanie pluginu");
	}
	static void wyłącz(String msg) {
		for (Minigra minigra : Minigry.mapaGier.values()) {
			for (Arena arena : minigra.mapaAren.values())
				if (arena.grane) {
					arena.napiszGraczom(msg);
					arena.koniec();
				}
			if (minigra.zaczynanaArena != null) {
				minigra.zaczynanaArena.napiszGraczom(msg);
				minigra.zaczynanaArena.koniec();
				minigra.zaczynanaArena = null;
			}
		}
	}


	public boolean onCommand(CommandSender sender, String[] args) {
		if (args.length < 2) return staty(sender, args);
		
		switch (args[1]) {
		case "staty":	return staty(sender, args);
		}
				
		if (!(sender instanceof Player))
			return Func.powiadom(prefix, sender, "Paintball jest tylko dla graczy");
		Player p = (Player) sender;
		
		Arena arena;
		
		switch (Func.odpolszcz(args[1])) {
		case "dolacz":
			arena = (Arena) zaczynanaArena();
			if (arena == null)
				return Func.powiadom(prefix, sender, "Aktualnie nie ma żadnych wolnych aren");
			if (arena.pełna())
				return Func.powiadom(prefix, sender, "Brak miejsc w poczekalni");
			arena.dołącz(p);
			break;
		case "opusc":
			arena = arena(p);
			if (arena == null)
				return Func.powiadom(prefix, sender, "Nie jesteś w żadnej rozgrywce");
			arena.opuść(p);
			break;
		default:
			return staty(p, p.getName());
		}
		return true;
	}
	private boolean staty(CommandSender sender, String[] args) {
		if (args.length <= 2 && (!(sender instanceof Player)))
			return Func.powiadom(prefix, sender, "/" + args[0] + " staty <nick>");
		return staty(sender, args.length <= 2 ? sender.getName() : args[2]);
	}
	private boolean staty(CommandSender sender, String nick) {
		Player p = Bukkit.getPlayer(nick);
		if (p != null) {
			Statystyki staty = staty(p);
			if (staty != null)
				return staty(sender, p.getName(), staty);
		}
		
		Gracz g = Gracz.wczytaj(nick);
		return staty(sender, g.nick, (Statystyki) g.staty.get(Arena.class.getName()));
	} 
	private boolean staty(CommandSender sender, String nick, Statystyki staty) {
		return Func.powiadom(prefix, sender, "Staty %s\n\n%s",
				nick, staty == null ? nick + " §6Nigdy nie grał w " + getClass().getSimpleName() : staty.rozpisz());
	}
}





























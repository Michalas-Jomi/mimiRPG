package me.jomi.mimiRPG.Minigry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.PojedynczeKomendy.Antylog;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KolorRGB;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.Box;
import me.jomi.mimiRPG.util.Krotki.TriKrotka;
import me.jomi.mimiRPG.util.NowyEkwipunek;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

public abstract class Minigra implements Listener, Przeładowalny, Zegar  {
	public static abstract class Arena extends Mapowany {
		@Mapowane List<Integer> sekundyPowiadomien;
		@Mapowane int max_gracze = -1;
		@Mapowane int czasStartu = 60;
		@Mapowane int min_gracze = 2;
		@Mapowane Location zbiorka;
		@Mapowane int slotWPanelu = -1; // -1 oznacza auto przydzielanie slotu
		@Mapowane boolean rangingowe = true; // liczenie pkt

		Set<String> wszyscyGracze = Sets.newConcurrentHashSet();
		List<Player> gracze = Lists.newArrayList();
		String nazwa = "Minigra";
		boolean grane = false;
		int timer = -1;

		// abstract
		abstract Minigra getInstMinigra();
		abstract <M extends Minigra> void setInst(M inst);
		
		abstract int policzGotowych();
		
		
		// obsługa start / koniec
		void start() {
			timer = -1;
			grane = true;
			for (Player p : gracze) {
				p.getInventory().clear();
				if (rangingowe)
					getInstMinigra().staty(p).rozegraneAreny++;
				p.closeInventory();
				p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
				wszyscyGracze.add(p.getName());
			}
			
			odświeżWPanelu();
			
			Main.log(getInstMinigra().getPrefix() + Func.msg("Arena %s wsytartowała z graczami(%s) %s", nazwa, gracze.size(), gracze));
		}
		
		boolean dołącz(Player p) {
			if (p.hasMetadata(getInstMinigra().getMetaId()) || gracze.size() >= max_gracze || opuść(p))
				return false;
			Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
				Arena arena = getInstMinigra().arena(onlinePlayer);
				if (arena != null) {
					if (arena.equals(this)) {
						p.showPlayer(Main.plugin, onlinePlayer);
						onlinePlayer.showPlayer(Main.plugin, p);
					} else {
						p.hidePlayer(Main.plugin, onlinePlayer);
						onlinePlayer.hidePlayer(Main.plugin, p);
					}
				} else
					p.hidePlayer(Main.plugin, onlinePlayer);
			});
			Func.ustawMetadate(p, getInstMinigra().getMetaId(), this);
			NowyEkwipunek.dajNowy(p, zbiorka, GameMode.ADVENTURE);
			gracze.add(p);
			napiszGraczom("%s dołączył do poczekalni %s/%s", p.getDisplayName(), gracze.size(), strMaxGracze());

			Statystyki staty = Gracz.wczytaj(p.getName()).staty.getOrDefault(getInstMinigra().getClass().getName(), getInstMinigra().noweStaty().get());
			Func.ustawMetadate(p, getInstMinigra().getMetaStatystyki(), staty);
			
			Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			p.setScoreboard(scoreboard);
			Objective obj = scoreboard.registerNewObjective("staty", "dummy", "§6§lStatystyki");
			if (rangingowe) {
				obj.setDisplaySlot(DisplaySlot.SIDEBAR);
				staty.rozpisz(obj, getInstMinigra());
			}
			
			Antylog.włączBypass(p);
			
			sprawdzStart();
			
			Statystyki.Ranga ranga = getInstMinigra().staty(p).ranga(getInstMinigra());
			if (ranga != null)
				ranga.ubierz(p);
			
			odświeżWPanelu();
			
			return true;
		}
		
		final boolean opuść(Player p) {
			String nick = p.getName();
			for (int i=0; i<gracze.size(); i++)
				if (gracze.get(i).getName().equals(nick)) {
					opuść(p, i, true);
					return true;
				}
			return false;
		}
		void opuść(Player p, int i, boolean info) {
			gracze.remove(i);

			NowyEkwipunek.wczytajStary(p);
			
			Antylog.wyłączBypass(p);
			
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			
			Statystyki staty = getInstMinigra().staty(p);
			if (staty != null) {
				Gracz g = Gracz.wczytaj(p.getName());
				staty.sprawdzTopke(p, getInstMinigra());
				if (Main.chat != null && Main.ust.wczytajLubDomyślna("Minigry.CaveWars.ustawSuffixRange", false))
					Func.wykonajDlaNieNull(staty.ranga(getInstMinigra()), ranga -> Main.chat.setPlayerSuffix(null, p, ranga.toString()));
				g.staty.put(getInstMinigra().getClass().getName(), staty);
				g.zapisz();
			}
			
			p.removeMetadata(getInstMinigra().getMetaId(), Main.plugin);
			p.removeMetadata(getInstMinigra().getMetaStatystyki(), Main.plugin);
			
			if (info)
				if (!grane)
					napiszGraczom("%s opuścił pokój", p.getDisplayName());
				else {
					napiszGraczom("%s opuścił rozgrywkę", p.getDisplayName());
					sprawdzKoniec();
				}
			p.sendMessage(getInstMinigra().getPrefix() + "Nie jesteś już w Minigrze");
			
			if (timer != -1 && policzGotowych() < min_gracze) {
				timer = -1;
				Bukkit.broadcastMessage(getInstMinigra().getPrefix() + Func.msg("Wstrzymano odliczanie areny %s , z powodu małej ilości graczy %s/%s",
						nazwa, gracze.size(), strMaxGracze()));
			}
			
			odświeżWPanelu();
			
			if (!Main.pluginWyłączany) {
				gracze.forEach(graczAreny -> graczAreny.hidePlayer(Main.plugin, p));
				Bukkit.getOnlinePlayers().forEach(onlinePlayer -> p.showPlayer(Main.plugin, onlinePlayer));
			}
		}

		boolean wygrana(Player p) {
			if (!grane) return true;
			wszyscyGracze.remove(p.getName());
			Set<String> set = Sets.newConcurrentHashSet();
			for (String nick : wszyscyGracze) {
				Player gracz = Bukkit.getPlayer(nick);
				set.add(gracz == null ? "§c" + nick : gracz.getDisplayName());
			}
			Bukkit.broadcastMessage(getInstMinigra().getPrefix() + Func.msg("%s Wygrał na arenie %s z %s",
					p.getDisplayName(), nazwa, nazwa, Func.listToString(wszyscyGracze, 0, "§6, §e")));
			koniec();
			return true;
		}

		
		void koniec() {
			grane = false;
			while (!gracze.isEmpty())
				opuść(gracze.get(0), 0, false);
			try {
				for (Integer id : doAnulowania)
					Bukkit.getScheduler().cancelTask(id);
				doAnulowania.clear();
			} catch (IllegalPluginAccessException e) {}
			
			odświeżWPanelu();
		}
		private Set<Integer> doAnulowania = Sets.newConcurrentHashSet();
		void opóznijTask(int ticki, Runnable runnable) {
			Krotka<Integer, ?> k = new Krotka<>();
			try {
				doAnulowania.add(k.a = Func.opóznij(ticki, () -> {
					doAnulowania.remove(k.a);
					runnable.run();
				}));
			} catch (IllegalPluginAccessException e) {}
		}
		
		
		
		// util
		Object strMaxGracze() {
			return max_gracze <= 0 ? min_gracze + "+" : max_gracze;
		}
		void napiszGraczom(String msg, Object... uzupełnienia) {
			if (!msg.startsWith(getInstMinigra().getPrefix()))
				msg = getInstMinigra().getPrefix() + msg;
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
	
		boolean sprawdzKoniec() {
			if (gracze.size() == 1)
				return wygrana(gracze.get(0));
			return false;
		}

		void sprawdzStart() {
			if (timer == -1 && policzGotowych() >= min_gracze)
				timer = czasStartu;
		}
		
		ItemStack dajItemWPanelu() {
			return Func.stwórzItem(
					grane ? Material.RED_DYE : (timer == -1 ? Material.LIME_DYE : Material.YELLOW_DYE),
					nazwa,
					Math.max(1, gracze.size() > 64 ? 1 : gracze.size()),
					"",
					"&e" + gracze.size() + "/" + max_gracze,
					rangingowe ? "&6Rankingowe" : "&aNie rankingowe",
					"",
					grane ? "&cW trakcie gry" : (timer == -1 ? "&cWolna" : "&6Rozpoczyna się"));
		}
		void odświeżWPanelu() {
			getInstMinigra().holderAren.ustawItem(this);
		}
		
		// Override
		
		void czas() {
			if (timer == -1) return;
			
			if (max_gracze > 0 && policzGotowych() >= max_gracze)
				timer = Math.min(timer, 11);
			
			String czas = Func.czas(--timer);
			
			if (sekundyPowiadomien.contains(timer))
				Bukkit.broadcastMessage(getInstMinigra().getPrefix() + Func.msg("Arena %s wystartuje za %s (%s/%s)",
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
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Arena)
				return ((Arena) obj).nazwa.equals(nazwa);
			return false;
		}
	}
	public static abstract class Statystyki extends Mapowany {
		public static class Rangi extends Mapowany {
			@Mapowane public List<Ranga> rangi;
			
			public boolean rozpisz(CommandSender p, String tytuł) {
				
				p.sendMessage(" ");
				p.sendMessage("§9Rangi " + tytuł + ":");
				p.sendMessage(" ");
				for (Ranga ranga : rangi)
					p.sendMessage(ranga.toString() + "§8: §e" + Func.IntToString(ranga.potrzebnePunkty) + "pkt");
				p.sendMessage(" ");
				return true;
			}
		}
		public static class Ranga extends Mapowany {
			@Mapowane public KolorRGB kolor = new KolorRGB();
			@Mapowane public int potrzebnePunkty;
			@Mapowane public String nazwa;
			
			void ubierz(Player p) {
				ItemStack item = Func.stwórzItem(Material.LEATHER_CHESTPLATE, this.toString());
				Func.pokolorujZbroje(item, kolor.kolor());
				p.getEquipment().setChestplate(item);
			}
			
			@Override
			public String toString() {
				return kolor.kolorChat() + nazwa;
			}
		}
		
		@Mapowane int rozegraneAreny;
		@Mapowane int przegraneAreny;
		@Mapowane int wygraneAreny;
		
		private int licz_linie;
		public void rozpisz(Objective obj, Minigra minigra) {
			licz_linie = -1;
			
			rozpiska(str -> obj.getScore(str).setScore(licz_linie--), true, minigra);
		}
		public String rozpisz(Minigra minigra) {
			StringBuilder s = new StringBuilder();

			s.append('\n');
			
			rozpiska(str -> s.append(str).append('\n'), false, minigra);
			
			s.append('\n');
			
			return s.toString();
		}
		
		static String _rozpiska(String info, Object stan) {
			return "§6" + info + "§8: §e" + stan;
		}
		static String _rozpiska(int liczone, int wszystkie) {
			if (wszystkie == 0) return "100%";
			return ((int) ( ((double) liczone) / ((double) wszystkie) * 100 ) ) + "%";
		}
		void rozpiska(Consumer<String> cons, boolean usuwaćKolor, Minigra minigra) {
			cons.accept(_rozpiska("win ratio", _rozpiska(wygraneAreny, rozegraneAreny)));
			cons.accept(_rozpiska("Rozegrane gry", rozegraneAreny));
			cons.accept(_rozpiska("Wygrane gry", wygraneAreny));
			cons.accept(_rozpiska("Przegrane gry", przegraneAreny));
			cons.accept(" ");
			
		}
	
		public int policzPunkty(Minigra minigra) {
			Box<Integer> punkty = new Box<>(0);

			Func.dajFields(getClass()).forEach(field -> {
				if (field.isAnnotationPresent(Mapowane.class)) {
					try {
						punkty.a += field.getInt(this) * Main.ust.wczytajLubDomyślna("Minigry." + minigra.getClass().getSimpleName() + ".punktacja." + field.getName(), 0);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
			
			return punkty.a;
		}
		
		public Ranga ranga(Minigra minigra) {
			return ranga(policzPunkty(minigra), minigra);
		}
		public static Ranga ranga(int pkt, Minigra minigra) {
			Ranga ranga = null;
			
			for (Ranga _ranga : minigra.rangi.rangi)
				if (_ranga.potrzebnePunkty <= pkt && (ranga == null || ranga.potrzebnePunkty < _ranga.potrzebnePunkty))
					ranga = _ranga;
			
			return ranga;
		}
		protected static void użyjRangi(Ranga ranga, boolean usuwaćKolor, Consumer<String> cons) {
			if (ranga != null) {
				String _ranga = usuwaćKolor ? Func.usuńKolor(ranga.toString()) : ranga.toString();
				Func.multiTry(IllegalArgumentException.class,
					() -> cons.accept(_rozpiska("Ranga", _ranga)),
					() -> cons.accept(_ranga),
					() -> cons.accept(_rozpiska("Ranga", Func.inicjały(_ranga))),
					() -> cons.accept(Func.inicjały(_ranga)),
					() -> cons.accept("    ")
					);
			}
		}
		
		
		private boolean wymaganyZapis; // zmienna pod metode
		final void sprawdzTopke(Player p, Minigra minigra) {
			wymaganyZapis = false;
			minigra.topki.forEach((pole, topka) -> {
				int akt = 0;
				try {
					akt = pole.equals("punkty") ? policzPunkty(minigra) : Func.dajField(this.getClass(), pole).getInt(this);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				String format = Main.ust.wczytajLubDomyślna("Minigry.CaveWars.format.wyświetlany nick", "%displayname%");
				format = format.replace("%nick%", p.getName());
				format = format.replace("%displayname%", p.getDisplayName());
				if (Main.chat != null) {
					format = format.replace("%prefix%", Func.koloruj(Main.chat.getPlayerPrefix(p)));
					format = format.replace("%suffix%", Func.koloruj(Main.chat.getPlayerSuffix(p)));
				}
				TriKrotka<String, Integer, String> krotka = new TriKrotka<>(p.getName(), akt, Func.koloruj(format));
				int index = Func.insort(krotka, topka, k -> (double) -k.b);
				if (index >= 10) {
					topka.remove(index);
					return;
				}
				
				boolean był = false;
				for (int i=0; i < topka.size(); i++)
					if (topka.get(i).a.equals(p.getName()))
						if (był) {
							topka.remove(i);
							break;
						} else
							był = true;
				
				if (był)
					minigra.postawHologram(pole, topka);
				
				wymaganyZapis = wymaganyZapis || był;
				
				while (topka.size() > 10)
					topka.remove(10);
			});
			
			if (wymaganyZapis)
				minigra.zapiszTopki();
		}
	}
	
	
	static Set<String> dozwoloneKomendy = Sets.newConcurrentHashSet();
	static Config configDane = new Config("configi/minigry/Dane");
	
	HashMap<String, Arena> mapaAren = new HashMap<String, Arena>();
	
	Statystyki.Rangi rangi;
	
	/*
	 * Założenia:
	 * nie da sie utracić wartości (pkt/kille/?)
	 * topki to posortowane malejąco listy
	 * 
	 * 
	 * ///
	 * - topX
	 * - <nick> <pkt/kille/?>
	 * ///
	 * 
	 *   punkty:
	 *   - top1 pkt displayname
	 *   - top2 pkt displayname
	 *   ...
	 *   - top10 pkt displayname
	 *   kille:
	 *   - top1 killi displayname
	 *   - top2 killi displayname
	 *   ...
	 *   - top10 killi displayname
	 * 
	 * 
	 * 
	 */
	
	// Topki
	private Config configTopki = new Config("configi/minigry/topki/" + this.getClass().getSimpleName());
	// {statystyka: [(nick, wartość, displayname)]}
	public final Map<String, List<TriKrotka<String, Integer, String>>> topki = new HashMap<>();
	public void wczytajTopki() {
		configTopki.przeładuj();
		topki.clear();
		Consumer<String> wczytaj = pole -> {
			List<TriKrotka<String, Integer, String>> topka = new ArrayList<>();
			configTopki.wczytajListe(pole).forEach(str -> {
				List<String> części = Func.tnij(str, " ");
				String nick = części.get(0);
				int wartość = Func.Int(części.get(1));
				String displayName = Func.listToString(części, 2);
				topka.add(new TriKrotka<>(nick, wartość, displayName));
				postawHologram(pole, topka);
			});
			topki.put(pole, topka);
		};
		Func.dajFields(noweStaty().get().getClass()).forEach(field -> {
			if (field.isAnnotationPresent(Mapowane.class))
				wczytaj.accept(field.getName());
		});
		wczytaj.accept("punkty");
	}
	public void zapiszTopki() {
		topki.forEach((klucz, topka) -> {
			List<String> lista = new ArrayList<>();
			topka.forEach(krotka -> 
				lista.add(new StringBuilder().append(krotka.a).append(' ').append(krotka.b).append(' ').append(krotka.c).toString()));
			configTopki.ustaw(klucz, lista);
		});
		configTopki.zapisz();
	}
	static final double odstępMiędzyLiniamiHologramu = .25;
	public void postawHologram(String pole, List<TriKrotka<String, Integer, String>> topka) {
		Func.wykonajDlaNieNull(Main.ust.wczytajStr("Minigry." + this.getClass().getSimpleName() + ".hologramy." + pole + ".loc"), strLoc -> {
			List<String> części = Func.tnij(strLoc, " ");
			Location loc = new Location(Bukkit.getWorld(części.get(0)),Func.Double(części.get(1)), Func.Double(części.get(2)), Func.Double(części.get(3)));
			this.postawHologram(loc, topka, "&9" + this.getClass().getSimpleName() + " Top " +
					Main.ust.wczytajLubDomyślna("Minigry." + this.getClass().getSimpleName() + ".hologramy." + pole + ".nazwa", pole));
		});
	}
	private void postawHologram(Location loc, List<TriKrotka<String, Integer, String>> topka, String tytuł) {
		String tag = "mimiHoloTopMinigry" + this.getClass().getSimpleName() + tytuł;
		for (Entity entity : loc.getChunk().getEntities())
			if (entity.getScoreboardTags().contains(tag))
				entity.remove();
		postawLinie(loc, Func.koloruj(tytuł), tag);
		topka.forEach(krotka -> {
			loc.add(0, -odstępMiędzyLiniamiHologramu, 0);
			postawLinie(loc, krotka.c + " " + krotka.b, tag);
		});
	}
	private void postawLinie(Location loc, String text, String tag) {
		ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		armorStand.setSmall(true);
		armorStand.setGravity(false);
		armorStand.setVisible(false);
		armorStand.setBasePlate(false);
		armorStand.setCustomName(text);
		armorStand.addScoreboardTag(tag);
		armorStand.setInvulnerable(true);
		armorStand.setCustomNameVisible(true);
		for (EquipmentSlot slot : EquipmentSlot.values())
			armorStand.addEquipmentLock(slot, LockType.ADDING_OR_CHANGING);
	}
	
	private Config configAreny = new Config("configi/minigry/areny/" + this.getClass().getSimpleName());
	public Config getConfigAreny() {
		return configAreny;
	}
	// abstract
	abstract String getPrefix();
	abstract String getMetaStatystyki();
	abstract String getMetaId();
	
	abstract Supplier<? extends Statystyki> noweStaty();
	
	
	
	public Minigra() {
		Minigry.mapaGier.put(this.getClass().getSimpleName().toLowerCase(), this);
	}


	Arena zaczynanaArena(List<String> patterny) {
		if (mapaAren.isEmpty())
			return null;
		
		Arena najArena = null;
		int najGracze = -1;
		
		for (Arena arena : mapaAren.values())
			if (!arena.grane)
				if (patterny.isEmpty()) {
					if (arena.gracze.size() < arena.max_gracze && arena.gracze.size() > najGracze) {
						najGracze = arena.gracze.size();
						najArena = arena;
						break;
					}
				} else
					for (String pattern : patterny)
						if (Pattern.compile(pattern).matcher(arena.nazwa).matches() && arena.gracze.size() < arena.max_gracze && arena.gracze.size() > najGracze) {
							najGracze = arena.gracze.size();
							najArena = arena;
							break;
						}
		
		return najArena;
	}


	// util
	<A extends Arena> 	   A arena(Entity p) { return metadata(p, getMetaId()); }
	<S extends Statystyki> S staty(Entity p) { return metadata(p, getMetaStatystyki()); }
	@SuppressWarnings("unchecked")
	static <T> T metadata(Entity p, String meta) {
		if (p == null || !p.hasMetadata(meta))
			return null;
		return (T) p.getMetadata(meta).get(0).value();	
	}
	
	
	// onDisable
	public static void wyłącz() {
		wyłącz("Wyłączanie pluginu");
	}
	static void wyłącz(String msg) {
		for (Minigra minigra : Minigry.mapaGier.values()) {
			for (Arena arena : minigra.mapaAren.values())
				if (arena.grane) {
					arena.napiszGraczom(msg);
					arena.koniec();
				} else if (!arena.gracze.isEmpty()) {
					arena.napiszGraczom(msg);
					arena.koniec();
					
				}
		}
	}
	
	
	class Holder extends Func.abstractHolder {
		public Holder(int rzędy, String nazwaMinigry, Iterable<Arena> areny) {
			super(rzędy, "&4&lAreny &1&l" + nazwaMinigry);
			Func.ustawPuste(inv);
			areny.forEach(this::ustawItem);
		}
		
		final HashMap<Integer, Arena> mapaAren = new HashMap<>();
		
		public void ustawItem(Arena arena) {
			inv.setItem(arena.slotWPanelu, arena.dajItemWPanelu());
			mapaAren.put(arena.slotWPanelu, arena);
		}
	}
	Holder holderAren;
	
	
	// EventHandler
	@EventHandler
	public void dołączanieDoGry(PlayerJoinEvent ev) {
		Bukkit.getOnlinePlayers().forEach(p ->
			Func.wykonajDlaNieNull(arena(p), arena ->
				p.hidePlayer(Main.plugin, ev.getPlayer())));
	}
	@EventHandler
	public void opuszczanieGry(PlayerQuitEvent ev) {
		Bukkit.getOnlinePlayers().forEach(p -> {
			ev.getPlayer().showPlayer(Main.plugin, p);
			p.showPlayer(Main.plugin, ev.getPlayer());
		});
	}
	
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		if (ev.getMessage().toLowerCase().startsWith("/opuśćminigre"))
			return;
		Player p = ev.getPlayer();
		if (p.hasMetadata(getMetaId())) {
			if (dozwoloneKomendy.contains(Func.tnij(ev.getMessage(), " ").get(0)))
				return;
			else if (p.hasPermission(Minigry.permCmdBypass))
				p.sendMessage(getPrefix() + "pamiętaj że jesteś w trakcie minigry");
			else {
				ev.setCancelled(true);
				p.sendMessage(getPrefix() + "Nie wolno tu uzywać komend");
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void śmierć(PlayerDeathEvent ev) {
		if (!ev.getKeepInventory()) return;
		Func.wykonajDlaNieNull(arena(ev.getEntity()), a -> ev.setKeepInventory(false));
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		if (!Main.pluginWyłączany) {
			Arena arena = arena(ev.getPlayer());
			Func.wykonajDlaNieNull(arena, a -> a.opuść(ev.getPlayer()));
		}
	}
	
	@EventHandler
	public void klikaniePaneluWyboruAreny(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class,  holder -> {
			ev.setCancelled(true);
			Func.wykonajDlaNieNull(holder.mapaAren.get(ev.getRawSlot()), arena -> {
				if (!arena.grane && !arena.pełna())
					arena.dołącz((Player) ev.getWhoClicked());
			});
		});
	}
	
	// Override
	@Override
	public int czas() {
		mapaAren.values().forEach(Arena::czas);// TODO sprawdzić czy nic sie nie posypie
		return 20;
	}
	
	@Override
	public void przeładuj() {
		configDane.przeładuj();
		wczytajTopki();
		
		rangi = new Config("configi/minigry/Rangi").wczytajLubDomyślna(this.getClass().getSimpleName(), () -> Func.utwórz(Statystyki.Rangi.class));

		wyłącz("Przeładowywanie pluginu");
		
		dozwoloneKomendy.clear();
		for (String komenda : Main.ust.wczytajListe("Minigry.Dozwolone komendy"))
			dozwoloneKomendy.add(komenda.startsWith("/") ? komenda : "/" + komenda);

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
		
		
		boolean[] zajęte = new boolean[6*9];
		List<Arena> autoAreny = Lists.newArrayList();
		for (Arena arena : mapaAren.values())
			if (arena.slotWPanelu == -1)
				autoAreny.add(arena);
			else if (zajęte[arena.slotWPanelu]) {
				Main.warn("Podwojono slot areny " + arena.slotWPanelu + " " + this.getClass().getSimpleName() + " " + arena.nazwa + ", autoprzydzielanie slotu w panelu");
				autoAreny.add(arena);
			} else
				zajęte[arena.slotWPanelu] = true;
		for (int i = 0; i < zajęte.length; i++)
			if (autoAreny.isEmpty())
				break;
			else if (!zajęte[i]) {
				autoAreny.remove(0).slotWPanelu = i;
				zajęte[i] = true;
			}
		int mxSlot = 0;
		for (int i=zajęte.length - 1; i > 0; i--)
			if (zajęte[i]) {
				mxSlot = i;
				break;
			}
		if (holderAren != null)
			Lists.newArrayList(holderAren.getInventory().getViewers()).forEach(HumanEntity::closeInventory);
		holderAren = new Holder(Func.potrzebneRzędy(mxSlot), this.getClass().getSimpleName(), mapaAren.values());
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane areny " + this.getClass().getSimpleName(), mapaAren.size());
	}
	
	boolean onCommand(CommandSender sender, String[] args) {
		if (args.length < 2) {
			if (sender instanceof Player)
				((Player) sender).openInventory(holderAren.getInventory());
			else
				return false;
			return true;
		}
		
		if (args[1].equalsIgnoreCase("staty"))
			return staty(sender, args);
		
		switch (args[1].toLowerCase()) {
		case "r":
		case "rangi":
		case "stopnie":
			return rangi.rozpisz(sender, getClass().getSimpleName());
		case "s":
		case "staty":
			return staty(sender, args);
		case "t":
		case "top":
		case "topka":
		case "najlepsi":
			if (args.length >= 3)
				return top(sender, args[2]);
			return top(sender, "punkty");
		}
				
		if (!(sender instanceof Player))
			return Func.powiadom(getPrefix(), sender, "Ta minigra jest tylko dla graczy");
		Player p = (Player) sender;
		
		Arena arena;
		
		switch (Func.odpolszcz(args[1])) {
		case "dolacz":
			List<String> patterny = Lists.newArrayList();
			for (int i=2; i < args.length; i++)// TODO jakić TabCompleter np z nazwami aren
				patterny.add(args[i]);
			
			arena = zaczynanaArena(patterny);
			if (arena == null)
				return Func.powiadom(getPrefix(), sender, "Aktualnie nie ma żadnych wolnych aren");
			if (arena.pełna())
				return Func.powiadom(getPrefix(), sender, "Brak miejsc w poczekalni");
			arena.dołącz(p);
			break;
		case "opusc":
			arena = arena(p);
			if (arena == null)
				return Func.powiadom(getPrefix(), sender, "Nie jesteś w żadnej rozgrywce");
			arena.opuść(p);
			break;
		default:
			return staty(p, p.getName());
		}
		return true;
	}
	protected boolean staty(CommandSender sender, String[] args) {
		if (args.length <= 2 && (!(sender instanceof Player)))
			return Func.powiadom(getPrefix(), sender, "/" + args[0] + " staty <nick>");
		return staty(sender, args.length <= 2 ? sender.getName() : args[2]);
	}
	protected boolean staty(CommandSender sender, String nick) {
		Player p = Bukkit.getPlayer(nick);
		if (p != null) {
			Statystyki staty = staty(p);
			if (staty != null)
				return staty(sender, p.getName(), staty);
		}
		
		Gracz g = Gracz.wczytaj(nick);
		return staty(sender, g.nick, g.staty.get(this.getClass().getName()));
	} 
	private boolean staty(CommandSender sender, String nick, Statystyki staty) {
		return Func.powiadom(getPrefix(), sender, "Staty %s\n\n%s",
				nick, staty == null ? nick + " §6Nigdy nie grał w " + this.getClass().getSimpleName() : staty.rozpisz(this));
	}
	
	protected boolean top(CommandSender sender, String pole) {
		Func.wykonajDlaNieNull(topki.get(pole), topka -> {
			
		}, () -> sender.sendMessage(getPrefix() + Func.msg("Niepoprawna Kategoria: %s", pole)));
		return true;
	}
}


package me.jomi.mimiRPG.Minigry;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Cooldown;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.KolorRGB;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Krotka;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.NowyEkwipunek;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import me.jomi.mimiRPG.Gracze.Gracz;
import me.jomi.mimiRPG.Minigry.Paintball.Statystyki.Rangi;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

// TODO personalizowany /powertool

@Moduł
public class Paintball extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Paintball");
	
	static final String metaStatystyki = "mimiPaintballStatystyki";
	static final String metaDrużynaId = "mimiPaintballDrużyna";
	static final String metaid = "mimiMinigra";
	
	static final String permCmdBypass = Func.permisja("minigra.paintball.bypasskomendy");
	
	static final Set<String> dozwoloneKomendy = Sets.newConcurrentHashSet();
	static List<Krotka<String, Integer>> topka = Lists.newArrayList();
	
	static final HashMap<String, Arena> mapaAren = new HashMap<String, Arena>();
	static final Config configTopki = new Config("configi/minigry/Topki"); // XXX ogólny dla minigier
	static final Config configAreny = new Config("configi/minigry/PaintballAreny");
	static final Config configRangi = new Config("configi/minigry/PaintballRangi");
	static Statystyki.Rangi rangi;
	
	static Arena zaczynanaArena;

	public Paintball() {
		super("paintball", null, "pb");
		Main.dodajPermisje(permCmdBypass);
	}
	
	public static class Arena extends Mapowany {
		static final ItemStack itemKask = Func.dajGłówkę("&bKask Paintballowca", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzN2VhZGM1M2MzOWI5Njg4Y2MzOTY1NWQxYjc4ZmQ2MTJjMWNkNjI1YzJhODk2MzhjNWUyNzIxNmM2ZTRkIn19fQ==", null);
		static final ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l §e§o");
		static final ItemStack itemWybórDrużyny = Func.stwórzItem(Material.COMPASS, "§9Wybierz Drużynę");
		static final ItemStack itemŚnieżka = Func.stwórzItem(Material.SNOWBALL, 1, "§9Śnieżka");
		
		static final String nazwaInv = "§1§lWybierz drużynę";
		private Inventory inv;
		private final Cooldown cooldownWyboruDrużyny = new Cooldown(5);
		
		List<Player> gracze = Lists.newArrayList();
		@Mapowane List<Integer> sekundyPowiadomien;
		@Mapowane List<Drużyna> druzyny;
		@Mapowane int max_gracze = -1;
		@Mapowane int czasStartu = 60;
		@Mapowane int min_gracze = 2;
		@Mapowane Location zbiorka;
		boolean grane = false;
		int punktyPotrzebne;
		String nazwa;
		
		void start() {
			timer = -1;
			grane = true;
			zaczynanaArena = null;
			punktyPotrzebne = gracze.size() * 5;
			for (Player p : gracze) {
				p.getInventory().clear();
				p.closeInventory();
				ubierz(p);
				respawn(p);
				staty(p).rozegraneAreny++;
				p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
			}
			cooldownWyboruDrużyny.wyczyść();
		}
		
		// Wykonywane co sekunde dla "zaczynanaArena"
		int timer = -1;
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
		
		Object strMaxGracze() {
			return max_gracze <= 0 ? min_gracze + "+" : max_gracze;
		}
		
		void dołącz(Player p) {
			if (opuść(p)) return;
			Func.ustawMetadate(p, metaid, this);
			NowyEkwipunek.dajNowy(p, zbiorka, GameMode.ADVENTURE);
			gracze.add(p);
			p.getInventory().setItem(4, itemWybórDrużyny);
			napiszGraczom("%s dołączył do poczekalni %s/%s", p.getDisplayName(), gracze.size(), strMaxGracze());

			Statystyki staty = Gracz.wczytaj(p.getName()).statypb;
			if (staty == null)
				staty = new Statystyki();
			Func.ustawMetadate(p, metaStatystyki, staty);
		
			Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			p.setScoreboard(scoreboard);
			Objective obj = scoreboard.registerNewObjective("pbstaty", "dummy", "§6§lStatystyki");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			staty.rozpisz(obj);
			p.setCustomNameVisible(false);
			
			Statystyki.Ranga ranga = staty.ranga();
			if (ranga != null)
				ranga.ubierz(p);
			
		}
		boolean opuść(Player p) {
			String nick = p.getName();
			for (int i=0; i<gracze.size(); i++)
				if (gracze.get(i).getName().equals(nick)) {
					opuść(i, true);
					return true;
				}
			return false;
		}
		void opuść(int i, boolean info) {
			Player p = gracze.remove(i);
			
			Statystyki staty = staty(p);
			if (staty != null) {
				Gracz g = Gracz.wczytaj(p.getName());
				staty.sprawdzTopke(p);
				g.statypb = staty;
				g.zapisz();
			}
			
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
			
			NowyEkwipunek.wczytajStary(p);
			
			Drużyna drużyna = drużyna(p);
			if (drużyna != null)
				drużyna.gracze--;
			
			p.removeMetadata(metaid, Main.plugin);
			p.removeMetadata(metaDrużynaId, Main.plugin);
			p.removeMetadata(metaStatystyki, Main.plugin);
			
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
		}
		boolean sprawdzKoniec() {
			Drużyna grająca = null;
			for (Player p : gracze) {
				Drużyna drużyna = drużyna(p);
				if (grająca == null)
					grająca = drużyna;
				else if (!grająca.equals(drużyna))
					return false;
			}
			return wygrana(grająca);
		}
		boolean sprawdzKoniec(Drużyna drużyna) {
			if (drużyna.punkty >= punktyPotrzebne)
				return wygrana(drużyna);
			return false;
		}
		
		boolean wygrana(Drużyna drużyna) {
			if (!grane) return true;
			for (Player p : gracze)
				if (drużyna.equals(drużyna(p)))
					staty(p).wygraneAreny++;
				else
					staty(p).przegraneAreny++;
			
			
			StringBuilder wygrani 	= new StringBuilder();
			StringBuilder przegrani = new StringBuilder();
			for (Player p : gracze) {
				Drużyna dp = drużyna(p);
				StringBuilder s = drużyna.equals(dp) ? wygrani : przegrani;
				s.append(' ').append(dp.napisy).append(p.getName());
			}
			
			Bukkit.broadcastMessage(prefix + Func.msg("Drużyna %s(%s) wygrała na arenie %s (z%s)",
					drużyna, wygrani.substring(1), nazwa, przegrani));
			koniec();
			return true;
		}
		void koniec() {
			grane = false;
			while (!gracze.isEmpty())
				opuść(0, false);
		
			for (Drużyna druzyna : druzyny)
				druzyna.punkty = 0;
		}
		
		void wybierzDrużyne(Player p, Drużyna drużyna) {
			if (!cooldownWyboruDrużyny.minął(p.getName())) {
				p.sendMessage(prefix + "Poczekaj chwile zanim zmienisz drużyne");
				return;
			}
			
			Drużyna stara = drużyna(p);
			if (stara != null) {
				if (stara.equals(drużyna)) 
					return;
				stara.gracze--;
			}
			
			cooldownWyboruDrużyny.ustaw(p.getName());
			
			Func.ustawMetadate(p, metaDrużynaId, drużyna);
			drużyna.gracze++;

			if (timer == -1 && policzGotowych() >= min_gracze)
				timer = czasStartu;
			if (timer != -1 && sprawdzKoniec())
				timer = -1;
			
			ubierz(p, drużyna);
			
			napiszGraczom("%s dołącza do drużyny %s", p.getDisplayName(), drużyna);
		}
		void ubierz(Player p) {
			ubierz(p, drużyna(p));
		}
		void ubierz(Player p, Drużyna drużyna) {
			Color kolor = drużyna.kolor;
			Function<Material, ItemStack> dajItem = mat -> 
					Func.pokolorujZbroje(Func.stwórzItem(mat, " "), kolor);
			PlayerInventory inv = p.getInventory();
			inv.setHelmet(itemKask);
			inv.setChestplate(dajItem.apply(Material.LEATHER_CHESTPLATE));
			inv.setLeggings(dajItem.apply(Material.LEATHER_LEGGINGS));
			inv.setBoots(dajItem.apply(Material.LEATHER_BOOTS));
		}
		
		Location respawn(Player p) {
			return respawn(p, drużyna(p));
		}
		Location respawn(Player p, Drużyna drużyna) {
			if (!grane) {
				p.teleport(zbiorka);
				return zbiorka;
			}
			
			dajŚnieżke(p);
			
			Location loc = Func.losuj(drużyna.respawny);
			p.teleport(loc);
			return loc;
		}

		int policzGotowych() {
			int w = 0;
			for (Drużyna drużyna : druzyny)
				w += drużyna.gracze;
			return w;
		}
		
		static void dajŚnieżke(Player p) {
			ItemStack item = p.getInventory().getItem(4);
			if (item == null || item.getType().equals(Material.SNOWBALL))
				p.getInventory().setItem(4, itemŚnieżka);
		}
		
		Drużyna znajdzDrużyne(String nazwa) {
			for (Drużyna drużyna : druzyny) {
				if (drużyna.toString().equals(nazwa))
					return drużyna;
			}
			return null;
		}
		
		void napiszGraczom(String msg, Object... uzupełnienia) {
			if (!msg.startsWith(prefix))
				msg = prefix + msg;
			msg = Func.msg(msg, uzupełnienia);
			for (Player p : gracze)
				p.sendMessage(msg);
		}
		
		public void trafienie(ProjectileHitEvent ev) {
			Player trafiony  = (Player) ev.getHitEntity();
			Player rzucający = (Player) ev.getEntity().getShooter();
			Drużyna dr;
			Drużyna dt;
			if (!grane || rzucający == null || // rzut w poczekalni lub rzucający nie istnieje
					!this.equals(arena(rzucający)) || // należą do innych aren
					(dt = drużyna(trafiony)).equals(dr = drużyna(rzucający))) return; // należą do tej samej drużyny 
			respawn(trafiony, dt);
			
			staty(trafiony).śmierci++;
			staty(rzucający).kille++;
			
			napiszGraczom(dr.napisy + rzucający.getName() + "§6 postrzelił " +
						  dt.napisy + trafiony.getName()  + " §e" + ++dr.punkty + "§6/§e" + punktyPotrzebne);
			sprawdzKoniec(dr);
		}
		
		Inventory dajInv() {
			if (inv == null)
				stwórzInv();
			return inv;
		}
		void odświeżInv() {
			for (Drużyna drużyna : druzyny)
				odświeżInv(drużyna);
		}
		void odświeżInv(Drużyna drużyna) {	
			dajInv().getItem(drużyna.slotInv).setAmount(drużyna.gracze);
		}
		private void stwórzInv() {
			Set<Integer> sloty = dajSloty();
			inv = Bukkit.createInventory(null, Math.min((Func.max(sloty) / 9 + 2)*9, 6*9), nazwaInv);
			
			for (int i=0; i<inv.getSize(); i++)
				inv.setItem(i, pustySlot);
			
			int i=0;
			for (int slot : sloty) {
				Drużyna drużyna = druzyny.get(i++);
				ItemStack item = Func.stwórzItem(Material.LEATHER_CHESTPLATE, drużyna.toString(), "§bKliknij aby Dołączyć");
				inv.setItem(slot, Func.pokolorujZbroje(item, drużyna.kolor));
				drużyna.slotInv = slot;
			}
		}
		private Set<Integer> dajSloty() {
			Set<Integer> sloty = Sets.newConcurrentHashSet();
			int pozostałe = druzyny.size();
			int poziom = 1;
			if (pozostałe <= 3*3+4)
				while (pozostałe > 0) {
					int x = poziom++ * 9;
					if (pozostałe > 4) {
						Func.dodajWszystkie(sloty, 2+x, 4+x, 6+x);
						pozostałe -= 3;
						continue;
					}
					switch (pozostałe) {
					case 1: Func.dodajWszystkie(sloty, 4+x); break;
					case 2: Func.dodajWszystkie(sloty, 3+x, 5+x); break;
					case 3: Func.dodajWszystkie(sloty, 2+x, 4+x, 6+x); break;
					case 4: Func.dodajWszystkie(sloty, 1+x, 3+x, 5+x, 7+x); break;
					}
					break;
				}
			else if (pozostałe <= 5*7)
				for (int i=10; i<5*9; i++) {
					if ((i+1) % 9 == 0) continue;
					sloty.add(i);
					if (--pozostałe <= 0)
						break;
				}
			else
				while (pozostałe-->0)
					sloty.add(pozostałe);
			
			return sloty;
		}
		
		boolean pełna() {
			return max_gracze > 0 && gracze.size() >= max_gracze;
		}		
		boolean poprawna() {
			for (Drużyna druzyna : druzyny)
				if (druzyna.respawny.isEmpty())
					return false;
			return nazwa != null && !nazwa.isEmpty() &&
					zbiorka != null &&
					(max_gracze < 0 || max_gracze >= min_gracze) &&
					druzyny.size() >= 2;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Arena)
				return ((Arena) obj).nazwa.equals(nazwa);
			return false;
		}
	}
	
	public static class Drużyna extends Mapowany {
		@Mapowane KolorRGB kolorRGB = new KolorRGB();
		@Mapowane List<Location> respawny;
		@Mapowane String nazwa;
		
		int punkty;
		
		int gracze;
		
		int slotInv = -1;
		
		Color kolor;
		String napisy;
		
		public void Init() {
			this.kolor 	= kolorRGB.kolor();
			this.napisy = kolorRGB.kolorChat();	
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Drużyna)
				return nazwa.equals(((Drużyna) obj).nazwa);
			return false;
		}
	
		public String toString() {
			return napisy + nazwa;
		}
	}
	
	public static class Statystyki extends Mapowany {
		public static class Rangi extends Mapowany {
			@Mapowane public List<Ranga> rangi;
			
			public boolean rozpisz(CommandSender p) {
				
				p.sendMessage(" ");
				p.sendMessage("§9Rangi Paintaballa:");
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
			
			public String toString() {
				return kolor.kolorChat() + nazwa;
			}
		}		
		
		@Mapowane int rozegraneAreny;
		@Mapowane int przegraneAreny;
		@Mapowane int wygraneAreny;
		@Mapowane int kille;
		@Mapowane int śmierci;
		@Mapowane int rzucone;
		
		private int punkty;
		
		public int policzPunkty() {
			punkty = 0;

			Consumer<String> consumer = (sc) -> {
				try {
					Field field = this.getClass().getDeclaredField(sc);
					field.setAccessible(true);
					punkty += ((int) field.get(this)) * configRangi.wczytajLubDomyślna("punktacja." + sc, 0);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			};
			
			consumer.accept("rozegraneAreny");
			consumer.accept("przegraneAreny");
			consumer.accept("wygraneAreny");
			consumer.accept("rzucone");
			consumer.accept("śmierci");
			consumer.accept("kille");
			
			return punkty;
		}
		
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
		
		private void rozpiska(Consumer<String> cons, boolean usuwaćKolor) {
			BiFunction<String, Object, String> bic = (info, stan) -> "§6" + info + "§8: §e" + stan;
			
			BiFunction<Integer, Integer, String> bif = (liczone, wszystkie) -> {
				if (wszystkie == 0) return "100%";
				return ((int) ( ((double) liczone) / ((double) wszystkie) * 100 ) ) + "%";
			};

			cons.accept(bic.apply("win ratio", bif.apply(wygraneAreny, rozegraneAreny)));
			cons.accept(bic.apply("Rozegrane gry", rozegraneAreny));
			cons.accept(bic.apply("Wygrane gry", wygraneAreny));
			cons.accept(bic.apply("Przegrane gry", przegraneAreny));
			cons.accept(" ");
			cons.accept(bic.apply("kill ratio", bif.apply(kille, kille + śmierci)));
			cons.accept(bic.apply("Kille", kille));
			cons.accept(bic.apply("Zgony", śmierci));
			cons.accept("  ");
			cons.accept(bic.apply("Rzucone śnieżki", rzucone));
			cons.accept("   ");
			int punkty = policzPunkty();
			cons.accept(bic.apply("Punkty", punkty));
			Ranga ranga = ranga(punkty);
			
			if (ranga != null) {
				String _ranga = usuwaćKolor ? Func.usuńKolor(ranga.toString()) : ranga.toString();
				Func.multiTry(IllegalArgumentException.class,
					() -> cons.accept(bic.apply("Ranga", _ranga)),
					() -> cons.accept(_ranga),
					() -> cons.accept(bic.apply("Ranga", Func.inicjały(_ranga))),
					() -> cons.accept(Func.inicjały(_ranga)),
					() -> cons.accept("    ")
					);
			}
			
		}
		
		void sprawdzTopke(Player p) {
			int pkt = policzPunkty();
			
			int i = -1;
			while (++i < topka.size()) {
				if (i >= 10) return;
				Krotka<String, Integer> k = topka.get(i);
				if (k.a.equals(p.getName())) return;
				if (pkt > k.b) break;
			}
		
			topka.add(i, new Krotka<>(p.getName(), pkt));
			
			while(++i < topka.size())
				if (topka.get(i).a.equals(p.getName())) {
					topka.remove(i);
					break;
				}
			
			if (topka.size() > 10)	
				topka.remove(10);
			
			configTopki.ustaw_zapisz("Paintball", topka);
		}
		
		Ranga ranga() {
			return ranga(policzPunkty());
		}
		Ranga ranga(int pkt) {
			Ranga ranga = null;
			
			for (Ranga _ranga : rangi.rangi)
				if (_ranga.potrzebnePunkty <= pkt && (ranga == null || ranga.potrzebnePunkty < _ranga.potrzebnePunkty))
					ranga = _ranga;
			
			return ranga;
		}
	}
	
	static Arena arena(Entity p)			{ return metadata(p, metaid); }
	static Drużyna drużyna(Player p)		{ return metadata(p, metaDrużynaId); }
	static Statystyki staty(Player p)		{ return metadata(p, metaStatystyki); }
	@SuppressWarnings("unchecked")
	private static <T> T metadata(Entity p, String meta) {
		if (p == null || !p.hasMetadata(meta)) return null;
		return (T) p.getMetadata(meta).get(0).value();	
	}

	@EventHandler
	public void rzucanie(ProjectileLaunchEvent ev) {
		ProjectileSource shooter = ev.getEntity().getShooter();
		if (shooter instanceof Player) {
			Player p = (Player) shooter;
			Statystyki stat = staty(p);
			if (stat != null) {
				stat.rzucone++;
				Func.opóznij(1, () -> Arena.dajŚnieżke(p));
			}
		}
	}
	@EventHandler
 	public void trafienie(ProjectileHitEvent ev) {
		Arena arena = arena(ev.getHitEntity());
		if (arena != null)
			arena.trafienie(ev);
	}
	@EventHandler
	public void śmieć(PlayerDeathEvent ev) {
		if (arena(ev.getEntity()) != null)
			ev.setKeepInventory(true);
	}
	@EventHandler
	public void respawn(PlayerRespawnEvent ev) {
		Player p = ev.getPlayer();
		Arena arena = arena(p);
		if (arena != null)
			ev.setRespawnLocation(arena.respawn(p));
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Player p = ev.getPlayer();
		Arena arena = arena(p);
		if (arena != null)
			arena.opuść(p);
	}
	
	public static void wyłącz() {
		wyłącz("Wyłączanie pluginu");
	}
	static void wyłącz(String msg) {
		if (zaczynanaArena != null) {
			zaczynanaArena.napiszGraczom(msg);
			zaczynanaArena.koniec();
			zaczynanaArena = null;
		}
		
		for (Arena arena : mapaAren.values())
			if (arena.grane) {
				arena.napiszGraczom(msg);
				arena.koniec();
			}
	}
	
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		Player p = ev.getPlayer();
		if (p.hasMetadata(metaid)) {
			if (dozwoloneKomendy.contains(Func.tnij(ev.getMessage(), " ").get(0)))
				return;
			else if (p.hasPermission(permCmdBypass))
				p.sendMessage(prefix + "pamiętaj że jesteś w trakcie minigry");
			else {
				ev.setCancelled(true);
				p.sendMessage(prefix + "Nie wolno tu uzywać komend");
			}
		}
	}
	
	@EventHandler
	public void KlikanieInv(InventoryClickEvent ev) {
		Arena arena = arena(ev.getWhoClicked());
		int slot = ev.getRawSlot();
		if (arena != null && Arena.nazwaInv.equals(ev.getView().getTitle()) && slot >= 0 && slot < ev.getInventory().getSize()) {
			ev.setCancelled(true);
			ItemStack item = ev.getCurrentItem();
			if (item.getType().equals(Material.LEATHER_CHESTPLATE)) {
				Drużyna drużyna = arena.znajdzDrużyne(item.getItemMeta().getDisplayName());
				arena.wybierzDrużyne((Player) ev.getWhoClicked(), drużyna);
			}
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void użycie(PlayerInteractEvent ev) {
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
		Arena arena = arena(ev.getPlayer());
		if (arena != null && !arena.grane && Arena.itemWybórDrużyny.equals(ev.getItem())) {
			ev.getPlayer().openInventory(arena.dajInv());
			ev.setCancelled(true);
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
	
	@Override
	public int czas() {
		if (zaczynanaArena != null)
			zaczynanaArena.czas();
		return 20;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void przeładuj() {
		configAreny.przeładuj();
		
		wyłącz("Przeładowywanie pluginu");
		
		mapaAren.clear();
		for (String klucz : configAreny.klucze(false))
			try {
				Arena arena = (Arena) configAreny.wczytaj(klucz);
				arena.nazwa = klucz;
				if (!arena.poprawna())
					throw new Throwable();
				mapaAren.put(klucz, arena);
			} catch (Throwable e) {
				Main.warn("Niepoprawna arena paintballa " + klucz + " w " + configAreny.path());
			}
		
		dozwoloneKomendy.clear();
		dozwoloneKomendy.add("/paintball");
		dozwoloneKomendy.add("/pb");
		for (String komenda : Main.ust.wczytajListe("Minigry.Dozwolone komendy"))
			dozwoloneKomendy.add(komenda.startsWith("/") ? "/" + komenda : komenda);

		configRangi.przeładuj();
		
		rangi = (Rangi) configRangi.wczytaj("rangi");
		if (rangi == null)
			rangi = Func.utwórz(Rangi.class);
		
		configTopki.przeładuj();
		topka = (List<Krotka<String, Integer>>) Func.nieNullList(configTopki.wczytaj("Paintball"));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane areny paintballa", mapaAren.size());
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "dołącz", "opuść", "staty", "stopnie", "topka");
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return staty(sender, args);
		
		switch (args[0]) {
		case "staty":	return staty(sender, args);
		case "stopnie": return rangi.rozpisz(sender);
		case "topka":
			sender.sendMessage(" ");
			sender.sendMessage(prefix + "Top 10 graczy paintballa");
			sender.sendMessage(" ");
			int i = 1;
			for (Krotka<String, Integer> krotka : topka)
				sender.sendMessage("§9" + i++ + ") §2" + krotka.a + "§e " + krotka.b + "pkt");
			sender.sendMessage(" ");
			return true;
		}
		
		if (!(sender instanceof Player))
			return Func.powiadom(prefix, sender, "Paintball jest tylko dla graczy");
		Player p = (Player) sender;
		
		Arena arena;
		
		switch (Func.odpolszcz(args[0])) {
		case "dolacz":
			arena = zaczynanaArena();
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
		if (args.length <= 1 && (!(sender instanceof Player)))
			return Func.powiadom(prefix, sender, "/pb staty <nick>");
		return staty(sender, args.length <= 1 ? sender.getName() : args[1]);
	}
	private boolean staty(CommandSender sender, String nick) {
		Player p = Bukkit.getPlayer(nick);
		if (p != null) {
			Statystyki staty = staty(p);
			if (staty != null)
				return staty(sender, p.getName(), staty);
		}
		
		Gracz g = Gracz.wczytaj(nick);
		return staty(sender, g.nick, g.statypb);
	} 
	private boolean staty(CommandSender sender, String nick, Statystyki staty) {
		return Func.powiadom(prefix, sender, "Staty %s\n\n%s",
				nick, staty == null ? nick + " §6Nigdy nie grał w Paintball" : staty.rozpisz());
		
	}
}



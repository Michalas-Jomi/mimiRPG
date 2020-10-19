package me.jomi.mimiRPG.Minigry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.projectiles.ProjectileSource;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Krotka;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.NowyEkwipunek;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import me.jomi.mimiRPG.Gracze.Gracz;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

// TODO edytor ogólny dla mapowalnych

// TODO personalizowany /powertool

// TODO przetestować mapowanie statycznej klasy

@Moduł
public class Paintball extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Paintball");
	static final String permCmdBypass = Func.permisja("minigra.paintball.bypasskomendy");
	static final String metaid = "mimiMinigra";
	
	static final HashMap<String, Arena> mapaAren = new HashMap<String, Arena>();
	final Config config = new Config("configi/minigry/Paintball");
	
	static Arena zaczynanaArena;
	
	public Paintball() {
		super("paintball", null, "pb");
		Main.dodajPermisje(permCmdBypass);
	}
		
	public static class Arena implements ConfigurationSerializable {
		static final ItemStack itemKask = Func.dajGłówkę("&bKask Paintballowca", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzN2VhZGM1M2MzOWI5Njg4Y2MzOTY1NWQxYjc4ZmQ2MTJjMWNkNjI1YzJhODk2MzhjNWUyNzIxNmM2ZTRkIn19fQ==", null);
		static final ItemStack itemŚnieżka = Func.stwórzItem(Material.SNOWBALL, 8, "§9Śnieżka");
		static final String metaStatystyki = "mimiPaintballStatystyki";
		static final String metaDrużynaId = "mimiPaintballDrużyna";
		List<Player> gracze = Lists.newArrayList();
		@Mapowane List<Location> respawnNiebiescy; // TODO zapewnić nienullisty przy demapowaniu
		@Mapowane List<Location> respawnCzerwoni;
		@Mapowane List<Integer> sekundyPowiadomień;
		@Mapowane int max_gracze = -1;
		@Mapowane int min_gracze = 2;
		@Mapowane Location zbiórka;
		@Mapowane int czasStartu;
		boolean grane = false;
		int punktyPotrzebne;
		String nazwa;		
		
		void start() {
			timer = -1;
			grane = true;
			zaczynanaArena = null;
			punktyPotrzebne = gracze.size() * 5;
			for (Player p : gracze) {
				respawn(p);
				Statystyki stat = Gracz.wczytaj(p.getName()).statypb;
				if (stat == null)
					stat = new Statystyki();
				stat.rozegraneAreny++;
				Func.ustawMetadate(p, metaStatystyki, stat);
			}
		}
		
		// Wykonywane co sekunde dla "zaczynanaArena"
		int timer = -1;
		void czas() {
			if (timer == -1) return;
			
			if (gracze.size() >= max_gracze)
				timer = Math.min(timer, 11);
			
			String czas = Func.czas(--timer);
			
			if (sekundyPowiadomień.contains(timer))
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
			NowyEkwipunek.dajNowy(p, zbiórka, GameMode.ADVENTURE);
			gracze.add(p);
			napiszGraczom("%s dołączył do poczekalni %s/%s", p.getDisplayName(), gracze.size(), strMaxGracze());
			if (timer == -1 && gracze.size() >= min_gracze)
				timer = czasStartu;
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
				g.statypb = staty;
				g.zapisz();
			}
			
			NowyEkwipunek.wczytajStary(p);
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
			
			if (timer != -1 && gracze.size() < min_gracze) {
				timer = -1;
				Bukkit.broadcastMessage(prefix + Func.msg("Wstrzymano odliczanie areny %s , z powodu małej ilości graczy %s/%s",
						nazwa, gracze.size(), strMaxGracze()));
			}
		}
		boolean sprawdzKoniec() {
			int c = 0;
			int n = 0;
			for (Player p : gracze) {
				switch(drużyna(p)) {
				case Czerwona:	c++; break;
				case Niebieska:	n++; break;
				}
				if (c >= 1 && n >= 1) return false;
			}
			if (c == 0) return wygrana(Drużyna.Niebieska);
			if (n == 0) return wygrana(Drużyna.Czerwona);
			return false;
		}
		boolean sprawdzKoniec(Drużyna drużyna) {
			if (drużyna.punkty >= punktyPotrzebne)
				return wygrana(drużyna);
			return false;
		}
		
		boolean wygrana(Drużyna drużyna) {
			for (Player p : gracze)
				if (drużyna.equals(drużyna(p)))
					staty(p).wygraneAreny++;
				else
					staty(p).przegraneAreny++;
			
			
			BiConsumer<Player, StringBuilder> bic = (p, s) -> s.append("§e").append(p.getDisplayName()).append("§6, ");
			StringBuilder c = new StringBuilder();
			StringBuilder n = new StringBuilder();
			for (Player p : gracze) {
				switch ((Drużyna) p.getMetadata(metaDrużynaId).get(0).value()) {
				case Czerwona:	bic.accept(p, c); break;
				case Niebieska:	bic.accept(p, n); break;
				}
			}
			
			Drużyna drużyna2 = Drużyna.Czerwona;
			StringBuilder p  = c;
			StringBuilder w  = n;
			if (drużyna.equals(Drużyna.Czerwona)) {
				drużyna2 = Drużyna.Niebieska;
				p = n;
				w = c;
			}
			
			Function<StringBuilder, String> f = s -> {
				int len = s.length();
				return len >= 2 ? s.substring(0, len - 2) : s.toString();
			};
			Bukkit.broadcastMessage(prefix + Func.msg("Drużyna %s(%s) wygrała na arenie %s z drużyną %s(%s)",
					drużyna, f.apply(w), nazwa, drużyna2, f.apply(p)));
			koniec();
			return true;
		}
		void koniec() {
			grane = false;
			while (!gracze.isEmpty())
				opuść(0, false);
		}
		
		void wybierzDrużyne(Player p, Drużyna drużyna) {
			Func.ustawMetadate(p, metaDrużynaId, drużyna);
			ubierz(p);
		}
		void ubierz(Player p) {
			Color kolor = ((Drużyna) p.getMetadata(metaDrużynaId).get(0).value()).kolor;
			Function<Material, ItemStack> dajItem = mat -> {
				ItemStack item = Func.stwórzItem(mat, " ");
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(kolor);
				meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				item.setItemMeta(meta);
				return item;
			};
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
				p.teleport(zbiórka);
				return zbiórka;
			}
			p.getInventory().setItem(4, itemŚnieżka);
			List<Location> lista = null;
			switch (drużyna) {
			case Czerwona:	lista = respawnCzerwoni;	break;
			case Niebieska:	lista = respawnNiebiescy;	break;
			}
			Location loc = Func.losuj(lista);
			p.teleport(loc);
			return loc;
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
		
		boolean pełna() {
			return max_gracze > 0 && gracze.size() >= max_gracze;
		}		
		boolean poprawna() {
			return nazwa != null && !nazwa.isEmpty() &&
					zbiórka != null &&
					(max_gracze < 0 || max_gracze >= min_gracze) &&
					!respawnNiebiescy.isEmpty() &&
					!respawnCzerwoni.isEmpty();
		}
	
		enum Drużyna {
			Czerwona(Color.RED,	  ChatColor.RED),
			Niebieska(Color.BLUE, ChatColor.BLUE);

			int punkty;
			Color kolor;
			ChatColor napisy;
			Drużyna(Color kolor, ChatColor napisy) {
				this.napisy = napisy;
				this.kolor = kolor;
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Arena)
				return ((Arena) obj).nazwa.equals(nazwa);
			return false;
		}

		@Override
		public Map<String, Object> serialize() {
			return Func.zmapuj(this);
		}
		public Arena(Map<String, Object> mapa) {
			Func.zdemapuj(this, mapa);
		}
	}
	
	public static class Statystyki implements ConfigurationSerializable {
		@Mapowane int rozegraneAreny;
		@Mapowane int przegraneAreny;
		@Mapowane int wygraneAreny;
		@Mapowane int kille;
		@Mapowane int śmierci;
		@Mapowane int punkty; // TODO punkty
		@Mapowane int rzucone;
		
		
		protected Statystyki() {};
		public Statystyki(Map<String, Object> mapa) {
			Func.zdemapuj(this, mapa);
		}
		@Override
		public Map<String, Object> serialize() {
			return Func.zmapuj(this);
		}
		
		String rozpisz() {
			StringBuilder s = new StringBuilder();
			
			BiConsumer<String, Object> bic = (info, stan) ->
					s.append("§6").append(info).append("§8: §e").append(stan).append('\n');
			
			BiFunction<Integer, Integer, String> bif = (liczone, wszystkie) -> {
				if (wszystkie == 0) return "100%";
				return (liczone / wszystkie) + "%";
			};
			
			bic.accept("Rozegrane gry", rozegraneAreny);
			bic.accept("Wygrane gry", wygraneAreny);
			bic.accept("Przegrane gry", przegraneAreny);
			bic.accept("Współczynnik zwycięstw", bif.apply(wygraneAreny, rozegraneAreny));
			s.append('\n');
			bic.accept("Kille", kille);
			bic.accept("Zgony", śmierci);
			bic.accept("Współczynnik zabójstw", bif.apply(kille, kille + śmierci));
			s.append('\n');
			bic.accept("Rzucone śnieżki", rzucone);
			s.append('\n');
			bic.accept("Punkty", punkty);
			bic.accept("Ranga", ranga(punkty));
			s.append('\n');
			
			return s.toString();
		}
		
		static String ranga(int pkt) {
			return "rangi"; // TODO rangi
		}
	}
	
	static Arena arena(Entity p)			{ return metadata(p, metaid); }
	static Statystyki staty(Player p)		{ return metadata(p, Arena.metaStatystyki); }
	static Arena.Drużyna drużyna(Player p)	{ return metadata(p, Arena.metaDrużynaId); }
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
			if (stat != null)
				stat.rzucone++;
		}
	}
	@EventHandler
 	public void trafienie(ProjectileHitEvent ev) {
		Arena arena = arena(ev.getHitEntity());
		if (arena != null)
			arena.trafienie(ev);;
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
		if (zaczynanaArena != null) {
			zaczynanaArena.napiszGraczom("Wyłączanie pluginu");
			zaczynanaArena.koniec();
		}
		
		for (Arena arena : mapaAren.values())
			if (arena.grane) {
				arena.napiszGraczom("Wyłączanie pluginu");
				arena.koniec();
			}
	}
	
	// TODO dozwolone komendy
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		Player p = ev.getPlayer();
		if (p.hasMetadata(metaid))
			if (p.hasPermission(permCmdBypass)) {
				p.sendMessage(prefix + "pamiętaj że jesteś w trakcie minigry");
			} else {
				ev.setCancelled(true);
				p.sendMessage(prefix + "Nie wolno tu uzywać komend");
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
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		
		if (zaczynanaArena != null) {
			zaczynanaArena.napiszGraczom("Przeładowywanie pluginu");
			zaczynanaArena.koniec();
			zaczynanaArena = null;
		}
		
		for (Arena arena : mapaAren.values())
			if (arena.grane) {
				arena.napiszGraczom("Przeładowywanie pluginu");
				arena.koniec();
			}
		
		mapaAren.clear();
		for (String klucz : config.klucze(false))
			try {
				Arena arena = (Arena) config.wczytaj(klucz);
				arena.nazwa = klucz;
				if (!arena.poprawna())
					throw new Throwable();
				mapaAren.put(klucz, arena);
			} catch (Throwable e) {
				Main.warn("Niepoprawna arena paintballa " + klucz + " w " + config.path());
			}
	}





	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane areny paintballa", mapaAren.size());
	}



	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) return utab(args, "dołącz", "opuść", "staty"); // TODO system statystyk
		return null;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false; // TODO staty
		if (!(sender instanceof Player)) return Func.powiadom(prefix, sender, "Paintball jest tylko dla graczy");
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
		case "staty": // TODO
			break;
		}
		return true;
	}
	
}



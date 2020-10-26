package me.jomi.mimiRPG.Minigry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import com.google.common.collect.Lists;
import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KolorRGB;
import me.jomi.mimiRPG.util.Krotka;

// TODO personalizowany /powertool

@Moduł
public class Paintball extends MinigraDrużynowa {
	public static final String prefix = Func.prefix("Paintball");
	
	static final String metaStatystyki = "mimiPaintballStatystyki";
	static final String metaDrużynaId = "mimiPaintballDrużyna";
	static final String metaid = "mimiMinigraPaintball";
	
	static List<Krotka<String, Integer>> topka = Lists.newArrayList();
	
	static final Config configAreny = new Config("configi/minigry/PaintballAreny");
	static final Config configRangi = new Config("configi/minigry/PaintballRangi");
	static Statystyki.Rangi rangi;

	public static class Arena extends MinigraDrużynowa.Arena {
		static final ItemStack itemKask = Func.dajGłówkę("&bKask Paintballowca", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzN2VhZGM1M2MzOWI5Njg4Y2MzOTY1NWQxYjc4ZmQ2MTJjMWNkNjI1YzJhODk2MzhjNWUyNzIxNmM2ZTRkIn19fQ==", null);
		static final ItemStack itemŚnieżka = Func.stwórzItem(Material.SNOWBALL, 1, "§9Śnieżka");
		
		@Mapowane List<Drużyna> druzyny;
		
		int punktyPotrzebne;
		
		@Override
		void start() {
			super.start();
			punktyPotrzebne = gracze.size() * 5;
			for (Player p : gracze)
				respawn(p);
		}	
		@Override
		boolean dołącz(Player p) {
			if (!super.dołącz(p)) return false;
			p.getInventory().setItem(4, itemWybórDrużyny);

			Statystyki.Ranga ranga = staty(p).ranga();
			if (ranga != null)
				ranga.ubierz(p);
			return true;
			
		}
		
		void ubierz(Player p, Drużyna drużyna) {
			super.ubierz(p, drużyna);
			p.getInventory().setHelmet(itemKask);
		}
		
		boolean sprawdzKoniec(Drużyna drużyna) {
			if (drużyna.punkty >= punktyPotrzebne)
				return wygrana(drużyna);
			return false;
		}
		@Override
		void koniec() {
			super.koniec();
			for (Drużyna druzyna : druzyny)
				druzyna.punkty = 0;
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

		static void dajŚnieżke(Player p) {
			ItemStack item = p.getInventory().getItem(4);
			if (item == null || item.getType().equals(Material.SNOWBALL))
				p.getInventory().setItem(4, itemŚnieżka);
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
		
		@Override
		boolean poprawna() {
			for (Drużyna druzyna : druzyny)
				if (druzyna.respawny.isEmpty())
					return false;
			 return super.poprawna() && druzyny.size() >= 2;
		}
		
		Statystyki noweStaty() {
			return new Statystyki();
		}
		@Override
		List<Drużyna> getDrużyny() {
			return druzyny;
		}
	}
	
	public static class Drużyna extends MinigraDrużynowa.Drużyna {
		@Mapowane List<Location> respawny;
		int punkty;
	}
	
	public static class Statystyki extends Minigra.Statystyki {
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
		
		@Mapowane int kille;
		@Mapowane int śmierci;
		@Mapowane int rzucone;
		
		private int punkty;
		
		public int policzPunkty() {
			punkty = 0;

			Consumer<String> consumer = (sc) -> {
				try {
					Field field = Func.dajField(getClass(), sc);
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
		
		@Override
		void rozpiska(Consumer<String> cons, boolean usuwaćKolor) {
			super.rozpiska(cons, usuwaćKolor);

			cons.accept(_rozpiska("kill ratio", _rozpiska(kille, kille + śmierci)));
			cons.accept(_rozpiska("Kille", kille));
			cons.accept(_rozpiska("Zgony", śmierci));
			cons.accept("  ");
			cons.accept(_rozpiska("Rzucone śnieżki", rzucone));
			cons.accept("   ");
			int punkty = policzPunkty();
			cons.accept(_rozpiska("Punkty", punkty));
			Ranga ranga = ranga(punkty);
			
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
	
	String getMetaId() {
		return metaid;
	}
	String getMetaDrużynaId() {
		return metaDrużynaId;
	}
	String getMetaStatystyki() {
		return metaStatystyki;
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

	static Statystyki staty(Player p) {
		return metadata(p, metaStatystyki);
	}
	static Drużyna drużyna(Player p) {
		return metadata(p, metaDrużynaId);
	}
	static Arena arena(Player p) {
		return metadata(p, metaid);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		super.przeładuj();
		
		configRangi.przeładuj();
		
		rangi = (Statystyki.Rangi) configRangi.wczytaj("rangi");
		if (rangi == null)
			rangi = Func.utwórz(Statystyki.Rangi.class);
		
		topka = (List<Krotka<String, Integer>>) Func.nieNullList(configTopki.wczytaj("Paintball"));
	}
	
	public static boolean onCommand(CommandSender sender, String[] args) {
		if (args.length < 2) return staty(sender, args);
		
		switch (args[1]) {
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
	
	private static boolean staty(CommandSender sender, String[] args) {
		if (args.length <= 2 && (!(sender instanceof Player)))
			return Func.powiadom(prefix, sender, "/pb staty <nick>");
		return staty(sender, args.length <= 2 ? sender.getName() : args[2]);
	}
	private static boolean staty(CommandSender sender, String nick) {
		Player p = Bukkit.getPlayer(nick);
		if (p != null) {
			Statystyki staty = staty(p);
			if (staty != null)
				return staty(sender, p.getName(), staty);
		}
		
		Gracz g = Gracz.wczytaj(nick);
		return staty(sender, g.nick, (Statystyki) g.staty.get(Arena.class.getName()));
	} 
	private static boolean staty(CommandSender sender, String nick, Statystyki staty) {
		return Func.powiadom(prefix, sender, "Staty %s\n\n%s",
				nick, staty == null ? nick + " §6Nigdy nie grał w Paintball" : staty.rozpisz());
	}
	

	static Arena arena(Entity p) {
		return metadata(p, metaid);
	}
	@Override
	Config getConfigAreny() {
		return configAreny;
	}
}



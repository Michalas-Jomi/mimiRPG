package me.jomi.mimiRPG.Minigry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
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
		
	static List<Krotka<String, Integer>> topka = Lists.newArrayList();
	
	static final Config configRangi = new Config("configi/minigry/PaintballRangi");
	static Statystyki.Rangi rangi;

	public static class Broń extends Mapowany {
		@Mapowane double siłaStrzału;
		@Mapowane ItemStack Item;
		@Mapowane double zasięg;
		@Mapowane int coIleSek;
		int timer;
		
		void strzel(Player p) {
			Egg pocisk = (Egg) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.EGG);
			pocisk.setVelocity(p.getLocation().getDirection().multiply(siłaStrzału));
			Func.ustawMetadate(pocisk, metaPocisków, this);
			pocisk.setShooter(p);
		}
		
		void wybuch(Projectile pocisk, Drużyna drużyna, Function<Entity, Arena> arena) {
			Location loc = pocisk.getLocation();
			
			Consumer<Double> particle = zasięg -> loc.getWorld().spawnParticle(Particle.REDSTONE, loc, (int) (double) zasięg*40 + 1,
					zasięg, zasięg, zasięg, 0, new Particle.DustOptions(drużyna.kolor, 1));
			
			for (int i=1; i <= 3; i++)
				particle.accept(zasięg / 3 * i);
			
			
			Player rzucający = (Player) pocisk.getShooter();
			for (Entity e : pocisk.getNearbyEntities(zasięg, zasięg, zasięg))
				if (e instanceof Player)
					Func.wykonajDlaNieNull(arena.apply(e), a -> a.trafienie((Player) e, rzucający));
		}
	}
	
	public static class Arena extends MinigraDrużynowa.Arena {
		static final ItemStack itemKask = Func.dajGłówkę("&bKask Paintballowca", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzN2VhZGM1M2MzOWI5Njg4Y2MzOTY1NWQxYjc4ZmQ2MTJjMWNkNjI1YzJhODk2MzhjNWUyNzIxNmM2ZTRkIn19fQ==", null);
		static final ItemStack itemŚnieżka = Func.stwórzItem(Material.SNOWBALL, 8, "§9Śnieżka");
		
		@Mapowane List<Drużyna> druzyny;
		@Mapowane List<Broń> dodatkoweBronie;
		
		int punktyPotrzebne;
		
		@Override
		void start() {
			super.start();
			punktyPotrzebne = gracze.size() * 5;
			for (Player p : gracze)
				respawn(p);
			for (Broń broń : dodatkoweBronie)
				broń.timer = broń.coIleSek;
		}
		@Override
		boolean dołącz(Player p) {
			if (!super.dołącz(p)) return false;

			Statystyki.Ranga ranga = inst.staty(p).ranga();
			if (ranga != null)
				ranga.ubierz(p);
			return true;
		}

		@Override
		<D extends MinigraDrużynowa.Drużyna> void ubierz(Player p, D drużyna) {
			ubierz(p, drużyna, false, true, true, true);
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
			return respawn(p, inst.drużyna(p));
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
		
		public void trafienie(Player trafiony, Player rzucający) {
			Drużyna dr;
			Drużyna dt;
			if (!grane || rzucający == null || // rzut w poczekalni lub rzucający nie istnieje
					!this.equals(inst.arena(rzucający)) || // należą do innych aren
					(dt = inst.drużyna(trafiony)).equals(dr = inst.drużyna(rzucający))) return; // należą do tej samej drużyny 
			
			Location loc = trafiony.getEyeLocation();
			
			respawn(trafiony, dt);
			
			inst.staty(trafiony).śmierci++;
			inst.staty(rzucający).kille++;
			
			napiszGraczom(dr.napisy + rzucający.getName() + "§6 postrzelił " +
						  dt.napisy + trafiony.getName()  + " §e" + ++dr.punkty + "§6/§e" + punktyPotrzebne);
			
			Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
			fwm.addEffect(FireworkEffect.builder()
							.with(Type.BURST)
							.withColor(dt.kolor)
							.build()
							);
			fwm.setPower(1);
			fw.setFireworkMeta(fwm);
			fw.detonate();
			
			sprawdzKoniec(dr);
		}
		
		@Override
		boolean poprawna() {
			for (Drużyna druzyna : druzyny)
				if (druzyna.respawny.isEmpty())
					return false;
			 return super.poprawna() && druzyny.size() >= 2;
		}
		
		void czasBroni() {
			for (Broń broń : dodatkoweBronie) {
				if (--broń.timer <= 0) {
					broń.timer = broń.coIleSek;
					for (Player p : gracze)
						p.getInventory().addItem(broń.Item);
				}
			}
		}
		
		@Override Supplier<Statystyki> noweStaty() { return Statystyki::new; }
		@Override List<Drużyna> getDrużyny() { return druzyny; }
		
		Paintball inst;
		@Override Paintball getInstMinigraDrużynowa()	   { return inst; }
		@Override <M extends Minigra> void setInst(M inst) { this.inst = (Paintball) inst; }
		@Override int getMinDrużyny() { return 2; }
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
			
			configDane.ustaw_zapisz("Topki.Paintball", topka);
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

	static final String metaPocisków = "mimiMinigraPaintballPocisk";
	static final String metaStatystyki = "mimiPaintballStatystyki";
	static final String metaDrużynaId = "mimiPaintballDrużyna";
	static final String metaid = "mimiMinigraPaintball";
	
	@Override String getPrefix() { return prefix; }
	final Config configAreny = new Config("configi/minigry/PaintballAreny");
	@Override Config getConfigAreny() { return configAreny; }
	@Override String getMetaId() { return metaid; }
	@Override String getMetaDrużynaId() { return metaDrużynaId; }
	@Override String getMetaStatystyki() { return metaStatystyki; }

	@EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent ev) {
        if (ev.getSpawnReason() == SpawnReason.EGG)
            ev.setCancelled(true);
    }
	@EventHandler
	public void specjalneBronie(PlayerInteractEvent ev) {
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK))
			return;
		
		ItemStack item = ev.getItem();
		if (item == null || item.getType().isAir())
			return;
		
		Player p = ev.getPlayer();
		
		Arena arena = arena(p);
		if (arena == null)
			return;
		
		for (Broń broń : arena.dodatkoweBronie) {
			if (item.isSimilar(broń.Item)) {
				ev.setCancelled(true);
				broń.strzel(p);
				int ile = item.getAmount();
				if (ile > 1)
					item.setAmount(ile - 1);
				else {
					PlayerInventory inv = p.getInventory();
					if (inv.getItemInMainHand().isSimilar(item))
						inv.setItemInMainHand(null);
					else
						inv.setItemInOffHand(null);
				}
						
				return;
			}
		}
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
		Projectile pocisk = ev.getEntity();
		Entity trafiony = ev.getHitEntity();
		Func.wykonajDlaNieNull(arena(trafiony), 
				arena -> arena.trafienie((Player) trafiony, (Player) pocisk.getShooter()));
		if (pocisk.hasMetadata(metaPocisków)) {
			Broń broń = (Broń) pocisk.getMetadata(metaPocisków).get(0).value();
			broń.wybuch(pocisk, drużyna((Entity) pocisk.getShooter()), this::arena);
		}
		
		ev.getEntity().remove();
	}
	@EventHandler
	public void śmieć(PlayerDeathEvent ev) {
		if (arena(ev.getEntity()) != null)
			ev.setKeepInventory(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
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

	@Override @SuppressWarnings("unchecked") Statystyki staty	(Entity p) { return super.staty(p); }
	@Override @SuppressWarnings("unchecked") Drużyna 	drużyna (Entity p) { return super.drużyna(p); }
	@Override @SuppressWarnings("unchecked") Arena 		arena	(Entity p) { return super.arena(p); }

	@Override
	public int czas() {
		int w = super.czas();
		for (Minigra.Arena _arena : mapaAren.values()) {
			Arena arena = (Arena) _arena;
			if (arena.grane)
				arena.czasBroni();
		}
		return w;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		super.przeładuj();
		
		configRangi.przeładuj();
		
		rangi = (Statystyki.Rangi) configRangi.wczytaj("rangi");
		if (rangi == null)
			rangi = Func.utwórz(Statystyki.Rangi.class);
		
		topka = (List<Krotka<String, Integer>>) Func.nieNullList(configDane.wczytaj("Topka.Paintball"));
	}
	
	public boolean onCommand(CommandSender sender, String[] args) {
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
	
	private boolean staty(CommandSender sender, String[] args) {
		if (args.length <= 2 && (!(sender instanceof Player)))
			return Func.powiadom(prefix, sender, "/pb staty <nick>");
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
				nick, staty == null ? nick + " §6Nigdy nie grał w Paintball" : staty.rozpisz());
	}
}



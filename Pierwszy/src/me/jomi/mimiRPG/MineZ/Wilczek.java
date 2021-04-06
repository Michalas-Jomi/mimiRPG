package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Wilczek extends Komenda implements Listener, Przeładowalny {
	public static class Wilk extends Mapowany {
		Player p;
		Wolf mob;
		@Mapowane long czas = -1;
		@Mapowane String nickGracza;
		
		public Wilk() {}
		@Override
		protected void Init() {
			p = Bukkit.getPlayer(nickGracza);
		}
		
		Wilk(Player p) {
			this.p = p;
			nickGracza = p.getName();
		}
		void przywołaj() {
			if (teleport()) return;
			if (!minąłCzas()) {
				p.sendMessage(prefix + "Musisz poczekać jeszcze " +
						Func.czas((int) ((czas - System.currentTimeMillis()) / 1000)) + " zanim znów przywołasz swojego wilka");
				return;
			}
			
			mob = (Wolf) p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF);
			mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(wczytaj("atak", 3d));
			mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(wczytaj("hp", 30d));
			mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(wczytaj("szybkość", .22d));
			mob.setCustomName("§aKompan §b" + p.getName());
			mob.setCanPickupItems(false);
			mob.setCollidable(false);
			mob.setTamed(true);
			mob.setOwner(p);
			mob.setAdult();
			Func.ustawMetadate(mob, metaId, this);
			p.sendMessage(prefix + "Przywołałeś swojego Wilka");
		}
		boolean teleport() {
			boolean w = mob != null && !mob.isDead(); 
			if (w) mob.teleport(p);
			return w;
		}
		void zabity() {
			Krotka<String, ?> k = new Krotka<>();
			Func.multiTry(
					() -> k.a = mob.getKiller().getName(),
					() -> k.a = mob.getCustomName(),
					() -> k.a = mob.getKiller().getDisplayName()
			);
			if (k.a == null)
				p.sendMessage(prefix + "Twój wilk umarł!");
			else
				p.sendMessage(prefix + Func.msg("%s zabił twojego Wilka!", k.a));
			Func.wykonajDlaNieNull(mob, Wolf::remove);
			mob = null;
			ustawCzas(wczytaj("czas po zabiciu", 15) * 60);
		}
		void odwołaj() {
			if (mob != null) {
				mob.remove();
				mob = null;
				ustawCzas(wczytaj("czas po odwołaniu", 5) * 60);
			}
		}
		void ustawCzas(int sekundy) {
			czas = System.currentTimeMillis() + sekundy * 1000;
			Gracz.wczytaj(p).zapisz();
		}
		boolean minąłCzas() {
			return czas == -1 || System.currentTimeMillis() >= czas;
		}
	}
	public static final String prefix = Func.prefix("Wilczek");
	static final String metaId = "mimiWilk";
	public Wilczek() {
		super("wilczek", null, "piesek", "wilk", "pet");
	}

	static <T> T wczytaj(String sc, T domyślna) {
		return Main.ust.wczytaj("Wilczek." + sc, domyślna);
	}
	void wykonaj(Player p, Consumer<Wilk> cons) {
		Gracz gracz = Gracz.wczytaj(p);
		if (gracz.wilk == null) {
			gracz.wilk = new Wilk(p);
			gracz.zapisz();
		}
		cons.accept(gracz.wilk);
		
	}
	
	@EventHandler
	public void śmierćWilka(EntityDeathEvent ev) {
		if (!EntityType.WOLF.equals(ev.getEntityType())) return;
		if (ev.getEntity().hasMetadata(metaId))
			((Wilk) ev.getEntity().getMetadata(metaId).get(0).value()).zabity();
	}
	@EventHandler
	public void śmierćGracza(PlayerDeathEvent ev) {
		Func.wykonajDlaNieNull(Gracz.wczytaj(ev.getEntity()).wilk, Wilk::odwołaj);
	}
	@EventHandler
	public void teleport(PlayerTeleportEvent ev) {
		Func.wykonajDlaNieNull(Gracz.wczytaj(ev.getPlayer()).wilk, Wilk::teleport);
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Func.wykonajDlaNieNull(Gracz.wczytaj(ev.getPlayer()).wilk, Wilk::odwołaj);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "przywołaj", "odwołaj");
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Wilczki są dla graczy ziom");
		Player p = (Player) sender;
		
		Supplier<Consumer<Wilk>> dajCons = () -> {
			if (args.length >= 1) 
				switch (args[0]) {
				case "o":
				case "odwolaj":
				case "odwołaj":
					p.sendMessage(prefix + "Odwołałeś swojego wilka");
					return Wilk::odwołaj;
				}
			return Wilk::przywołaj;
		};
		
		wykonaj(p, dajCons.get());
		
		return true;
	}

	@Override public void przeładuj() {}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wilki", "§aWłączone");
	}
}



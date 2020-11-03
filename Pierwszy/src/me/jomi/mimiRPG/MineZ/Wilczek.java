package me.jomi.mimiRPG.MineZ;

import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

@Moduł
public class Wilczek extends Komenda implements Listener {
	public static class Wilk {
		Player p;
		Wolf mob;
		long czas = -1;
		
		
		Wilk(Player p) {
			this.p = p;
		}
		void przywołaj() {
			if (teleport()) return;
			if (!minąłCzas()) {
				p.sendMessage(prefix + "Musisz poczekać jeszcze " +
						Func.czas((int) ((czas - System.currentTimeMillis()) / 1000)) + " zanim znów przywołasz swojego wilka");
				return;
			}
				
			mob = (Wolf) p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF);
			mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(2d); // TODO z configu
			mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2d); // TODO z configu
			mob.setCustomName("§aKompan §b" + p.getName());
			mob.setCanPickupItems(false);
			mob.setCollidable(false);
			mob.setTamed(true);
			mob.setOwner(p);
			mob.setAdult();
			Func.ustawMetadate(mob, metaId, this);
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
					() -> k.a = ((Player) mob.getKiller()).getDisplayName()
			);
			if (k.a == null)
				p.sendMessage(prefix + "Twój wilk umarł!");
			else
				p.sendMessage(prefix + Func.msg("%s zabił twojego Wilka!", k.a));
			mob.remove();
			mob = null;
			ustawCzas(5);// TODO z comnfigu
		}
		void odwołaj() {
			mob.remove();
			mob = null;
			ustawCzas(15);// TODO z configu
		}
		void ustawCzas(int sekundy) {
			czas = System.currentTimeMillis() + sekundy * 1000;
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
	
	void przywołaj(Player p) {
		Gracz gracz = Gracz.wczytaj(p);
		if (gracz.wilk == null)
			gracz.wilk = new Wilk(p);
		gracz.wilk.przywołaj();
	}
	
	
	@EventHandler
	public void śmierćWilka(EntityDeathEvent ev) {
		if (!EntityType.WOLF.equals(ev.getEntityType())) return;
		if (ev.getEntity().hasMetadata(metaId))
			((Wilk) ev.getEntity().getMetadata(metaId).get(0).value()).zabity();
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
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Wilczki są dla graczy ziom");
		
		przywołaj((Player) sender);
		
		return true;
	}
}



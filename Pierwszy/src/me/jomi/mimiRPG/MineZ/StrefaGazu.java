package me.jomi.mimiRPG.MineZ;

import com.google.common.collect.Sets;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.api._WorldGuard;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.function.BiPredicate;

@Moduł
public class StrefaGazu implements Listener, Zegar {
	public static final String prefix = Func.prefix("Strefa Gazu");
	public static boolean warunekModułu() {
		return _WorldGuard.rg != null;
	}

	private final BossBar bar;

	public StrefaGazu() {
		bar = Bukkit.createBossBar("Strefa Gazu", BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG);
		bar.setVisible(false);
	}

	
	final Set<String> bypassZwykła = Sets.newConcurrentHashSet();
	final Set<String> zwykła = Sets.newConcurrentHashSet();
	// radiacja od terenu
	void nałóżEfekt(Player p) {
		if (bypassZwykła.contains(p.getName()))
			return;
		PlayerInventory inv = p.getInventory();

		if (inv.getHelmet() != null && inv.getHelmet().getType() == Material.TURTLE_HELMET)
			return;
		
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20*20, 0, false, false, false));
		zwykła.add(p.getName());
		bar.addPlayer(p);
		Func.opóznij(20*20-1, () -> zwykła.remove(p.getName()));
	}
	// withering od moba
	void nałóżMobEfekt(Player p) {
		zwykła.remove(p.getName());
		bypassZwykła.add(p.getName());
		
		p.removePotionEffect(PotionEffectType.WITHER);
		p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20*5, 0, true, false, true));

		bar.addPlayer(p);
		
		Func.opóznij(20*5+1, () -> {
			bypassZwykła.remove(p.getName());
			bar.removePlayer(p);
			sprawdzRadiacjeZTerenu(p);
		});
	}

	@EventHandler
	public void dmgEfektu(EntityDamageEvent ev) {
		if (ev.getCause() == DamageCause.WITHER && zwykła.contains(ev.getEntity().getName()))
			ev.setDamage(Main.ust.wczytaj("StrefaGazu.dmg", 1.0));
	}
	
	void sprawdzRadiacjeZTerenu(Player p) {
		if (Func.regiony(p.getWorld()).getApplicableRegions(Func.locToVec3(p.getLocation()))
				.testState(_WorldGuard.rg.wrapPlayer(p), _WorldGuard.flagaStrefaGazu)) {
			nałóżEfekt(p);
		} else
			bar.removePlayer(p);
	}
	@Override
	public int czas() {
		int mx = Main.ust.wczytaj("ogólne.skanowanie.gracze na sekunde", 40);
		int licz = 0;
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (licz++ > mx)
				break;
			sprawdzRadiacjeZTerenu(p);
		}
		
		return 100;
	}
	@EventHandler
	public void uderzenie(EntityDamageByEntityEvent ev) {
		Entity e = ev.getDamager();
		if (!(ev.getEntity() instanceof Player) || e instanceof Player || !(e instanceof LivingEntity))
			return;
		Player p = (Player) ev.getEntity();
		
		if (Func.regiony(p.getWorld()).getApplicableRegions(Func.locToVec3(p.getLocation()))
					.testState(_WorldGuard.rg.wrapPlayer(p), _WorldGuard.flagaStrefaGazu))
			nałóżMobEfekt(p);
	}
}

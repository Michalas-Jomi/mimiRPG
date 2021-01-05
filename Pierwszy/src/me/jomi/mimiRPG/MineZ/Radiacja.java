package me.jomi.mimiRPG.MineZ;

import java.util.Set;
import java.util.function.BiPredicate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Radiacja implements Listener, Zegar, Przeładowalny {
	public static final String prefix = Func.prefix("Radiacja");
	public static boolean warunekModułu() {
		return Main.rg != null;
	}

	
	final Set<String> bypassZwykła = Sets.newConcurrentHashSet();
	final Set<String> zwykła = Sets.newConcurrentHashSet();
	final Set<String> wypiliPłyn = Sets.newConcurrentHashSet();
	// radiacja od terenu
	void nałóżEfekt(Player p) {
		if (wypiliPłyn.contains(p.getName()) || bypassZwykła.contains(p.getName()))
			return;
		PlayerInventory inv = p.getInventory();
		BiPredicate<ItemStack, Material> bip = (item, mat) -> item != null && item.getType() == mat;
		
		if (bip.test(inv.getHelmet(), Material.GOLDEN_HELMET) && bip.test(inv.getChestplate(), Material.GOLDEN_CHESTPLATE) &&
				bip.test(inv.getLeggings(), Material.GOLDEN_LEGGINGS) && bip.test(inv.getBoots(), Material.GOLDEN_BOOTS))
			return;
		
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20*20, 0, false, false, false));
		zwykła.add(p.getName());
		Func.opóznij(20*20-1, () -> zwykła.remove(p.getName()));
	}
	// withering od moba
	void nałóżMobEfekt(Player p) {
		zwykła.remove(p.getName());
		bypassZwykła.add(p.getName());
		
		p.removePotionEffect(PotionEffectType.WITHER);
		p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20*5, 0, true, false, true));
		
		Func.opóznij(20*5+1, () -> {
			bypassZwykła.remove(p.getName());
			sprawdzRadiacjeZTerenu(p);
		});
	}
	
	
	@EventHandler
	public void piciePłynu(PlayerItemConsumeEvent ev) {
		if (ev.getItem().isSimilar(płyn)) {
			wypiliPłyn.add(ev.getPlayer().getName());
			ev.getPlayer().removePotionEffect(PotionEffectType.WITHER);
			ev.getPlayer().sendMessage(prefix + Func.msg("Wypiłeś płyn lugola, przez następne %s jesteś odporny na radiacje wyspy", Func.czas(czasPłynu)));
			Func.opóznij(20 * czasPłynu, () -> {
				ev.getPlayer().sendMessage(prefix + Func.msg("Wypity płyn lugola przestaje działać!"));
				wypiliPłyn.remove(ev.getPlayer().getName());
			});
		}
	}
	
	@EventHandler
	public void dmgEfektu(EntityDamageEvent ev) {
		if (ev.getCause() == DamageCause.WITHER && zwykła.contains(ev.getEntity().getName()))
			ev.setDamage(Main.ust.wczytajLubDomyślna("Radiacja.dmg", 1.0));
	}
	
	void sprawdzRadiacjeZTerenu(Player p) {
		if (Func.regiony(p.getWorld()).getApplicableRegions(Func.locToVec3(p.getLocation()))
				.testState(Main.rg.wrapPlayer(p), Main.flagaRadiacja)) {
			nałóżEfekt(p);
		}
	}
	@Override
	public int czas() {
		int mx = Main.ust.wczytajLubDomyślna("ogólne.skanowanie.gracze na sekunde", 40);
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
					.testState(Main.rg.wrapPlayer(p), Main.flagaRadiacja))
			nałóżMobEfekt(p);
	}
	
	
	ItemStack płyn;
	int czasPłynu;
	
	@Override
	public void przeładuj() {
		czasPłynu = Main.ust.wczytajLubDomyślna("Radiacja.czas działania płynu", 60*20);
		płyn = Main.ust.wczytajItem("Radiacja.płyn");
		if (płyn == null)
			płyn = Func.stwórzItem(Material.HONEY_BOTTLE, "&6Płyn Lugola", "&aPowstrzymuje większość radiacji", "&a na " + Func.czas(czasPłynu));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Radiacja", Func.koloruj("&aWłączona"));
	}
}

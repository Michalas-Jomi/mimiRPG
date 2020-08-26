package me.jomi.mimiRPG.MiniGierki;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;

public class OneShotOneKill extends MiniGra{
	private boolean ochrona = true;

	public OneShotOneKill() {
		super("Zastrzel lub Zgiñ", "OneShotOneKill");
	}
	
	protected void start(Arena arena) {
		super.start(arena);
		PotionEffect efekt = new PotionEffect(PotionEffectType.SPEED, 400, 9, false, false, false);
		PotionEffect efekt2 = new PotionEffect(PotionEffectType.JUMP, 300400, 2, false, false, false);
		for (Player p : arena.gracze) {
			p.sendMessage(prefix + "Ochrona startowa bêdzie aktywna przez §e20 sekund");
			p.addPotionEffect(efekt);
			p.addPotionEffect(efekt2);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		    public void run() {
		    	if (!arena.grane) return;
		    	ochrona = false;
				ItemStack ³uk = Func.stwórzItem(Material.BOW, 1, "&2Taktyczny ³uk", Arrays.asList("Zastrzel nim wszystkich przeciwników!"));
				PotionEffect efekt = new PotionEffect(PotionEffectType.SPEED, 300000, 2, false, false, false);
				ItemStack strza³a = Func.stwórzItem(Material.ARROW, 1, "&2Taktyczna Strza³a", null);
				ItemMeta meta = ³uk.getItemMeta();
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				³uk.setItemMeta(meta);
				for (Player p : arena.gracze) {
					p.addPotionEffect(efekt);
					p.getInventory().setItem(4, ³uk);
					p.getInventory().setItem(22, strza³a);
					p.sendMessage(prefix + "Koniec ochrony, do walki!");
				}
		    }
		}, 405);
	}
	
	protected void zwyciêstwo(Arena arena) {
		if (arena.gracze.size() != 1) return;
		Bukkit.broadcastMessage(prefix + "§e" + arena.gracze.get(0).getName() + "§6 wygrywa rozgrywkê!");
		opuœæ(arena.gracze.get(0), false);
		ochrona = true;
		arena.grane = false;
	}
	
	@EventHandler
	public void postrzelenie(ProjectileHitEvent ev) {
		if (!(ev.getHitEntity() instanceof Player && !ochrona)) return;
		Player p = (Player) ev.getHitEntity();
		
		if (arenaGracza(p) != null)
			opuœæ(p, true);
	}
	
}

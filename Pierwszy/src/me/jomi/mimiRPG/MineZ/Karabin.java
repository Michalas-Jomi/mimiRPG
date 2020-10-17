package me.jomi.mimiRPG.MineZ;

import java.util.Map;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowalne;
import me.jomi.mimiRPG.Mapowane;

public class Karabin extends Mapowalne {
	@Mapowane String nazwa = "abc";
	@Mapowane ItemStack item;
	@Mapowane ItemStack ammo;
	@Mapowane double dmg;
	@Mapowane double attackCooldown; // w sekundach
	
	public Karabin(Map<String, Object> mapa) {
		super(mapa);
		Karabiny.karabiny.put(nazwa, this);
	}
	
	void strzel(Player p) {
		if (!minąłCooldown(p)) return;
		if (!zabierzPocisk(p)) return; // XXX info o braku ammo
		Vector wzrok = p.getLocation().getDirection();
		Arrow pocisk = (Arrow) p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.ARROW);
		pocisk.setMetadata("mimiPocisk", new FixedMetadataValue(Main.plugin, nazwa));
		pocisk.setVelocity(wzrok.multiply(10));
		// XXX dzwięk strzału
			
		if (attackCooldown > 0) 
			Func.ustawMetadate(p, "mimiKarabinCoolown" + nazwa, System.currentTimeMillis() + (attackCooldown * 1000));
	}
	private boolean minąłCooldown(Player p) {
		if (attackCooldown <= 0) return true;
		final String meta = "mimiKarabinCoolown" + nazwa;
		long następny = p.hasMetadata(meta) ? p.getMetadata(meta).get(0).asLong() : 0L;
		return następny <= System.currentTimeMillis();
	}
	private boolean zabierzPocisk(Player p) {
		if (ammo == null) return true;
		PlayerInventory inv = p.getInventory();
		for (int i=0; i<inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (Func.porównaj(ammo, item)) {
				int ile = item.getAmount() - 1;
				item.setAmount(ile);
				inv.setItem(i, ile > 0 ? item : null);
				p.updateInventory();
				return true;
			}
		}
		return false;
	}
}
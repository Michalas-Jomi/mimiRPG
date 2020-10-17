package me.jomi.mimiRPG.MineZ;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Karabin implements ConfigurationSerializable {
	@Mapowane EntityType typPocisku = EntityType.ARROW;
	@Mapowane String nazwa = "Karabin";
	@Mapowane double attackCooldown; // w sekundach
	@Mapowane double siłaStrzału = 3;
	@Mapowane int przybliżenie = 1;
	@Mapowane double dmg = 2;
	@Mapowane ItemStack item;
	@Mapowane ItemStack ammo;
	
	public Karabin(Map<String, Object> mapa) {
		Func.zdemapuj(this, mapa);
		Karabiny.karabiny.put(nazwa, this);
	}
	@Override
	public Map<String, Object> serialize() {
		return Func.zmapuj(this);
	}
	
	void strzel(Player p) {
		if (!minąłCooldown(p)) return;
		if (!zabierzPocisk(p)) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cBrak amunicji"));
			return;
		}
		Vector wzrok = p.getLocation().getDirection();
		Projectile pocisk = (Projectile) p.getWorld().spawnEntity(p.getEyeLocation(), typPocisku);
		Func.ustawMetadate(pocisk, "mimiPocisk", nazwa);
		pocisk.setVelocity(wzrok.multiply(siłaStrzału));
		pocisk.setGravity(false);
		pocisk.setShooter(p);
		
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

	public void przybliż(Player p) {
		if (odbliż(p)) return;
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*60*60*2, przybliżenie, false, false, false));
		Func.ustawMetadate(p, "mimiKarabinPrzybliżenie", true);
	}
	public static boolean odbliż(HumanEntity p) {
		if (p.hasMetadata("mimiKarabinPrzybliżenie")) {
			p.removePotionEffect(PotionEffectType.SLOW);
			p.removeMetadata("mimiKarabinPrzybliżenie", Main.plugin);
			return true;
		}
		return false;
	}
}


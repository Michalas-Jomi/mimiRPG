package me.jomi.mimiRPG.Maszyny;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Cena;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;

@Moduł
public class Kolektor extends ModułMaszyny {
	public static class Maszyna extends ModułMaszyny.Maszyna {
		@Mapowane int lvlZasięg;
		@Mapowane String uuidNapis;

		public int getZasięg() {
			return ulepszeniaZasięgu.get(lvlZasięg).b;
		}
		
		@Override
		protected void zlikwiduj() {
			super.zlikwiduj();
			Func.wykonajDlaNieNull(Bukkit.getEntity(UUID.fromString(uuidNapis)), Entity::remove);
		}
		
		@Override
		protected void wykonaj() {
			if (getContainer().getInventory().firstEmpty() == -1)
				return;
			
			int zasięg = getZasięg();
			Collection<Item> itemy = Func.pewnyCast(locShulker.getWorld().getNearbyEntities(locShulker, zasięg, zasięg, zasięg, e -> e instanceof Item));
			
			itemy.forEach(item -> {
				if (getContainer().getInventory().addItem(item.getItemStack()).isEmpty());
					item.remove();
			});
		}
		@Override
		public ModułMaszyny getModuł() {
			return inst;
		}
	}
	
	static Kolektor inst;
	public Kolektor() {
		inst = this;
	}

	@Override
	public Material getShulkerType() {
		return Material.YELLOW_SHULKER_BOX;
	}

	@Override
	public Maszyna postawMaszyne(Player p, Location loc) {
		Maszyna maszyna = new Maszyna();
		maszyna.locShulker = loc.clone();
		
		ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(.5, 0, .5), EntityType.ARMOR_STAND);
		armorStand.setInvulnerable(true);
		armorStand.setSmall(true);
		armorStand.setVisible(false);
		armorStand.setGravity(false);
		armorStand.setRemoveWhenFarAway(false);
		for (EquipmentSlot slot : EquipmentSlot.values())
			armorStand.addEquipmentLock(slot, LockType.REMOVING_OR_CHANGING);
		armorStand.setCustomName(Func.losujPrzejścieKolorów("&lKolektor"));
		armorStand.setCustomNameVisible(true);
		maszyna.uuidNapis = armorStand.getUniqueId().toString();
		
		return maszyna;
	}

	static List<Krotka<Cena, Integer>> ulepszeniaZasięgu = new ArrayList<>();
	@Override
	protected Map<ItemStack, BiConsumer<Player, ModułMaszyny.Maszyna>> getFunkcjePanelu (ModułMaszyny.Maszyna m) {
		Map<ItemStack, BiConsumer<Player, ModułMaszyny.Maszyna>> funkcjePanelu = new HashMap<>();

		funkcjePanelu.put(
				super.standardowyItemFunckjiPrędkości(m),
				super::standardowaFunckjaPrędkości
				);
		
		funkcjePanelu.put(
				super.fabrykatorItemów(
						Material.PRISMARINE_CRYSTALS,
						"Zasięg",
						((Maszyna) m).lvlZasięg,
						ulepszeniaZasięgu,
						ile -> ile + "m"
						),
				super.fabrykatorFunkcji(
						ulepszeniaZasięgu,
						maszyna -> ((Maszyna) maszyna).lvlZasięg,
						maszyna -> ((Maszyna) maszyna).lvlZasięg++
						)
		);
		
		return funkcjePanelu;
	}
	
	@Override
	public void przeładuj() {
		super.przeładuj();
		wczytajUlepszeniaStandardowo(ulepszeniaZasięgu, "zasięg", "zasięg");
	}
}

	


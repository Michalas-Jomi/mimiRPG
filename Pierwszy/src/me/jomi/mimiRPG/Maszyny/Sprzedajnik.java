package me.jomi.mimiRPG.Maszyny;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.PojedynczeKomendy.Sklep;
import me.jomi.mimiRPG.util.Func;

@Moduł(priorytet = Moduł.Priorytet.NISKI)
public class Sprzedajnik extends ModułMaszyny {
	static boolean warunekModułu() {
		return Main.włączonyModół(Sklep.class);
	}
	
	public static class Maszyna extends ModułMaszyny.Maszyna {
		@Mapowane int ileNaRaz = 8;
		@Mapowane String właściciel;
		
		@Mapowane String uuidNapis;
		
		@Override
		protected void zlikwiduj() {
			super.zlikwiduj();
			Func.wykonajDlaNieNull(Bukkit.getEntity(UUID.fromString(uuidNapis)), Entity::remove);
		}
		
		@Override
		protected void wykonaj() {
			Container shulker = (Container) locShulker.getBlock().getState();
			
			double zarobione = 0;
			int ile = ileNaRaz;
			
			for (int i=0; i < shulker.getInventory().getSize(); i++) {
				ItemStack item = shulker.getInventory().getItem(i);
				if (item == null)
					continue;
				double cena = Sklep.getCena(item);
				if (cena != 0) {
					int możliwa = Math.min(item.getAmount(), ile);
					
					zarobione += cena * możliwa;
					ile -= możliwa;
					
					item.setAmount(item.getAmount() - możliwa);
					if (item.getAmount() < 0)
						item = null;
					shulker.getInventory().setItem(i, item);
					
					if (ile <= 0)
						break;
				}
			}
			
			double fzarobione = zarobione;
			if (zarobione != 0)
				Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> Main.econ.depositPlayer(Func.graczOffline(właściciel), fzarobione));
		}
		@Override
		public ModułMaszyny getModuł() {
			return inst;
		}
	}
	
	static Sprzedajnik inst;
	public Sprzedajnik() {
		inst = this;
	}
	
	
	private static final Map<ItemStack, BiConsumer<Player, ModułMaszyny.Maszyna>> funkcjePanelu = new HashMap<>();
	@Override
	protected Map<ItemStack, BiConsumer<Player, ModułMaszyny.Maszyna>> getFunkcjePanelu() {
		return funkcjePanelu;
	}
	@Override
	public Material getShulkerType() {
		return Material.BROWN_SHULKER_BOX;
	}
	@Override
	public ModułMaszyny.Maszyna postawMaszyne(Player p, Location loc) {
		Maszyna maszyna = new Maszyna();
		maszyna.locShulker = loc.clone();
		maszyna.właściciel = p.getName();
		
		ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(.5, 0, .5), EntityType.ARMOR_STAND);
		armorStand.setInvulnerable(true);
		armorStand.setSmall(true);
		armorStand.setVisible(false);
		armorStand.setGravity(false);
		armorStand.setRemoveWhenFarAway(false);
		for (EquipmentSlot slot : EquipmentSlot.values())
			armorStand.addEquipmentLock(slot, LockType.REMOVING_OR_CHANGING);
		armorStand.setCustomName(Func.losujPrzejścieKolorów("&lSprzedajnik"));
		armorStand.setCustomNameVisible(true);
		maszyna.uuidNapis = armorStand.getUniqueId().toString();
		
		return maszyna;
	}
}

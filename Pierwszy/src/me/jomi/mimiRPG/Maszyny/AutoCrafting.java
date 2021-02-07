package me.jomi.mimiRPG.Maszyny;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.util.EulerAngle;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class AutoCrafting extends ModułMaszyny {
	public static class Maszyna extends ModułMaszyny.Maszyna {
		@Mapowane private String recepta;
		@Mapowane List<String> armorstandy;
		
		ItemStack result;
		List<RecipeChoice> choice;
		
		
		@Override
		protected void Init() {
			odświeżRecepte();
		}
		
		@Override
		protected void zlikwiduj() {
			super.zlikwiduj();
			armorstandy.forEach(uuid ->
				Func.wykonajDlaNieNull(Bukkit.getEntity(UUID.fromString(uuid)), Entity::remove));
		}
		
		@Override
		protected void wykonaj() {
			if (result == null) {
				odświeżRecepte();
				if (result == null)
					return;
			}
			Container shulker = (Container) locShulker.getBlock().getState();
			
			Inventory inv = shulker.getInventory();
			List<ItemStack> wchłonięte = Lists.newArrayList();

			Supplier<Boolean> sup = () -> {
				Predicate<RecipeChoice> predChoice= choicer -> {
					if (choicer == null)
						return true;
					ItemStack nowy;
					for (int i=0; i < inv.getSize(); i++) {
						if ((nowy = inv.getItem(i)) == null)
							continue;
						if (choicer.test(nowy)) {
							if (nowy.getAmount() == 1)
								inv.setItem(i, null);
							else {
								nowy.setAmount(nowy.getAmount() - 1);
								inv.setItem(i, nowy);
							}
							wchłonięte.add(Func.ilość(nowy.clone(), 1));
							return true;
						}
					}
					return false;
				};
				
				for (RecipeChoice recipeChoice : choice)
					if (!predChoice.test(recipeChoice))
						return false;
				
				return true;
			};
			
			if (!sup.get() || !inv.addItem(result).isEmpty())
				wchłonięte.forEach(inv::addItem);
		}
		
		@SuppressWarnings("deprecation")
		void odświeżRecepte() {
			if (recepta == null)
				return;
			int index = recepta.indexOf(':');
			Func.wykonajDlaNieNull(Bukkit.getRecipe(new NamespacedKey(recepta.substring(0, index), recepta.substring(index + 1))), rec -> {
				result = rec.getResult();
				if (rec instanceof ShapedRecipe) {
					ShapedRecipe shapedRec = (ShapedRecipe) rec;
					
					Map<Character, Integer> ilości = new HashMap<>();
					for (String linia : shapedRec.getShape())
						for (char znak : linia.toCharArray())
							ilości.put(znak, ilości.getOrDefault(znak, 0) + 1);
					
					choice = Lists.newArrayList();
					shapedRec.getChoiceMap().forEach((znak, choicer) -> {
						if (choicer == null)
							return;
						int ile = ilości.remove(znak);
						for (int i=0; i < ile; i++)
							choice.add(choicer);
					});

					
				} else if (rec instanceof ShapelessRecipe) {
					ShapelessRecipe shapelessRec = (ShapelessRecipe) rec;
					choice = shapelessRec.getChoiceList();
				} else
					result = null;
			});
			
			armorstandy.forEach(uuid ->
				Func.wykonajDlaNieNull(Bukkit.getEntity(UUID.fromString(uuid)), en ->
					((ArmorStand) en).getEquipment().setHelmet(result == null ? new ItemStack(Material.BARRIER) : result)));
		}
		public void ustawRecepte(NamespacedKey recepta) {
			this.recepta = recepta.toString();
			zapisz();
			odświeżRecepte();
		}
		public void wybierzRecepte(Player p) {
			p.openWorkbench(p.getLocation(), true);
			mapaWybierającychRecepte.put(p.getName(), this);
		}
		
		@Override
		public ModułMaszyny getModuł() {
			return inst;
		}
	}
	
	static final Map<String, Maszyna> mapaWybierającychRecepte = new HashMap<>();

	static AutoCrafting inst;
	public AutoCrafting() {
		inst = this;
	}
	
	
	@Override
	public ModułMaszyny.Maszyna postawMaszyne(Player p, Location loc) {
		Maszyna maszyna = new Maszyna();
		maszyna.locShulker = loc.clone();
		maszyna.armorstandy = Lists.newArrayList();
		for (int i=0; i < 4; i++) {
			ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(.5, 0, .5), EntityType.ARMOR_STAND);
			armorStand.setGravity(false); armorStand.teleport(loc.clone().add(.5, 0, .5));
			armorStand.setSmall(true);
			armorStand.setVisible(false);
			armorStand.setInvulnerable(true);
			armorStand.setRotation(90 * i, 0);
			armorStand.setRemoveWhenFarAway(false);
			armorStand.setHeadPose(new EulerAngle(45, 90, 45));
			for (EquipmentSlot slot : EquipmentSlot.values())
				armorStand.addEquipmentLock(slot, LockType.REMOVING_OR_CHANGING);
			armorStand.getEquipment().setHelmet(new ItemStack(Material.BARRIER));
			maszyna.armorstandy.add(armorStand.getUniqueId().toString());
		}
		return maszyna;
	}
	
	@Override
	public Material getShulkerType() {
		return Material.BARREL;
	}
	
	@EventHandler
	public void wybieranieRecepty(CraftItemEvent ev) {
		Func.wykonajDlaNieNull(mapaWybierającychRecepte.remove(ev.getWhoClicked().getName()), maszyna -> {
			ev.setCancelled(true);
			try {
				maszyna.recepta = Func.dajMetode(ev.getRecipe().getClass(), "getKey").invoke(ev.getRecipe()).toString();
				maszyna.odświeżRecepte();
				maszyna.zapisz();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTask(Main.plugin, ev.getWhoClicked()::closeInventory);
		});
	}
	
	
	@Override
	protected Map<ItemStack, BiConsumer<Player, ModułMaszyny.Maszyna>> getFunkcjePanelu(ModułMaszyny.Maszyna m) {
		Map<ItemStack, BiConsumer<Player, ModułMaszyny.Maszyna>> funkcjePanelu = new HashMap<>();

		funkcjePanelu.put(
				super.standardowyItemFunckjiPrędkości(m),
				super::standardowaFunckjaPrędkości
				);
		funkcjePanelu.put(
				Func.stwórzItem(Material.CRAFTING_TABLE, "&6Wybierz recepture", "&aKliknij aby zmienić recepture craftingu"),
				(p, maszyna) -> ((Maszyna) maszyna).wybierzRecepte(p)
				);
		
		return funkcjePanelu;
	}
}

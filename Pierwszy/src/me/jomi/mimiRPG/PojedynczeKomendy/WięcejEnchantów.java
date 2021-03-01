package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class WięcejEnchantów implements Listener {
	public static class Timber {
		static final String tag = "mimiBlokEnchantTimber";
		
		private static boolean jestDrzewem(Material typ) {
			switch (typ) {
			case CRIMSON_STEM:
			case WARPED_STEM:
				return true;
			default:
				return typ.toString().endsWith("_LOG");
			}
		}
		private static boolean jestLiśćmi(Material mat) {
			switch (mat) {
			case NETHER_WART_BLOCK:
			case WARPED_WART_BLOCK:
			case SHROOMLIGHT:
				return true;
			default:
				return mat.toString().endsWith("_LEAVES");
			}
		}
		static void zetnijDrzewo(Location loc) {
			if (jestDrzewem(loc.getBlock().getType())) {
				List<FallingBlock> bloki = new ArrayList<>();
				zetnij(loc.getBlockX(), loc.getBlockZ(), loc, Func.losujWZasięgu(360), 0, bloki);
				tick(bloki);
			}
		}
		private static void tick(List<FallingBlock> bloki) {
			if (bloki.isEmpty())
				return;
			
			for (int i=0; i < bloki.size(); i++) {
				FallingBlock blok = bloki.get(i);
				if (blok.getLocation().getY() < 0)
					blok.remove();
				
				if (blok.isDead())
					bloki.remove(i--);
				else {
					Vector vel = blok.getVelocity();
					blok.setVelocity(new Vector(vel.getX(), (vel.getY() - .02) * 1.05, vel.getZ()));
					blok.setTicksLived(1);
				}
			}
			
			Func.opóznij(3, () -> tick(bloki));
		}
		private static void zetnij(int startX, int startZ, Location loc, int rotacja, int moc, List<FallingBlock> bloki) {
			if (	Math.abs(loc.getBlockX() - startX) > 5 ||
					Math.abs(loc.getBlockZ() - startZ) > 5)
				return;
			Material mat = loc.getBlock().getType();
			if (jestDrzewem(mat) || jestLiśćmi(mat)) {
				loc.getBlock().setType(Material.AIR);
				bloki.add(zresp(loc, mat, rotacja, moc));

				zetnij(startX, startZ, loc.clone().add(0, 1, 0),  rotacja, moc + 1, bloki);
				zetnij(startX, startZ, loc.clone().add(1, 0, 0),  rotacja, moc, 	bloki);
				zetnij(startX, startZ, loc.clone().add(0, 0, 1),  rotacja, moc, 	bloki);
				zetnij(startX, startZ, loc.clone().add(-1, 0, 0), rotacja, moc, 	bloki);
				zetnij(startX, startZ, loc.clone().add(0, 0, -1), rotacja, moc, 	bloki);
			}
		}
		private static FallingBlock zresp(Location loc, Material mat, int rotacja, int moc) {
			FallingBlock fb = loc.getWorld().spawnFallingBlock(loc, Bukkit.createBlockData(mat));
			fb.setGravity(false);
			fb.setDropItem(true);
			fb.setRotation(rotacja, 0);
			Vector vel = fb.getLocation().getDirection().multiply(.1);
			fb.setVelocity(vel.add(vel.clone().multiply(.5).multiply(moc)));
			fb.addScoreboardTag(tag);
			return fb;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void niszczenieBloku(BlockBreakEvent ev) {
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		// TODO napisać jako enchant
		if (!ev.isCancelled() && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains("§7Timber")) {
			Timber.zetnijDrzewo(ev.getBlock().getLocation().add(.5, 0, .5));
			ev.setDropItems(false);
		}
	}
	@EventHandler
	public void stawianieBloku(EntityChangeBlockEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains(Timber.tag)) {
			ev.setCancelled(true);
			timberDrop(ev.getEntity().getLocation(), ((FallingBlock) ev.getEntity()).getBlockData().getMaterial());
		}
	}
	@EventHandler
	public void wypadanieBloku(EntityDropItemEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains(Timber.tag)) {
			ev.setCancelled(true);
			timberDrop(ev.getEntity().getLocation(), ev.getItemDrop().getItemStack().getType());
		}
	}
	private void timberDrop(Location loc, Material mat) {
		FallingBlock bloczek = loc.getWorld().spawnFallingBlock(loc, Bukkit.createBlockData(mat));
		bloczek.setGravity(false);

		Func.particle(Particle.CLOUD, loc, 5, .5, .5, .5, .1);
		loc.getWorld().playSound(loc, Timber.jestDrzewem(mat) ? Sound.BLOCK_WOOD_BREAK : Sound.BLOCK_GRASS_BREAK, (float) Func.losuj(.1, .5), (float) Func.losuj(.5, 1.5));

		loc.getWorld().getNearbyEntities(loc, 1, 1, 1, e -> e instanceof LivingEntity).forEach(e -> ((LivingEntity) e).damage(1, bloczek));

		Func.opóznij(50, () -> {
			bloczek.remove();
			
			Block blok = loc.getBlock();
			
			Material stare = blok.getType();
			BlockData data = blok.getBlockData();
			
			blok.setType(mat, false);
			blok.breakNaturally();
			blok.setType(stare, false);
			blok.setBlockData(data);
		});
	}

	
	// Zaciśnięte więzi
	@EventHandler
	public void niszczenieItemku(BlockBreakEvent ev) {
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
		if (!item.getItemMeta().getLore().contains("§7Zaciśnięte więzi")) return;
		if (!(item.getItemMeta() instanceof Damageable)) return;
		Damageable meta = (Damageable) item.getItemMeta();
		if (meta.getDamage() + 1 >= item.getType().getMaxDurability()) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(Func.prefix("Zaciśnięte więzi") + "twoje narzędzie jest już na wykończeniu, uważaj na nie!");
		}
	}
}

package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class WięcejEnchanment implements Listener {
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
				return true;
			default:
				return mat.toString().endsWith("_LEAVES");
			}
		}
		static void zetnijDrzewo(Location loc) {
			if (jestDrzewem(loc.getBlock().getType())) {
				Vector vel = new Vector(Func.losuj(-1, 1), 0, Func.losuj(-1,  1)).multiply(.1);
				List<FallingBlock> bloki = new ArrayList<>();
				zetnij(loc.getBlockX(), loc.getBlockZ(), loc, vel, bloki);
				tick(bloki);
			}
		}
		private static void tick(List<FallingBlock> bloki) {
			if (bloki.isEmpty())
				return;
			
			for (int i=0; i < bloki.size(); i++) {
				FallingBlock blok = bloki.get(i);
				if (blok.isDead())
					bloki.remove(i--);
				else {
					Vector vel = blok.getVelocity();
					blok.setVelocity(new Vector(vel.getX(), vel.getY() - .02, vel.getZ()));
					blok.setTicksLived(1);
				}
			}
			
			Func.opóznij(3, () -> tick(bloki));
		}
		private static void zetnij(int startX, int startZ, Location loc, Vector vel, List<FallingBlock> bloki) {
			if (	Math.abs(loc.getBlockX() - startX) > 5 ||
					Math.abs(loc.getBlockZ() - startZ) > 5)
				return;
			Material mat = loc.getBlock().getType();
			if (jestDrzewem(mat) || jestLiśćmi(mat)) {
				loc.getBlock().setType(Material.AIR);
				bloki.add(zresp(loc, mat, vel));

				UnaryOperator<Double> plus = x -> x + .03 * ( x > 0 ? 1 : -1);
				zetnij(startX, startZ, loc.clone().add(0, 1, 0),  new Vector(plus.apply(vel.getX()), vel.getY(), plus.apply(vel.getZ())), bloki);
				zetnij(startX, startZ, loc.clone().add(1, 0, 0),  vel, bloki);
				zetnij(startX, startZ, loc.clone().add(0, 0, 1),  vel, bloki);
				zetnij(startX, startZ, loc.clone().add(-1, 0, 0), vel, bloki);
				zetnij(startX, startZ, loc.clone().add(0, 0, -1), vel, bloki);
			}
		}
		private static FallingBlock zresp(Location loc, Material mat, Vector vel) {
			FallingBlock fb = loc.getWorld().spawnFallingBlock(loc, Bukkit.createBlockData(mat));
			fb.setGravity(false);
			fb.setDropItem(true);
			fb.setVelocity(vel);
			fb.addScoreboardTag(tag);
			return fb;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void niszczenieBloku(BlockBreakEvent ev) {
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		// TODO napisać jako enchant
		if (!ev.isCancelled() && item.getItemMeta().getLore().contains("§7Timber")) {
			Timber.zetnijDrzewo(ev.getBlock().getLocation());
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
}

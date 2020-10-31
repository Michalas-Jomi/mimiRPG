package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Ciąg;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class AirDrop extends Komenda implements Listener, Przeładowalny {
	class Drop {
		int x;
		int z;
		List<FallingBlock> bloki = Lists.newArrayList();
		Drop(Location loc) {
			x = loc.getBlockX();
			z = loc.getBlockZ();
			przywołaj(new Location(loc.getWorld(), x, loc.getY(), z));
		}

		void przywołaj(Location loc) {
			World świat = loc.getWorld();
			Consumer<Material> cztery = typ -> {
				zrespBlok(świat, loc.add(0, 1, -1), typ);
				zrespBlok(świat, loc.add(1, 0, 0), typ);
				zrespBlok(świat, loc.add(0, 0, 1), typ);
				zrespBlok(świat, loc.add(-1, 0, 0), typ);
			};
			cztery.accept(Material.BEEHIVE);
			cztery.accept(Material.BARREL);
			cztery.accept(Material.BEEHIVE);
			cztery.accept(Material.CHAIN);
			cztery.accept(Material.CHAIN);
			cztery.accept(Material.LIGHT_BLUE_WOOL);

			zrespBlok(świat, loc.add(0, -1, -2), Material.LIGHT_BLUE_WOOL);
			zrespBlok(świat, loc.add(1, 0, 0), Material.LIGHT_BLUE_WOOL);
			
			zrespBlok(świat, loc.add(1, 0, 1), Material.LIGHT_BLUE_WOOL);
			zrespBlok(świat, loc.add(0, 0, 1), Material.LIGHT_BLUE_WOOL);
			
			zrespBlok(świat, loc.add(-1, 0, 1), Material.LIGHT_BLUE_WOOL);
			zrespBlok(świat, loc.add(-1, 0, 0), Material.LIGHT_BLUE_WOOL);
			
			zrespBlok(świat, loc.add(-1, 0, -1), Material.LIGHT_BLUE_WOOL);
			zrespBlok(świat, loc.add(0, 0, -1), Material.LIGHT_BLUE_WOOL);
			
		}
		void zrespBlok(World świat, Location loc, Material typ) {
			FallingBlock blok = świat.spawnFallingBlock(loc, Bukkit.createBlockData(typ));
			blok.setDropItem(false);
			blok.setHurtEntities(false);
			Func.ustawMetadate(blok, metaId, this);
			bloki.add(blok);
		}
	
		void rozbij(int y) {
			World świat = bloki.get(0).getWorld();
			for (FallingBlock blok : bloki)
				blok.remove();
			świat.spawnParticle(Particle.CLOUD, x, y, z, 200, 2, 2, 2, 1);
			zbuduj(new Location(świat, x, y, z));
		}
		void zbuduj(Location loc) {
			loc.add(0, 0, 0).getBlock().setType(Material.BEEHIVE);
			loc.add(1, 0, 0).getBlock().setType(Material.BEEHIVE);
			loc.add(0, 0, 1).getBlock().setType(Material.BEEHIVE);
			loc.add(-1, 0, 0).getBlock().setType(Material.BEEHIVE);
			
			ustawDrop(loc.add(0, 1, -1).getBlock());
			ustawDrop(loc.add(1, 0, 0).getBlock());
			ustawDrop(loc.add(0, 0, 1).getBlock());
			ustawDrop(loc.add(-1, 0, 0).getBlock());
			
			loc.add(0, 1, -1).getBlock().setType(Material.BEEHIVE);
			loc.add(1, 0, 0).getBlock().setType(Material.BEEHIVE);
			loc.add(0, 0, 1).getBlock().setType(Material.BEEHIVE);
			loc.add(-1, 0, 0).getBlock().setType(Material.BEEHIVE);
		}
		void ustawDrop(Block blok) {
			blok.setType(Material.BARREL, false);
			Barrel skrzynia = (Barrel) blok.getState();
			ustawDrop(skrzynia.getInventory());
			skrzynia.setCustomName("§cAir Drop");
			skrzynia.update();
		}
		void ustawDrop(Inventory inv) {
			if (dropy.ciąg.wielkość() > 0)
				for (int i=0; i<inv.getSize(); i++)
					inv.setItem(i, dropy.ciąg.losuj());
		}
	}
	static final String metaId = "mimiAriDrop";
	
	Config config = new Config("AirDropy");
	static Dropy dropy;

	public AirDrop() {
		super("airdrop");
	}
	
	public static class Box extends Mapowany {
		@Mapowane ItemStack item;
		@Mapowane int szansa;
	}
	public static class Dropy extends Mapowany {
		@Mapowane List<Box> dropy;
		
		Ciąg<ItemStack> ciąg = new Ciąg<>();
		
		public void Init() {
			for (Box box : dropy)
				ciąg.dodaj(box.item, box.szansa);
		}
	}
	
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new Drop(((Player) sender).getLocation());
		return true;
	}
	@EventHandler
	public void __(EntityChangeBlockEvent ev) {
		if (!ev.getEntity().hasMetadata(metaId)) return;
		Drop drop = (Drop) ev.getEntity().getMetadata(metaId).get(0).value();
		drop.rozbij(ev.getBlock().getY());
	}

	
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		dropy = (Dropy) config.wczytaj("dropy");
	}

	@Override
	public Krotka<String, Object> raport() {
		int x = 0;
		try {
			x = dropy.ciąg.wielkość();
		} catch(Throwable e) {}
		return Func.r("Wczytane dropy", x);
	}
}

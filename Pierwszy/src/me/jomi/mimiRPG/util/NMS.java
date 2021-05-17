package me.jomi.mimiRPG.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.World;

public class NMS {
	public static EntityPlayer nms(Player p) {
		return ((CraftPlayer) p).getHandle();
	}
	public static IBlockData nms(Block blok) {
		return ((CraftBlock) blok).getNMS();
	}
	public static Entity nms(org.bukkit.entity.Entity mob) {
		return ((CraftEntity) mob).getHandle();
	}
	public static World nms(org.bukkit.World świat) {
		return ((CraftWorld) świat).getHandle();
	}
	
	public static Location loc(org.bukkit.World world, BlockPosition pos) {
		return new Location(world, pos.getX(), pos.getY(), pos.getZ());
	}
}

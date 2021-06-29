package me.jomi.mimiRPG.util;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_17_R1.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;

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
	public static ItemStack nms(org.bukkit.inventory.ItemStack item) {
		try {
			return (ItemStack) Func.dajZField(item, "handle");
		} catch (Throwable e) {
			return null;
		}
	}
	public static BlockPosition nms(Location loc) {
		return new BlockPosition(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public static void wyślij(Player p, Packet<?> packet) {
		nms(p).b.sendPacket(packet);
	}
	
	public static Location loc(org.bukkit.World world, BlockPosition pos) {
		return new Location(world, pos.getX(), pos.getY(), pos.getZ());
	}


	public static PersistentDataContainer utwórzDataContainer() {
		return new CraftPersistentDataContainer(new CraftPersistentDataTypeRegistry());
	}
	public static PersistentDataContainer utwórzDataContainer(NBTTagCompound tag) {
		return new CraftPersistentDataContainer(getRaw(tag), new CraftPersistentDataTypeRegistry());
	}

	public static <T, Z> void set(PersistentDataContainer data, String key, PersistentDataType<T, Z> type, Z value) {
		CraftPersistentDataContainer _data = (CraftPersistentDataContainer) data;
		_data.getRaw().put(key.toString(), _data.getDataTagTypeRegistry().wrap(type.getPrimitiveType(), type.toPrimitive(value, data.getAdapterContext())));
	}
	public static <T, Z> Z get(PersistentDataContainer data, String key, PersistentDataType<T, Z> type) {
		CraftPersistentDataContainer _data = (CraftPersistentDataContainer) data;
		NBTBase value = _data.getRaw().get(key.toString());
		return value == null ? null : type.fromPrimitive(_data.getDataTagTypeRegistry().extract(type.getPrimitiveType(), value), data.getAdapterContext());
	}
	public static <T, Z> Z get(PersistentDataContainer data, String key, PersistentDataType<T, Z> type, Z domyślna) {
		Z w = get(data, key, type);
		return w == null ? domyślna : w;
	}
	public static Map<String, NBTBase> getRaw(PersistentDataContainer data) {
		return ((CraftPersistentDataContainer) data).getRaw();
	}
	@SuppressWarnings("unchecked")
	public static Map<String, NBTBase> getRaw(NBTTagCompound tag) {
		try {
			return (Map<String, NBTBase>) Func.dajZField(tag, "x");
		} catch (Throwable e) {
			throw Func.throwEx(e);
		}
	}
	public static NBTTagCompound tag(PersistentDataContainer data) {
		return ((CraftPersistentDataContainer) data).toTagCompound();
	}
}

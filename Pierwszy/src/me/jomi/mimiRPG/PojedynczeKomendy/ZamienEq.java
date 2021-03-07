package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R2.NBTTagCompound;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class ZamienEq extends Komenda{
	public static final String prefix = Func.prefix("Zamiana eq");

	public ZamienEq() {
		super("zamieńeq", prefix + "/zamieńeq <nick> (nick)");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		if (istnieje(sender, args[0]))
			if (args.length >= 2 && istnieje(sender, args[1]))
				zamień(sender, args[0], args[1]);
			else if (args.length == 1) {
				if (!(sender instanceof Player))
					return false;
				zamień(sender, sender.getName(), args[0]);
			}
		return true;
	}
	

	private void zamień(CommandSender sender, String nick1, String nick2) {
		if (nick1.equals(nick2)) {
			sender.sendMessage(prefix + "Nie możesz zamiń eq gracza z nim samym");
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(Main.plugin, () -> {
			String u1 = Func.graczOffline(nick1).getUniqueId().toString();
			String u2 = Func.graczOffline(nick2).getUniqueId().toString();

			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				try {
					Player p1, p2;
					p1 = Bukkit.getPlayer(nick1);
					p2 = Bukkit.getPlayer(nick2);
					Func.wykonajDlaNieNull(p1, Player::saveData);
					Func.wykonajDlaNieNull(p2, Player::saveData);

					File f1 = new File("world/playerdata/" + u1 + ".dat");
					File f2 = new File("world/playerdata/" + u2 + ".dat");

					NBTTagCompound tag1 = NBTCompressedStreamTools.a(f1);
					NBTTagCompound tag2 = NBTCompressedStreamTools.a(f2);
					
					f1.delete(); f1.createNewFile();
					f2.delete(); f2.createNewFile();

					NBTBase UUID1 = tag1.get("UUID");
					NBTBase UUID2 = tag2.get("UUID");
					tag1.set("UUID", UUID2);
					tag2.set("UUID", UUID1);
					
					NBTTagCompound nbtbukkit1 = tag1.getCompound("bukkit");
					NBTTagCompound nbtbukkit2 = tag2.getCompound("bukkit");
					String pNick1 = nbtbukkit1.getString("lastKnownName");
					String pNick2 = nbtbukkit2.getString("lastKnownName");
					nbtbukkit1.setString("lastKnownName", pNick2);
					nbtbukkit2.setString("lastKnownName", pNick1);
					tag1.set("bukkit", nbtbukkit1);
					tag2.set("bukkit", nbtbukkit2);
					
					NBTCompressedStreamTools.a(tag1, f2);
					NBTCompressedStreamTools.a(tag2, f1);
					
					Func.wykonajDlaNieNull(p1, Player::loadData);
					Func.wykonajDlaNieNull(p2, Player::loadData);
					
					Main.log(prefix + "Zamieniono eq, ec i exp graczy", nick1, nick2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			});
		});
	}
	private boolean istnieje(CommandSender p, String nick) {
		if (Func.graczOffline(nick) != null)
			return true;
		p.sendMessage(prefix + "Gracz " + nick + " nigdy nie był online!");
		return false;
	}
	

	/*
	NBTTagCompound tag(String uuid) {
		return ((CraftServer) Bukkit.getServer()).getHandle().playerFileData.getPlayerData(uuid);
	}
	Map<Integer, ItemStack> wczytajEc (NBTTagCompound tag) { return wczytajItemy(tag, "EnderItems"); }
	Map<Integer, ItemStack> wczytajInv(NBTTagCompound tag) { return wczytajItemy(tag, "Inventory"); }
	private Map<Integer, ItemStack> wczytajItemy(NBTTagCompound tag, String klucz) {
		HashMap<Integer, ItemStack> mapa = new HashMap<>();
		
		for (NBTBase nbt : (NBTTagList) tag.get(klucz))
			mapa.put(((NBTTagCompound) nbt).getInt("Slot"), CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R2.ItemStack.a((NBTTagCompound) nbt)));
		
		return mapa;
	}
	*/
}

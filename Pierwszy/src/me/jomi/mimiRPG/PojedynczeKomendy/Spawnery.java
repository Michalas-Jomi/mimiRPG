package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftCreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagList;
import net.minecraft.server.v1_16_R2.TileEntityMobSpawner;

public class Spawnery extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Spawner");
	private static final HashMap<String, String> mapa = new HashMap<>();
	private static final List<String> tłumaczenia = Lists.newArrayList();
	private static final List<String> typy = Lists.newArrayList();
	
	private static int MaxNearbyEntities;
	private static int MaxSpawnDelay;
	private static int SpawnCount;
	private static int SpawnRange;
	private static int MinSpawnDelay;
	private static int RequiredPlayerRange;
	
	public Spawnery() {
		super("spawner");
		for (EntityType en : EntityType.values())
			typy.add(en.toString());
	}
	public void przeładuj() {
		mapa.clear();
		tłumaczenia.clear();
		Config config = Main.ust;
		for (String klucz : config.sekcja("Spawnery", "tłumaczenia").getKeys(false)) {
				String wartość = config.wczytajStr("Spawnery", "tłumaczenia", klucz);
				if (!typy.contains(klucz.toUpperCase()))
					Main.plugin.getLogger().warning(prefix + "Nie odnaleziono moba " + klucz);
				mapa.put(klucz, wartość);
				mapa.put(wartość, klucz);
				tłumaczenia.add(wartość);
			}
		SpawnCount 			= (int) config.wczytaj("Spawnery", "SpawnCount");
		SpawnRange 			= (int) config.wczytaj("Spawnery", "SpawnRange");
		MinSpawnDelay 		= (int) config.wczytaj("Spawnery", "MinSpawnDelay");
		MaxSpawnDelay 		= (int) config.wczytaj("Spawnery", "MaxSpawnDelay");
		MaxNearbyEntities 	= (int) config.wczytaj("Spawnery", "MaxNearbyEntities");
		RequiredPlayerRange = (int) config.wczytaj("Spawnery", "RequiredPlayerRange");
	}
	public String raport() {
		return "§6Spawnery: §e" + tłumaczenia.size();
	}
	
	private static String dajNazwe(String klucz) {
		if (mapa.containsKey(klucz))
			return mapa.get(klucz);
		return klucz;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void stwianieBloków(BlockPlaceEvent ev) {
		if (ev.isCancelled() || ev.getBlock() == null || !(ev.getBlock().getState() instanceof CraftCreatureSpawner)) return;
		CraftCreatureSpawner sspawner = (CraftCreatureSpawner) ev.getBlock().getState();
		ItemStack item = ev.getItemInHand();
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return;
		List<String> lore = item.getItemMeta().getLore();
		if (lore.size() < 3) return;
		if (!lore.get(0).equals("§bNiezwykły blok")) return;
		if (!lore.get(1).equals("§bWokół niego pojawiają się moby")) return;
		String nazwa = lore.get(2).split(" to§d ")[1];
 		sspawner.setRequiredPlayerRange(RequiredPlayerRange);
 		sspawner.setMaxNearbyEntities(MaxNearbyEntities);
 		sspawner.setMaxSpawnDelay(MaxSpawnDelay);
 		sspawner.setMinSpawnDelay(MinSpawnDelay);
 		sspawner.setSpawnCount(SpawnCount);
 		sspawner.setSpawnRange(SpawnRange);
		sspawner.update();
 		
 		BlockPosition blockPos = new BlockPosition(ev.getBlock().getX(), ev.getBlock().getY(), ev.getBlock().getZ());
 		TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) sspawner.getWorld()).getHandle().getTileEntity(blockPos);
 		NBTTagCompound spawnerTag = spawner.b();
 		
 		NBTTagCompound spawnData = new NBTTagCompound();
 		spawnData.setString("id", dajNazwe(nazwa));
 		
 		NBTTagList attributes = new NBTTagList();
 		attributes.add(dajAtrybut("generic.movement_speed", 0));
 		attributes.add(dajAtrybut("generic.attack_damage", 0));
 		spawnData.set("Attributes", attributes);
 		
 		spawnerTag.set("SpawnData", spawnData);
 		spawner.load(spawner.getBlock(), spawnerTag);
	}
	private static NBTTagCompound dajAtrybut(String nazwa, double wartość) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("Name", "minecraft:" + nazwa);
		nbt.setDouble("Base", wartość);
		return nbt;
	}
	@EventHandler(priority=EventPriority.HIGH)
	public void niszczenieBloków(BlockBreakEvent ev) {
		Block blok = ev.getBlock();
		if (blok == null || blok.getState() == null || ev.isCancelled() || !(blok.getState() instanceof CraftCreatureSpawner)) return;
		Player p = ev.getPlayer();
		if (p != null && p.getGameMode().equals(GameMode.CREATIVE)) return;
		ev.setDropItems(false);
		ev.setExpToDrop(0);
		CraftCreatureSpawner spawner = (CraftCreatureSpawner) blok.getState();
		ItemStack item = dajSpawner(spawner.getCreatureTypeName());
		if (p != null && p.getInventory().firstEmpty() != -1)
			p.getInventory().addItem(item);
		else
			blok.getWorld().dropItem(blok.getLocation(), item);
	}
	
	private static ItemStack dajSpawner(String nazwa) {
		if (!tłumaczenia.contains(nazwa))
			nazwa = dajNazwe(nazwa);
		ItemStack item = Func.stwórzItem(Material.SPAWNER, 1, "§cSpawner " + nazwa);
		Func.dodajLore(item, "§bNiezwykły blok");
		Func.dodajLore(item, "§bWokół niego pojawiają się moby");
		Func.dodajLore(item, "§bTroche dziwne że wszystkie to§d " + nazwa);
		return item;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return uzupełnijTabComplete(args, tłumaczenia);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Main.powiadom(sender, prefix + "Tylko gracz może stworzyć spawner");
		Player p = (Player) sender;
		
		if (args.length < 1) return Main.powiadom(p, prefix + "Brak nazwy moba");
		String nazwa = Func.listToString(args, 0);
		if (!mapa.containsKey(nazwa))
			p.sendMessage(prefix + "§cNie odnaleziono spawnera w bazie danych, upewnij się że wszystko jest ok");
		Func.dajItem(p, dajSpawner(nazwa));
		return true;
	}
	
	
}

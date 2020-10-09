package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.iridium.iridiumskyblock.User;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.MojangsonParser;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.TileEntityMobSpawner;

public class Spawnery extends Komenda implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix("Spawner");
	Config config = new Config("Spawnery");
	ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§9§l ");
	Inventory inv;
	int sloty;
	
	final HashMap<String, CreatureSpawner> panele = new HashMap<>();
	
	public Spawnery() {
		super("spawner", "/spawner <mob> (gracz)");
		Main.dodajPermisje("spawnery.bypass");
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		
		sloty = 9 * Math.max(1, Math.min(6, config.wczytajLubDomyślna("Panel.rzędy", 6)));
		
		inv = Bukkit.createInventory(null, sloty, "§9§lSpawner");
		for (int i=0; i<sloty; i++)
			inv.setItem(i, pustySlot);
		
		ConfigurationSection sekcja = config.sekcja("Panel.ulepszenia");
		if (sekcja != null)
			for (Entry<String, Object> entry : sekcja.getValues(false).entrySet()) {
				int slot = Func.Int(entry.getKey(), -1);
				if (slot <= -1 || slot >= sloty)
					Main.warn("Niepoprawy nr slotu " + entry.getKey() + " w Spawnery.yml");
				else {
					MemorySection mapa = (MemorySection) entry.getValue();
					
					ItemStack item = Config.item(mapa.get("item"));
					if (item == null) item = new ItemStack(Material.SPAWNER);
					
					double koszt = (double) mapa.get("koszt", 100d);
					
					int wartość = (int) mapa.get("wartość", 1);
					
					String parametr = (String) mapa.get("parametr", "");
					// TODO poprawność parametru sprawdzić
					
					inv.setItem(slot, Func.stwórzItem(item.getType(), wartość, "§b" + parametr, Arrays.asList(
							"§6Koszt:§e " + koszt + "$",
							"§6Poziom:§e " + wartość)));
				}
			}
		
		/*
		 * slot:
		 *   item: item
		 *   koszt: koszt
		 *   parametr: Liczebność/Zasięg/Szybkość
		 *   wartość: int
		 * 
		 * 
		 */
	}
	@Override
	public String raport() {
		int x = 0;
		try {
			x = config.sekcja("Panel.ulepszenia").getKeys(false).size();
		} catch (Exception e) {}
		return "§6wczytane ulepszenia: §e" + x;
	}
	
	void edytuj(Player p, CreatureSpawner spawner) {
		// TODO nazwa moba z dużej litery
		Inventory _inv = Func.CloneInv(inv, "§9§lSpawner §9§4" + spawner.getSpawnedType().toString().toLowerCase());
		
		int licz = 0;
		for (ItemStack item : _inv.getContents()) {
			int poziom = item.getAmount();
			boolean ench = false;
			switch (item.getItemMeta().getDisplayName()) {
			case "§bLiczebność":
				ench = poziom == spawner.getSpawnCount();
				break;
			case "§bZasięg":
				ench = poziom == spawner.getSpawnRange() - 1;
				break;
			case "§bSzybkość":
				ench = poziom == -spawner.getMaxSpawnDelay() / 100 + 9;
				break;
			}
			if (ench) {
				Func.połysk(item);
				Func.ustawLore(item, "§aZakupiono", 0);
				if (licz++ >= 3)
					break;
			}
		}
		p.openInventory(_inv);
		panele.put(p.getName(), spawner);
	}	
	void ulepsz(Player p, CreatureSpawner spawner, ItemStack item) {
		if (Func.porównaj(item, pustySlot))
			return;
		List<String> lore = item.getItemMeta().getLore();
		if (lore.get(0).equals("§aZakupiono"))
			return;
		if (!Main.ekonomia) {
			p.sendMessage(prefix + "Sory nie wypali, nie ma ekonomi na serwerze");
			return;
		}
		double kasa = Main.econ.getBalance(p);
		double cena = Func.Double(lore.get(0).substring(11, lore.get(0).length()-2), -1);
		if (cena == -1) { Main.error("Niepoprawa cena w upgr spawnerów " + item); return;}
		if (cena > kasa) {
			p.sendMessage(prefix + "§4Nie stać cię na to");
			return;
		}
		
		int poziom = item.getAmount();
		
		switch (item.getItemMeta().getDisplayName()) {
		case "§bLiczebność":
			spawner.setSpawnCount(poziom + 1);
			spawner.setMaxNearbyEntities((int) ((poziom + 1) * 2.3));
			break;
		case "§bZasięg":
			spawner.setSpawnRange(poziom + 2);
			spawner.setRequiredPlayerRange((poziom + 2) * 4);
			break;
		case "§bSzybkość":
			int maxSpawnDeley = 900 - (poziom + 1) * 5*20;
			spawner.setMaxSpawnDelay(maxSpawnDeley);
			spawner.setMinSpawnDelay(maxSpawnDeley / 4);
			break;
		}
		spawner.update();
		edytuj(p, spawner);
	}
	
	
	@EventHandler
	public void stawianie(BlockPlaceEvent ev) {
		if (!(ev.getBlock().getState() instanceof CreatureSpawner)) return;
		CraftCreatureSpawner spawner = (CraftCreatureSpawner) ev.getBlock().getState();
		NBTTagCompound tag = CraftItemStack.asNMSCopy(ev.getItemInHand()).getOrCreateTag().getCompound("BlockEntityTag");
		
		BlockPosition blockPos = new BlockPosition(ev.getBlock().getX(), ev.getBlock().getY(), ev.getBlock().getZ());
		TileEntityMobSpawner _spawner = (TileEntityMobSpawner) ((CraftWorld) spawner.getWorld())
																			.getHandle().getTileEntity(blockPos);
		_spawner.load(_spawner.getBlock(), tag);
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void niszczenie(BlockBreakEvent ev) {
		if (!ev.getBlock().getType().equals(Material.SPAWNER)) return;
		
		ev.setExpToDrop(0);
		ev.setDropItems(false);
		
		CraftCreatureSpawner spawner = (CraftCreatureSpawner) ev.getBlock().getState();
		
		Func.dajItem(ev.getPlayer(), dajItem(
				spawner.getCreatureTypeName(),
				spawner.getSpawnCount(),
				-spawner.getMaxSpawnDelay() / 100 + 9,
				spawner.getSpawnRange() - 1,
				spawner.getSnapshotNBT()
				));
		
	}
	ItemStack dajItem(String mob, int spawnCount, int maxSpawnDelay, int spawnRange, NBTTagCompound __tag) {

		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound _tag = new NBTTagCompound();
		__tag.remove("x"); __tag.remove("y"); __tag.remove("z");
		__tag.remove("Delay"); __tag.remove("SpawnPotentials"); __tag.remove("id");
		_tag.set("BlockEntityTag", __tag);
		tag.set("tag", _tag);
		tag.setInt("Count", 1);
		tag.setString("id", "spawner");
		
		ItemStack item = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R2.ItemStack.a(tag));
		
		Func.nazwij(item, "§2Spawner §a" + mob);
		Func.dodajLore(item, "§bLiczebność: §e"	+ spawnCount	+ "lvl");
		Func.dodajLore(item, "§bSzybkość: §e"	+ maxSpawnDelay	+ "lvl");
		Func.dodajLore(item, "§bZasięg: §e"		+ spawnRange	+ "lvl");
		
		return item;
	}
	@EventHandler
	public void klikanieSpawnera(PlayerInteractEvent ev) {
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		if (!ev.getPlayer().isSneaking()) return;
		if (!(blok.getState() instanceof CreatureSpawner)) return;
		CreatureSpawner spawner = (CreatureSpawner) blok.getState();
		Player p = ev.getPlayer();
		
		if (p.hasPermission("mimirpg.spawnery.bypass") || 
				(Main.iridiumSkyblock && User.getUser(p).getIsland().isInIsland(spawner.getLocation())))
			edytuj(p, spawner);
	}
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		Player p = (Player) ev.getWhoClicked();
		CreatureSpawner spawner = panele.get(p.getName());
		if (spawner == null) return;
		int slot = ev.getRawSlot();
		if (slot < 0 || slot >= sloty) return;
		ev.setCancelled(true);
		if (!ev.getClick().equals(ClickType.LEFT)) return;
		ItemStack item = ev.getCurrentItem();
		ulepsz(p, spawner, item);
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		panele.remove(ev.getPlayer().getName());
	}

	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO completer
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		
		Player p;
		if (args.length == 1)
			p = (Player) sender;
		else
			p = Bukkit.getPlayer(args[1]);
		if (p == null)
			return Main.powiadom(sender, prefix + "Nieprawidłowy gracz");
		
		try {
			EntityType.valueOf(args[0].toUpperCase());
		} catch (Exception e) {
			return Main.powiadom(sender, prefix + "Nieprawidłowy mob");
		}
		
		try {
			Func.dajItem(p, dajItem(args[0], 1, 1, 1, MojangsonParser.parse(
					"{SpawnData:{id:\""+args[0].toLowerCase()+"\"},MaxNearbyEntities:2,MinSpawnDelay:200,"
							+ "SpawnRange:2,MaxSpawnDelay:800,RequiredPlayerRange:8,SpawnCount:1}")));
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}






// Stare spawnery (bezpieczne)
/*import java.util.HashMap;
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
	
	
}*/

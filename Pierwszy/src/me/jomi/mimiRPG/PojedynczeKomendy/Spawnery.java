package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
	
	ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§9§l ");
	Inventory inv;
	int sloty;
	
	final HashMap<String, CreatureSpawner> panele = new HashMap<>();
	
	public Spawnery() {
		super("spawner", "/spawner <mob> (gracz)");
		Main.dodajPermisje("spawnery.bypass");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void przeładuj() {
		sloty = 9 * Math.max(1, Math.min(6, Main.ust.wczytajLubDomyślna("Spawnery.rzędy", 6)));
		
		inv = Bukkit.createInventory(null, sloty, "§9§lSpawner");
		for (int i=0; i<sloty; i++)
			inv.setItem(i, pustySlot);
		
		int marginesy = Main.ust.wczytajInt("Spawnery.marginesy");
		Consumer<String> ulepszenia = typ -> {
			String sc = "Spawnery.ulepszenia." + typ + ".";
			int slot = Main.ust.wczytajInt(sc + "slot");
			if (slot <= -1 || slot >= sloty)
				Main.warn("Niepoprawy nr slotu " + slot + " w Spawnery.yml");
			else {
				ItemStack item = Main.ust.wczytajItem(sc + "item");
				if (item == null) item = new ItemStack(Material.SPAWNER);
				
				List<Double> koszty = Func.nieNullList((List<Double>) Main.ust.wczytaj(sc + "koszty"));
				
				for (int wartość=1; slot<sloty && !koszty.isEmpty(); slot++) {
					if (slot % 9 + 1 <= marginesy || slot % 9 + 1 > 9 - marginesy) continue;
					Object obj = koszty.remove(0);
					double koszt = obj instanceof Double ? (double) obj : (int) obj;
					ItemStack _item = Func.stwórzItem(item.getType(), wartość, "§b" + typ, Arrays.asList(
							"§6Koszt:§e " + koszt + "$",
							"§6Poziom:§e " + wartość++));
					for (String linia : Main.ust.wczytajListe(sc + "opis"))
						Func.dodajLore(_item, Func.koloruj(linia));
					inv.setItem(slot, _item);
				}
				if (!koszty.isEmpty())
					Main.warn("Nie można zmieścić wszystkich ulepszeń w panelu, zmień początkowe sloty lub marginesy Spawnery.yml " + sc);
			}
		};

		ulepszenia.accept("Liczebność");
		ulepszenia.accept("Szybkość");
		ulepszenia.accept("Zasięg");
	}
	@Override
	public String raport() {
		int x = 0;
		try {
			x = Main.ust.sekcja("Spawnery.ulepszenia").getKeys(false).size();
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
				ench = poziom == (800 - spawner.getMaxSpawnDelay()) / (20 * mnożnik("Szybkość")) + 1;
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

		double mnożnik;
		switch (item.getItemMeta().getDisplayName()) {
		case "§bLiczebność":
			spawner.setSpawnCount(poziom + 1);
			mnożnik = mnożnik("Liczebność");
			spawner.setMaxNearbyEntities((int) ((poziom + 1) * mnożnik));
			break;
		case "§bZasięg":
			spawner.setSpawnRange(poziom + 2);
			mnożnik = mnożnik("Zasięg");
			spawner.setRequiredPlayerRange((int) ((poziom + 1) * mnożnik));
			break;
		case "§bSzybkość":
			mnożnik = mnożnik("Szybkość");
			double maxSpawnDeley = 800 - poziom * mnożnik*20;;
			spawner.setMaxSpawnDelay((int) maxSpawnDeley);
			mnożnik = Main.ust.wczytajDouble("Spawnery.ulepszenia.Szybkość.dzielnik");
			spawner.setMinSpawnDelay((int) (maxSpawnDeley / mnożnik));
			break;
		}
		spawner.update();
		edytuj(p, spawner);
	}
	double mnożnik(String typ) {
		return Main.ust.wczytajDouble("Spawnery.ulepszenia." + typ + ".mnożnik");
		
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
		if (!ev.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		if (!ev.getPlayer().isSneaking()) return;
		if (!(blok.getState() instanceof CreatureSpawner)) return;
		CreatureSpawner spawner = (CreatureSpawner) blok.getState();
		Player p = ev.getPlayer();
		
		if (p.hasPermission("mimirpg.spawnery.bypass") || 
				(Main.iridiumSkyblock && User.getUser(p).getIsland().isInIsland(spawner.getLocation()))) {
			edytuj(p, spawner);
			ev.setCancelled(true);
		}
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
		// TODO completer i polski nazwy
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
					String.format("{SpawnData:{id:\"minecraft:%s\"},MaxNearbyEntities:%ss,"
							+ "MinSpawnDelay:200s,SpawnRange:2s,MaxSpawnDelay:800s,RequiredPlayerRange:%ss,SpawnCount:1s}",
							args[0].toLowerCase(), (int) mnożnik("Liczebność"), (int) mnożnik("Zasięg")))));
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}

/*
  # <id moba>: <polska nazwa>
  # Moby nieuwzględnione tu, nie będą tłumaczone
  # ani wyświetlać się pod tabem
  tłumaczenia:
    blaze: blaze
    cat: kot
    chicken: kurczak
    cow: krowa
    creeper: creeper
    enderman: enderman
    evoker: evoker
    ghast: ghast
    guardian: guardian
    horse: koń
    iron_golem: żelazny golem
    magma_cube: kostka magmy
    parrot: papuga
    pig: świnia
    rabbit: zając
    sheep: owca
    skeleton: szkielet
    slime: szlam
    snowman: bałwan
    spider: pająk
    squid: kałamarnica
    strider: strider
    vindicator: windykator
    wither_skeleton: witherowy szkielet
    zoglin: zoglin
    zombie: zombie
    zombified_piglin: zzombifikowany piglin
*/
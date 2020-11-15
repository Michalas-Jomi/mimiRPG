package me.jomi.mimiRPG.SkyBlock;

import java.util.HashMap;
import java.util.List;

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

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.MojangsonParser;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.TileEntityMobSpawner;

@Moduł
public class Spawnery extends Komenda implements Przeładowalny, Listener {
	public static class Ulepszenie extends Mapowany {
		public static class Ulepszenia extends Mapowany {
			public static class Atrybut extends Mapowany {
				@Mapowane ItemStack item;
				@Mapowane double mnoznik;
				@Mapowane List<String> opis;
				@Mapowane List<Integer> koszty;
			}
			@Mapowane Atrybut zasięg;
			@Mapowane Atrybut liczebnosc;
			@Mapowane Atrybut Szybkosc;
			@Mapowane double SzybkośćDzielnik = 3;
			@Mapowane double SzybkośćPoczatkowa = 40;
		}
		@Mapowane Ulepszenia ulepszenia;
		@Mapowane EntityType mob;
		@Mapowane double cena;
	}
	public static class ListUlepszen extends Mapowany {
		@Mapowane List<Ulepszenie> ulepszenia;
	}
	
	
	public static final String prefix = Func.prefix("Spawner");
	
	final static ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§9§l ");
	Inventory inv; // TODO usunąć
	static int sloty;
	
	final HashMap<String, CreatureSpawner> panele = new HashMap<>();
	
	// TODO ulepszanie mob1 -> mob2
	// co któryś mob resetować upgrd spawnera
	
	/*
	 * ulepszenia:
	 * - mob: <mob>
	 *   cena: <double> # opcjonalne
	 *   ulepszenia: # opcjonalne
	        # ulepszanie nie wymusza wykupywania wszystkich tierów po kolei, umożliwia wykupywanie niższych tierów
			  ulepszenia:
			    # zasięg w jakim spawner respi moby (SpawnRange)
			    # poziom + 1
			    Zasięg:
			      item: prismarine_shard
			      slot: 10
			      koszty: [100, 200.5, 300, 400]
			      # zasięg w jakim musi być gracz aby spawner działał (RequiredPlayerRange)
			      # poziom * mnożnik
			      mnożnik: 4
			      #opis # opcjonalne
			    
			    # maksymalna ilość respionych mobów na raz (SpawnCount)
			    # poziom
			    Liczebność:
			      item: prismarine_crystals
			      slot: 19
			      koszty: [100, 200, 250, 300, 500]
			      # maksymalna ilość zrespionych mobów w pobliżu (MaxNearbyEntities)
			      # poziom * mnożnik
			      mnożnik: 2.3
			      # opis na itemie # opcjonalne
			      opis:
			        - '&dLiczebność to fajna sprawa jest'
			    
			    # szybkość respienia mobów (MaxSpawnDelay / MinSpawnDelay)
			    Szybkość:
			      item: sugar
			      slot: 28
			      koszty: [100.0, 200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0]
			      # początkowa ilość sekund
			      początkowa: 40 # opcjonalne domyślnie 40
			      # ilość ujmowanych sekund na poziom (MaxSpawnDelay)
			      # (poziom - 1) * mnożnik
			      mnożnik: 4
			      # najszybsze możliwe zrespienia moba
			      # MinSpawnDelay = MaxSpawnDelay / dzielnik
			      dzielnik: 3
			      #opis # opcjonalne
	 * 
	 * 
	 * 
	 */
	
	List<Ulepszenie> ulepszenia;
	
	int marginesy;
	
	public Spawnery() {
		super("spawner", "/spawner <mob> (gracz)");
		Main.dodajPermisje("spawnery.bypass");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		Config config = new Config("configi/Spawnery");
		
		sloty = 9 * Math.max(1, Math.min(6, Main.ust.wczytajLubDomyślna("Spawnery.rzędy", 6)));
		
		inv = Bukkit.createInventory(null, sloty, "§9§lSpawner");
		for (int i=0; i<sloty; i++)
			inv.setItem(i, pustySlot);
		
		marginesy = Main.ust.wczytajInt("Spawnery.marginesy");
		
		ulepszenia = (List<Ulepszenie>) config.wczytaj("Spawnery.ulepszenia");
		
		/*Consumer<String> ulepszenia = typ -> {
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
		ulepszenia.accept("Zasięg");*/
	}
	@Override
	public Krotka<String, Object> raport() {
		// TODO raport spawnerów
		int x = 0;
		try {
			x = Main.ust.sekcja("Spawnery.ulepszenia").getKeys(false).size();
		} catch (Exception e) {}
		return Func.r("wczytane ulepszenia", x);
	}
	
	int getMaxSpawnDelay() {
		return Main.ust.wczytajLubDomyślna("Spawnery.ulepszenia.Szybkość.początkowa", 40) * 20;
	}
	int getMinSpawnDelay(int maxSpawnDelay) {
		return maxSpawnDelay / Main.ust.wczytajLubDomyślna("Spawnery.ulepszenia.Szybkość.dzielnik", 3);
	}
	
	void edytuj(Player p, CreatureSpawner spawner) {
		// TODO nazwa moba z dużej litery, tłumaczenie na polski
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
				ench = poziom == (getMaxSpawnDelay() - spawner.getMaxSpawnDelay()) / (20 * mnożnik("Szybkość")) + 1;
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

		String nazwa = item.getItemMeta().getDisplayName();
		double mnożnik = mnożnik(nazwa.substring(2));
		switch (nazwa) {
		case "§bLiczebność":
			spawner.setSpawnCount(poziom);
			spawner.setMaxNearbyEntities((int) (poziom * mnożnik));
			break;
		case "§bZasięg":
			spawner.setSpawnRange(poziom + 1);
			spawner.setRequiredPlayerRange((int) (poziom * mnożnik));
			break;
		case "§bSzybkość":
			double maxSpawnDeley = getMaxSpawnDelay() - (poziom - 1) * mnożnik*20;;
			spawner.setMaxSpawnDelay((int) maxSpawnDeley);
			spawner.setMinSpawnDelay(getMinSpawnDelay((int) maxSpawnDeley));
			break;
		}
		spawner.update();
		
		Main.econ.withdrawPlayer(p, cena);
		
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
			return Func.powiadom(sender, prefix + "Nieprawidłowy gracz");
		
		try {
			EntityType.valueOf(args[0].toUpperCase());
		} catch (Exception e) {
			return Func.powiadom(sender, prefix + "Nieprawidłowy mob");
		}
		
		try {
			int maxSD = getMaxSpawnDelay();
			Func.dajItem(p, dajItem(args[0], 1, 1, 1, MojangsonParser.parse(
					("{SpawnData:{id:\"minecraft:<mob>\"},MaxNearbyEntities:<maxNE>s,MinSpawnDelay:<minSD>s,"
					+ "SpawnRange:2s,MaxSpawnDelay:<maxSD>s,RequiredPlayerRange:<RPR>s,SpawnCount:1s}")
					.replace("<mob>", args[0].toLowerCase())
					.replace("<maxNE>", "" + (int) mnożnik("Liczebność"))
					.replace("<RPR>",	"" + (int) mnożnik("Zasięg"))
					.replace("<minSD>", "" + getMinSpawnDelay(maxSD))
					.replace("<maxSD>", "" + maxSD)
					)));
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
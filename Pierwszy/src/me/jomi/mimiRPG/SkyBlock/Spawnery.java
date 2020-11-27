package me.jomi.mimiRPG.SkyBlock;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

import com.google.common.collect.Lists;
import com.iridium.iridiumskyblock.User;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.SkyBlock.Spawnery.Ulepszenie.Ulepszenia;
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
	static int _id = 0;
	public static class Ulepszenie extends Mapowany {
		public static class Atrybut extends Mapowany {
			@Mapowane int slot;
			@Mapowane Material item = Material.SPAWNER;
			@Mapowane double mnoznik = 1;
			@Mapowane List<String> opis;
			@Mapowane List<Double> koszty;
			
			int id;
			public Atrybut() {
				id = _id++;
			}
		}
		public static class Ulepszenia extends Mapowany {
			@Mapowane Atrybut zasieg;
			@Mapowane Atrybut liczebnosc;
			@Mapowane Atrybut szybkosc;
			@Mapowane Double SzybkoscDzielnik;
			@Mapowane Integer SzybkoscPoczatkowa;
			
			int id;
			public Ulepszenia() {
				id = _id++;
			}
			public void Init() {
				SzybkoscPoczatkowa *= 20;
			}
		}
		@Mapowane Ulepszenia ulepszenia;
		@Mapowane EntityType mob = EntityType.PIG;
		@Mapowane double cena = 100;
		@Mapowane boolean broadcast = true;
		@Mapowane double szansaEwolucji = 1;
		
		// Warunek używania tego pola to stworzone inv
		boolean resetowaćNastępny = false;
		
		NBTTagCompound tag0() {
			try {
				return MojangsonParser.parse(
						("{SpawnData:{id:\"minecraft:<mob>\"},MaxNearbyEntities:<maxNE>s,MinSpawnDelay:<minSD>s,"
						+ "SpawnRange:2s,MaxSpawnDelay:<maxSD>s,RequiredPlayerRange:<RPR>s,SpawnCount:1s}")
						.replace("<mob>", mob.toString().toLowerCase())
						.replace("<maxNE>", "" + (int) ulepszenia.liczebnosc.mnoznik)
						.replace("<RPR>",	"" + (int) ulepszenia.zasieg.mnoznik)
						.replace("<minSD>", "" + minSpawnDelay(ulepszenia.SzybkoscPoczatkowa))
						.replace("<maxSD>", "" + ulepszenia.SzybkoscPoczatkowa));
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}
		void zresetuj(CreatureSpawner spawner) {
			spawner.setSpawnedType(mob);
			spawner.setMaxNearbyEntities((int) ulepszenia.liczebnosc.mnoznik);
			spawner.setMinSpawnDelay(minSpawnDelay(ulepszenia.SzybkoscPoczatkowa));
			spawner.setSpawnRange(2);
			spawner.setMaxSpawnDelay(ulepszenia.SzybkoscPoczatkowa);
			spawner.setRequiredPlayerRange((int) ulepszenia.zasieg.mnoznik);
			spawner.setSpawnCount(1);
		}
		int minSpawnDelay(int maxSpawnDeley) {
			return (int) Math.min((double) maxSpawnDeley / ulepszenia.SzybkoscDzielnik, 1);
		}
		
		private Inventory inv;
		Inventory getInv() {
			return inv != null ? inv : stwórzInv();
		}
		private Inventory stwórzInv() {
			inv = Bukkit.createInventory(null, dane.sloty, "§9§lSpawner");
			for (int i=0; i<dane.sloty; i++)
				inv.setItem(i, pustySlot);
			
			BiConsumer<Atrybut, String> ulepszenia = (attr, nazwa) -> {
				int slot = attr.slot;
				if (slot <= -1 || slot >= dane.sloty)
					Main.warn("Niepoprawy nr slotu " + slot + " w Spawnery.yml");
				else {
			
					List<Double> koszty = Lists.newArrayList(attr.koszty);
					
					for (int wartość=1; slot<dane.sloty && !koszty.isEmpty(); slot++) {
						if (slot % 9 + 1 <= dane.marginesy || slot % 9 + 1 > 9 - dane.marginesy) continue;
						Object obj = koszty.remove(0);
						double koszt = obj instanceof Double ? (double) obj : (int) obj;
						ItemStack _item = Func.stwórzItem(attr.item, wartość, "§b" + nazwa, Arrays.asList(
								"§6Koszt:§e " + koszt + "$",
								"§6Poziom:§e " + wartość++)
								);
						for (String linia : attr.opis)
							Func.dodajLore(_item, Func.koloruj(linia));
						inv.setItem(slot, _item);
					}
					if (!koszty.isEmpty())
						Main.warn("Nie można zmieścić wszystkich ulepszeń w panelu, zmień początkowe sloty lub marginesy Spawnery.yml");
				}
			};

			ulepszenia.accept(this.ulepszenia.liczebnosc, "Liczebność");
			ulepszenia.accept(this.ulepszenia.szybkosc, "Szybkość");
			ulepszenia.accept(this.ulepszenia.zasieg, "Zasięg");
			
			int i = znajdzIndex(mob);
			Ulepszenie upgr1;
			try {
				upgr1 = dane.ulepszenia.get(i + 1);
			} catch (Throwable e) {
				return inv;
			}
			
			Material mat = Material.SPAWNER;
			try {
				mat = Material.valueOf((upgr1.mob + "_spawn_egg").toUpperCase());
			} catch(Throwable e) {}
			
			ItemStack item = Func.stwórzItem(mat, "§9Ewolułuj Moba", "§bKoszt: §e" + Func.DoubleToString(upgr1.cena) + "$", "§bAktualnie: §e" + mob, "§bNastępny: §e" + upgr1.mob, "§bSzansa: §e" + Func.zaokrąglij(szansaEwolucji, 2) * 100 + "%");
			if (upgr1.ulepszenia.id != this.ulepszenia.id) {
				Func.dodajLore(item, "§cZapomni dotychczasowe ulepszenia");
				resetowaćNastępny = true;
			}
			inv.setItem(dane.slotUlepszeńMoba, item);
			return inv;
		}
	}
	public static class Dane extends Mapowany {
		@Mapowane int sloty = 6;
		@Mapowane int marginesy = 1;
		@Mapowane int slotUlepszeńMoba = 8;
		@Mapowane List<Ulepszenie> ulepszenia;
		
		public void Init() {
			sloty = Math.max(1, Math.min(6, sloty));
			sloty *= 9;
		}
	}
	
	public static final String prefix = Func.prefix("Spawner");
	final static ItemStack pustySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§9§l ");

	final HashMap<String, CreatureSpawner> panele = new HashMap<>();
	
	
	static Dane dane;
	
	
	public Spawnery() {
		super("spawner", "/spawner <mob> (gracz)");
		Main.dodajPermisje("spawnery.bypass");
	}
		
	static int znajdzIndex(EntityType mob) {
		int i=0;
		for (Ulepszenie ulepszenie : dane.ulepszenia) {
			if (ulepszenie.mob.equals(mob))
				return i;
			i++;
		}
		return -1;	
	}
	Ulepszenie znajdz(EntityType mob) {
		for (Ulepszenie ulepszenie : dane.ulepszenia)
			if (ulepszenie.mob.equals(mob))
				return ulepszenie;
		return null;
	}
	
	void edytuj(Player p, CreatureSpawner spawner) {
		// TODO nazwa moba z dużej litery, tłumaczenie na polski
		Ulepszenie upgr = znajdz(spawner.getSpawnedType());
		Inventory inv = Func.CloneInv(upgr.getInv(), "§9§lSpawner §9§4" + spawner.getSpawnedType().toString().toLowerCase());
		
		int licz = 0;
		for (ItemStack item : inv.getContents())
			if (testEnch(spawner, upgr, item.getItemMeta().getDisplayName(), item.getAmount())) {
				Func.połysk(item);
				Func.ustawLore(item, "§aZakupiono", 0);
				if (licz++ >= 3)
					break;
			}
		p.openInventory(inv);
		panele.put(p.getName(), spawner);
	}
	private boolean testEnch(CreatureSpawner spawner, Ulepszenie upgr, String str, int poziom) {
		switch(str) {
		case "§bLiczebność": return poziom == spawner.getSpawnCount();
		case "§bSzybkość":	 return poziom == (upgr.ulepszenia.SzybkoscPoczatkowa - spawner.getMaxSpawnDelay()) / (20 * upgr.ulepszenia.szybkosc.mnoznik) + 1;
		case "§bZasięg":	 return poziom == spawner.getSpawnRange() - 1;
		default: return false;
		}
	}
	
	void ulepsz(Player p, CreatureSpawner spawner, ItemStack item, int slot) {
		if (Func.porównaj(item, pustySlot))
			return;
		if (!Main.ekonomia) {
			p.sendMessage(prefix + "Sory nie wypali, nie ma ekonomi na serwerze");
			return;
		}

		double kasa = Main.econ.getBalance(p);
		double cena;
		
		if (slot == dane.slotUlepszeńMoba) {
			int i = znajdzIndex(spawner.getSpawnedType());
			Ulepszenie upgr = dane.ulepszenia.get(i + 1);
			cena = upgr.cena;
			if (cena > kasa) {
				p.sendMessage(prefix + "§4Nie stać cię na to");
				return;
			}
			if (!Func.losuj(upgr.szansaEwolucji))
				p.sendMessage(prefix + "Nie udało się Ewoluować Spawnera §b:(");
			else {
				if (upgr.broadcast)
					Bukkit.broadcastMessage(Func.msg(prefix + "%s Wyeluował spawner | %s -> %s |", p.getDisplayName(), spawner.getSpawnedType(), upgr.mob));
				else
					p.sendMessage(prefix + Func.msg("Wyeluowałeś Spawner! | %s -> %s |", spawner.getSpawnedType(), upgr.mob));
				spawner.setSpawnedType(upgr.mob);
				if (dane.ulepszenia.get(i).resetowaćNastępny)
					upgr.zresetuj(spawner);
				spawner.getWorld().spawnParticle(Particle.HEART, spawner.getBlock().getLocation().add(.5, 1, .5), 5, .5, .5, .5, 0);
				spawner.getWorld().playSound(spawner.getLocation(), Sound.ENTITY_WITHER_SPAWN, .5f, 0f);
			}
		} else {
			List<String> lore = item.getItemMeta().getLore();
			if (lore.get(0).equals("§aZakupiono"))
				return;
			cena = Func.Double(lore.get(0).substring(11, lore.get(0).length()-2), -1);
			if (cena == -1) { Main.error("Niepoprawa cena w upgr spawnerów " + item); return;}
			if (cena > kasa) {
				p.sendMessage(prefix + "§4Nie stać cię na to");
				return;
			}
			
			int poziom = item.getAmount();
	
			Ulepszenie upgr = znajdz(spawner.getSpawnedType());
			
			String nazwa = item.getItemMeta().getDisplayName();
			switch (nazwa) {
			case "§bLiczebność":
				spawner.setSpawnCount(poziom);
				spawner.setMaxNearbyEntities((int) (poziom * upgr.ulepszenia.liczebnosc.mnoznik));
				break;
			case "§bZasięg":
				spawner.setSpawnRange(poziom + 1);
				spawner.setRequiredPlayerRange((int) (poziom * upgr.ulepszenia.zasieg.mnoznik));
				break;
			case "§bSzybkość":
				int maxSpawnDeley = (int) (upgr.ulepszenia.SzybkoscPoczatkowa - (poziom - 1) * upgr.ulepszenia.szybkosc.mnoznik*20);;
				spawner.setMaxSpawnDelay(maxSpawnDeley);
				spawner.setMinSpawnDelay(upgr.minSpawnDelay(maxSpawnDeley));
				break;
			}
		}
		spawner.update();
		
		Main.econ.withdrawPlayer(p, cena);
		
		edytuj(p, spawner);
	}

	
	
	// EventHandler
	
	@EventHandler
	public void stawianie(BlockPlaceEvent ev) {
		if (!(ev.getBlock().getState() instanceof CreatureSpawner)) return;
		CraftCreatureSpawner spawner = (CraftCreatureSpawner) ev.getBlock().getState();
		NBTTagCompound tag = CraftItemStack.asNMSCopy(ev.getItemInHand()).getOrCreateTag().getCompound("BlockEntityTag");
		
		BlockPosition blockPos = new BlockPosition(ev.getBlock().getX(), ev.getBlock().getY(), ev.getBlock().getZ());
		TileEntityMobSpawner _spawner = (TileEntityMobSpawner) ((CraftWorld) spawner.getWorld()).getHandle().getTileEntity(blockPos);
		_spawner.load(_spawner.getBlock(), tag);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void niszczenie(BlockBreakEvent ev) {
		if (ev.isCancelled()) return;
		if (!ev.getBlock().getType().equals(Material.SPAWNER)) return;
		
		ev.setExpToDrop(0);
		ev.setDropItems(false);
		
		CraftCreatureSpawner spawner = (CraftCreatureSpawner) ev.getBlock().getState();
		Ulepszenia upgr = znajdz(spawner.getSpawnedType()).ulepszenia;
		
		Func.dajItem(ev.getPlayer(), dajItem(
				spawner.getCreatureTypeName(),
				spawner.getSpawnCount(),
				(int) (1 - (spawner.getMaxSpawnDelay() - upgr.SzybkoscPoczatkowa) / (upgr.szybkosc.mnoznik * 20)),
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
		if (slot < 0 || slot >= dane.sloty) return;
		ev.setCancelled(true);
		if (!ev.getClick().equals(ClickType.LEFT)) return;
		ItemStack item = ev.getCurrentItem();
		ulepsz(p, spawner, item, slot);
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		panele.remove(ev.getPlayer().getName());
	}

	
	
	// Override
	
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
		
		EntityType mob;
		try {
			mob = EntityType.valueOf(args[0].toUpperCase());
		} catch (Exception e) {
			return Func.powiadom(sender, prefix + "Nieprawidłowy mob");
		}
		
		Func.dajItem(p, dajItem(args[0], 1, 1, 1, znajdz(mob).tag0()));
		
		return true;
	}

	@Override
	public void przeładuj() {
		dane =  new Config("configi/Spawnery").wczytajLubDomyślna("Spawnery", Dane::new);
		
		List<Ulepszenie> ulepszenia = Lists.newArrayList(dane.ulepszenia);
		dane.ulepszenia.clear();
		Krotka<Ulepszenie.Ulepszenia, Boolean> krotka = new Krotka<>(null, true);
		for (Ulepszenie upgr : ulepszenia) {
			if (upgr.ulepszenia == null)
				upgr.ulepszenia = krotka.a;
			else {
				Consumer<String> cons = str -> {
					try {
						Field f = Ulepszenie.Ulepszenia.class.getDeclaredField(str);
						f.setAccessible(true);
						
						if (f.get(upgr.ulepszenia) == null)
							f.set(upgr.ulepszenia, f.get(krotka.a));
						else
							krotka.b = false;
						
					} catch (Throwable e) {
						e.printStackTrace();
					}
				};
				krotka.b = true;
				cons.accept("zasieg");
				cons.accept("szybkosc");
				cons.accept("liczebnosc");
				cons.accept("SzybkoscDzielnik");
				cons.accept("SzybkoscPoczatkowa");
				if (krotka.b)
					upgr.ulepszenia = krotka.a;
			}
			dane.ulepszenia.add(upgr);
			krotka.a = upgr.ulepszenia;
		}
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
}


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
 */

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
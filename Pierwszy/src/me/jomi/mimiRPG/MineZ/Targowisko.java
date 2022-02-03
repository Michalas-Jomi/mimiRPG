package me.jomi.mimiRPG.MineZ;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.PlayerCreatePlayerShopkeeperEvent;
import com.nisovin.shopkeepers.api.events.PlayerDeleteShopkeeperEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperCreateException;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopCreationData;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.SKDefaultShopTypes;
import com.nisovin.shopkeepers.shopkeeper.player.AbstractPlayerShopkeeper;
import com.nisovin.shopkeepers.shopobjects.SKDefaultShopObjectTypes;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.api._WorldGuard;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Targowisko extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix(Targowisko.class);
	static RegionContainer regiony;
	static ItemStack itemRegionu;
	
	static int priorityTargów = 10; // priorityTargów - teren wokół; priorityTargów + 1 - teren targu
	static long startowyCzas;
	
	Config config = new Config("configi/Targowisko");
	
	Map<String, Long> czasy;
	
	
	public Targowisko() {
		super("targowisko", "/targowisko <nick> <item> <czas w minutach>");
		regiony = WorldGuard.getInstance().getPlatform().getRegionContainer();
	}
	
	String id(Player p) {
		return id(p.getName());
	}
	String id(String nick) {
		return "targowisko-" + nick.toLowerCase();
	}
	boolean jego(Player p, ApplicableRegionSet regiony) {
		String id = id(p);
		for (ProtectedRegion region : regiony.getRegions())
			if (region.getId().equals(id))
				return true;
		return false;
	}
	boolean możePostawić(Player p) {
		String id = id(p);
		for (World world : Bukkit.getWorlds())
			if (regiony.get(BukkitAdapter.adapt(world)).hasRegion(id))
				return false;
		return true;
	}
	private String znajdzRotacje(Location loc, Location gracz) {
		return Func.max(
				Arrays.asList(
						new Krotka<Location, String>(loc.clone().add(1,  0,  0), "east"),
						new Krotka<Location, String>(loc.clone().add(-1, 0,  0), "west"),
						new Krotka<Location, String>(loc.clone().add(0,  0,  1), "south"),
						new Krotka<Location, String>(loc.clone().add(0,  0, -1), "north")
						),
				(k1, k2) -> {
					if (k1.a.distance(gracz) < k2.a.distance(gracz))
						return k1;
					return k2;
				}
				).b;
	}
	ApplicableRegionSet regiony(Location loc) {
		return Func.regiony(loc.getWorld()).getApplicableRegions(Func.locToVec3(loc));
	}
	
	void postaw(Player p, Location loc) {
		BlockVector3 vec = Func.locToVec3(loc);
		
		ProtectedCuboidRegion regionTarg = new ProtectedCuboidRegion(	   id(p), vec, 				  vec.add(0, 1, 0));
		ProtectedCuboidRegion regionProt = new ProtectedCuboidRegion("p" + id(p), vec.add(-1, 0, -1), vec.add(1, 1, 1));
		
		regionTarg.setPriority(priorityTargów + 1);
		regionProt.setPriority(priorityTargów);
		
		regionProt.setFlag(_WorldGuard.flagaTargowisko, StateFlag.State.DENY);
		
		loc.getBlock().setBlockData(Bukkit.createBlockData("minecraft:chest[type=single,facing=" + znajdzRotacje(loc, p.getLocation()) + "]"), false);
		
		PlayerShopCreationData data = PlayerShopCreationData.create(
				p,
				SKDefaultShopTypes.PLAYER_SELLING(),
				SKDefaultShopObjectTypes.LIVING().get(EntityType.VILLAGER),
				loc,
				BlockFace.UP,
				loc.getBlock()
				);
		AbstractShopkeeper shop;
		try {
			shop = SKShopkeepersPlugin.getInstance().getShopkeeperRegistry().createShopkeeper(data);
		} catch (ShopkeeperCreateException e) {
			p.sendMessage(prefix + "Nie udało się postawić Targowiska w tym miejscu");
			e.printStackTrace();
			return;
		}

		Func.regiony(loc.getWorld()).addRegion(regionProt);
		Func.regiony(loc.getWorld()).addRegion(regionTarg);
		
		shop.setName("§2Targowisko gracza §e" + Func.getDisplayName(p));
		
		dodajCzas(p.getName(), startowyCzas);
	}
	public void usuńTargowisko(String nick) {
		nick = nick.toLowerCase();
		czasy.remove(nick);
		
		config.ustaw_zapisz("czasy." + nick, null);
		
		String id = id(nick);
		
		Bukkit.getWorlds().forEach(world -> {
			RegionManager regiony = Func.regiony(world);
			regiony.removeRegion("p" + id);
			regiony.removeRegion(id);
		});

		for (AbstractPlayerShopkeeper shop : SKShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers())
			if (nick.equalsIgnoreCase(shop.getOwnerName())) {
				shop.getContainer().setType(Material.AIR);
				shop.delete();
			}
		
		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(prefix + "Twoje targowisko zostało zlikwidowane"));
	}
	public void dodajCzas(String nick, long ile) {
		nick = nick.toLowerCase();
		
		long czas = czasy.getOrDefault(nick, System.currentTimeMillis()) + ile;
		
		czasy.put(nick, czas);
		config.ustaw_zapisz("czasy." + nick, czas);
	}
	
	
	// EventHandler
	
	@EventHandler
	public void ShopkeeperCreate(PlayerCreatePlayerShopkeeperEvent ev) {
		Location loc = ev.getShopCreationData().getSpawnLocation();
		ApplicableRegionSet region = regiony(loc);
		Player p = ev.getShopCreationData().getCreator();
		if (!region.testState(null, _WorldGuard.flagaTargowisko) || !jego(p, region)) {
			p.sendMessage(prefix + "Targowisko mozna postawić tylko w wyznaczonych miejscach");
			ev.setCancelled(true);
		}
	}
	@EventHandler
	public void stawianieTargowiska(PlayerInteractEvent ev) {
		if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (ev.getHand() != EquipmentSlot.HAND) return;
		ItemStack hand = ev.getPlayer().getEquipment().getItemInMainHand();
		if (!itemRegionu.isSimilar(hand)) return;
		
		ev.setCancelled(true);
		
		Player p = ev.getPlayer();
		Block click = ev.getClickedBlock();
		Location loc = click.isPassable() ? click.getLocation() : click.getRelative(ev.getBlockFace()).getLocation();
;		if (regiony(loc).testState(null, _WorldGuard.flagaTargowisko))
			if (możePostawić(p)) {
				hand.setAmount(hand.getAmount() - 1);
				if (hand.getAmount() <= 0)
					hand = null;
				ev.getPlayer().getEquipment().setItemInMainHand(hand);
				
				postaw(p, loc);
			} else
				p.sendMessage(prefix + "Nie możesz postawić więcej Targowisk");
		else
			p.sendMessage(prefix + "Targowisko mozna postawić tylko w wyznaczonych miejscach");
	}
	@EventHandler
	public void usuwanieShopkeepera(PlayerDeleteShopkeeperEvent ev) {
		if (ev.getShopkeeper() instanceof AbstractPlayerShopkeeper) {
			AbstractPlayerShopkeeper shop = (AbstractPlayerShopkeeper) ev.getShopkeeper();
			if (czasy.containsKey(shop.getOwnerName().toLowerCase()))
				usuńTargowisko(shop.getOwnerName().toLowerCase());
		}
	}
	@EventHandler
	public void edit(ShopkeeperEditedEvent ev) {
		ev.getShopkeeper().getUISessions().forEach(ses -> ses.close());
	}
	
	
	// Override
	
	@Override
	public int czas() {
		Long teraz = System.currentTimeMillis();
		Set<String> doUsunięcia = new HashSet<>();
		czasy.forEach((String nick, Long czas) -> {
			if (czas < teraz)
				doUsunięcia.add(nick);
		});
		doUsunięcia.forEach(this::usuńTargowisko);
		
		return 20 * 60 * 3;
	}

	@Override
	public void przeładuj() {
		priorityTargów = Main.ust.wczytajPewnyD("Targowisko.priorityRegionów");
		itemRegionu = Main.ust.wczytajItemD("Targowisko.item");
		startowyCzas = ((int) Main.ust.wczytajPewnyD("Targowisko.startowyCzas")) * 60 * 1000L; // minuty
		
		config.przeładuj();
		
		czasy = new HashMap<>();
		if (config.klucze().contains("czasy"))
			Func.wykonajDlaNieNull(config.sekcja("czasy").getValues(false), m -> {
				m.forEach((nick, czas) -> {
					if (czas instanceof String)
						czasy.put(nick, Long.parseLong((String) czas));
					else if (czas instanceof Long)
						czasy.put(nick, (long) czas);
					else if (czas instanceof Integer)
						czasy.put(nick, (long) (int) czas);
					else if (czas != null)
						Main.warn(prefix + Func.msg("Problem z czasem gracza %s, czas: %s (%s)", nick, czas, czas.getClass()));
				});
			});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Aktywne Targowiska", czasy.size());
	}

	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return utab(args, CustomoweItemy.customoweItemy.keySet());
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (args.length < 3) return false;
		
		// cmd syntax
		
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null)
			throwFormatMsg("Gracz %s nie jest online", args[0]);
		if (!czasy.containsKey(p.getName().toLowerCase()))
			throwFormatMsg("Gracz %s nie posiada swojego targowiska", args[0]);
		
		ItemStack item = Config.item(args[1]);
		if (item == null)
			throwFormatMsg("Item %s nie istnieje, użyj /citem ustaw " + args[1], args[1]);
		
		int czas = Func.Int(args[2], -1);
		if (czas <= 0)
			throwFormatMsg("Liczba %s jest niepoprawna, czas (w minutach) musi być liczbą większą od 0", args[2]);
		
		
		// cmd run
		
		ItemStack hand = p.getEquipment().getItemInMainHand();
		if (item.isSimilar(hand)) {
			hand.setAmount(hand.getAmount() - 1);
			if (hand.getAmount() <= 0)
				hand = null;
			p.getEquipment().setItemInMainHand(hand);
			
			dodajCzas(p.getName(), czas * 60L * 1000L);
			p.sendMessage(prefix + "");
		} else {
			Napis n = new Napis(prefix + "Musisz trzymać w ręku ")
				.dodaj(Napis.item(item))
				.dodaj(Func.msg(" aby przedłużyć wynajem targu o %s.", Func.czas(czas * 60)));
			
			if (czasy.containsKey(p.getName().toLowerCase())) {
				long pozostały = Math.max(0L, czasy.get(p.getName().toLowerCase()) - System.currentTimeMillis());
				n.dodaj(Func.msg(" Zostało ci jeszcze %s czasu wynajmu.", Func.czas((int) (pozostały / 1000))));
			}
			
			n.wyświetl(p);
		}
		
		
		return true;
	}
}

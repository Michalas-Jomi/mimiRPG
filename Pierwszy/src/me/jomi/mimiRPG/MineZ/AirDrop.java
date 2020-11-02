package me.jomi.mimiRPG.MineZ;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
import me.jomi.mimiRPG.util.Zegar;

// TODO szablon configu

@Moduł
public class AirDrop extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix("Air Drop");
	List<Drop> wszystkieDropy = Lists.newArrayList();
	static int _id = 0;
	class Drop {
		int czas = -1;
		int id;
		int x;
		int z;
		List<FallingBlock> bloki = Lists.newArrayList();
		BossBar bossbar;
		Location loc;
		Vector spadanie = new Vector(0, -prędkośćSpadania, 0);
		Drop(Location loc) {
			id = _id++;
			wszystkieDropy.add(this);
			x = loc.getBlockX();
			z = loc.getBlockZ();
			this.loc = new Location(loc.getWorld(), x, loc.getY(), z);
			this.loc.add(.5, 0, .5);
			
			bossbar = Bukkit.createBossBar("§bZrzut zostanie zrzucony na " + loc.getBlockX() + "x " + loc.getBlockZ() + "z", BarColor.GREEN, BarStyle.SOLID);
			for (Player p : Bukkit.getOnlinePlayers())
				bossbar.addPlayer(p);
			bossbar.setVisible(true);
			tick();
		}
		
		boolean ładowany;
		
		int timer = 40;
		void tick() {
			if (--timer <= 0) {
				timer = 40;
				bossbar.setColor(bossbar.getColor().equals(BarColor.GREEN) ? BarColor.YELLOW : BarColor.GREEN);
			}
			
			bossbar.setProgress(((double) ++czas) / ((double) czasRespienia));
			if (czas < czasRespienia) 
				Func.opóznij(1, this::tick);
			else
				przywołaj();
		}

		void przywołaj() {
			Location loc = this.loc.clone();
			World świat = loc.getWorld();
			Consumer<Material> cztery = typ -> {
				zrespBlok(świat, loc.add(0, 1, -1), typ, this);
				zrespBlok(świat, loc.add(1, 0, 0), typ, this);
				zrespBlok(świat, loc.add(0, 0, 1), typ, this);
				zrespBlok(świat, loc.add(-1, 0, 0), typ, this);
			};
			cztery.accept(Material.BEEHIVE);
			cztery.accept(Material.BARREL);
			cztery.accept(Material.BEEHIVE);
			cztery.accept(Material.CHAIN);
			cztery.accept(Material.CHAIN);
			cztery.accept(Material.LIGHT_BLUE_WOOL);

			zrespBlok(świat, loc.add(0, -1, -2), Material.LIGHT_BLUE_WOOL, null);
			zrespBlok(świat, loc.add(1, 0, 0), Material.LIGHT_BLUE_WOOL, null);
			
			zrespBlok(świat, loc.add(1, 0, 1), Material.LIGHT_BLUE_WOOL, null);
			zrespBlok(świat, loc.add(0, 0, 1), Material.LIGHT_BLUE_WOOL, null);
			
			zrespBlok(świat, loc.add(-1, 0, 1), Material.LIGHT_BLUE_WOOL, null);
			zrespBlok(świat, loc.add(-1, 0, 0), Material.LIGHT_BLUE_WOOL, null);
			
			zrespBlok(świat, loc.add(-1, 0, -1), Material.LIGHT_BLUE_WOOL, null);
			zrespBlok(świat, loc.add(0, 0, -1), Material.LIGHT_BLUE_WOOL, null);
			
			ładowany = this.loc.getChunk().isForceLoaded();
			this.loc.getChunk().setForceLoaded(true);
			
			tickSpadanie();
		}
		void zrespBlok(World świat, Location loc, Material typ, Drop drop) {
			FallingBlock blok;
			if (Material.BARREL.equals(typ))
				blok = świat.spawnFallingBlock(loc, Bukkit.createBlockData("minecraft:barrel[facing=up,open=false]"));
			else
				blok = świat.spawnFallingBlock(loc, Bukkit.createBlockData(typ));
			blok.setDropItem(false);
			blok.setHurtEntities(false);
			blok.setGravity(false);
			Func.ustawMetadate(blok, metaId, drop);
			bloki.add(blok);
		}
		void tickSpadanie() {
			if (bloki.isEmpty()) return;
			for (FallingBlock blok : bloki) {
				blok.setVelocity(spadanie);
				blok.setTicksLived(1);
			}
			Func.opóznij(10, this::tickSpadanie);
		}
	
		void rozbij(int y) {
			World świat = bloki.get(0).getWorld();
			for (FallingBlock blok : bloki)
				blok.remove();
			świat.spawnParticle(Particle.CLOUD, loc.getBlockX(), y, loc.getBlockZ(), 200, 2, 2, 2, 1);
			this.loc.getChunk().setForceLoaded(ładowany);
			loc.setY(y);
			zbuduj();
			bossbar.setVisible(false);
			bossbar.removeAll();
			for (int i=0; i < wszystkieDropy.size(); i++)
				if (wszystkieDropy.get(i).id == id) {
					wszystkieDropy.remove(i);
					break;
				}
		}
		void zbuduj() {
			loc.setX(x);
			loc.setZ(z);
			Bukkit.broadcastMessage(Func.msg(prefix + "Air Drop spadł na koordynatach %sx %sy %sz", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
			loc.add(0, 0, -1).getBlock().setType(Material.BEEHIVE);
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
			
			Func.opóznij(20*60*30, () -> {
				for (Block blok : Func.bloki(loc, loc.clone().add(1, -2, -1)))
					blok.setType(Material.AIR);
			});
		}
		void ustawDrop(Block blok) {
			blok.setType(Material.BARREL, false);
			blok.setBlockData(Bukkit.createBlockData("minecraft:barrel[facing=up,open=false]"), false);
			Barrel skrzynia = (Barrel) blok.getState();
			ustawDrop(skrzynia.getSnapshotInventory());
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
	Dropy dropy;

	static AirDrop inst;
	public AirDrop() {
		super("airdrop", "/airdrop [przywołaj / (dajitem <nick>)]");
		inst = this;
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

	void przywołaj() {
		if (Bukkit.getOnlinePlayers().size() <= 0) return;
		Location loc;
		
		do loc = new Location(Func.losuj(Bukkit.getOnlinePlayers()).getWorld(), Func.losuj(-2000, 2000), 240, Func.losuj(-2000, 2000));
		while(woda(loc));

		new Drop(loc);
	}
	boolean woda(Location _loc) {
		Location loc = _loc.clone();
		
		while (loc.getBlock().getType().isAir() && loc.getBlockY() >= 0)
			loc.add(0, -1, 0);
		
		return Func.multiEquals(loc.getBlock().getType(), Material.WATER, Material.SNOW, Material.SPRUCE_LEAVES, Material.CACTUS) || loc.getBlockY() < 0;
	}
	

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, "przywołaj", "dajitem");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Predicate<Player> dajItem = p -> {
			Func.dajItem(p, itemRespienia);
			return Func.powiadom(prefix, sender, "%s otrzymał item respienia AirDropu", p.getDisplayName());
		};
		
		Player p = null;
		if (args.length >= 2) {
			p = Bukkit.getPlayer(args[1]);
			if (p == null)
				return Func.powiadom(sender, prefix + Func.msg("Niepoprawna nazwa gracza %s", args[1]));
		} else if (sender instanceof Player)
			p = (Player) sender;
		
		if (p == null)	
			return Func.powiadom(sender, prefix + "Z konsoli musisz podać nazwe gracza");
		
		if (args.length <= 0) {
			if (sender instanceof Player)
				return dajItem.test((Player) sender);
			else
				return false;			
		}
		switch (args[0].toLowerCase()) {
		case "przywołaj":
			new Drop(p.getLocation());
			return Func.powiadom(prefix, sender, "Przywołano air drop na pozycji gracza %s", p.getDisplayName());
		case "dajitem":
			Func.dajItem(p, itemRespienia);
			return Func.powiadom(prefix, sender, "%s otrzymał item respienia Air Dropu", p.getDisplayName());
		}
		
		return true;
	}
	@EventHandler
	public void __(EntityChangeBlockEvent ev) {
		if (!ev.getEntity().hasMetadata(metaId)) return;
		ev.setCancelled(true);
		Drop drop = (Drop) ev.getEntity().getMetadata(metaId).get(0).value();
		if (drop != null)
			drop.rozbij(ev.getBlock().getY());
	}
	@EventHandler
	public void __(PlayerInteractEvent ev) {
		if (itemRespienia == null) return;
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		if (item == null) return;
		
		if (!item.isSimilar(itemRespienia)) return;
		Location loc = ev.getPlayer().getLocation().clone();
		loc.setY(240);
		new Drop(loc);
		Bukkit.broadcastMessage(prefix + Func.msg("%s przywołał zrzut!", ev.getPlayer().getDisplayName()));
			
		item.setAmount(item.getAmount() - 1);
		if (item.getAmount() <= 0)
			item = null;
		ev.getPlayer().getInventory().setItemInMainHand(item);
	}
	@EventHandler
	public void __(PlayerJoinEvent ev) {
		for (Drop drop : wszystkieDropy)
			if (drop.bossbar != null)
				drop.bossbar.addPlayer(ev.getPlayer());
	}
	
	int czasRespienia;
	double prędkośćSpadania;
	int coIleRespić;
	ItemStack itemRespienia;
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		dropy = (Dropy) config.wczytaj("dropy");
	
		czasRespienia = config.wczytajLubDomyślna("czasRespienia", 120) * 20;
		prędkośćSpadania = config.wczytajLubDomyślna("prędkośćSpadania", 1.0);
		coIleRespić = config.wczytajLubDomyślna("co ile minut respić", 180);
		itemRespienia = config.wczytajItem("Item do zrespienia");
	}
	@Override
	public Krotka<String, Object> raport() {
		int x = 0;
		try {
			x = dropy.ciąg.wielkość();
		} catch(Throwable e) {}
		return Func.r("Wczytane dropy", x);
	}
	
	@Override
	public int czas() {
		if (--coIleRespić <= 0) {
			przywołaj();
			coIleRespić = config.wczytajLubDomyślna("co ile minut respić", 180);
		}
		return 20 * 60;
	}
}

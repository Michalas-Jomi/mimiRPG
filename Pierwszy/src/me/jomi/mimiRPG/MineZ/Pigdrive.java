package me.jomi.mimiRPG.MineZ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Pigdrive extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Pig Drive");
	
	public static class Drive extends Mapowany {
		static final Map<String, Drive> zNazwy = new HashMap<>();
		
		String nazwa;
		@Mapowane double maxPrędkość = 50d;
		@Mapowane int tickiDoMaxa = 50;
		
		@Mapowane ItemStack klucz;
		
		public ItemStack klucz(int id) {
			ItemStack klucz = this.klucz.clone();
			
			ItemMeta meta = klucz.getItemMeta();
			meta.getPersistentDataContainer().set(nskDriveType, PersistentDataType.STRING, nazwa);
			meta.getPersistentDataContainer().set(nskDrive, PersistentDataType.INTEGER, 0);
			klucz.setItemMeta(meta);
			
			return klucz;
		}
		
		public double speedPerTick() {
			return maxPrędkość / 36d / tickiDoMaxa;
		}
		public Entity summon(Location loc, int id) {
			loc = loc.clone();
			loc.setY(loc.getWorld().getMaxHeight() + 10);
			loc.add(Func.losuj(-5, 5), 0, Func.losuj(-5, 5));
			
			Entity pig = loc.getWorld().spawnEntity(loc, EntityType.PIG);

			Func.ustawMetadate(pig, nskDriveType.getNamespace(), this);
			pig.getPersistentDataContainer().set(nskDrive, PersistentDataType.INTEGER, id);
			pig.setInvulnerable(true);
			
			summonTick(pig);
			
			return pig;
		}
		private void summonTick(Entity pig) {
			if (!pig.isOnGround())
				Func.opóznij(1, () -> summonTick(pig));
			else
				spawnEffect(pig);
		}
		
		public void spawnEffect(Entity pig) {
			Location loc = pig.getLocation();
			
			Func.particle(loc, 100, 1, .6, 1, 0, Color.BLACK, 8);
			for (int i=0; i < 3; i++)
				Func.particle(loc, 60, 1.2, .8, 1.2, 0, Color.fromRGB(Func.losuj(0, 255), Func.losuj(0, 255), Func.losuj(0, 255)), 4 + i);
		}
	
	
		public void getIn(Player p, Entity pig) {
			pig.addPassenger(p);
			
			((Pig) pig).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
			
			driveTick(p, (Pig) pig);
		}
		private void driveTick(Player p, Pig pig) {
			if (!p.isInsideVehicle() || !p.getVehicle().getUniqueId().equals(pig.getUniqueId())) {
				pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
				return;
			}
			Func.opóznij(5, () -> driveTick(p, pig));
			
			double speed = pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
			Func.sendActionBar(p, prefix + "§8LPM \\/ §6| §8PPM /\\ §6" + Func.zaokrąglij(speed * 36, 2) + "km/h");
			
			pig.setRotation(p.getLocation().getYaw(), 0);
			
			Vector vel = p.getLocation().getDirection();
			vel.setY(0);
			
			if (!pig.getLocation().add(vel).getBlock().isPassable() && pig.getLocation().add(vel).add(0, 1, 0).getBlock().isPassable())
				pig.setJumping(true);
			
			vel.multiply(speed);
			if (pig.getLocation().add(0, -1, 0).getBlock().isPassable())
				vel.multiply(.2);
			
			pig.setVelocity(vel);
			
			
			Func.particle(pig.getLocation(), 1, 0, 0, 0, 0, Color.fromRGB(20, 20, 20), 3);
		}
		public void speedUp(Entity pig) {
			double speed = ((Pig) pig).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
			if (speed * 36 < maxPrędkość)
				((Pig) pig).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Math.min(maxPrędkość / 36d, speed + speedPerTick()));
		}
		public void speedDown(Entity pig) {
			double speed = ((Pig) pig).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
			if (speed > 0)
				((Pig) pig).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Math.max(0, speed - speedPerTick() * 3));
		}
	}
	
	
	public Pigdrive() {
		super("pigdrive", "/pigdrive [id | klucz <drive> (id) | edytor]");
	}

	@Nullable
	public static Drive drive(Entity pig) {
		if(pig.hasMetadata(nskDriveType.getNamespace()))
			return (Drive) pig.getMetadata(nskDriveType.getNamespace()).get(0).value();
		return null;
	}
	public static Drive drive(ItemStack item) {
		return Drive.zNazwy.get(item.getItemMeta().getPersistentDataContainer().get(nskDriveType, PersistentDataType.STRING));
	}
	

	private static NamespacedKey nskDriveType = new NamespacedKey(Main.plugin, "mimipigdrivetype");
	private static NamespacedKey nskDrive = new NamespacedKey(Main.plugin, "mimipigdrive");

	static int id(Entity pig) {
		try {
			return pig.getPersistentDataContainer().get(nskDrive, PersistentDataType.INTEGER);
		} catch (NullPointerException e) {
			return 0;
		}
	}
	static int id(ItemStack key) {
		return key.getItemMeta().getPersistentDataContainer().get(nskDrive, PersistentDataType.INTEGER);
	}
	static void id(ItemStack key, int id) {
		ItemMeta meta = key.getItemMeta();
		meta.getPersistentDataContainer().set(nskDrive, PersistentDataType.INTEGER, id);
		key.setItemMeta(meta);
	}
	static boolean isKey(ItemStack key) {
		if (key == null || !key.hasItemMeta()) return false;
		return key.getItemMeta().getPersistentDataContainer().has(nskDrive, PersistentDataType.INTEGER);
	}
	public static boolean match(Entity pig, ItemStack key) {
		return id(pig) == id(key);
	}
	
	public static boolean isDrive(Entity pig) {
		return pig.getPersistentDataContainer().has(nskDrive, PersistentDataType.INTEGER);
	}
	
	public static Entity findDrive(World world, int id) {
		for (Entity mob : world.getEntities())
			if (id(mob) == id)
				if (drive(mob) == null)
					mob.remove();
				else
					return mob;
		return null;
	}
	
	
	public static int generateId() {
		Config config = new Config("configi/pigdriveData");
		int id = config.wczytaj("driveid", 0);
		config.ustaw_zapisz("driveid", ++id);
		return id;
	}
	
	
	/// EventHandler
	
	@EventHandler
	public void interact(PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if (ev.getAction() == Action.PHYSICAL) return;
		Player p = ev.getPlayer();
		Func.wykonajDlaNieNull(p.getEquipment().getItemInMainHand(), item -> {
			if (isKey(item)) {
				ev.setCancelled(true);
				
				Func.wykonajDlaNieNull(p.getVehicle(), pig -> {
					if (match(pig, item)) {
						if (Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK))
							drive(pig).speedUp(pig);
						else
							drive(pig).speedDown(pig);
					}
				}, () -> {
					if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
					int id = id(item);
					if (id == 0) {
						if (item.getAmount() > 1) {
							ItemStack item2 = item.clone();
							item2.setAmount(item.getAmount() - 1);
							item.setAmount(1);
							Func.opóznij(1, () -> Func.dajItem(p, item2));
						}
						
						int newId = generateId();
						id(item, newId);
						p.getEquipment().setItemInMainHand(item);
					} else {
						Func.wykonajDlaNieNull(findDrive(p.getWorld(), id), pig -> {
							if (p.getLocation().distance(pig.getLocation()) > 800)
								pig.remove();
							else
								Func.particle(p.getEyeLocation(), pig.getLocation(), .3, (loc, step) -> Func.particle(p, loc, 1, 0, 0, 0, 0, Color.RED, 1));
						}, () -> {
							drive(item).summon(p.getLocation(), id);
						});
					}
				});
				
			}
		});
	}
	@EventHandler
	public void wsiadanie(PlayerInteractAtEntityEvent ev) {
		Func.wykonajDlaNieNull(ev.getPlayer().getEquipment().getItemInMainHand(), item -> {
			if (isKey(item) && match(ev.getRightClicked(), item)) {
				drive(ev.getRightClicked()).getIn(ev.getPlayer(), ev.getRightClicked());
			}
		});
	}
	@EventHandler
	public void chunk(ChunkLoadEvent ev) {
		Func.opóznij(1, () -> {
			Func.forEach(ev.getChunk().getEntities(), mob -> {
				if (isDrive(mob) && drive(mob) == null)
					mob.remove();
			});
		});
	}
	@EventHandler
	public void chunk(ChunkUnloadEvent ev) {
		Func.forEach(ev.getChunk().getEntities(), mob -> {
			if (isDrive(mob))
				mob.remove();
		});
	}
	
	
	/// Override
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1) {
			return utab(args, "klucz", "id", "edytor");
		}
		switch (args[0].toLowerCase()) {
		case "edytor": return edytor.wymuśConfig_onTabComplete(new Config("configi/pigdrive"), sender, label, args);
		case "klucz": return utab(args, Drive.zNazwy.keySet());
		case "kluczid": return new ArrayList<>();
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (args.length < 1)
			return false;
		if (!(sender instanceof Player))
			throwFormatMsg("Musisz być graczem żeby używać PigDrive'u wariacia");
		Player p = (Player) sender;
		
		switch (args[0].toLowerCase()) {
		case "edytor":
			return edytor.wymuśConfig_onCommand(label, "configi/pigdrive", sender, label, args);
		case "klucz":
			if (args.length < 2)
				return false;
			
			int id = 0;
			if (args.length >= 3 && (id = Func.Int(args[2], 0)) <= 0)
				throwFormatMsg(prefix + "id musi być większe niż 0");
			
			int fid = id;
			Func.wykonajDlaNieNull(Drive.zNazwy.get(args[1]), drive -> {
				Func.dajItem(p, drive.klucz(fid));
				throwFormatMsg("Otrzymałeś klucz do PigDrive'u %s (id %s)", args[1], fid);
			}, () -> throwFormatMsg("Nieprawidłowy Drive: %s", args[2]));
			
			break;
		case "id":
			RayTraceResult rezult = p.getWorld().rayTrace(p.getEyeLocation(), p.getLocation().getDirection(), 5, FluidCollisionMode.NEVER, true, 0, e -> e instanceof LivingEntity && ! p.getUniqueId().equals(e.getUniqueId()));
			
			AtomicBoolean pokazane = new AtomicBoolean(false);
			Func.wykonajDlaNieNull(rezult.getHitEntity(), mob -> {
				if (isDrive(mob)) {
					p.sendTitle("§a" + id(mob), "§6id§8: §a" + id(mob), 30, 40, 30);
					pokazane.set(true);
				}
			});
			if (!pokazane.get())
				p.sendTitle("§aBrak id", "§6Musisz patrzeć się na Pig Drive", 30, 40, 30);
			break;
		default:
			return false;
		}
		return true;
	}

	private EdytorOgólny<Drive> edytor = new EdytorOgólny<>("pigdrive", Drive.class);
	@Override
	public void przeładuj() {
		Config config = new Config("configi/pigdrive");
		
		Drive.zNazwy.clear();
		config.klucze().forEach(klucz -> {
			Drive drive = config.wczytajPewny(klucz);
			drive.nazwa = klucz;
			Drive.zNazwy.put(klucz, drive);
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("PigDrive'y", Drive.zNazwy.size());
	}
}

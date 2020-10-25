package me.jomi.mimiRPG.MineZ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Krotka;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Napis;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import me.jomi.mimiRPG.Gracze.Gracz;
import net.md_5.bungee.api.chat.ClickEvent.Action;



import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.bukkit.protection.events.flags.FlagContextCreateEvent;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.entity.SlimeSplitEvent;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChannelEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;
import org.bukkit.event.player.PlayerVelocityEvent;


// TODO title w actionbarze przy wchodzeniu/wychodzeniu z bazy
@SuppressWarnings({ "unused" })
@Moduł
public class Bazy extends Komenda implements Listener, Przeładowalny, Zegar {
	public static class Baza {
		ProtectedCuboidRegion region;
		World świat;
		
		Baza(int x, int y, int z, int dx, int dy, int dz, World świat, Player właściciel) {
			Player p = właściciel;
			this.świat = świat;
					
			String nazwaBazy = String.format("baza%sx%sy%sz", x, y, z);
			region = new ProtectedCuboidRegion(
					nazwaBazy,
					BlockVector3.at(x+dx, y+dy, z+dz),
					BlockVector3.at(x-dx, y-1, z-dz)
					);
			Bazy.inst.regiony.get(BukkitAdapter.adapt(świat)).addRegion(region);
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(p.getName());
			region.setOwners(owners);
			region.setPriority(Bazy.config.wczytajInt("ustawienia.prority baz"));
			region.setFlag(Main.flagaCustomoweMoby, "brak");
			region.setFlag(Main.flagaStawianieBaz, StateFlag.State.DENY);
			region.setFlag(Main.flagaC4, 		   StateFlag.State.ALLOW);
			
			Gracz g = Gracz.wczytaj(p.getName());
			Func.wezUstaw(g.bazy, świat.getName()).add(nazwaBazy);
			g.zapisz();
			
			// ognisko to rdzeń bazy, zniszczenie ogniska = usunięcie bazy
			Func.opóznij(1, () -> new Location(świat, x, y, z).getBlock().setType(Material.CAMPFIRE));
		}
		
		static Baza wczytaj(int x, int y, int z, World świat, ItemStack item, BlockPlaceEvent ev, Map<String, Object> mapa) {
			if (mapa == null) return null;
			int dx = (int) mapa.get("dx");
			int dy = (int) mapa.get("dy");
			int dz = (int) mapa.get("dz");
			ProtectedCuboidRegion region = new ProtectedCuboidRegion(
					"mimiBazaTestowana",
					BlockVector3.at(x+dx, y+dy, z+dz),
					BlockVector3.at(x-dx, y-1,  z-dz)
					);
			if (Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
					.getApplicableRegions(region).testState(null, Main.flagaStawianieBaz))
				return new Baza(x, y, z, dx, dy, dz, świat, ev.getPlayer());
			Bazy.inst.blokuj = true;
			ev.getPlayer().sendMessage(Bazy.prefix + "Nie możesz tu postawić swojej bazy");
			return null;
		}

		Baza(World świat, String nazwa) {
			region = (ProtectedCuboidRegion) Bazy.inst.regiony(świat).getRegion(nazwa);
			this.świat = świat;
		}
		private Baza(World świat, ProtectedCuboidRegion region) {
			this.region = region;
			this.świat = świat;
		}
		static Baza wczytaj(World świat, ProtectedRegion region) {
			if (świat == null) return null;
			if (!(region instanceof ProtectedCuboidRegion)) return null;
			Pattern patern = Pattern.compile("baza-?\\d+x-?\\d+y-?\\d+z");
			if (patern.matcher(region.getId()).find())
				return new Baza(świat, (ProtectedCuboidRegion) region);
			return null;
		}
		
		void usuń() {
			for (String owner : region.getOwners().getPlayers()) {
				Gracz g = Gracz.wczytaj(owner);
				List<String> bazy = g.bazy.get(świat.getName());
				if (bazy == null) continue;
				if (bazy.remove(region.getId()) && bazy.size() == 0)
					g.bazy.remove(świat.getName());
			}
			Bazy.inst.regiony(świat).removeRegion(region.getId());
		}

		// TODO ceny surowkami
		void ulepsz(int ile) {
			ulepsz(region::getMaximumPoint, region::setMaximumPoint, ile, ile);
			ulepsz(region::getMinimumPoint, region::setMinimumPoint, -ile, 0);
		}
		private void ulepsz(Supplier<BlockVector3> supplier, Consumer<BlockVector3> consumer, int xz, int y) {	
			consumer.accept(supplier.get().add(xz, y, xz));
		}
	}

	public static final String prefix = Func.prefix("Baza");
	RegionContainer regiony;
	static Config config = new Config("Bazy");
	
	static Bazy inst;
	public Bazy() {
		super("gildia", null, "g");
		ustawKomende("usuńbaze", null, null);
		ustawKomende("ulepszbaze", null, null);
		inst = this;
		regiony = WorldGuard.getInstance().getPlatform().getRegionContainer();
	}
	public static boolean warunekModułu() {
		return Main.rg != null;
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		ConfigurationSection sekcja = config.sekcja("bazy");
		return Func.r("Itemy dla Baz/schematów/C4", (sekcja == null ? 0 : sekcja.getKeys(false).size()));
	}
	
	@EventHandler
	public void explozja(ExplosionPrimeEvent ev) {
		if (ev.getEntity().getScoreboardTags().contains("mimiC4")) {
			ev.setCancelled(true);
			
			final ConfigurationSection mapa = config.sekcja("c4");
			if (mapa == null)
				return;
			
			final List<String> niezniszczalne = config.wczytajListe("c4.niezniszczalne");
			final List<Krotka<Block, Material>> kolejka = Lists.newArrayList();
			Function<Block, String> dajDate = blok -> {
				return blok.getBlockData().getAsString(false).substring(10 + blok.getType().toString().length());
			};
			Consumer<Block> zniszcz = blok -> {
				final String mat = blok.getType().toString();
				final String str = (String) mapa.get(mat);
				
				if (str != null) {
					final String data = dajDate.apply(blok);
					blok.setBlockData(Bukkit.createBlockData(Material.valueOf(str), data), false);
					
					if (mat.endsWith("_DOOR"))
						kolejka.add(new Krotka<>(
								blok.getLocation().add(0, data.contains("half=upper") ? -1 : 1, 0).getBlock(),
								Material.valueOf(str))
								);
					else if (mat.endsWith("_BED")) {
						UnaryOperator<String> znajdz = co -> {
							String w = data.substring(data.indexOf(co + "=") + co.length() + 1);
							int i = w.indexOf(",");
							if (i == -1) i = w.length()-2;
							return w.substring(0, i);
						};
						
						int x = 0;
						int z = 0;
						switch (znajdz.apply("facing")) {
						case "north": z = -1; break;
						case "south": z = 1;  break;
						case "west":  x = -1; break;
						case "east":  x = 1;  break;
						}

						int i = data.contains("part=foot") ? 1 : -1;
						
						kolejka.add(new Krotka<>(
								blok.getLocation().add(x*i, 0, z*i).getBlock(),
								Material.valueOf(str))
								);
					}
				}
				else if (!niezniszczalne.contains(mat))
					blok.setType(Material.AIR);
			};
			
			Location loc = ev.getEntity().getLocation();
			Location _loc = loc.clone();
			RegionManager regiony = Bazy.inst.regiony.get(BukkitAdapter.adapt(loc.getWorld()));
			final float zasięg = ev.getRadius();
			int mx = (int) (zasięg*2+1);
			loc.add(-zasięg, -zasięg, -zasięg);
			
			float r = zasięg/3*2;
			_loc.getWorld().spawnParticle(Particle.CLOUD, 		_loc, (int) zasięg*50, r, r, r, 0);
			_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, _loc, (int) zasięg*20, r, r, r, 0);
			r *= .4;
			_loc.getWorld().spawnParticle(Particle.CLOUD, 		_loc, (int) zasięg*20, r, r, r, 0);
			_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, _loc, (int) zasięg*15, r, r, r, 0);
			r *= 1.5;
			_loc.getWorld().spawnParticle(Particle.FLAME, _loc, (int) zasięg*20, r, r, r, .1);
			
			_loc.getWorld().playSound(_loc, Sound.ENTITY_RAVAGER_STEP, 100, 0);
			
			BiConsumer<Float, Double> uderz = (Zasięg, dmg) -> {
				for (Entity mob : _loc.getWorld().getNearbyEntities(_loc, Zasięg, Zasięg, Zasięg))
					if (mob instanceof Damageable && !mob.isInvulnerable())
						((Damageable) mob).damage(dmg);
			};
			uderz.accept(zasięg+3,   	4d);
			uderz.accept(zasięg/3*2.5f, 8d);
			uderz.accept(zasięg/3, 		8d);
			
			
			for (int y=0; y<mx; y++) {
				for (int z=0; z<mx; z++) {
					for (int x=0; x<mx; x++) {
						if (!loc.getBlock().getType().isAir()) {
							double dystans = _loc.distance(loc);
							if (dystans <= zasięg) {
								int szansa = dystans < zasięg/3*1 ? 90 : (dystans < zasięg/3*2 ? 60 : 30);
								if (Func.losuj(1, 100) <= szansa &&
										regiony.getApplicableRegions(locToVec3(loc)).testState(null, Main.flagaC4))
									zniszcz.accept(loc.getBlock());
							}
						}
						loc.add(1, 0, 0);
					}
					loc.add(-mx, 0, 1);
				}
				loc.add(0, 1, -mx);
			}
			for (Krotka<Block, Material> krotka : kolejka)
				if (krotka.a.getType() != krotka.b)
					krotka.a.setBlockData(Bukkit.createBlockData(krotka.b, dajDate.apply(krotka.a)), false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void preNiszczenie2(PlayerInteractEvent ev) {
		Block blok = ev.getClickedBlock();
		if (blok != null && blok.getType().equals(Material.CAMPFIRE))
			ev.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void niszczenie(BlockBreakEvent ev) {
		if (!ev.getBlock().getType().equals(Material.CAMPFIRE)) return;
		
		Location loc = ev.getBlock().getLocation();
		for (ProtectedRegion region : regiony(loc.getWorld()).getApplicableRegions(locToVec3(loc))) {
			Baza baza = Baza.wczytaj(loc.getWorld(), region);
			if (baza != null) {
				ev.setCancelled(true);
				if (baza.region.getOwners().contains(ev.getPlayer().getName())) {
					Func.powiadom(prefix, ev.getPlayer(), "Nie możesz zniszczyć własnej bazy, jeśli musisz użyj /usuńbaze");
					return;
				}
				if (baza.region.getMembers().contains(ev.getPlayer().getName())) {
					Func.powiadom(prefix, ev.getPlayer(), "Nie możesz zniszczyć bazy członka twojej gildi");
					return;
				}
				baza.usuń();
				Func.opóznij(1, () -> ev.getBlock().setType(Material.AIR));
				ev.getPlayer().sendMessage(prefix + Func.msg("Zniszczyłeś baze gracza %s", Func.listToString(
						baza.region.getOwners().getPlayers(), 0, "§6, §e")));
				for (String owner : baza.region.getOwners().getPlayers()) {
					Player p = Bukkit.getPlayer(owner);
					if (p != null) Func.powiadom(prefix, p, "%s zniszczył twoją baze!", ev.getPlayer().getDisplayName());
				}
				return;
			}
		}
	}
	
	boolean blokuj;
	@EventHandler(priority = EventPriority.LOWEST)
	public void stawianie(BlockPlaceEvent ev) {
		World świat = ev.getBlock().getWorld();
		ItemStack item = ev.getPlayer().getEquipment().getItemInMainHand();
		int x = ev.getBlock().getX();
		int y = ev.getBlock().getY();
		int z = ev.getBlock().getZ();
		
		if (config.klucze(false).contains("bazy"))
			for (Entry<String, Object> en : config.sekcja("bazy").getValues(false).entrySet()) {
				Map<String, Object> mapa = ((ConfigurationSection) en.getValue()).getValues(false);
				if (Func.porównaj((ItemStack) Config.item(mapa.get("item")), item)) {
					ev.setCancelled(true);
					
					if (Func.multiEquals(ev.getBlockReplacedState().getType(), Material.WATER, Material.LAVA)) return;
					
					Runnable zabierzItem = () -> {
						item.setAmount(item.getAmount()-1);
						ev.getPlayer().getEquipment().setItemInMainHand(item);
					};

					// C4
					if (mapa.containsKey("c4")) {
						Map<String, Object> mapaC4 = ((ConfigurationSection) mapa.get("c4")).getValues(false);
						
						float zasięg = (float) (double) mapaC4.getOrDefault("zasięg", 1f);
						int czas   	 = (int)   			mapaC4.getOrDefault("czas",   1);
						Location loc = ev.getBlock().getLocation().add(.5, 0, .5);
						
						TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
						tnt.addScoreboardTag("mimiC4");
						tnt.setFuseTicks(czas);
						tnt.setGravity(false);
						tnt.setYield(zasięg);
						tnt.setVelocity(new Vector());
						try { 
							tnt.setCustomName(ev.getItemInHand().getItemMeta().getDisplayName());
						} catch (Exception e) {}
						
						zabierzItem.run();
						return;
					}
					
					// Baza/Schemat
					// jeśli baza nie może być postawiona przez flage -> blokuj = true
					blokuj = false;
					boolean zabierz = Baza.wczytaj(x, y, z, świat, item, ev,
							((ConfigurationSection) mapa.get("baza")).getValues(false)) != null;
					
					if (mapa.containsKey("schemat") && !blokuj && 
							Bazy.inst.regiony.get(BukkitAdapter.adapt(świat))
								.getApplicableRegions(BlockVector3.at(x, y, z))
								.testState(Main.rg.wrapPlayer(ev.getPlayer()), Flags.BUILD) &&
							wklejSchemat((String) mapa.get("schemat"), świat, x, y, z))
								zabierz = true;
					
					if (zabierz) 
						zabierzItem.run();
					return;
				}
			}
	}
	boolean wklejSchemat(String schematScieżka, World świat, int x, int y, int z) {
		String scieżka = Main.path + schematScieżka;
		File file = new File(scieżka);
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file));
				EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(świat))) {
			Operations.complete(
					new ClipboardHolder(reader.read())
		            .createPaste(editSession)
		            .to(BlockVector3.at(x, y, z))
		            .ignoreAirBlocks(true)
		            .build()
		            );
		} catch (IOException  e) {
			Main.warn("Nie odnaleziono pliku " + scieżka + " schemat z Bazy.yml nie został wybudowany.");
			return false;
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
		return true;
	}
		
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "zaproś", "wyrzuć", "opuść", "stwórz");
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, "Ta komenda jest zarezerwowana tylko dla graczy");
		Player p = (Player) sender;
		switch (cmd.getName()) {
		case "gildia":
			return komendaGildia(p, args);
		case "usuńbaze":
			for (ProtectedRegion region : regiony(p.getWorld()).getApplicableRegions(locToVec3(p.getLocation()))) {
				Baza baza = Baza.wczytaj(p.getWorld(), region);
				if (baza != null) {
					if (!baza.region.getOwners().contains(sender.getName()))
						return Func.powiadom(prefix, sender, "To nie twoja baza");
					baza.usuń();
					sender.sendMessage(prefix + "Usunięto baza z pod twoich nóg");
					return true;
				}
			}
			sender.sendMessage(prefix + "W tym miejscu nie ma żadnej bazy");
			break;
		case "ulepszbaze":
			for (ProtectedRegion region : regiony(p.getWorld()).getApplicableRegions(locToVec3(p.getLocation()))) {
				Baza baza = Baza.wczytaj(p.getWorld(), region);
				if (baza != null) {
					if (!baza.region.getOwners().contains(sender.getName()))
						return Func.powiadom(prefix, sender, "To nie twoja baza");
					baza.ulepsz(2);
					sender.sendMessage(prefix + "Ulepszono baze z pod twoich nóg");
					return true;
				}
			}
			sender.sendMessage(prefix + "Tu nie ma żadnej bazy");
			break;
		}
		return true;
	}
	
	@Override
	public int czas() {
		Set<String> doUsunięcia = Sets.newConcurrentHashSet();
		for (Entry<String, Krotka<String, Integer>> en : mapaZaproszeń.entrySet()) {
			if ((en.getValue().b -= 1) <= 0) {
				Func.napisz(en.getKey(), Gildia.prefix + Func.msg("Zaproszenie do gildi dla %s wygasło", en.getValue().a));
				Func.napisz(en.getValue().a, Gildia.prefix + Func.msg("Zaproszenie do gildi od %s wygasło", en.getKey()));
				doUsunięcia.add(en.getKey());
			}
		}
		for (String nick : doUsunięcia)
			mapaZaproszeń.remove(nick);
		return 20;
	}
	
	// nick zapraszającego: (zaproszony, czas)
	private final HashMap<String, Krotka<String, Integer>> mapaZaproszeń = new HashMap<>();
	private final int czasZaproszeń = 2*60; // max czas zaproszeń w sekundach
	boolean komendaGildia(Player sender, String[] args) {
		if (args.length < 1) return edytor(sender);
		
		Gracz g = Gracz.wczytaj(sender.getName());
		Gildia gildia = Gildia.wczytaj(g.gildia);
		
		BooleanSupplier maGildie = () -> {
			if (g.gildia == null || g.gildia.isEmpty()) {
				sender.sendMessage(Gildia.prefix + "Nie należysz do żadnej gildi");
				return false;
			}
			return true;
		};
		BooleanSupplier przywódca = () -> {
			if (gildia.przywódca.equals(sender.getName()))
				return true;
			sender.sendMessage(Gildia.prefix + "Tylko przywódca gildi może to zrobić");
			return false;
		};
			
		switch (args[0].toLowerCase()) {
		case "zaproś":
		case "zapros":
			if (args.length < 2) 
				return Func.powiadom(sender, Gildia.prefix + "/gildia zaproś <nick>");
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;
			if (mapaZaproszeń.containsKey(sender.getName()))
				return Func.powiadom(sender, Gildia.prefix + "Poczekaj aż minie poprzednie zaproszenie zanim wyślesz kolejne");
			
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) return Func.powiadom(sender, Gildia.prefix + "Gracz nie jeste online");
			
			Gracz zaproszony = Gracz.wczytaj(p.getName());
			if (!(zaproszony.gildia == null || zaproszony.gildia.isEmpty()))
				return Func.powiadom(sender, Gildia.prefix + Func.msg("%s nalezy już do gildi %s", args[1], zaproszony.gildia));
			
			mapaZaproszeń.put(sender.getName(), new Krotka<>(p.getName(), czasZaproszeń));
			
			Napis n = new Napis();
			n.dodaj(Func.msg("%s zaprasza cię do gildi %s ", sender.getName(), gildia.nazwa));
			n.dodaj(new Napis("§a[dołącz]", "§9Kliknij aby dołączyć", "/gildia dołącz " + sender.getName() + " " + gildia.nazwa));
			n.wyświetl(p);
			
			sender.sendMessage(Gildia.prefix + Func.msg("Wysłano zaproszenie dla gracza %s, które wygaśnie za %s", args[1], Func.czas(czasZaproszeń)));
			
			break;
		case "wyrzuć":
		case "wyrzuc":
			if (!maGildie.getAsBoolean()) break;
			if (!przywódca.getAsBoolean()) break;
			if (args.length < 2) 
				return Func.powiadom(sender, Gildia.prefix + "/gildia wyrzuć <nick>");
			if (!gildia.gracze.contains(args[1])) 
				return Func.powiadom(sender, Gildia.prefix + Func.msg("%s nie należy do twojej gildii", args[1]));
			gildia.opuść(args[1]);
			gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s wyrzucił %s z gildi", gildia.nazwa, sender.getName(), args[1]));
			Func.napisz(args[1], Gildia.prefix + Func.msg("Zostałeś wyrzucony z gildi %s przez %s", gildia.nazwa, sender.getName()));
			break;
		case "opuść":
		case "opuśc":
		case "opusć":
		case "opusc":
			if (!maGildie.getAsBoolean()) break;
			gildia.opuść(sender.getName());
			gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s opuścił gildię", gildia.nazwa, sender.getName()));
			sender.sendMessage(Gildia.prefix + Func.msg("Opuściłeś gildię %s", gildia.nazwa));
			break;
		case "stwórz":
		case "stworz":
			if (args.length < 2)
				return Func.powiadom(sender, Gildia.prefix + "/gildia stwórz <nazwa>");
			
			if (Gildia.istnieje(args[1]))
				return Func.powiadom(sender, Gildia.prefix + Func.msg("gildia %s już istnieje", args[1]));
			
			Gildia.stwórz(args[1], sender.getName());
			sender.sendMessage(Gildia.prefix + Func.msg("Gildia %s została utworzona", args[1]));
			break;
		case "dołącz": // musi byc na dole bo nie ma bezpośrednio break
			try {
				String zapraszający = args[1];
				String nazwaGildi = args[2];
				if (!mapaZaproszeń.containsKey(zapraszający))
					return Func.powiadom(sender, Gildia.prefix + "To zaproszenie już nie jest aktualne");
				if (!(g.gildia == null || g.gildia.isEmpty()))
					return Func.powiadom(sender, Gildia.prefix + "Należysz już do gildi");
				Gildia _gildia = Gildia.wczytaj(nazwaGildi);
				if (_gildia == null)
					return Func.powiadom(sender, Gildia.prefix + "Ta gildia już nie istnieje");
				_gildia.dołącz(sender);
				_gildia.wyświetlCzłonkom(Gildia.prefix + Func.msg("%s %s na mocy %s dołączył do gildi", zapraszający, nazwaGildi, sender.getName()));

				mapaZaproszeń.remove(zapraszający);
				break;
			} catch (Throwable e) {}
		default:
			return edytor(sender);
		}
		return true;
	}
	boolean edytor(Player p) {
		Napis n = new Napis();
		
		Gracz g = Gracz.wczytaj(p.getName());
		if (!g.posiadaGildie()) {
			n.dodaj(Gildia.prefix);
			n.dodaj(new Napis("§a[stwórz gildie]\n", "§bWymagana nazwa gildi", "/gildia stwórz ", Action.SUGGEST_COMMAND));
		} else {
			Gildia gildia = Gildia.wczytaj(g.gildia);	
			n.dodaj("\n\n\n\n\n§9Gracze gildi " + gildia.nazwa + ":\n");
			n.dodaj("§e§l- §e^§b" + gildia.przywódca + "§e^\n");
			for (String nick : gildia.gracze) {
				n.dodaj("§e§l- §b" + nick + " ");
				if (p.getName().equals(gildia.przywódca))
					n.dodaj(new Napis("§c[x]", "§cWyrzuć", "/gildia wyrzuć " + nick));
				n.dodaj("\n");
			}
			n.dodaj(new Napis("§c[opuść]", "§cKliknij aby opuść gildie", "/gildia opuść"));
			n.dodaj("\n\n");
		}
		
		n.wyświetl(p);
		
		return true;
	}

	RegionManager regiony(World świat) {
		return regiony.get(BukkitAdapter.adapt(świat));
	}
	BlockVector3 locToVec3(Location loc) {
		return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public static Set<String> getBazy() {
		ConfigurationSection sekcja = config.sekcja("bazy");
		if (sekcja == null) return Sets.newConcurrentHashSet();
		return sekcja.getKeys(false);
	}
	public static ItemStack getBaze(String nazwa) {
		return config.wczytajItem("bazy." + nazwa + ".item");
	}
}


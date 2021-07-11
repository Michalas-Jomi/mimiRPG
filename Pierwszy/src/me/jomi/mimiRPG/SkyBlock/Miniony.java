package me.jomi.mimiRPG.SkyBlock;

import static me.jomi.mimiRPG.util.NMS.nms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.world.entity.decoration.EntityArmorStand;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.KolorRGB;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Miniony extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix(Miniony.class);
	static final NamespacedKey kluczMiniona = new NamespacedKey(Main.plugin, "mimiSkyblockMinion");
	static final String metaMiniona = "mimiSkyblockMinion";
	public static class DropMiniona extends Mapowany {
		@Mapowane ItemStack item;
		@Mapowane double szansa;
	}
	public static class MinionDaneLvl extends Mapowany {
		@Mapowane int czas; // w sekundach
		@Mapowane int slotyEq;
		
		@Mapowane Material narzędzie;
		
		@Mapowane ItemStack potrzebnyItemUpgradu;
		@Mapowane int ilośćPotrzebnegoItemuUpgradu;
	}
	public static class MinionDane extends Mapowany {
		static final Map<String, MinionDane> mapa = new HashMap<>();
		
		@Mapowane List<MinionDaneLvl> lvle;
		@Mapowane String skinurl;
		@Mapowane KolorRGB ubranko;
		@Mapowane String nazwa;
		@Mapowane List<DropMiniona> produkowaneItemy;
		@Mapowane(nieTwórz = true) Material wymaganyBlok;
		
		@Override
		protected void Init() {
			if (skinurl == null) return;
			
			itemLvl0 = Func.dajGłówkę("§6Minion §c" + nazwa, skinurl, "§7Minion pracuje nawet", "§7gdy jesteś offline!");
			itemLvl0 = CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(itemLvl0));
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("dane", nazwa);
			tag.setByte("lvl", (byte) 0);
			tag.set("itemy", new NBTTagList());
			NMS.nms(itemLvl0).getTag().set("minion", tag);
			
			if (produkowaneItemy.isEmpty())
				Main.warn(Func.msg("Minion %s nie produkuje żadnych itemów!", nazwa));
		}
		ItemStack itemLvl0;
		public ItemStack itemLvl0() {
			return itemLvl0.clone();
		}
		
		public static MinionDane daj(String nazwa) {
			return mapa.get(nazwa);
		}
		
		public Minion postaw(Location loc, NBTTagCompound tag) {
			EntityArmorStand as = new EntityArmorStand(NMS.nms(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
			as.setInvulnerable(true);
			as.setNoGravity(true);
			as.setBasePlate(true);
			as.setSmall(true);
			as.setArms(true);
			
			ArmorStand bukkit = (ArmorStand) as.getBukkitEntity();
			bukkit.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.HAND,  	LockType.ADDING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.HAND,  	LockType.REMOVING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.HEAD,  	LockType.ADDING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.HEAD,  	LockType.REMOVING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.CHEST,	LockType.ADDING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.CHEST,	LockType.REMOVING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.LEGS,		LockType.ADDING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.LEGS,		LockType.REMOVING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.FEET,  	LockType.ADDING_OR_CHANGING);
			bukkit.addEquipmentLock(EquipmentSlot.FEET,  	LockType.REMOVING_OR_CHANGING);
			
			tag.setLong("ost", System.currentTimeMillis());
			bukkit.getPersistentDataContainer().set(kluczMiniona, PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer(tag));
			
			
			Minion minion = new Minion(bukkit);
			
			minion.ubierz();

			NMS.nms(loc.getWorld()).addEntity(as, SpawnReason.CUSTOM);
			
			return minion;
		}
	}
	public static class Minion {
		static final Map<UUID, Minion> miniony = new HashMap<>();
		final Inventory inv;
		final MinionDane dane;
		ArmorStand as;
		int timer;
		int lvl;
		
		boolean pełny = false;
		
		int wyprodukowane;
		
		Minion(ArmorStand as) {
			this.as = as;
			
			if (miniony.containsKey(this.as.getUniqueId())) {
				this.inv = null;
				this.dane = null;
				
				Bukkit.getScheduler().runTask(Main.plugin, () -> miniony.get(as.getUniqueId()).as = (ArmorStand) Bukkit.getEntity(as.getUniqueId()));
				
				return;
			}
			
			miniony.put(this.as.getUniqueId(), this);
			Func.ustawMetadate(this.as, metaMiniona, this);
			
			NBTTagCompound tag = tag();
			this.lvl = tag.getByte("lvl");
			this.dane = MinionDane.daj(tag.getString("dane"));
			this.wyprodukowane = tag.getInt("wyprodukowane");
			
			timer = dane.lvle.get(lvl).czas;
			
			inv = panelMiniona.stwórz(this, 6, "§4Minion §1" + dane.nazwa);
			Func.ustawPuste(inv, Baza.pustySlotCzarny);
			
			inv.setItem(slotMinionZbierzWszystko, itemMinionZbierzWszystko);
			inv.setItem(slotMinionPodnieś, itemMinionPodnieś);
			ustawItemUlepszeniaIInfo();
			
			for (int i = 10; i < 10 + 4*9; i += 9)
				inv.setItem(i, itemMinionUlepszenia);
			
			int dostępneSloty = dane.lvle.get(lvl).slotyEq;
			for (int i=0; i < 15; i++)
				inv.setItem((i / 5 + 2) * 9 + i % 5 + 3, i < dostępneSloty ? null : Func.stwórzItem(Material.WHITE_STAINED_GLASS_PANE, "§cSlot niedostępny"));

			NBTTagList itemy = (NBTTagList) tag.get("itemy");
			for (int i = 0; i < itemy.size(); i++) {
				NBTTagCompound item = itemy.getCompound(i);
				inv.setItem(item.getByte("Slot"), CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.a(item)));
			}
			
			odrób((int) ((System.currentTimeMillis() - tag.getLong("ost")) / 1_000));
		}
		
		// czas w sekundach
		private void odrób(int czas) {
			int tickiPracy = czas / dane.lvle.get(lvl).czas;
			
			Set<Location> puste = new HashSet<>();
			if (!przeskanuj(puste))
				return;
			
			for (Location loc : puste) {
				if (tickiPracy-- <= 0)
					return;
				loc.getBlock().setType(dane.wymaganyBlok);
			}
			
			tickiPracy /= 2;
			
			Set<Integer> pełne = new HashSet<>();
			
			while (tickiPracy-- > 0)
				for (int i = 0; i < dane.produkowaneItemy.size(); i++) {
					if (pełne.contains(i))
						continue;
					DropMiniona drop = dane.produkowaneItemy.get(i);
					if (Func.losuj(drop.szansa))
						if (!inv.addItem(drop.item).isEmpty()) {
							pełne.add(i);
							if (pełne.size() == dane.produkowaneItemy.size()) {
								ustawPełny(true);
								return;
							}
						}
				}
		}
		
		private NBTTagCompound tag() {
			return NMS.tag(as.getPersistentDataContainer().get(kluczMiniona, PersistentDataType.TAG_CONTAINER));
		}
		void zapisz() {
			NBTTagCompound tag = tag();
			tag.setByte("lvl", (byte) lvl);
			tag.setString("dane", dane.nazwa);
			tag.setLong("ost", System.currentTimeMillis());
			NBTTagList itemy = new NBTTagList();
			for (int y=2; y < 5; y++)
				for (int x=3; x < 8; x++) {
					int slot = y * 9 + x;
					ItemStack item = inv.getItem(slot);
					if (item == null) continue;
					if (slotNiedostępny(item))
						break;
					
					NBTTagCompound tagItemu = new NBTTagCompound();
					NMS.nms(item).save(tagItemu);
					tagItemu.setByte("Slot", (byte) slot);
					itemy.add(tagItemu);
				}
			tag.set("itemy", itemy);
			tag.setInt("wyprodukowane", wyprodukowane);
			
			as.getPersistentDataContainer().set(kluczMiniona, PersistentDataType.TAG_CONTAINER, NMS.utwórzDataContainer(tag));
		}
	
		
		/// Praca
		void czas() {
			if (timer-- <= 0) {
				timer = dane.lvle.get(lvl).czas;
				
				if (pełny)
					if (inv.firstEmpty() == -1)
						return;
					else
						ustawPełny(false);
				
				if (as.isDead()) {
					Bukkit.getScheduler().runTask(Main.plugin, () -> miniony.remove(as.getUniqueId()));
					return;
				}
				
				Set<Location> puste = new HashSet<>();
				if (!przeskanuj(puste)) {
					as.setCustomNameVisible(true);
					as.setCustomName("§cNie mogę pracować w takich warunkach!");
					return;
				} else if (as.isCustomNameVisible()) {
					as.setCustomNameVisible(false);
					as.setCustomName(null);
				}
				
				if (puste.isEmpty()) {
					Location loc = losujBlok();
					spójrz(loc);
					kop(loc, 0);
				} else {
					Location loc = Func.losuj(puste);
					spójrz(loc);
					postaw(loc, 0);
				}
				
			}
		}
		// szuka niepoprawnego bloku i pustych miejsc
		private boolean przeskanuj(Set<Location> puste) {
			if (dane.wymaganyBlok == null)
				return true;
			
			for (int x = -2; x < 3; x++)
				for (int z = -2; z < 3; z++) {
					if (x == 0 && z == 0) continue;
					Location loc = as.getLocation().clone().add(x, -1, z);
					Material type = loc.getBlock().getType();
					if (type.isAir()) {
						puste.add(loc);
					} else if (type != dane.wymaganyBlok)
						return false;
				}
			
			return true;
		}

		// losuje blok do wykopania
		private Location losujBlok() {
			int x, z;
			do {
				x = Func.losuj(-2, 2);
				z = Func.losuj(-2, 2);
			} while(x == 0 && z == 0);
			return as.getLocation().add(x, -.5, z);
		}
		
		
		/// Panel
		public void zbierzWszystko(Player p) {
			AtomicInteger ile = new AtomicInteger();
			for (int i=0; i < dane.lvle.get(lvl).slotyEq; i++) {
				int slot = (i / 5 + 2) * 9 + i % 5 + 3;
				Func.wykonajDlaNieNull(inv.getItem(slot), item -> {
					ile.getAndAdd(item.getAmount());
					Func.dajItem(p, item);
					inv.setItem(slot, null);
				});
			}
			Main.log(prefix + "%s zebrał %s itemów z miniona %s lvl %s na %s", p.getName(), ile.get(), dane.nazwa, this.lvl+1, Func.locBlockToString(as.getLocation()));
		}
		
		public void ulepsz(Player p) {
			if (dane.lvle.size() <= this.lvl + 1) {
				Func.powiadom(prefix, p, "Osiągnięto już maksymany poziom tego miniona");
				return;
			}
			
			PlayerInventory inv = p.getInventory();
			MinionDaneLvl lvl = dane.lvle.get(this.lvl + 1);
			
			int ile = 0;
			for (ItemStack item : inv)
				if (lvl.potrzebnyItemUpgradu.isSimilar(item)) {
					ile += item.getAmount();
					if (ile >= lvl.ilośćPotrzebnegoItemuUpgradu)
						break;
				}
			
			int zabrane = 0;
			if (ile >= lvl.ilośćPotrzebnegoItemuUpgradu) {
				for (int i=0; i < inv.getSize(); i++) {
					ItemStack item = inv.getItem(i);
					if (lvl.potrzebnyItemUpgradu.isSimilar(item)) {
						zabrane += item.getAmount();
						if (zabrane <= lvl.ilośćPotrzebnegoItemuUpgradu) {
							inv.setItem(i, null);
							if (zabrane == lvl.ilośćPotrzebnegoItemuUpgradu)
								break;
						} else {
							item.setAmount(zabrane - lvl.ilośćPotrzebnegoItemuUpgradu);
							inv.setItem(i, item);
							break;
						}
					}
				}
				p.closeInventory();
				this.lvl++;
				ustawItemUlepszeniaIInfo();
				
				Minion.miniony.remove(as.getUniqueId());
				zapisz();
				new Minion(as);
				
				Func.powiadom(prefix, p, "Ulepszyłeś Swojego Miniona %s na poziom %s!", dane.nazwa, this.lvl + 1);
				Main.log(prefix + "%s ulepszył miniona %s na poziom %s", p.getName(), dane.nazwa, this.lvl + 1);
			} else
				Func.powiadom(prefix, p, "Potrzebujesz jeszcze %s %s aby ulepszyć tego miniona",
						lvl.ilośćPotrzebnegoItemuUpgradu - ile, Func.nazwaItemku(lvl.potrzebnyItemUpgradu));
		}
		private void ustawItemUlepszeniaIInfo() {
			inv.setItem(slotMinionUlepsz, itemUlepszenia());
			inv.setItem(slotMinionInfo, item());
			ubierz();
		}
		
		ItemStack itemUlepszenia() {
			ItemStack item = itemMinionUlepsz.clone();
			
			if (dane.lvle.size() <= this.lvl + 1)
				return Func.dodajLore(item, "§6lvl §4§l§oMAX " + (this.lvl + 1));
			
			MinionDaneLvl lvl = dane.lvle.get(this.lvl + 1);
			
			List<String> lore = new ArrayList<>();
			
			lore.add(Func.msg("%s lvl -> %s lvl", this.lvl + 1, this.lvl + 2));
			lore.add("§6Potrzeba: §e" + Func.nazwaItemku(lvl.potrzebnyItemUpgradu) + "§b x " + Func.IntToString(lvl.ilośćPotrzebnegoItemuUpgradu));
			
			Func.ustawLore(item, lore);
			
			return item;
		}
		
		public void usuń(Player p) {
			Func.dajItem(p, item());
			zbierzWszystko(p);
			Func.wykonajDlaNieNull(Bukkit.getEntity(as.getUniqueId()), Entity::remove);
			as.remove();
			
			while (!inv.getViewers().isEmpty())
				inv.getViewers().get(0).closeInventory();
			
			Main.log("%s podniósł miniona %s lvl %s z %s", p.getName(), dane.nazwa, lvl + 1, Func.locBlockToString(as.getLocation()));
		}
		
		
		/// Animacje
		public void spójrz(Block blok) {
			spójrz(blok.getLocation().add(.5, .5, .5));
		}
		public void spójrz(Location loc) {
			Location głowa = as.getEyeLocation();
			double a, b, c, sin;
			float kąt;
			
			a = loc.getX() - głowa.getX();
			b = loc.getZ() - głowa.getZ();
			c = Math.sqrt(a*a + b*b);
			sin = Math.abs(a) / c;
			kąt = (float) Math.toDegrees(Math.asin(sin));
			
			if (a * b > 0)
				kąt *= -1;
			if (b < 0 || (b == 0 && a > 0))
				kąt += 180;
				
			Location locMiniona = as.getLocation();
			locMiniona.setYaw(kąt);
			as.teleport(locMiniona);
			
			a = loc.getY() - głowa.getY();
			c = loc.distance(głowa);
			sin = Math.abs(a) / c;
			as.setHeadPose(new EulerAngle((a > 0 ? -1 : 1) * Math.asin(sin), 0, 0));
		}
		
		public void kop(Location loc, int progress) {
			double zasięg = 25;
			if (progress >= 20) {
				loc.getBlock().setType(Material.AIR);
				Packet<?> packet = new PacketPlayOutBlockBreakAnimation(as.getEntityId() + 1, NMS.nms(loc), -1);
				as.getWorld().getNearbyEntities(as.getLocation(), zasięg, zasięg, zasięg,
						e -> e instanceof Player).forEach(gracz -> nms((Player) gracz).b.sendPacket(packet));
				AtomicBoolean pełny = new AtomicBoolean(true);
				AtomicBoolean cośDropnięte = new AtomicBoolean(false);
				dane.produkowaneItemy.forEach(drop -> {
					if (Func.losuj(drop.szansa)) {
						cośDropnięte.set(true);
						pełny.set(pełny.get() && !inv.addItem(drop.item).isEmpty());
						wyprodukowane += drop.item.getAmount();
					}
				});
				if (cośDropnięte.get() && pełny.get())
					ustawPełny(true);
				Func.opóznij(5, this::resetAnimacji);
				return;
			}

			Packet<?> packet = new PacketPlayOutBlockBreakAnimation(as.getEntityId() + 1, NMS.nms(loc), (int) (progress / 20d * 10));
			as.getWorld().getNearbyEntities(as.getLocation(), zasięg, zasięg, zasięg,
					e -> e instanceof Player).forEach(gracz -> nms((Player) gracz).b.sendPacket(packet));
			
			int x = progress % 10;
			// pi -> pi / 2
			as.setRightArmPose(new EulerAngle((Math.PI / 20) * x + Math.PI / 10 * 13, 0, -Math.PI / 10));
			Func.opóznij(2, () -> kop(loc, progress + 1));
		}
		
		public void postaw(Location loc, int progress) {
			if (progress >= 20) {
				strzelBlokiem(loc);
				return;
			}
			
			double x = progress / 20d;
			EulerAngle angle = new EulerAngle(Math.PI / -2 + Math.PI / 4 * 1 * x, Math.PI / -2 + Math.PI / 4 * 3 * x, 0);
			as.setLeftArmPose(angle);
			as.setRightArmPose(new EulerAngle(angle.getX() * -1, angle.getY() * -1 + Math.PI, 0));
			
			Func.opóznij(1, () -> postaw(loc, progress + 1));
		}
		private void strzelBlokiem(Location B) {
			Location A = this.as.getEyeLocation();
			
			ArmorStand as = Func.zrespNietykalnyArmorStand(this.as.getLocation());
			as.getEquipment().setHelmet(new ItemStack(dane.wymaganyBlok));
			as.addScoreboardTag(Main.tagTempMoba);
			
			double x = B.getX() - A.getX();
			double y = B.getY() - A.getY();
			double z = B.getZ() - A.getZ();
			
			Vector v = new Vector(x, y, z).multiply(.05);
			
			strzelBlokiem(B, as, v, 0);
		}
		private void strzelBlokiem(Location loc, ArmorStand as, Vector v, int kontrolny) {
			if (kontrolny > 21) {
				as.remove();
				if (loc.getBlock().getType().isAir())
					loc.getBlock().setType(dane.wymaganyBlok);
				Func.opóznij(5, this::resetAnimacji);
				return;
			}
			
			as.teleport(as.getLocation().add(v));
			as.setHeadPose(new EulerAngle(Math.PI / 9 * kontrolny, 0, 0));
			
			Func.particle(Particle.CLOUD, as.getEyeLocation(), 1, 0, 0, 0, 0);
			
			Func.opóznij(1, () -> strzelBlokiem(loc, as, v, kontrolny + 1));
		}
		
		public void resetAnimacji() {
			as.setHeadPose(EulerAngle.ZERO);
			as.setRightArmPose(EulerAngle.ZERO);
			as.setLeftArmPose(EulerAngle.ZERO);
			as.setRightLegPose(EulerAngle.ZERO);
			as.setLeftLegPose(EulerAngle.ZERO);
			as.setBodyPose(EulerAngle.ZERO);
		}

		
		/// util
		public ItemStack item() {
			ItemStack item = dane.itemLvl0();
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.getLore();
			
			lore.add(" ");
			lore.add("§7Poziom§8: §e" + (lvl + 1));
			lore.add("§7Pracuje co§8: §e" + lvl().slotyEq + "s");
			lore.add("§7Ekwipunek§8: §e" + lvl().slotyEq + " slotów");
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			return item;
		}

		public void ubierz() {
			EntityEquipment eq = as.getEquipment();
			
			eq.setHelmet		(item());
			eq.setChestplate	(Func.pokolorujZbroje(Func.stwórzItem(Material.LEATHER_CHESTPLATE), dane.ubranko.kolor()));
			eq.setLeggings		(Func.pokolorujZbroje(Func.stwórzItem(Material.LEATHER_LEGGINGS), 	dane.ubranko.kolor()));
			eq.setBoots			(Func.pokolorujZbroje(Func.stwórzItem(Material.LEATHER_BOOTS), 		dane.ubranko.kolor()));
			eq.setItemInMainHand(Func.stwórzItem(dane.lvle.get(lvl).narzędzie));
		}
		
		public void ustawPełny(boolean pełny) {
			this.pełny = pełny;
			as.setCustomNameVisible(pełny);
			as.setCustomName(pełny ? "§cMój Ekwipunek jest pełny!" : null);
		}
		
		public MinionDaneLvl lvl() {
			return dane.lvle.get(lvl);
		}
	}
	
	static int slotMinionInfo = 4;
	static int slotMinionPodnieś = 53;
	static int slotMinionZbierzWszystko = 48;
	static int slotMinionUlepsz = 50;
	static ItemStack itemMinionUlepsz = Func.stwórzItem(Material.DIAMOND, "§aUlepsz");
	static ItemStack itemMinionPodnieś = Func.stwórzItem(Material.BEDROCK, "§aPodnieś Miniona");
	static ItemStack itemMinionZbierzWszystko = Func.stwórzItem(Material.CHEST, "§aZbierz wszystko");
	static ItemStack itemMinionUlepszenia = Func.stwórzItem(Material.GRAY_STAINED_GLASS_PANE, "§aBoostery", "§4Dostępne niebawem!");
	// TODO boostery miniona
	
	static Panel panelMiniona = new Panel(true);
	
	public Miniony() {
		super("edytujminiony");
		Bukkit.getScheduler().runTask(Main.plugin, () -> Bukkit.getWorlds().forEach(world -> Func.forEach(world.getLoadedChunks(), Miniony::wczytywanieChunka)));
		
		panelMiniona.ustawClick(ev -> {
			Player p = (Player) ev.getWhoClicked();
			int slot = ev.getRawSlot();
			int c = slot / 9;
			int mod = slot % 9;
			if (c >= 2 && c <= 4 && mod >= 3 && mod <= 7) {
				if (!slotNiedostępny(ev.getCurrentItem()))
					ev.setCancelled(false);
			} else if (slot == slotMinionZbierzWszystko)
				((Minion) panelMiniona.dajDanePanelu(ev.getInventory())).zbierzWszystko(p);
			else if (slot == slotMinionPodnieś) {
				Minion minion = ((Minion) panelMiniona.dajDanePanelu(ev.getInventory()));
				minion.usuń(p);
			} else if (slot == slotMinionUlepsz)
				((Minion) panelMiniona.dajDanePanelu(ev.getInventory())).ulepsz(p);
		});
	}

	
	public static boolean slotNiedostępny(ItemStack item) {
		return	item != null &&
				item.getType() == Material.WHITE_STAINED_GLASS_PANE &&
				item.hasItemMeta() &&
				"§cSlot niedostępny".equals(item.getItemMeta().getDisplayName());
	}
	
	static void wczytywanieChunka(Chunk chunk) {
		Func.forEach(chunk.getEntities(), e -> {
			if (e.getPersistentDataContainer().has(kluczMiniona, PersistentDataType.TAG_CONTAINER))
				new Minion((ArmorStand) e);
		});
	}

	
	/// EventHandler
	@EventHandler
	public void klikanieMiniona(PlayerInteractAtEntityEvent ev) {
		if (ev.getRightClicked().hasMetadata(metaMiniona))
			ev.getPlayer().openInventory(((Minion) ev.getRightClicked().getMetadata(metaMiniona).get(0).value()).inv);// TODO permisje
	}
	@EventHandler
	public void spawnMoba(EntitySpawnEvent ev) {
		if (ev.getEntity().getPersistentDataContainer().has(kluczMiniona, PersistentDataType.TAG_CONTAINER))
			new Minion((ArmorStand) ev.getEntity());
	}
	@EventHandler
	public void wczytywanieChunka(ChunkLoadEvent ev) {
		wczytywanieChunka(ev.getChunk());
	}
	@EventHandler
	public void unloadChunka(ChunkUnloadEvent ev) {
		Func.forEach(ev.getChunk().getEntities(), e -> {
			if (e.hasMetadata(metaMiniona)) {
				Minion.miniony.remove(e.getUniqueId());
				((Minion) e.getMetadata(metaMiniona).get(0).value()).zapisz();
			}
		});
		Func.opóznij(20, () -> {
			if (ev.getChunk().isLoaded())
				wczytywanieChunka(ev.getChunk());
		});
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void stawianieMiniona(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		Func.wykonajDlaNieNull(NMS.nms(ev.getItemInHand()), nms ->
				Func.wykonajDlaNieNull(nms.getTag(), itemTag -> {
					NBTTagCompound tag = itemTag.getCompound("minion");
					if (tag.isEmpty()) return;
					
					MinionDane minion = MinionDane.daj(tag.getString("dane"));
					if (minion == null) {
						ev.setCancelled(true);
						return;
					}
					
					Bukkit.getScheduler().runTask(Main.plugin, () -> {
						if (ev.isCancelled()) return;
						
						ev.getBlock().setType(Material.AIR, false);
						
						minion.postaw(ev.getBlock().getLocation().add(.5, 0, .5), tag);
						Main.log(prefix + "%s postawił miniona %s na %s", ev.getPlayer().getName(), minion.nazwa, Func.locBlockToString(ev.getBlock().getLocation()));
					});
				}));
	}
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getRawSlot() >= ev.getInventory().getSize() && panelMiniona.jestPanelem(ev.getInventory()))
			if (Func.multiEquals(ev.getClick(), ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT))
				ev.setCancelled(true);
	}
	
	
	/// Override
	@Override
	public int czas() {
		Minion.miniony.values().forEach(Minion::czas);
		return 20;
	}
	
	@Override
	public void przeładuj() {
		Config config = new Config("configi/Miniony");
		
		MinionDane.mapa.values().forEach(minion -> CustomoweItemy.customoweItemy.remove("Minion_" + minion.nazwa));
		MinionDane.mapa.clear();
		
		config.klucze().forEach(klucz -> {
			MinionDane minion = config.wczytajPewny(klucz);
			if (!klucz.equals(minion.nazwa)) {
				Main.warn(prefix + Func.msg("Nazwa miniona %s jest różna z jego kluczem %s", minion.nazwa, klucz));
				return;
			}
			MinionDane.mapa.put(klucz, minion);
			CustomoweItemy.customoweItemy.put("Minion_" + minion.nazwa, minion.itemLvl0());
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane miniony", MinionDane.mapa.size());
	}
	
	EdytorOgólny<MinionDane> edytor = new EdytorOgólny<>("edytujminiony", MinionDane.class);
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return edytor.wymuśConfig_onTabComplete(new Config("configi/Miniony"), sender, label, args);
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		return edytor.wymuśConfig_onCommand(prefix, "configi/Miniony", sender, label, args);
	}
}

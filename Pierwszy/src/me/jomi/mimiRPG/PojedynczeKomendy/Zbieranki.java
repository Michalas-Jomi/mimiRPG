package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.EulerAngle;

import com.mojang.datafixers.util.Pair;

import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Zbieranki extends Komenda implements Listener, Przeładowalny, Zegar {
	public Zbieranki() {
		super("ustawZbieranke");
		ustawKomende("resetZbieranek", "/resetZbieranek <gracz>", null);
		CustomoweItemy.customoweItemy.put("zbieranki_destruktor", destruktor);
	}
	
	Set<UUID> uuids = new HashSet<>(); 
	
	private static final org.bukkit.inventory.ItemStack destruktor = Func.stwórzItem(Material.STICK, "&4Destruktor Zbieranek", 1, "&aNiszczy zbieranki");
	
	void ukryj(Player p, Entity armorStand) {
		Packet<?> packet = new PacketPlayOutEntityEquipment(
				armorStand.getEntityId(),
				Arrays.asList(new Pair<>(EnumItemSlot.HEAD, ItemStack.b))
				);
		
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	void pokaż(Player p, Entity armorStand) {
		Packet<?> packet = new PacketPlayOutEntityEquipment(
				armorStand.getEntityId(),
				Arrays.asList(new Pair<>(EnumItemSlot.HEAD, ((CraftArmorStand) armorStand).getHandle().getEquipment(EnumItemSlot.HEAD)))
				);
		
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	ArmorStand zresp(Location loc, org.bukkit.inventory.ItemStack item) {
		loc.add(0, -1.8, 0);
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.getEquipment().setHelmet(item);
		as.setInvulnerable(true);
		as.setGravity(false);
		as.setVisible(false);
		as.setBasePlate(false);
		as.addEquipmentLock(EquipmentSlot.HEAD,  LockType.REMOVING_OR_CHANGING);
		as.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
		as.addEquipmentLock(EquipmentSlot.LEGS,  LockType.ADDING_OR_CHANGING);
		as.addEquipmentLock(EquipmentSlot.FEET,  LockType.ADDING_OR_CHANGING);
		EulerAngle angle = new EulerAngle(Math.PI, 0, 0);
		as.setRightArmPose(angle);
		as.setLeftArmPose(angle);
		as.setRightLegPose(angle);
		as.setLeftLegPose(angle);
		as.setBodyPose(angle);
		as.setHeadPose(new EulerAngle(Math.PI / 8d, 0, 0));
		as.teleport(loc);
		return as;
	}
	
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		Func.opóznij(1, () -> {
			Gracz g = Gracz.wczytaj(ev.getPlayer());
			for (int i=0; i < g.zbierankiZebrane.size(); i++) {
				UUID uuid = UUID.fromString(g.zbierankiZebrane.get(i));
				Entity e = Bukkit.getEntity(uuid);
				if (e == null || !(e instanceof ArmorStand))
					g.zbierankiZebrane.remove(i--);
				else
					ukryj(ev.getPlayer(), e);
			}
		});
	}
	@EventHandler
	public void interact(PlayerInteractAtEntityEvent ev) {
		UUID uuid = ev.getRightClicked().getUniqueId();
		if (uuids.contains(uuid)) {
			if (Func.porównaj(destruktor, ev.getPlayer().getInventory().getItemInMainHand())) {
				uuids.remove(uuid);
				zapisz();
				ev.getRightClicked().remove();
				ev.getPlayer().sendMessage(preThrowFormatMsg("Zbieranka usunięta"));
				return;
			}
			
			ArmorStand armorStand = (ArmorStand) ev.getRightClicked();
			Gracz g = Gracz.wczytaj(ev.getPlayer());
			if (!g.zbierankiZebrane.contains(uuid.toString())) {
				Func.particle(Particle.TOTEM, armorStand.getLocation().add(0, armorStand.getHeight() + .2, 0), 20, 1, 1, 1, .1);
				Func.particle(Particle.ASH,   armorStand.getLocation().add(0, armorStand.getHeight() + .2, 0), 5, 0, 0, 0, 0);
				
				g.zbierankiZebrane.add(uuid.toString());
				g.zapisz();
				
				ukryj(ev.getPlayer(), armorStand);
				
				Func.dajItem(ev.getPlayer(), armorStand.getEquipment().getHelmet());
			}
		}
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (cmd.getName().equalsIgnoreCase("resetZbieranek")) {
			if (args.length < 1)
				return false;
			Gracz gracz = Gracz.wczytaj(args[0]);
			if (gracz.zbierankiZebrane.isEmpty())
				throwFormatMsg("gracz %s nie zebrał żadnych zbieranek", gracz.nick);
			gracz.zbierankiZebrane.clear();
			gracz.zapisz();
			Func.wykonajDlaNieNull(Bukkit.getPlayer(gracz.nick), p -> uuids.forEach(uuid -> Func.wykonajDlaNieNull(Bukkit.getEntity(uuid), e -> pokaż(p, e))));
			throwFormatMsg("Zresetowano zbieranki gracza %s", gracz.nick);
		}
		
		
		if (!(sender instanceof Player))
			throwFormatMsg("Musisz być graczem aby tego użyć");
		Player p = (Player) sender;
		
		if (p.getInventory().getItemInMainHand().getType().isAir())
			throwFormatMsg("Musisz coś trzymać w ręce");
		
		uuids.add(zresp(p.getLocation(), p.getInventory().getItemInMainHand()).getUniqueId());
		zapisz();
		
		throwFormatMsg("Zbieranka ustawiona");
		
		return true;
	}
	
	Config config = new Config("configi/zbieranki");
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		uuids.clear();
		config.wczytajListe("uuids").forEach(uuid -> uuids.add(UUID.fromString(uuid)));
	}
	void zapisz() {
		config.ustaw_zapisz("uuids", Func.wykonajWszystkim(uuids, uuid -> uuid.toString()));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane Zbieranki", uuids.size());
	}
	@Override
	public int czas() {
		uuids.forEach(uuid -> Func.wykonajDlaNieNull(Bukkit.getEntity(uuid), entity -> {
			Location loc = entity.getLocation();
			loc.setYaw(loc.getYaw() + 5f);
			entity.teleport(loc);
		}));
		return 3;
	}
}

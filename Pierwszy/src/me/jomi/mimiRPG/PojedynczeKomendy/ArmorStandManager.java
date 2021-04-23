package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class ArmorStandManager extends Komenda implements Listener {
	public static final String prefix = Func.prefix(ArmorStandManager.class);
	
	public enum Axis {
		X("--"),
		Y("|"),
		Z("+");

		public final String symbol;
		Axis(String symbol) {
			this.symbol = symbol;
		}
	}
	public enum Target {
		HEAD("Głowa"),
		BODY("Tłów"),
		LEFT_ARM("Lewa Ręka"),
		RIGHT_ARM("Prawa Ręka"),
		LEFT_LEG("Lewa Noga"),
		RIGHT_LEG("Prawa Noga"),
		ROT("Rotacja"),
		POZ("Pozycja");
		
		public final String symbol;
		Target(String symbol) {
			this.symbol = symbol;
		}
	}
	public static class Manager {
		final ArmorStand armorStand;
		
		boolean przemieszczaj = false;
		double czułość = .1;
		Axis axis = Axis.Y;
		
		Target target = Target.POZ;
		
		public Manager(ArmorStand armorStand) {
			this.armorStand = armorStand;
		}

		public Napis generuj() {
			Napis n = new Napis("\n\n§2~~ §aArmorStand Manager §2~~\n\n");
			
			if (przemieszczaj) {
				n.dodaj("\n§9ArmorStandem poruszasz klikając sloty 1 i 2 na hotbarze, slotami 7, 8, 9 zmienisasz kierunek\n");
				n.dodajEnd(new Napis(
						"§a[Przemieszczaj]",
						"§bKliknij aby wyłączyć tryb przemieszczania",
						"/edytujarmorstand przemieszczaj")
						);
				n.dodaj("\n");
				
				n.dodajEnd("§6Ruszane§8:");
				Func.forEach(Target.values(), target -> {
					n.dodajEnd(new Napis(
							"§e§l- §" + (this.target == target ? "a" : "7") + target.symbol,
							"§bKliknij aby poruszyć " + target.symbol,
							"/edytujarmorstand target " + target));
				});
				
				n.dodaj("\n");
				
				n.dodajEnd(new Napis(
						"§6czułość§8: §e" + Func.DoubleToString(czułość),
						"§bKliknij aby zmienić czułość",
						"/edytujarmorstand czułość >> ")
						);
				n.dodaj("\n");
				Func.forEach(Axis.values(), axis -> 
					n.dodaj(new Napis(
						"§" + (this.axis == axis ? "a" : "7") + axis.symbol,
						"§b" + axis.name(),
						"/edytujarmorstand axis " + axis
						)).dodaj(" "));
				n.dodaj("\n\n");
			} else {
				
				n.dodaj("§6Dostępne Sloty§8:");
				Func.forEach(LockType.values(), lockType -> {
					n.dodaj("\n");
					Func.forEach(EquipmentSlot.values(), slot -> {
						if (!armorStand.hasArms() && Func.multiEquals(slot, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND))
							return;
						
						n.dodaj(
								new Napis(
										"§7|§" + (armorStand.hasEquipmentLock(slot, lockType) ? "c" : "a") + Func.enumToString(slot) + "§7| ",
										"§bKliknij aby zmienić",
										"/edytujarmorstand lock " + lockType + " " + slot
										));
					});
					n.dodaj("§e- §6" + Func.enumToString(lockType));
				});
				
				n.dodajEnd("\n\n§6Flagi§8:");
				
				n.dodajEnd(
						fabricTag("Grawitacja",			"Gravity",				armorStand.hasGravity()),
						fabricTag("Nieśmiertelny",		"Invulnerable",			armorStand.isInvulnerable()),
						fabricTag("Ręce",				"Arms",					armorStand.hasArms()),
						fabricTag("Podstawka",			"BasePlate",			armorStand.hasBasePlate()),
						fabricTag("Nick na wierzchu",	"CustomNameVisible",	armorStand.isCustomNameVisible()),
						fabricTag("Poświata",			"Glowing",				armorStand.isGlowing()),
						fabricTag("Widzialny",			"Visible",				armorStand.isVisible()),
						fabricTag("Malutki",			"Small",				armorStand.isSmall())
						);
				
				n.dodaj("\n");
				
				n.dodajEnd(new Napis(
						"§6nick§8: §f" + (armorStand.getCustomName() == null ? "§3brak" : armorStand.getCustomName()),
						"§bKliknij aby ustawić",
						"/edytujarmorstand nick >> "));
				
				
				n.dodajEnd(new Napis(
					"§e[Przemieszcaj]",
					"§bKliknij aby wejść w tryb przemieszczania",
					"/edytujarmorstand przemieszczaj"
					));
			}
			
			return n;
		}
		private Napis fabricTag(String nazwa, String setter, boolean ma) {
			return new Napis(
					"§e§l- §" + (ma ? "a" : "c") + nazwa,
					"§bKliknij aby zmienić",
					"/edytujarmorstand tag " + setter + " " + !ma);
		}
		
	}
	
	public ArmorStandManager() {
		super("edytujarmorstand");
	}
	
	Map<String, Manager> mapa = new HashMap<>(); 
	
	final ItemStack różdzka = Func.stwórzItem(Material.STICK, "ArmorStandSelector", "&aKliknij nim armorstand, aby", "&ago edytować");
	
	
	@EventHandler
	public void wybieranieArmorstanda(PlayerInteractAtEntityEvent ev) {
		if (!(ev.getRightClicked() instanceof ArmorStand)) return;
		if (!ev.getPlayer().hasPermission("mimirpg.edytujarmorstand")) return;
		if (!ev.getPlayer().getInventory().getItemInMainHand().isSimilar(różdzka)) return;
		
		Manager manager = new Manager((ArmorStand) ev.getRightClicked()); 
		
		mapa.put(ev.getPlayer().getName(), manager);
		
		manager.generuj().wyświetl(ev.getPlayer());
		
		ev.setCancelled(true);
		
		Main.log(prefix + Func.msg("%s edytuje ArmorStand %s na pozycji %s %s",
				ev.getPlayer().getName(), ev.getRightClicked().getUniqueId(),
				ev.getRightClicked().getWorld().getName(), Func.locToString(ev.getRightClicked().getLocation())));
		
	}
	
	@EventHandler
	public void zmianaSlotu(PlayerItemHeldEvent ev) {
		Func.wykonajDlaNieNull(mapa.get(ev.getPlayer().getName()), manager -> {
			if (!manager.przemieszczaj) return;

			double ile = manager.czułość;
			switch (ev.getNewSlot()) {
			case 6: manager.axis = Axis.X; break;
			case 7: manager.axis = Axis.Y; break;
			case 8: manager.axis = Axis.Z; break;
			case 1:
				ile *= -1;
			case 0:
				if (manager.target == Target.POZ || manager.target == Target.ROT) {
					Vector vec = new Vector(0, 0, 0);
					switch (manager.axis) {
					case X: vec.setX(ile); break;
					case Y: vec.setY(ile); break;
					case Z: vec.setZ(ile); break;
					}
					
					Location loc = manager.armorStand.getLocation();
					if (manager.target == Target.POZ)
						loc.add(vec);
					else
						loc.setDirection(loc.getDirection().add(vec));
					manager.armorStand.teleport(loc);
				} else {
					Function<ArmorStand, EulerAngle> getter = null;
					BiConsumer<ArmorStand, EulerAngle> setter = null;
					
					switch (manager.target) {
					case BODY:
						getter = ArmorStand::getBodyPose;
						setter = ArmorStand::setBodyPose;
						break;
					case HEAD:
						getter = ArmorStand::getHeadPose;
						setter = ArmorStand::setHeadPose;
						break;
					case LEFT_ARM:
						getter = ArmorStand::getLeftArmPose;
						setter = ArmorStand::setLeftArmPose;
						break;
					case LEFT_LEG:
						getter = ArmorStand::getLeftLegPose;
						setter = ArmorStand::setLeftLegPose;
						break;
					case RIGHT_ARM:
						getter = ArmorStand::getRightArmPose;
						setter = ArmorStand::setRightArmPose;
						break;
					case RIGHT_LEG:
						getter = ArmorStand::getRightLegPose;
						setter = ArmorStand::setRightLegPose;
						break;
					case ROT:
					case POZ: break;
					}
					
					EulerAngle angle = getter.apply(manager.armorStand);
					
					angle = angle.add(
						manager.axis == Axis.X ? ile : 0,
						manager.axis == Axis.Y ? ile : 0,
						manager.axis == Axis.Z ? ile : 0
						);
					
					setter.accept(manager.armorStand, angle);
				}
				
				ev.getPlayer().getInventory().setHeldItemSlot(4);
			default:
				return;
			}
			
			manager.generuj().wyświetl(ev.getPlayer());
		});
	}
	

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (!(sender instanceof Player))
			throwFormatMsg("Tylko gracz może tego użyć");
		Player p = (Player) sender;
		
		if (args.length <= 0) {
			Func.dajItem(p, różdzka);
			throwFormatMsg("Otrzymałeś różdzkę");
		}
		
		
		Func.wykonajDlaNieNull(mapa.get(sender.getName()), manager -> {
			
			if (args.length >= 1)
				switch (args[0]) {
				case "przemieszczaj":
					manager.przemieszczaj = !manager.przemieszczaj;
					break;
				case "czułość":
					manager.czułość = Func.Double(Func.listToString(args, 2));
					break;
				case "axis":
					manager.axis = Func.StringToEnum(Axis.class, args[1]);
					break;
				case "tag":
					try {
						Func.dajMetode(manager.armorStand.getClass(), "set" + args[1], boolean.class).invoke(manager.armorStand, Boolean.parseBoolean(args[2]));
					} catch (Throwable e) {
						e.printStackTrace();
					}
					break;
				case "nick":
					manager.armorStand.setCustomName(Func.koloruj(Func.listToString(args, 2)));
					break;
				case "target":
					manager.target = Func.StringToEnum(Target.class, args[1]);
					break;
				case "lock":
					LockType lockType = Func.StringToEnum(LockType.class, args[1]);
					EquipmentSlot slot = Func.StringToEnum(EquipmentSlot.class, args[2]);
					
					if (manager.armorStand.hasEquipmentLock(slot, lockType))
						manager.armorStand.removeEquipmentLock(slot, lockType);
					else
						manager.armorStand.addEquipmentLock(slot, lockType);
					
					break;
				}
			
			
			manager.generuj().wyświetl(sender);
		}, () -> {
			Func.dajItem(p, różdzka);
			throwFormatMsg("Otrzymałeś różdzkę");
		});
		return true;
	}
}

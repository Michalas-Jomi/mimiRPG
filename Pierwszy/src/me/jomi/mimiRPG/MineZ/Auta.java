package me.jomi.mimiRPG.MineZ;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.MonoKrotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Auta extends Komenda implements Przeładowalny, Listener, Zegar {
	public static final String prefix = Func.prefix(Auta.class);
	private static final String metaAuta = "mimiAuto";
	private static final String tagAutaKierowcy = "mimiAutoKierowca";
	public Auta() {
		super("auto");
	}
	
	public static class AutoDane {
		public static class Model {
			final Map<EquipmentSlot, ItemStack> eq = new EnumMap<>(EquipmentSlot.class);
			final Map<VariantyPose, EulerAngle> pose = new EnumMap<>(VariantyPose.class);
			final Vector przesunięcie = new Vector();
		}
		public static enum VariantyPose {
			BODY	 (ArmorStand::setBodyPose),
			HEAD	 (ArmorStand::setHeadPose),
			LEFT_ARM (ArmorStand::setLeftArmPose),
			RIGHT_ARM(ArmorStand::setRightArmPose),
			LEFT_LEG (ArmorStand::setLeftLegPose),
			RIGHT_LEG(ArmorStand::setRightLegPose);
			
			public final BiConsumer<ArmorStand, EulerAngle> bic;
			VariantyPose(BiConsumer<ArmorStand, EulerAngle> bic) {
				this.bic = bic;
			}
		}
		
		public final String nazwa;
		public final List<Model> modele = new ArrayList<>();
		
		AutoDane(String nazwa, ConfigurationSection sekcja) {
			this.nazwa = nazwa;
			
			// Chest:NETHERITE_CHESTPLATE +.2x +.5y -.2z LEFT_ARM:90,10,20
			sekcja.getStringList("modele").forEach(str -> {
				Model model = new Model();
				
				Func.tnij(str, " ").forEach(arg -> {
					try {
						if (arg.contains(":"))
							try {
								parsePose(model, arg);
							} catch (IllegalArgumentException e) {
								try {
									parseEq(model, arg);
								} catch (IllegalArgumentException e2) {
									throw new IllegalArgumentException(e2.getMessage(), e);
								}
							}
						else if (arg.startsWith("+") || arg.startsWith("-"))
							parsePrzesunięcie(model, arg);
						else
							throw new IllegalArgumentException("Niepoprawne argument modelu \"" + arg + "\"");
					} catch (IllegalArgumentException e) {
						Throwable err = e;
						Set<Integer> były = new HashSet<>();
						while (err != null && były.add(System.identityHashCode(err))) {
							if (err instanceof IllegalArgumentException && err.getMessage() != null)
								Main.warn(prefix + "(" + nazwa + ") " + err.getMessage());
							err = e.getCause();
						}
					}
				});
				
				modele.add(model);
			});
		}
		private void parseEq(Model model, String arg) throws IllegalArgumentException {
			int index = arg.indexOf(':');
			
			EquipmentSlot slot = Func.StringToEnum(EquipmentSlot.class, arg.substring(0, index));
			ItemStack item = Config.item(arg.substring(index + 1));
			
			model.eq.put(slot, item);
		}
		private void parsePose(Model model, String arg) throws IllegalArgumentException {
			int index = arg.indexOf(':');
			
			VariantyPose pose = Func.StringToEnum(VariantyPose.class, arg.substring(0, index));
			
			List<String> xyz = Func.tnij(arg.substring(index + 1), ",");
			if (xyz.size() != 3) throw new IllegalArgumentException("Niepoprawna poza \"" + arg.substring(index + 1) + "\"");
			EulerAngle angle = new EulerAngle(Func.Double(xyz.get(0)), Func.Double(xyz.get(1)), Func.Double(xyz.get(2)));
											
			model.pose.put(pose, angle);
		}
		private void parsePrzesunięcie(Model model, String arg) throws IllegalArgumentException {
			Consumer<Double> cons;
			switch (arg.charAt(arg.length() - 1)) {
			case 'x': cons = model.przesunięcie::setX; break;
			case 'y': cons = model.przesunięcie::setY; break;
			case 'z': cons = model.przesunięcie::setZ; break;
			default: throw new IllegalArgumentException("Niepoprawne przesunięcie modelu \"" + arg + "\"");
			}
			
			cons.accept(Func.Double(arg.substring(0, arg.length() - 1)));
		}
	
		public List<Krotka<ArmorStand, Vector>> zrespModele(Location loc) {
			List<Krotka<ArmorStand, Vector>> lista = new ArrayList<>();
			
			modele.forEach(model -> {
				ArmorStand as = Func.zrespNietykalnyArmorStand(loc.clone().add(model.przesunięcie));
				as.setArms(true);
				model.eq.forEach(as.getEquipment()::setItem);
				model.pose.forEach((pose, angle) -> pose.bic.accept(as, angle));
				lista.add(new Krotka<>(as, model.przesunięcie));
			});
			
			return lista;
		}
	}
	public static class Auto {
		ArmorStand kierowca;
		List<Krotka<ArmorStand, Vector>> modele;
		public double prędkość = .2d;
		public final AutoDane dane;
		
		public Auto(AutoDane dane, Location loc) {
			this.dane = dane;
			zresp(loc);
		}
		private void zresp(Location loc) {
			kierowca = zrespArmorStand(loc);
			kierowca.addScoreboardTag(tagAutaKierowcy);
			
			modele = dane.zrespModele(loc);
			ustawModele();
			
			Func.ustawMetadate(kierowca, metaAuta, this);
			modele.forEach(krotka -> krotka.wykonaj((as, vector) -> Func.ustawMetadate(as, metaAuta, this)));
		}
		private ArmorStand zrespArmorStand(Location loc) {
			ArmorStand as = Func.zrespNietykalnyArmorStand(loc);

			as.setGravity(true);
			
			return as;
		}
		
		public Location getLocation() {
			return kierowca.getLocation();
		}
		
		public boolean wsiądz(Player p) {
			if (!kierowca.getPassengers().isEmpty()) return false;
			
			kierowca.addPassenger(p);
			tick();
			return true;
		}
		public void przesiądz(Auto auto) {
			auto.kierowca.eject();
			if (!kierowca.getPassengers().isEmpty())
				auto.kierowca.addPassenger(kierowca.getPassengers().get(0));
		}
		
		public void despawn() {
			kierowca.remove();
			modele.forEach(krotka -> krotka.a.remove());
		}
		
		private void tick() {
			if (kierowca.getPassengers().isEmpty()) return;
			
			Location locGracz = kierowca.getPassengers().get(0).getLocation();
			Vector vel = locGracz.getDirection().multiply(prędkość);
			
			kierowca.setVelocity(vel);
			kierowca.setRotation(locGracz.getYaw(), locGracz.getPitch());
			ustawModele();
			
			Func.opóznij(1, this::tick);
		}
		private void ustawModele() {
			modele.forEach(krotka -> krotka.wykonaj((as, vec) -> {
				vec = vec.clone();
				Location loc = kierowca.getLocation().clone();
				vec = rotateVector(vec, loc.getYaw(), 0);
				
				as.teleport(loc.add(vec));
			}));
		}
		
		public static final Vector rotateVector(Vector v, float yawDegrees, float pitchDegrees) {
	        double yaw = Math.toRadians(-1 * (yawDegrees + 90));
	        double pitch = Math.toRadians(-pitchDegrees);

	        double cosYaw = Math.cos(yaw);
	        double cosPitch = Math.cos(pitch);
	        double sinYaw = Math.sin(yaw);
	        double sinPitch = Math.sin(pitch);

	        double initialX, initialY, initialZ;
	        double x, y, z;

	        
	        // Z_Axis rotation (Pitch)
	        initialX = v.getX();
	        initialY = v.getY();
	        x = initialX * cosPitch - initialY * sinPitch;
	        y = initialX * sinPitch + initialY * cosPitch;
	        
	        // Y_Axis rotation (Yaw)
	        initialX = x;
	        initialZ = v.getZ();
	        z = initialZ * cosYaw - initialX * sinYaw;
	        x = initialZ * sinYaw + initialX * cosYaw;
	        

	        return new Vector(x, y, z);
	    }
	}

	Map<String, AutoDane> mapaDanych = new HashMap<>();
	
	public List<Auto> znajdzAuta() {
		List<Auto> auta = new ArrayList<>();
		
		Bukkit.getWorlds().forEach(world ->
			world.getEntitiesByClass(ArmorStand.class).forEach(as -> {
				if (as.getScoreboardTags().contains(tagAutaKierowcy) && as.hasMetadata(metaAuta))
					auta.add((Auto) as.getMetadata(metaAuta).get(0).value());
			}));
		
		return auta;
	}
	
	
	@EventHandler
	public void wsiadanie(PlayerInteractAtEntityEvent ev) {
		if (ev.getPlayer().getVehicle() == null && ev.getRightClicked().hasMetadata(metaAuta))
			((Auto) ev.getRightClicked().getMetadata(metaAuta).get(0).value()).wsiądz(ev.getPlayer());
	}
	
	
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, mapaDanych.keySet());
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		new Auto(mapaDanych.get(args[0]), ((Player) sender).getLocation());
		return true;
	}

	@Override
	public void przeładuj() {
		Config config = new Config("Auta");
		
		Map<String, AutoDane> stareDane = mapaDanych;
		mapaDanych = new HashMap<>();
		
		Func.wykonajDlaNieNull(config.sekcja("auta"), sekcjaAut ->
			sekcjaAut.getKeys(false).forEach(idAuta ->
				mapaDanych.put(idAuta, new AutoDane(idAuta, sekcjaAut.getConfigurationSection(idAuta)))));
		
		Set<String> usunięte = new HashSet<>();
		Map<String, MonoKrotka<AutoDane>> nadpisane = new HashMap<>();
		
		stareDane.forEach((id, dane) -> {
			if (mapaDanych.containsKey(id))
				nadpisane.put(id, new MonoKrotka<>(dane, mapaDanych.get(id)));
			else
				usunięte.add(id);
		});
		
		List<Auto> auta = znajdzAuta();
		
		for (int i=0; i < auta.size(); i++) {
			Auto auto = auta.get(i);
			if (usunięte.contains(auto.dane.nazwa)) {
				auto.despawn();
			} else
				Func.wykonajDlaNieNull(nadpisane.get(auto.dane.nazwa), krotka -> krotka.wykonaj((stare, nowe) -> {
					auto.przesiądz(new Auto(nowe, auto.getLocation()));
					auto.despawn();
				}));
		}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("wczytane auta", mapaDanych.size());
	}


	long last = 0;
	@Override
	public int czas() {
		long last = new File("plugins/mimiRPG/Auta.yml").lastModified();
		if (this.last != last) {
			this.last = last;
			przeładuj();
		}
		return 1;
	}
}

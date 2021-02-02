package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class SiadanieNaBlokach implements Listener, Przeładowalny {
	final Map<Material, Double> mapaWysokości = new HashMap<>();
	final String tagPojazdu = "mimiSiadanieNaBlokachPojazd";
	final String perm = Func.permisja("siadanienablokach");
	
	public SiadanieNaBlokach() {
		Main.dodajPermisje(perm);
	}
	
	@EventHandler
	public void klikanieBloki(PlayerInteractEvent ev) {
		if (ev.getPlayer().hasPermission(perm) && ev.getAction() == Action.RIGHT_CLICK_BLOCK && ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.SADDLE)
			Func.wykonajDlaNieNull(mapaWysokości.get(ev.getClickedBlock().getType()), wysokość -> {
				Predicate<Integer> wolny = plus -> ev.getClickedBlock().getLocation().add(0, plus, 0).getBlock().getType().isAir();
				if (!wolny.test(1) || !wolny.test(2))
					return;
				ev.getPlayer().leaveVehicle();
				Pig pojazd = zrespPojazd(ev.getClickedBlock().getLocation().add(.5, wysokość - .6, .5));
				pojazd.addPassenger(ev.getPlayer());
				tick(ev.getPlayer(), pojazd);
			});
	}
	
	private void tick(Player p, Pig pojazd) {
		if (p.getVehicle() == null || !pojazd.getUniqueId().equals(p.getVehicle().getUniqueId()))
			pojazd.remove();
		else {
			pojazd.setRotation(p.getLocation().getYaw(), p.getLocation().getPitch());
			Func.opóznij(1, () -> tick(p, pojazd));
		}
	}
	
	Pig zrespPojazd(Location loc) {
		Pig pojazd = (Pig) loc.getWorld().spawnEntity(loc, EntityType.PIG);
		
		pojazd.setBaby();
		pojazd.setAI(false);
		pojazd.setSilent(true);
		pojazd.setGravity(false);
		pojazd.setInvulnerable(true);
		pojazd.setRemoveWhenFarAway(true);
		pojazd.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(0);
		pojazd.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
		
		Func.ustawMetadate(pojazd, tagPojazdu, true);
		
		return pojazd;
	}
	
	
	
	@Override
	public void przeładuj() {
		mapaWysokości.clear();
		Func.wykonajDlaNieNull(Main.ust.sekcja("SiadanieNaBlokach"), sekcja ->
			sekcja.getValues(false).forEach((blok, wysokość) -> {
				boolean warn = true;
				if (blok.startsWith("$")) {
					blok = blok.substring(1).toUpperCase().replace(' ', '_');
					wysokość = Func.DoubleObj(wysokość);
					for (Material mat : Material.values())
						if (mat.toString().contains(blok)) {
							mapaWysokości.put(mat, (double) wysokość);
							warn = false;
						}
				} else
					try {
						mapaWysokości.put(Func.StringToEnum(Material.class, blok), Func.DoubleObj(wysokość));
						warn = false;
					} catch (Throwable e) {
					}
				if (warn)
					Main.warn("Nieprawidłowy blok " + blok + " w sekcji SiadanieNaBlokach w ustawienia.yml");
			}));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Bloki do siadania", mapaWysokości.size());
	}
}

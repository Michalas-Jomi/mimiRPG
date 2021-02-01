package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class SiadanieNaBlokach implements Listener, Przeładowalny {
	final Map<Material, Double> mapaWysokości = new HashMap<>();
	final String tagStrzały = "mimiSiedzenieSiadanieNaBlokach";
	
	
	@EventHandler
	public void klikanieBloki(PlayerInteractEvent ev) {
		if (ev.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK && ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
			Func.wykonajDlaNieNull(mapaWysokości.get(ev.getClickedBlock().getType()), wysokość -> {
				ev.getPlayer().leaveVehicle();
				
			});
		}
	}
	
	Arrow zrespStrzałe(Location loc) {
		Arrow strzała = (Arrow) loc.getWorld().spawnEntity(loc, EntityType.ARROW);
		
		strzała.setGravity(false);
		strzała.setInvulnerable(true);
		strzała.setSilent(true);
		
		return strzała;
	}
	
	
	
	
	@Override
	public void przeładuj() {
		mapaWysokości.clear();
		Func.wykonajDlaNieNull(Main.ust.sekcja("SiadanieNaBlokach"), sekcja ->
			sekcja.getValues(false).forEach((blok, wysokość) -> {
				try {
					mapaWysokości.put(Func.StringToEnum(Material.class, blok), Func.DoubleObj(wysokość));
				} catch (Throwable e) {
					Main.warn("Nieprawidłowy blok " + blok + " w sekcji SiadanieNaBlokach w ustawienia.yml");
				}
			}));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Bloki do siadania", mapaWysokości.size());
	}
}

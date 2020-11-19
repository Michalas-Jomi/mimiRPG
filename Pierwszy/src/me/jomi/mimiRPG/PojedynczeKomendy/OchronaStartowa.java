package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Set;
import java.util.function.BiPredicate;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class OchronaStartowa implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix("Ochrona Początkowa");
	
	final Set<String> ochrona = Sets.newConcurrentHashSet();
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev){
        Player p = ev.getPlayer();
        if(!p.hasPlayedBefore()) {
        	ochrona.add(p.getName());
        	Func.opóznij(Main.ust.wczytajLubDomyślna("OchronaStartowa.minuty", 30) * 60 * 20, () -> ochrona.remove(p.getName()));
        }
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev){
    	if (!(ev.getEntity() instanceof Player))
    		return;
        Player uderzony = (Player) ev.getEntity();
        
        Player uderzający;
        	if (ev.getDamager() instanceof Player)
        		uderzający = (Player) ev.getDamager();
        	else if (ev.getDamager() instanceof Projectile && ((Projectile) ev.getDamager()).getShooter() instanceof Player)
        		uderzający = (Player) ((Projectile) ev.getDamager()).getShooter();
        	else
        		return;
        
        BiPredicate<Player, String> bip = (p, msg) -> {
        	boolean w = ochrona.contains(p.getName());
        	if (w) {
        		ev.setCancelled(true);
        		uderzający.sendMessage(prefix + msg);
        	}
        	return w;
        };
        
        if (!bip.test(uderzony, "Ten gracz dopiero zaczyna grę na tym serwerze, nie bij go!"))
    		bip.test(uderzający, "Dopiero zaczynasz gre na tym serwerze, nie wdawaj się tak szybko w bójki.");
    }
	
	
	
	@Override
	public void przeładuj() {}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Ochrona początkowa", Main.ust.wczytajLubDomyślna("OchronaStartowa.minuty", 30));
	}
}

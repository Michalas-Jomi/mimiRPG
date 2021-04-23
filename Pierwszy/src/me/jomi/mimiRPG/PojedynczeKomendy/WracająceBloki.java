package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class WracająceBloki implements Listener, Przeładowalny {
	
	final HashMap<String, Integer> mapa = new HashMap<>();
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void niszczenie(BlockBreakEvent ev) {
		if (!ev.isCancelled()) return;
		
		Material mat = ev.getBlock().getType();
		Integer a = mapa.get(mat.toString());
		if (a == null) return;
		BlockData data = ev.getBlock().getBlockData();
		
		ev.setCancelled(false);
		Func.opóznij(a, () -> {
			if (ev.getBlock().getType().isAir()) {
				ev.getBlock().setType(mat);
				ev.getBlock().setBlockData(data);
			}
		});
	}
	
	
	@Override
	public void przeładuj() {
		mapa.clear();
		Config config = new Config("Wracające Bloki");
		for (String _klucz : config.klucze())
			try {
				String klucz = _klucz.toUpperCase();
				Material.valueOf(klucz);
				mapa.put(klucz, config.wczytajInt(klucz));
			} catch (Throwable e) {
				Main.warn("Niepoprawny blok: " + _klucz);
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wracające Bloki", mapa.size());
	}
}

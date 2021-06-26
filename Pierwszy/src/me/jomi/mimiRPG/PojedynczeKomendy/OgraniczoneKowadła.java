package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class OgraniczoneKowadła implements Listener, Przeładowalny {
	static List<Material> zablokowane;
	
	@Override
	public void przeładuj() {
		zablokowane = Func.wykonajWszystkim(Main.ust.wczytajListe("OgraniczoneKowadła.zablokowane"), str -> Func.StringToEnum(Material.class, str));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("zablokowane itemy w kowadle", zablokowane.size());
	}
	
	@EventHandler
	public void anvil(PrepareAnvilEvent ev) {
		if (ev.getResult() != null && zablokowane.contains(ev.getResult().getType()))
			ev.setResult(null);
	}
}

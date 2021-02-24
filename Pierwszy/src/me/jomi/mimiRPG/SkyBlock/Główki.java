package me.jomi.mimiRPG.SkyBlock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Główki implements Listener, Przeładowalny {
	static final Map<EntityType, ItemStack> mapa = new HashMap<>();
	static final double szansaGłowy = .03;
	
	@EventHandler
	public void zabijanieMoba(EntityDeathEvent ev) {
		if (Func.losuj(szansaGłowy))
			Func.wykonajDlaNieNull(mapa.get(ev.getEntity().getType()), ev.getDrops()::add);
	}
	
	
	@Override
	public void przeładuj() {
		mapa.clear();
		Config config = new Config("config/Główki");
		for (String klucz : config.klucze(false))
			try {
				mapa.put(Func.StringToEnum(EntityType.class, klucz), config.wczytajItem(klucz));
			} catch(Throwable e) {
				Main.warn("Niepoprawna główka w config/Główki.yml: " + klucz);
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane głowy", mapa.size() + "/" + EntityType.values().length);
	}
}

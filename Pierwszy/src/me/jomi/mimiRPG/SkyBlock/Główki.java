package me.jomi.mimiRPG.SkyBlock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
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
		Config config = new Config("configi/Główki");
		for (String klucz : config.klucze()) {
			try {
				EntityType mob = Func.StringToEnum(EntityType.class, klucz);
				ItemStack item = Func.dajGłówkę(ChatColor.GOLD + "Głowa " + Func.enumToString(mob), config.wczytajStr(klucz));
				mapa.put(mob, item);
				CustomoweItemy.customoweItemy.put("głowa_" + mob.toString().toLowerCase(), item);
			} catch(Throwable e) {
				Main.warn("Niepoprawna główka w configi/Główki.yml: " + klucz);
			}
			
		}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane głowy", mapa.size() + "/" + EntityType.values().length);
	}
}

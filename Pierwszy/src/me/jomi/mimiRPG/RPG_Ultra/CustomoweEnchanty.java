package me.jomi.mimiRPG.RPG_Ultra;

import org.bukkit.event.Listener;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;

@Moduł
public class CustomoweEnchanty implements Listener {
	public static boolean warunekModułu() {
		return Main.włączonyModół(ZfaktoryzowaneItemy.class);
	}
	
	// TODO: customwoe Enchanty
	
}

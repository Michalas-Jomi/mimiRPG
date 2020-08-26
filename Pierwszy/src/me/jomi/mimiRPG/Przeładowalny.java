package me.jomi.mimiRPG;

import java.util.HashMap;

public interface Prze³adowalny {
	public static final HashMap<String, Prze³adowalny> prze³adowalne = new HashMap<>();
	
	public void prze³aduj();
	public String raport();
}

package me.jomi.mimiRPG;

import java.util.HashMap;

public interface Prze�adowalny {
	public static final HashMap<String, Prze�adowalny> prze�adowalne = new HashMap<>();
	
	public void prze�aduj();
	public String raport();
}

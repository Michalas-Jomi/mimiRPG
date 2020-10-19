package me.jomi.mimiRPG;

import java.util.HashMap;

public interface Przeładowalny {
	public static final HashMap<String, Przeładowalny> przeładowalne = new HashMap<>();
	
	public void przeładuj();
	public Krotka<String, Object> raport();
}

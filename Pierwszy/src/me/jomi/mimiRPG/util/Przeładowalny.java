package me.jomi.mimiRPG.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;

public interface Przeładowalny {
	@Target(value=ElementType.TYPE)
	@Retention(value=RetentionPolicy.RUNTIME)
	public @interface WymagaReloadBukkitData {
	}
	
	public static final HashMap<String, Przeładowalny> przeładowalne = new HashMap<>();
	
	public default void preReloadBukkitData() {}
	public void przeładuj();
	public Krotka<String, Object> raport();
}


package me.jomi.mimiRPG.util;

import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public interface IConfig {
	
	public Set<String> klucze();
	
	public ConfigurationSection sekcja(String ścieżka);
	
	public Object wczytaj(String ścieżka);
	public ItemStack wczytajItem(String ścieżka);

	public void ustaw(String ścieżka, Object obj);
	
	public void przeładuj();
}

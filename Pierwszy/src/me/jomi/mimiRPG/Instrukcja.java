package me.jomi.mimiRPG;

import java.util.HashMap;

import org.bukkit.command.CommandSender;

public interface Instrukcja {
	public static final HashMap<String, Instrukcja> mapa = new HashMap<>();
	
	public void info(CommandSender p, int strona);
}

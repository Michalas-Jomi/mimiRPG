package me.jomi.mimiRPG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Grupa implements ConfigurationSerializable {
	public String kolorpisania = "";
	public Grupa() {}
	
	public Grupa(Map<String, Object> mapa) {
		kolorpisania = (String) mapa.getOrDefault("kolorpisania", "");
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> mapa = new HashMap<>();
		if (!kolorpisania.isEmpty())
			mapa.put("kolorpisania", kolorpisania);
		return mapa;
	}

}

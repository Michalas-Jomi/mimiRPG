package me.jomi.mimiRPG;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class Mapowalne implements ConfigurationSerializable {
	public Mapowalne(Map<String, Object> mapa) {
		if (mapa != null)
			Func.zdemapuj(this, mapa);
	}
	@Override
	public Map<String, Object> serialize() {
		return Func.zmapuj(this);
	}
}

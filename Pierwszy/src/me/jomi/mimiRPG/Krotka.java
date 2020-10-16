package me.jomi.mimiRPG;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Krotka<T1, T2> implements ConfigurationSerializable {
	@Mapowane public T1 a;
	@Mapowane public T2 b;
	
	public Krotka(T1 a, T2 b) {
		this.a = a;
		this.b = b;
	}

	
	@Override
	public Map<String, Object> serialize() {
		return Func.zmapuj(this);
	}
	public Krotka(Map<String, Object> mapa) {
		Func.zdemapuj(this, mapa);
	}
}

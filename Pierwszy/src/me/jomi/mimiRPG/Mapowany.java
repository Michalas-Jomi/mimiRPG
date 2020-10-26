package me.jomi.mimiRPG;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.jomi.mimiRPG.util.Func;

public abstract class Mapowany implements ConfigurationSerializable {
	public static Mapowany deserialize(Map<String, Object> mapa) {
		Mapowany obj = null;
		String klasa = (String) mapa.get("=mimi=");
		try {
			obj = (Mapowany) Class.forName(klasa).newInstance();
		} catch (ClassNotFoundException e) {
			Main.error("Nieodnaleziono klasy " + klasa);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		Func.zdemapuj(obj, mapa);
		
		try {
			obj.getClass().getDeclaredMethod("Init").invoke(obj);
		} catch (Throwable e) {};
		
		return obj;
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> mapa = Func.zmapuj(this);
		mapa.put("=mimi=", this.getClass().getName());
		mapa.put("==", "me.jomi.mimiRPG.Mapowany");
		return mapa;
	}
}

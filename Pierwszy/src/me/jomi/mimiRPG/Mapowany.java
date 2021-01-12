package me.jomi.mimiRPG;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.MimiObject;

public abstract class Mapowany extends MimiObject implements ConfigurationSerializable {
	public static Mapowany deserialize(Map<String, Object> mapa) {
		Mapowany obj = null;
		String klasa = (String) mapa.get("=mimi=");
		try {
			obj = (Mapowany) Func.nowaInstancja(Class.forName(klasa, false, Main.classLoader));
		} catch (ClassNotFoundException e) {
			Main.error("Nieodnaleziono klasy " + klasa);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		Func.zdemapuj(obj, mapa);
		
		try {
			Method met = Func.dajMetode(obj.getClass(), "Init");
			met.setAccessible(true);
			met.invoke(obj);
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


	@Override
	public String[] dajSprawdzanePola() {
		Set<String> set = mapowane().keySet();
		String[] w = new String[set.size()];
		int i=0;
		for (String nazwa : set)
			w[i++] = nazwa;
		return w;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)", this.getClass().getSimpleName(), mapowane());
	}
	
	private HashMap<String, Object> mapowane() {
		HashMap<String, Object> mapa = new HashMap<>();
		
		for (Class<?> c : Lists.reverse(Func.dajKlasy(this.getClass())))
			for (Field f : c.getDeclaredFields())
				if (f.isAnnotationPresent(Mapowane.class))
					try {
						f.setAccessible(true);
						mapa.put(f.getName(), f.get(this));
					} catch (Throwable e) {
						e.printStackTrace();
					}
		
		return mapa;
	}
}

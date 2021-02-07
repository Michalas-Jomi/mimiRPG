package me.jomi.mimiRPG;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.MimiObject;

public abstract class Mapowany extends MimiObject implements ConfigurationSerializable, Cloneable {
	/**
	 * zamienia mape na obiekt
	 * @param mapa do zdemapowania
	 * @param clazz klasa do demapowania
	 * @return zdemapowany objekt
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Mapowany> T deserialize(Map<String, Object> mapa, Class<T> clazz) {
		mapa.put("=mimi=", clazz.getName());
		return (T) deserialize(mapa);
	}
	/**
	 * zamienia mape na obiekt
	 * @param mapa z elementem "=mimi=" wskazującym demapowaną klasę {@code Class.getName()}
	 * @return zdemapowany obiekt
	 */
	public static Mapowany deserialize(Map<String, Object> mapa) {
		Mapowany obj = null;
		String klasa = (String) mapa.get("=mimi=");
		try {
			obj = (Mapowany) Func.nowaInstancja(Class.forName(klasa, false, Main.classLoader));
		} catch (ClassNotFoundException e) {
			Main.error("Nieodnaleziono klasy " + klasa);
			return obj;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		Func.zdemapuj(obj, mapa);
		
		try {
			obj.Init();
		} catch (NiepoprawneDemapowanieException e) {
			Main.warn(obj != null ? obj.getClass().getSimpleName() + ": " : "", e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
		};
		
		return obj;
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> mapa = Func.zmapuj(this);
		mapa.put("=mimi=", this.getClass().getName());
		mapa.put("==", "me.jomi.mimiRPG.Mapowany");
		return mapa;
	}
	
	protected void Init() throws NiepoprawneDemapowanieException {};

	@Override
	public String[] dajSprawdzanePola() {
		Set<String> set = mapowane().keySet();
		String[] w = new String[set.size()];
		int i=0;
		for (String nazwa : set)
			w[i++] = nazwa;
		return w;
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

	@Override
	public Mapowany clone() {
		Mapowany nowy = Func.utwórz(this.getClass());
		
		mapowane().forEach((pole, obj) -> {
			try {
				Func.dajField(this.getClass(), pole).set(nowy, obj);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		
		try {
			nowy.Init();
		} catch (NiepoprawneDemapowanieException e) {}
		
		return nowy;
	}
}

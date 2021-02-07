package me.jomi.mimiRPG.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class LepszaMapa<K> {
	private final Map<K, Object> mapa;
	public LepszaMapa(Map<K, Object> mapa) {
		this.mapa = mapa;
	}

	public <T> Collection<T> values	(Class<T> clazz)									{ return (Collection<T>) mapa.values(); }
	public boolean			 containsValue	(Object obj)								{ return mapa.containsValue(obj); }
	public boolean			 containsKey	(K klucz)									{ return mapa.containsKey(klucz); }
	public Object			 put			(K klucz, Object obj)						{ return mapa.put(klucz, obj); }
	public Object			 remove			(K klucz)									{ return mapa.remove(klucz); }
	public boolean			 isEmpty		()											{ return mapa.isEmpty(); }
	public Collection<Object>values			()											{ return mapa.values(); }
	public Set<K>			 keySet			()											{ return mapa.keySet(); }
	public int				 size			()											{ return mapa.size(); }
	public void				 forEach		(BiConsumer<? super K, ? super Object> bic)	{ mapa.forEach(bic); }
	
	public final <T>	List<T>		 getList(K klucz) { return getD(klucz); }
	public final <T>	Set<T>		 getSet (K klucz) { return getD(klucz); }
	public final <T,V>	Map<T, V>	 getMap (K klucz) { return getD(klucz); }
	public final <T> 	LepszaMapa<T>getLMap(K klucz) { return new LepszaMapa<>(getMap(klucz)); }
	
	public final <T> T		getD(K klucz) 						{ return (T) get(klucz); }
	public final <T> T 		getD(K klucz, Class<T> typ) 		{ return getD(klucz); }
	public final <T> T		get	(K klucz, T domyślna) 			{ return Func.domyślna(getD(klucz), domyślna); }
	public final <T> T		get	(K klucz, Supplier<T> domyślna) { return Func.domyślna(getD(klucz), domyślna); }
	public Object			get	(K klucz)						{ return mapa.get(klucz); }
	
	public final int		getInt			(K klucz) { return getD(klucz); }
	public final char		getChar			(K klucz) { return getD(klucz); }
	public final long		getLong			(K klucz) { return getD(klucz); }
	public final boolean	getBoolean		(K klucz) { return getD(klucz); }
	public final World		getWorld		(K klucz) { return getD(klucz); }
	public final String		getString		(K klucz) { return getD(klucz); }
	public final Location	getLocation		(K klucz) { return getD(klucz); }
	public final JSONArray	getJSONArray	(K klucz) { return getD(klucz); }
	public final JSONObject	getJSONObject	(K klucz) { return getD(klucz); }
	public final float		getFloat		(K klucz) { return Func.Float(get(klucz)); }
	public final double		getDouble		(K klucz) { return Func.DoubleObj(get(klucz)); }
	public final ItemStack	getItemStack	(K klucz) { return Config.item(get(klucz)); }
}

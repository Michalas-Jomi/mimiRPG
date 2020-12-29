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

public class LepszaMapa<K> {
	private final Map<K, Object> mapa;
	public LepszaMapa(Map<K, Object> mapa) {
		this.mapa = mapa;
	}

	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked") public <T> T getOrDefaultFactory(K klucz, Supplier<T> factory) { return mapa.containsKey(klucz) ? (T) mapa.get(klucz) : factory.get();}
	@SuppressWarnings("unchecked") public <T> T getOrDefault(K klucz, T domyślna) { return (T) mapa.getOrDefault(klucz, domyślna); }
	@SuppressWarnings("unchecked") public <T> T get(K klucz, Class<T> clazz) { return (T) mapa.get(klucz); }

	@SuppressWarnings("unchecked") public <T>	List<T>		getList(K klucz) { return (List<T>)	  mapa.get(klucz); }
	@SuppressWarnings("unchecked") public <T>	Set<T>		getSet (K klucz) { return (Set<T>) 	  mapa.get(klucz); }
	@SuppressWarnings("unchecked") public <T, V>Map<T, V>	getMap (K klucz) { return (Map<T, V>) mapa.get(klucz); }
	public <T> LepszaMapa<T> getLMap(K klucz) { return new LepszaMapa<>(getMap(klucz)); }
	
	public int			getInt			(K klucz) { return (int)		mapa.get(klucz); }
	public char			getChar			(K klucz) { return (char)		mapa.get(klucz); }
	public long			getLong			(K klucz) { return (long)		mapa.get(klucz); }
	public float		getFloat		(K klucz) { return (float)		mapa.get(klucz); }
	public double		getDouble		(K klucz) { return (double)		mapa.get(klucz); }
	public boolean		getBoolean		(K klucz) { return (boolean)	mapa.get(klucz); }
	public World		getWorld		(K klucz) { return (World)		mapa.get(klucz); }
	public String		getString		(K klucz) { return (String)		mapa.get(klucz); }
	public Location		getLocation		(K klucz) { return (Location)	mapa.get(klucz); }
	public JSONArray	getJSONArray	(K klucz) { return (JSONArray)	mapa.get(klucz); }
	public ItemStack	getItemStack	(K klucz) { return (ItemStack)	mapa.get(klucz); }
	public JSONObject	getJSONObject	(K klucz) { return (JSONObject)	mapa.get(klucz); }
}

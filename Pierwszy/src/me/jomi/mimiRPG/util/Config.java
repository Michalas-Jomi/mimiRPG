package me.jomi.mimiRPG.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;

public class Config {
	private YamlConfiguration plik;
	public File f;
	
	private String sciezkaJarDomyślny = null;
	private String sciezka;
	
	public Config(String nazwa) {
		this.sciezka = Main.path + nazwa + ".yml";
		this.sciezkaJarDomyślny = "Configi/" + nazwa.substring(nazwa.replace('\\', '/').lastIndexOf("/")+1) + ".yml";
		przeładuj();
	}
	public Config(File plik) {
		this.sciezka = plik.getPath().replace('\\', '/');
		przeładuj();
	}
	
	public void ustaw(String sciezka, Object obj) {
		plik.set(sciezka, _item(obj));
	}
	@SuppressWarnings("unchecked")
	protected static Object _item(Object item) {
		if (item instanceof List) {
			List<Object> lista = Lists.newArrayList();
			for (Object _obj : (List<Object>) item)
				lista.add(_item(_obj));
			return lista;
		}
		if (item instanceof ItemStack) {
			for (Entry<String, ItemStack> en : Baza.itemy.entrySet())
				if (Func.porównaj(en.getValue(), (ItemStack) item))
					return en.getKey() + " " + ((ItemStack) item).getAmount();
			if (((ItemStack) item).isSimilar(new ItemStack(((ItemStack) item).getType())))
				return ((ItemStack) item).getType().toString().toLowerCase() + " " + ((ItemStack) item).getAmount();
		}
		return item;
	}
	public boolean ustawDomyślne(String sciezka, Object obj) {
		if (wczytaj(sciezka) != null)
			return false;
		ustaw(sciezka, obj);
		return true;
	}
	public boolean ustaw_zapiszDomyślne(String sciezka, Object obj) {
		if (wczytaj(sciezka) != null)
			return false;
		ustaw_zapisz(sciezka, obj);
		return true;		
	}
	public void ustaw_zapisz(String sciezka, Object obj) {
		ustaw(sciezka, obj);
		zapisz();
	}
	public void zapisz() {
		try {
			plik.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> klucze(boolean wszystkie){
		return plik.getKeys(wszystkie);
	}
	public ConfigurationSection sekcja(Object... sciezka) {
		return plik.getConfigurationSection(sc(sciezka));
	}
	@SuppressWarnings("unchecked")
	public <T> Collection<T> wartości(Class<T> clazz) {
		return (Collection<T>) plik.getValues(false).values();
	}
	public Map<String, Object> mapa(boolean pełna) {
		return plik.getValues(pełna);
	}
	
	public Object  wczytaj		 (String sciezka) {return plik.get(sciezka);}
	public int 	   wczytajInt	 (String sciezka) {return plik.getInt(sciezka);}
	public boolean wczytajBoolean(String sciezka) {return plik.getBoolean(sciezka);}
	public String  wczytajStr	 (String sciezka) {return Func.koloruj(plik.getString(sciezka));}
	public double  wczytajDouble (String sciezka) {return Func.Double("" + wczytaj(sciezka), 0);}

	@SuppressWarnings("unchecked")
	public <T> T wczytajLubDomyślna(String sciezka, T domyślna) {
		Object obj = wczytaj(sciezka);
		if (obj == null)
			return domyślna;
		return (T) obj;
	}
	@SuppressWarnings("unchecked")
	public <T> T wczytajLubDomyślna(String sciezka, Supplier<T> domyślna) {
		Object obj = wczytaj(sciezka);
		if (obj == null)
			return domyślna.get();
		return (T) obj;
	}
	
	public List<String> wczytajListe(Object... sciezka){
		List<String> lista = plik.getStringList(sc(sciezka));
		if (lista == null) lista = Lists.newArrayList();
		return lista;
	}
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> wczytajListeMap(Object... sciezka){
		List<Map<String, Object>> lista = (List<Map<String, Object>>) plik.getList(sc(sciezka));
		if (lista == null) lista = Lists.newArrayList();
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public List<ItemStack> wczytajItemy(Object... sciezka) {
		List<ItemStack> lista = Lists.newArrayList();
		Object objListy = wczytaj(sc(sciezka));
		if (objListy == null) return lista;
		for (Object obj : (List<Object>) objListy)
			lista.add(item(obj));
		return lista;
	}
	public ItemStack wczytajItem(Object... sciezka) {
		return item(wczytaj(sc(sciezka)));
	}
	@SuppressWarnings("unchecked")
	public static ItemStack item(Object item) {
		if (item == null) return null;
		if (item instanceof ItemStack)
			return (ItemStack) item;
		if (item instanceof ConfigurationSection)
			item = ((ConfigurationSection) item).getValues(false);
		ItemStack _item;
		if (item instanceof Map) {
			Map<String, Object> mapa = (Map<String, Object>) item;
			_item = item(mapa.getOrDefault("item", "Stone")).clone();
			BiConsumer<String, BiConsumer<ItemStack, Object>> bic = (str, bic2) -> {
				Object obj = mapa.get(str);
				if (obj != null)
					bic2.accept(_item, obj);
			};
			bic.accept("nazwa", (i, o) -> Func.nazwij(i, (String) o));
			bic.accept("lore", (i, o) -> Func.ustawLore(i, (List<String>) o));
			if ((boolean) mapa.getOrDefault("ench", false))
				Func.połysk(_item);
			return _item;
		}
		String[] wejscie = ((String) item).split(" ");
		if (!Baza.itemy.containsKey(wejscie[0])) {
			try {
				_item = new ItemStack(Material.valueOf(wejscie[0].toUpperCase()));
			} catch (Exception e) {
				return null;
			}
		} else
			_item = Baza.itemy.get(wejscie[0]).clone();
		if (wejscie.length >= 2)
			_item.setAmount(Func.Int(wejscie[1], 1));
		return _item;
	}
	public static List<ItemStack> itemy(List<?> itemy) {
		List<ItemStack> _itemy = Lists.newArrayList();
		
		for (Object item : itemy)
			_itemy.add(item(item));
		
		return _itemy;
	}
	
	@SuppressWarnings("unchecked")
	public List<Drop> wczytajDropy(Object... scieżka) {
		List<Drop> lista = Lists.newArrayList();
		Object objListy = wczytaj(sc(scieżka));
		for (Object obj : (List<Object>) objListy)
			lista.add(drop(obj));
		return lista;
		
	}
	public Drop wczytajDrop(Object... scieżka) {
		return drop(wczytaj(sc(scieżka)));
	}
	@SuppressWarnings("unchecked")
	public static Drop drop(Object drop) {
		if (drop == null) return null;
		if (drop instanceof Drop)
			return (Drop) drop;
		if (drop instanceof ItemStack)
			return new Drop((ItemStack) drop);
		if (drop instanceof ConfigurationSection)
			drop = ((ConfigurationSection) drop).getValues(false);
		if (drop instanceof Map)
			return new Drop((Map<String, Object>) drop);
		return Drop.wczytaj((String) drop);
	}
	public static List<Drop> dropy(List<?> dropy) {
		List<Drop> _dropy = Lists.newArrayList();
		
		for (Object drop : dropy)
			_dropy.add(drop(drop));
		
		return _dropy;
	}
	
	
	public Napis wczytajNapis(Object... sciezka) {
		return _napis(wczytaj(sc(sciezka)));
	}
	public List<Napis> wczytajListeNapisów(Object... sciezka){
		List<Napis> lista = Lists.newArrayList();
		Object obj = wczytaj(sc(sciezka));
		if (obj != null)
			if (obj instanceof List)
				for (Object napis : (List<?>) obj)
					lista.add(_napis(napis));
			else
				lista.add(_napis(obj));
		return lista;
	}
	@SuppressWarnings("unchecked")
	private Napis _napis(Object obj) {
		if (obj instanceof List) {
			Napis n = new Napis();
			for (Object napis : (List<?>) obj)
				n.dodaj(_napis(napis)).dodaj("\n");
			return n;
		}
		if (obj instanceof Napis)
			return (Napis) obj;
		if (obj instanceof String)
			return Napis.wczytaj((String) obj);
		if (obj instanceof Map)
			return new Napis((Map<String, Object>) obj);
		return null;
	}
	
	private void stwórz() {
		File dir = new File(sciezka.substring(0, sciezka.lastIndexOf("/")));
		if (!dir.exists())
			dir.mkdirs();
		
		f = new File(sciezka);
		if (f.exists()) return;
		if (sciezkaJarDomyślny != null && !Func.wyjmijPlik(sciezkaJarDomyślny, sciezka)) {
			try {
				f.createNewFile();
				String path = f.getAbsolutePath().replace("\\", "/");
				if (!path.contains("/configi/"))
					Main.log("Utworzono pusty plik konfiguracyjny " + path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void przeładuj() {
		stwórz();
		plik = YamlConfiguration.loadConfiguration(f);
	}
	
	private String sc(Object[] sciezka) {
		return Func.listToString(sciezka, 0, ".");
	}

	public String path() {
		return sciezka;
	}
	
	public boolean usuń() {
		return f.delete();
	}
}

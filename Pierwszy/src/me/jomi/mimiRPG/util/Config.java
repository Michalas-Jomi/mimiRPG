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
	public File f;
	private YamlConfiguration plik;
	private final YamlConfiguration domyślne;
	
	private final String sciezkaJarDomyślny;
	private final String sciezka;
	
	public Config(String nazwa) {
		this(Main.path + nazwa.replace('\\', '/') + (nazwa.endsWith(".yml") ? "" : ".yml"),
				"Configi/" + nazwa.substring(nazwa.replace('\\', '/').lastIndexOf("/")+1) + ".yml");
		przeładuj();
	}
	public Config(File plik) {
		this(plik.getPath().replace('\\', '/'), null);
		przeładuj();
	}
	private Config(String sciezka, String sciezkaJarDomyślny) {
		this.sciezkaJarDomyślny = sciezkaJarDomyślny;
		this.sciezka = sciezka;
		
		domyślne = new YamlConfiguration();
		if (sciezkaJarDomyślny != null)
			try {
				String str = Func.wczytajZJara(sciezkaJarDomyślny);
				if (str != null)
					domyślne.loadFromString(str);
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}
	
	
	public void ustaw(String sciezka, Object obj) {
		plik.set(sciezka, zserializujItem(obj));
	}
	@SuppressWarnings("unchecked")
	public static Object zserializujItem(Object item) {
		if (item == null) return null;
		if (item instanceof List) {
			List<Object> lista = Lists.newArrayList();
			for (Object _obj : (List<Object>) item)
				lista.add(zserializujItem(_obj));
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

	public Set<String> klucze(){
		return plik.getKeys(false);
	}
	
	public ConfigurationSection sekcja(String sciezka) {
		return plik.getConfigurationSection(sciezka);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Collection<T> wartości(Class<T> clazz) {
		return (Collection<T>) plik.getValues(false).values();
	}
	public Map<String, Object> mapa(boolean pełna) {
		return plik.getValues(pełna);
	}

	@SuppressWarnings("unchecked")
	public <T> T   wczytajPewny  (String sciezka) {return (T) plik.get(sciezka);}
	public Object  wczytaj		 (String sciezka) {return plik.get(sciezka);}
	public int 	   wczytajInt	 (String sciezka) {return plik.getInt(sciezka);}
	public boolean wczytajBoolean(String sciezka) {return plik.getBoolean(sciezka);}
	public String  wczytajStr	 (String sciezka) {return Func.koloruj(plik.getString(sciezka));}
	public double  wczytajDouble (String sciezka) {return Func.Double("" + wczytaj(sciezka), 0);}

	public <T> T wczytaj(String sciezka, T domyślna) {
		return wczytaj(sciezka, () -> domyślna);
	}
	public <T> T wczytaj(String sciezka, Supplier<T> domyślna) {
		Object obj = wczytaj(sciezka);
		if (obj == null)
			return domyślna.get();
		return Func.pewnyCast(obj);
	}
	
	
	public Object wczytajD(String ścieżka) {
		Object wynik = plik.get(ścieżka);
		return (wynik != null || domyślne == null) ? wynik : domyślne.get(ścieżka);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T wczytajPewnyD(String ścieżka) {
		return (T) wczytajD(ścieżka);
	}
	
	
	public List<String> wczytajListe(String sciezka){
		List<String> lista = plik.getStringList(sciezka);
		if (lista == null) lista = Lists.newArrayList();
		return lista;
	}
	
	@SuppressWarnings("unchecked")
	public List<LepszaMapa<String>> wczytajListeMap(String sciezka){
		List<Map<String, Object>> lista = (List<Map<String, Object>>) plik.getList(sciezka);
		if (lista == null) lista = Lists.newArrayList();
		
		List<LepszaMapa<String>> końcowaLista = Lists.newArrayList();
		lista.forEach(mapa -> końcowaLista.add(new LepszaMapa<>(mapa)));
		
		return końcowaLista;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ItemStack> wczytajItemy(String sciezka) {
		List<ItemStack> lista = Lists.newArrayList();
		Object objListy = wczytaj(sciezka);
		if (objListy == null) return lista;
		for (Object obj : (List<Object>) objListy)
			lista.add(item(obj));
		return lista;
	}
	
	public ItemStack wczytajItemD(String sciezka) {
		return item(wczytajD(sciezka));
	}
	public ItemStack wczytajItem(String sciezka) {
		return item(wczytaj(sciezka));
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
				_item = new ItemStack(Func.StringToEnum(Material.class, wejscie[0]));
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
	public List<Drop> wczytajDropy(String scieżka) {
		List<Drop> lista = Lists.newArrayList();
		Object objListy = wczytaj(scieżka);
		for (Object obj : (List<Object>) objListy)
			lista.add(drop(obj));
		return lista;
		
	}
	public Drop wczytajDrop(String scieżka) {
		return drop(wczytaj(scieżka));
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
	@SuppressWarnings("unchecked")
	protected static Object _drop(Object drop) {
		if (drop == null) return null;
		if (drop instanceof List) {
			List<Object> lista = Lists.newArrayList();
			for (Object _obj : (List<Object>) drop)
				lista.add(_drop(_obj));
			return lista;
		}
		if (drop instanceof Drop) {
			for (Entry<String, Drop> en : Baza.dropy.entrySet())
				if (((Drop) drop).equals(en.getValue()))
					return en.getKey();
		}
		return drop;
	}
	
	public SelektorItemów wczytajSelektorItemów(String klucz) {
		return (SelektorItemów) wczytaj(klucz);
	}
	public static SelektorItemów selektorItemów(Object obj) {
		if (obj == null) return null;
		if (obj instanceof SelektorItemów)
			return (SelektorItemów) obj;
		return Baza.selektoryItemów.get(obj);
	}
	@SuppressWarnings("unchecked")
	protected static Object _selektorItemów(Object selektor) {
		if (selektor == null) return null;
		if (selektor instanceof List) {
			List<Object> lista = Lists.newArrayList();
			for (Object _obj : (List<Object>) selektor)
				lista.add(_selektorItemów(_obj));
			return lista;
		}
		if (selektor instanceof SelektorItemów) {
			for (Entry<String, SelektorItemów> en : Baza.selektoryItemów.entrySet())
				if (((SelektorItemów) selektor).equals(en.getValue()))
					return en.getKey();
		}
		return selektor;
	}
	
	public Napis wczytajNapis(String sciezka) {
		return _napis(wczytaj(sciezka));
	}
	public List<Napis> wczytajListeNapisów(String sciezka){
		List<Napis> lista = Lists.newArrayList();
		Object obj = wczytaj(sciezka);
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
	
	public String path() {
		return sciezka;
	}
	
	public boolean usuń() {
		return f.delete();
	}
}

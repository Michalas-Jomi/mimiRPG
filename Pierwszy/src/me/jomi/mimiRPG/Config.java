package me.jomi.mimiRPG;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

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
		this.sciezka = plik.getPath();
		przeładuj();
	}
	
	public void ustaw(String sciezka, Object obj) {
		plik.set(sciezka, _item(obj));
	}
	@SuppressWarnings("unchecked")
	private Object _item(Object item) {
		if (item instanceof List) {
			List<Object> lista = Lists.newArrayList();
			for (Object _obj : (List<Object>) item)
				lista.add(_item(_obj));
			return lista;
		}
		if (item instanceof ItemStack)
			for (Entry<String, ItemStack> en : Baza.itemy.entrySet())
				if (Func.porównaj(en.getValue(), (ItemStack) item))
					return en.getKey() + " " + ((ItemStack) item).getAmount();
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
		przeładuj();
	}

	public Set<String> klucze(boolean wszystkie){
		return plik.getKeys(wszystkie);
	}
	public ConfigurationSection sekcja(Object... sciezka) {
		return plik.getConfigurationSection(sc(sciezka));
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
	public List<String> wczytajListe(Object... sciezka){
		List<String> lista = plik.getStringList(sc(sciezka));
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
	public static ItemStack item(Object item) {
		if (item == null) return null;
		if (item instanceof ItemStack)
			return (ItemStack) item;
		String[] wejscie = ((String) item).split(" ");
		ItemStack _item;
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
		if (!Func.wyjmijPlik(sciezkaJarDomyślny, sciezka)) {
			try {
				f.createNewFile();
				String path = f.getAbsolutePath();
				if (!path.contains("\\configi\\"))
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

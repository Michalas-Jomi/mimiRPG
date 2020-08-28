package me.jomi.mimiRPG;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class Config {
	private YamlConfiguration plik;
	private File f;
	
	private String sciezkaJarDomyœlny = null;
	private String sciezka;
	
	public Config(String nazwa) {
		this.sciezka = "plugins/"+Main.plugin.getDescription().getName()+"/" + nazwa + ".yml";
		this.sciezkaJarDomyœlny = "Configi/" + nazwa.substring(nazwa.replace('\\', '/').lastIndexOf("/")+1) + ".yml";
		prze³aduj();
	}
	public Config(File plik) {
		this.sciezka = plik.getPath();
		prze³aduj();
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
	public boolean ustawDomyœlne(String sciezka, Object obj) {
		if (wczytaj(sciezka) == null) {
			ustaw(sciezka, obj);
			return true;
		}
		return false;
	}
	public boolean ustaw_zapiszDomyœlne(String sciezka, Object obj) {
		if (wczytaj(sciezka) == null) {
			ustaw_zapisz(sciezka, obj);
			return true;
		}
		return false;
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
		prze³aduj();
	}

	public Set<String> klucze(boolean wszystkie){
		return plik.getKeys(wszystkie);
	}
	public ConfigurationSection sekcja(Object... sciezka) {
		return plik.getConfigurationSection(sc(sciezka));
	}
	
	public Object  wczytaj		 (Object... sciezka) { return     plik.get(sc(sciezka));}
	public int 	   wczytajInt	 (Object... sciezka) { return (int)    wczytaj(sciezka);}
	public double  wczytajDouble (Object... sciezka) { return (double) wczytaj(sciezka);}
	public boolean wczytajBoolean(Object... sciezka) { return (boolean)wczytaj(sciezka);}
	public String  wczytajStr	 (Object... sciezka) { return Func.koloruj((String) wczytaj(sciezka));}
	
	public Object wczytajLubDomyœlna(String sciezka, Object domyœlna) {
		Object obj = wczytaj(sciezka);
		return obj != null ? obj : domyœlna;
	}
	public List<String> wczytajListe(Object... sciezka){
		List<String> lista = plik.getStringList(sc(sciezka));
		if (lista == null) lista = Lists.newArrayList();
		return lista;
	}
	@SuppressWarnings("unchecked")
	public List<ItemStack> wczytajItemy(Object... sciezka) {
		List<ItemStack> lista = Lists.newArrayList();
		List<Object> _lista = (List<Object>) wczytaj(sciezka);
		for (Object obj : _lista)
			lista.add(item(obj));
		return lista;
	}
	public ItemStack wczytajItem(Object... sciezka) {
		return item(wczytaj(sciezka));
	}
	public static ItemStack item(Object item) {
		if (item == null) return null;
		if (item instanceof ItemStack)
			return (ItemStack) item;
		String[] wejscie = ((String) item).split(" ");
		ItemStack _item = Baza.itemy.get(wejscie[0]).clone();
		if (wejscie.length >= 2)
			_item.setAmount(Func.Int(wejscie[1], 1));
		return _item;
	}
	
	public Napis wczytajNapis(Object... sciezka) {
		return _napis(wczytaj(sciezka));
	}
	public List<Napis> wczytajListeNapisów(Object... sciezka){
		List<Napis> lista = Lists.newArrayList();
		Object obj = wczytaj(sciezka);
		if (obj != null)
			for (Object napis : (List<?>) obj)
				lista.add(_napis(napis));
		return lista;
	}
	@SuppressWarnings("unchecked")
	private Napis _napis(Object obj) {
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
		if (!Func.wyjmijPlik(sciezkaJarDomyœlny, sciezka)) {
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
	public void prze³aduj() {
		stwórz();
		plik = YamlConfiguration.loadConfiguration(f);
	}
	
	private String sc(Object[] sciezka) {
		return Func.listToString(sciezka, 0, ".");
	}

	public boolean usuñ() {
		return f.delete();
	}
}

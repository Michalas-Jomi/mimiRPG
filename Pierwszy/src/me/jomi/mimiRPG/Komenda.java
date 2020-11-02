package me.jomi.mimiRPG;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.util.Func;

public abstract class Komenda implements TabExecutor {
	List<PluginCommand> _komendy = Lists.newArrayList();
	boolean _zarejestrowane_komendy = true;
	public Komenda(String komenda) {
		ustawKomende(komenda, null, null);
	}
	public Komenda(String komenda, String użycie) {
		ustawKomende(komenda, użycie, null);
	}
	public Komenda(String komenda, String użycie, String... aliasy) {
		ustawKomende(komenda, użycie, Lists.newArrayList(aliasy));
	}
	
	/**
	 * Tworzy komendę i ustawia jej executora na ten Objekt
	 * 
	 * @param komenda nazwa komendy
	 * @param użycie info wyświetlane gdy onCommand zwróci false, dodaje prefix
	 * @param aliasy lista alternatywnych nazw dla komendy
	 * 
	 */
	protected void ustawKomende(String komenda, String użycie, List<String> aliasy) {
		if (użycie == null)
			użycie = "/" + komenda;
		try {
	    	Field fCommandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
	        fCommandMap.setAccessible(true);
	        CommandMap commandMap = (CommandMap) fCommandMap.get(Bukkit.getPluginManager());
	        commandMap.register(Main.plugin.getName(), komenda(komenda, użycie, aliasy));
	    } catch (Exception e) {
	    	Main.error("Nie udało sie Stworzyć komendy " + komenda);
	    	return;
	    }
		PluginCommand cmd = Main.plugin.getCommand(komenda);
		cmd.setTabCompleter(this);
		cmd.setExecutor(this);
	}
	private PluginCommand komenda(String nazwa, String użycie, List<String> aliasy) throws Exception {
		Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
		c.setAccessible(true);
		PluginCommand komenda = c.newInstance(nazwa, Main.plugin);
		String prefix;
		try { 
			prefix = (String) this.getClass().getDeclaredField("prefix").get(null);
		} catch (NoSuchFieldException e) {
			prefix = Func.prefix(this.getClass().getSimpleName());
		}
		komenda.setPermissionMessage(prefix + "§cNie masz uprawnień ziomuś");
		komenda.setPermission((Main.plugin.getName() + "." + nazwa).toLowerCase());
		if (!użycie.startsWith(prefix))
			użycie = prefix + użycie;
		komenda.setUsage(użycie);
		if (aliasy != null)
			komenda.setAliases(aliasy);
		_komendy.add(komenda);
		return komenda;
	}
	
	/**
	 * Wywoływana przy wpisywaniu komendy
	 * 
	 * @param sender wpisujący komendę
	 * @param cmd Objekt Komendy
	 * @param label dokładna forma wpisanej komendy
	 * @param args Tablica wpisanych argumentów
	 * 
	 * @return lista słów wyświetlanych pod tabem
	 */
	@Override
	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);
	/**
	 * Wowołana po wpisaniu komendy
	 * 
	 * @param sender wpisujący komendę
	 * @param cmd Objekt Komendy
	 * @param label dokładna forma wpisanej komendy
	 * @param args Tablica wpisanych argumentów
	 * 
	 * @return jeśli zwrócone zostanie false </br>
	 * sender zobaczy cmd.getPermissionMessage();
	 */
	@Override
	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	protected List<String> utab(String[] wpisane, String... Podpowiedzi) {
		return uzupełnijTabComplete(wpisane, Lists.newArrayList(Podpowiedzi));
	}
	protected List<String> utab(String[] wpisane, Iterable<String> Podpowiedzi) {
		return uzupełnijTabComplete(wpisane, Podpowiedzi);
	}
	protected List<String> uzupełnijTabComplete(String[] wpisane, Iterable<String> Podpowiedzi) {
		return uzupełnijTabComplete(Func.ostatni(wpisane), Podpowiedzi);
	}
	protected List<String> uzupełnijTabComplete(String wpisane, Iterable<String> Podpowiedzi) {
    	List<String> lista = Lists.newArrayList();
    	wpisane = wpisane.toLowerCase();
		for (String podpowiedz : Podpowiedzi)
			if (podpowiedz.toLowerCase().startsWith(wpisane))
				lista.add(podpowiedz);
		return lista;
    }
}

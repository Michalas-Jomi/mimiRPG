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

public abstract class Komenda implements TabExecutor {
	public Komenda(String komenda) {
		ustawKomende(komenda, null, null);
	}
	public Komenda(String komenda, String użycie) {
		ustawKomende(komenda, użycie, null);
	}
	public Komenda(String komenda, String użycie, String... aliasy) {
		ustawKomende(komenda, użycie, Lists.newArrayList(aliasy));
	}
	
	protected void ustawKomende(String komenda, String użycie, List<String> aliasy) {
		if (użycie == null)
			użycie = "/" + komenda;
		try {
	    	Field fCommandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
	        fCommandMap.setAccessible(true);
	        
	        Object commandMapObject = fCommandMap.get(Bukkit.getPluginManager());
	        if (commandMapObject instanceof CommandMap) {
	            CommandMap commandMap = (CommandMap) commandMapObject;
	    		commandMap.register(Main.plugin.getName(), komenda(komenda, użycie, aliasy));
	        }
	    } catch (NoSuchFieldException | IllegalAccessException e) {
	    	Main.log("§cNie udało sie Stworzyć komendy " + komenda);
	    	return;
	    }
		PluginCommand cmd = Main.plugin.getCommand(komenda);
		cmd.setTabCompleter(this);
		cmd.setExecutor(this);
		cmd.setUsage(użycie);
	}
	private PluginCommand komenda(String nazwa, String użycie, List<String> aliasy) {
		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			PluginCommand komenda = c.newInstance(nazwa, Main.plugin);
			String prefix = Func.prefix("Komenda");
			try { 
				prefix = (String) this.getClass().getDeclaredField("prefix").get(null);
			} catch (NoSuchFieldException e) {}
			komenda.setPermissionMessage(prefix + "§cNie masz uprawnień ziomuś");
			komenda.setPermission((Main.plugin.getName() + "." + nazwa).toLowerCase());
			komenda.setUsage(użycie);
			if (aliasy != null)
				komenda.setAliases(aliasy);
			return komenda;
		} catch (Exception e) {
			Main.error("Problem przy komendzie:", nazwa);
		}
		return null;
	}
	
	@Override
	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);
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

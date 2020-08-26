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
	public Komenda(String komenda, String u¿ycie) {
		ustawKomende(komenda, u¿ycie, null);
	}
	public Komenda(String komenda, String u¿ycie, String... aliasy) {
		ustawKomende(komenda, u¿ycie, Lists.newArrayList(aliasy));
	}
	
	protected void ustawKomende(String komenda, String u¿ycie, List<String> aliasy) {
		if (u¿ycie == null)
			u¿ycie = "/" + komenda;
		try {
	    	Field fCommandMap = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
	        fCommandMap.setAccessible(true);
	        
	        Object commandMapObject = fCommandMap.get(Bukkit.getPluginManager());
	        if (commandMapObject instanceof CommandMap) {
	            CommandMap commandMap = (CommandMap) commandMapObject;
	    		commandMap.register(Main.plugin.getName(), komenda(komenda, u¿ycie, aliasy));
	        }
	    } catch (NoSuchFieldException | IllegalAccessException e) {
	    	Main.log("§cNie uda³o sie Stworzyæ komendy " + komenda);
	    	return;
	    }
		PluginCommand cmd = Main.plugin.getCommand(komenda);
		cmd.setTabCompleter(this);
		cmd.setExecutor(this);
		cmd.setUsage(u¿ycie);
	}
	private PluginCommand komenda(String nazwa, String u¿ycie, List<String> aliasy) {
		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			PluginCommand komenda = c.newInstance(nazwa, Main.plugin);
			komenda.setPermission((Main.plugin.getName() + "." + nazwa).toLowerCase());
			komenda.setUsage(u¿ycie);
			if (aliasy != null)
				komenda.setAliases(aliasy);
			return komenda;
		} catch (Exception e) {
			Main.log("Problem przy komendzie:", nazwa);
		}
		return null;
	}
	
	
	@Override
	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);
	@Override
	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	protected List<String> utab(String wpisane, String... Podpowiedzi) {
		return uzupe³nijTabComplete(wpisane, Lists.newArrayList(Podpowiedzi));
	}
	protected List<String> utab(String[] wpisane, String... Podpowiedzi) {
		return uzupe³nijTabComplete(wpisane, Lists.newArrayList(Podpowiedzi));
	}
	protected List<String> uzupe³nijTabComplete(String[] wpisane, Iterable<String> Podpowiedzi) {
		return uzupe³nijTabComplete(Func.ostatni(wpisane), Podpowiedzi);
	}
	protected List<String> uzupe³nijTabComplete(String wpisane, Iterable<String> Podpowiedzi) {
    	List<String> lista = Lists.newArrayList();
		for (String podpowiedz : Podpowiedzi)
			if (podpowiedz.toLowerCase().startsWith(wpisane.toLowerCase()))
				lista.add(podpowiedz);
		return lista;
    }
}

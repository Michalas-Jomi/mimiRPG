package me.jomi.mimiRPG.util;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Timming;

@SuppressWarnings("unchecked")
public abstract class Komenda implements TabExecutor {
	@SuppressWarnings("serial")
	public static class MsgCmdError extends Error {
		public MsgCmdError(String msg) {
			super(msg);
		}
	}
	
	
	private List<PluginCommand> _komendy = Lists.newArrayList();
	public Komenda(String komenda) {
		this(komenda, null);
	}
	public Komenda(String komenda, String użycie) {
		this(komenda, użycie, new String[0]);
	}
	public Komenda(String komenda, String użycie, String... aliasy) {
		ustawKomende(komenda, użycie, Lists.newArrayList(aliasy));
	}

	static final CommandMap commandMap;
	static final Map<String, Command> mapaKomend;
	static {
		CommandMap cmdMap = null;
		Map<String, Command> mapa = null;
		try {
			cmdMap = (CommandMap) Func.dajField(Bukkit.getServer().getClass(), "commandMap").get(Bukkit.getServer());
			mapa = (Map<String, Command>) Func.dajField(cmdMap.getClass(), "knownCommands").get(cmdMap);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		commandMap = cmdMap;
		mapaKomend = mapa;
	}
	static void syncCommands() {
		try {
			Func.dajMetode(Bukkit.getServer().getClass(), "syncCommands").invoke(Bukkit.getServer());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/**
	 * Tworzy komendę i ustawia jej executora na ten Objekt
	 * 
	 * @param komenda nazwa komendy
	 * @param użycie info wyświetlane gdy onCommand zwróci false, dodaje prefix
	 * @param aliasy lista alternatywnych nazw dla komendy
	 * 
	 */
	protected PluginCommand ustawKomende(String komenda, String użycie, List<String> aliasy) {
		if (użycie == null)
			użycie = "/" + komenda;
		try {
			PluginCommand cmd = komenda(komenda, użycie, aliasy);
			cmd.setTabCompleter(this);
			cmd.setExecutor(this);
			return cmd;
	    } catch (Throwable e) {
	    	Main.error("Nie udało sie Stworzyć komendy " + komenda);
	    	return null;
	    }
	}
	private PluginCommand komenda(String nazwa, String użycie, List<String> aliasy) throws Exception {
		Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
		Func.ustawAccessible(c);
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
	public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try (Timming timming = new Timming("Komenda " + cmd.getName())) {
			try {
				return wykonajKomende(sender, cmd, label, args);
			} catch (MsgCmdError e) {
				return Func.powiadom(sender, e.getMessage());
			}
		}
	}
	public abstract boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError;
	
	
	protected String preThrowMsg(String lokalizacja, Object... args) {
		return Baza.msg(Func.prefix(this.getClass()), lokalizacja, args);
	}
	protected void broadcastMsg(String lokalizacja, Object... args) {
		String msg = preThrowMsg(lokalizacja, args);
		if (!msg.isEmpty())
			Func.broadcast(msg);
	}
	protected void throwMsg(String lokalizacja, Object... args) throws MsgCmdError {
		throw new MsgCmdError(preThrowMsg(lokalizacja, args));
	}
	protected String preThrowFormatMsg(String format, Object... args) {
		return Func.msg(Func.prefix(this.getClass()) + format, args);
	}
	protected void throwFormatMsg(String format, Object... args) throws MsgCmdError {
		throw new MsgCmdError(Func.prefix(this.getClass()) + Func.msg(format, args));
	}
	
	public static List<String> utab(String[] wpisane, String... Podpowiedzi) {
		return uzupełnijTabComplete(wpisane, Lists.newArrayList(Podpowiedzi));
	}
	public static List<String> utab(String[] wpisane, Iterable<String> Podpowiedzi) {
		return uzupełnijTabComplete(wpisane, Podpowiedzi);
	}
	public static List<String> uzupełnijTabComplete(String[] wpisane, Iterable<String> Podpowiedzi) {
		return uzupełnijTabComplete(Func.ostatni(wpisane), Podpowiedzi);
	}
	public static List<String> uzupełnijTabComplete(String wpisane, Iterable<String> Podpowiedzi) {
    	List<String> lista = Lists.newArrayList();
    	wpisane = wpisane.toLowerCase();
		for (String podpowiedz : Podpowiedzi)
			if (podpowiedz.toLowerCase().startsWith(wpisane))
				lista.add(podpowiedz);
		return lista;
    }
}

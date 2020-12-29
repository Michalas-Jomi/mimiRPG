package me.jomi.mimiRPG.Chat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

import joptsimple.ValueConversionException;

@Moduł
public class Debug extends Komenda {
	public Debug() {
		super("mdebug", "/debug <klasa> <odwołania>", "mimidebug");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) return false;
		try {
			List<String> _args = Func.tnij(Func.listToString(args, 1), ".");
			
			Class<?> klasa = Class.forName(args[0].startsWith("-c") ? args[0].substring(2) : "me.jomi.mimiRPG." + args[0], false, Debug.class.getClassLoader());
			Object obj = wez(klasa, null, _args.remove(0));
			
			for (String arg : _args)
				obj = wez(obj, arg);
			
			// TODO napisać rekurencyjnie
			if (obj.getClass().isArray()) {
				sender.sendMessage("[");
				for (Object o : ((Object[]) obj))
					sender.sendMessage(o.toString());
				sender.sendMessage("]");
			} else
				sender.sendMessage(obj.toString());
		} catch (Throwable e) {
			sender.sendMessage("§cNiepowodzenie " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	Object wez(Object skąd, String co) throws Throwable {
		return wez(skąd.getClass(), skąd, co);
	}
	Object wez(Class<?> klasa, Object naCzym, String co) throws Throwable {
		if (co.endsWith(")")) {
			Object[] parametry = parametry(co.substring(co.indexOf('(')+1, co.indexOf(')')));
			
			List<List<Class<?>>> klasy = Lists.newArrayList();
			for (int i=0; i<parametry.length; i++)
				klasy.add(Func.dajKlasy(parametry[i].getClass()));
			
			String metoda = co.substring(0, co.indexOf('('));
			Method met = wezMetode(klasa, metoda, klasy, 0, new Class<?>[klasy.size()]);
			met.setAccessible(true);
			return met.invoke(naCzym, parametry);
		} else {
			Field f = Func.dajField(klasa, co);
			f.setAccessible(true);
			return f.get(naCzym);
		}
	}
	Method wezMetode(Class<?> klasa, String metoda, List<List<Class<?>>> klasy, int i, Class<?>[] args) throws Throwable {
		if (i >= args.length)
			return Func.dajMetode(klasa, metoda, args);
		for (Class<?> arg : klasy.get(i))
			try {
				args[i] = arg;
				return wezMetode(klasa, metoda, klasy, i + 1, args);
			} catch(Throwable e) {}
		throw new Throwable();
	}
	Method wezMetode(Class<?> klasa, String metoda, Class<?>[] args) throws Throwable {
		return Func.dajMetode(klasa, metoda, args);
	}
	Object[] parametry(String nawiasy) {
		List<Object> lista = Lists.newArrayList();
		for (String arg : Func.tnij(nawiasy, ","))
			lista.add(typ(arg.trim()));
		return lista.toArray();
	}
	Object typ(String arg) {
		// null
		if (arg.equals("null")) return null;
		// String
		if (arg.startsWith("\"")) {
			if (arg.endsWith("\"") && arg.length() > 2)
				return arg.substring(1, arg.length()-1);
			throw new ValueConversionException("brak drugiego cudzysłowia (\"): " + arg);
		}
		// char
		if (arg.startsWith("\'")) {
			if (arg.length() == 3 && arg.endsWith("\'"))
				return arg.charAt(1);
			throw new ValueConversionException("brak drugiego apostrofa (\'): " + arg);
		}
		// int
		try { return Integer.parseInt(arg);
		} catch(NumberFormatException nfe) {}
		// double
		try { return Double.parseDouble(arg);
		} catch(NumberFormatException nfe) {}
		// float
		try { return Float.parseFloat(arg);
		} catch(NumberFormatException nfe3) {}
		// boolean
		if (arg.equals("true")) return true;
		if (arg.equals("false")) return false;
		throw new ValueConversionException("Nieprawidłowy parametr: '" + arg + "'");
	}
}



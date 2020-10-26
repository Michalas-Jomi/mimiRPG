package me.jomi.mimiRPG.Chat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import joptsimple.ValueConversionException;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

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
			
			Class<?> klasa = Class.forName(args[0].startsWith("-c") ? args[0].substring(2) : "me.jomi.mimiRPG." + args[0]);
			Object obj = wez(klasa, null, _args.remove(0));
			
			for (String arg : _args)
				obj = wez(obj, arg);
			
			sender.sendMessage(obj.toString());
		} catch (Throwable e) {
			sender.sendMessage("§cNiepowodzenie " + e.getClass().getSimpleName() + " " + e.getMessage());
		}
		return true;
	}

	Object wez(Object skąd, String co) throws Throwable {
		return wez(skąd.getClass(), skąd, co);
	}
	Object wez(Class<?> klasa, Object naCzym, String co) throws Throwable {
		if (co.endsWith(")")) {
			Object[] parametry = parametry(co.substring(co.indexOf('(')+1, co.indexOf(')')));
			
			Class<?>[] klasy = new Class<?>[parametry.length];
			for (int i=0; i<parametry.length; i++)
				klasy[i] = parametry[i].getClass();
			
			Method met = klasa.getDeclaredMethod(co.substring(0, co.indexOf('(')), klasy);
			met.setAccessible(true);
			return met.invoke(naCzym, parametry);
		} else {
			Field f = klasa.getDeclaredField(co);
			f.setAccessible(true);
			return f.get(naCzym);
		}
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



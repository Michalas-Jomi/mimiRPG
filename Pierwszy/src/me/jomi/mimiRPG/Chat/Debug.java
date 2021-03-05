package me.jomi.mimiRPG.Chat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Debug extends Komenda {
	public Debug() {
		super("mdebug", "/debug <klasa> <odwołania>", "mimidebug", "mdebugrozpisz");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		try {
			Class<?> klasa = Class.forName(args[0].startsWith("-c") ? args[0].substring(2) : "me.jomi.mimiRPG." + args[0], false, Main.classLoader);

			if (label.equalsIgnoreCase("mdebugrozpisz"))
				return Func.powiadom(sender, infoSimple(klasa));

			if (args.length < 2) return false;
			
			Object obj = null;
			
			int wNawiasie = 0;
			boolean wStringu = false;
			String metoda = null;
			StringBuilder strB = new StringBuilder();
			List<String> parametry = null;
			for (char znak : Func.listToString(args, 1).toCharArray()) {
				if (wStringu && znak != '"') {
					strB.append(znak);
					continue;
				}
				switch (znak) {
				case '.':
					if (wNawiasie != 0 || wStringu)
						strB.append(znak);
					else {
						obj = wez(sender, obj == null ? klasa : obj.getClass(), obj, metoda == null ? strB.toString() : metoda, parametry);
						parametry = null;
						metoda = null;
						strB = new StringBuilder();
					}
					break;
				case '(':
					if (wNawiasie == 0) {
						parametry = Lists.newArrayList();
						metoda = strB.toString();
						strB = new StringBuilder();
					}
					wNawiasie++;
					break;
				case ')':
					wNawiasie--;
					if (wNawiasie == 0) {
						parametry.add(strB.toString());
						strB = new StringBuilder();
					} else if (wNawiasie < 0)
						throw new Error("Za dużo nawiasów \")\"");
					break;
				case ',':
					parametry.add(strB.toString());
					strB = new StringBuilder();
					break;
				case '"':
					wStringu = !wStringu;
					strB.append(znak);
					break;
				case ' ':
					if (!wStringu)
						break;
				default:
					strB.append(znak);
				}
			}
			
			if (parametry != null || (strB.length() != 0 && metoda == null))
				obj = wez(sender, obj == null ? klasa : obj.getClass(), obj, metoda == null ? strB.toString() : metoda, parametry);
			
			if (obj == null)
				return Func.powiadom(sender, "null");
			if (obj.getClass().isArray())
				sender.sendMessage(Func.arrayToString((Object[]) obj));
			else
				sender.sendMessage(obj.toString());
		} catch (Throwable e) {
			sender.sendMessage("§cNiepowodzenie " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	Object wez(CommandSender p, Class<?> klasa, Object naCzym, String co, List<String> parametry) throws Throwable {
		if (parametry != null) {
			if (parametry.size() == 1 && parametry.get(0).isEmpty())
				parametry.clear();
			Object[] args = new Object[parametry.size()];
			for (Method met : Func.dajMetody(klasa))
				if (met.getName().equals(co))
					try {
						for (int i=0; i < args.length; i++)
							args[i] = typ(p, met.getParameters()[i], parametry.get(i));
						return met.invoke(naCzym, args);
					} catch (Throwable e) {}
			throw new Error("Nie odnaleziono takiej metody");
		} else
			return Func.dajField(klasa, co).get(naCzym);
	}
	Object typ(CommandSender p, Parameter parameter, String str) {
		if (str.equals("null"))
			return null;
		
		if (str.startsWith("\"") && str.endsWith("\""))
			str = str.substring(1, str.length() - 1);
		
		if (parameter.getType().isEnum())
			return Func.StringToEnum(parameter.getType(), str);
		
		switch (parameter.getType().getSimpleName()) {
		case "Integer":
		case "int":
			return Func.Int(str);
		case "Double":
		case "double":
			return Func.Double(str);
		case "Float":
		case "float":
			return Func.Float(str);
		case "Boolean":
		case "boolean":
			return Boolean.parseBoolean(str);
		case "Character":
		case "char":
			return str.charAt(0);
		case "String":
			return str;
		case "ItemStack":
			return ((Player) p).getInventory().getItemInMainHand();
		case "Location":
			return ((Player) p).getLocation();
		case "Player":
			return Bukkit.getPlayer(str);
		case "Object":
			return str;
		}
		
		p.sendMessage("Nieobsługiwany typ zmiennych " + parameter.getType().getSimpleName());
		return null;
	}


	private static String params(Parameter[] parameters) {
		StringBuilder str = new StringBuilder();
		
		for (Parameter param : parameters)
			str.append(param.getType().getSimpleName()).append(", ");
		
		return str.isEmpty() ? str.toString() : str.substring(0, str.length() - 2);
	}
	public static String info(Class<?> clazz) {
		StringBuilder str = new StringBuilder('\n');
		
		for (Field f : clazz.getDeclaredFields())
			str.append(f.getType()).append(' ').append(f.getName()).append('\n');
		str.append('\n');
		for (Constructor<?> c : clazz.getDeclaredConstructors())
			str.append(clazz.getSimpleName()).append('(').append(Lists.newArrayList(c.getParameters())).append(")\n");
		str.append('\n');
		for (Method m : clazz.getDeclaredMethods())
			str.append(m.getReturnType()).append(' ').append(m.getName()).append('(').append(Lists.newArrayList(m.getParameters())).append(")\n");
		
		return str.toString();
	}
	public static String infoSimple(Class<?> clazz) {
		StringBuilder str = new StringBuilder();
		
		for (Field f : clazz.getDeclaredFields())
			str.append(f.getGenericType().getTypeName()).append(' ').append(f.getName()).append('\n');
		str.append('\n');
		for (Constructor<?> c : clazz.getDeclaredConstructors())
			str.append(clazz.getSimpleName()).append('(').append(params(c.getParameters())).append(")\n");
		str.append('\n');
		for (Method m : clazz.getDeclaredMethods())
			str.append(m.getReturnType().getSimpleName()).append(' ').append(m.getName()).append('(').append(params(m.getParameters())).append(")\n");
		
		return str.toString();
	}
}



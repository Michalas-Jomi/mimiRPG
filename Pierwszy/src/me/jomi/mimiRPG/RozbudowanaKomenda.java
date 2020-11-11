package me.jomi.mimiRPG;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.util.Func;

public abstract class RozbudowanaKomenda extends Komenda {
	public RozbudowanaKomenda(String komenda) {
		super(komenda);
		Init();
	}
	public RozbudowanaKomenda(String komenda, String użycie) {
		super(komenda, użycie);
		Init();
	}
	public RozbudowanaKomenda(String komenda, String użycie, String[] aliasy) {
		super(komenda, użycie, aliasy);
		Init();
	}
	
	private final List<Stopień> stopnie = Lists.newArrayList();
	
	private final void Init() {
		for (Class<?> klasa : Func.dajKlasy(this.getClass())) // TODO sprawdzić
			for (Method metoda : klasa.getDeclaredMethods())
				if (metoda.isAnnotationPresent(Argument.class))
					stopnie.add(new Stopień(metoda.getDeclaredAnnotation(Argument.class), metoda));
	}
	
	@Target(value=ElementType.METHOD)
	@Retention(value=RetentionPolicy.RUNTIME)
	public @interface Argument {
		String poprzedni() default "";
		String permisja() default "";
		String[] aliasy() default {};
		boolean pusty() default false;
		String nazwa();
	}
	class Stopień {
		String permisja;
		String[] aliasy;
		String nazwa;
		String poprzedni;
		Method func;
		Stopień(Argument a, Method func) {
			aliasy = new String[a.aliasy().length];
			for (int i=0; i<a.aliasy().length; i++)
				aliasy[i] = a.aliasy()[i].toLowerCase();
			poprzedni = a.poprzedni();
			permisja = a.permisja();
			nazwa = a.nazwa().toLowerCase();
			this.func = func;
		}
		
		boolean wykonaj(CommandSender sender, String[] args) {
			if (!sender.hasPermission(permisja))
				return Func.powiadom(sender, prefix() + "§4Nie masz wystarczających uprawnień do tego");
			
			try {
				func.invoke(this, sender, args);
			} catch (Throwable e) {
				return Func.powiadom(sender, prefix() + "§4Coś poszło nie tak");
			}
			return true;
		}
	}
	private String prefix() {
		try {
			return (String) this.getClass().getDeclaredField("prefix").get(null);
		} catch (Throwable e) {
			return "";
		}
	}
	Stopień znajdzStopień(String poprzednie, String str) {
		str = str.toLowerCase();
		for (Stopień stopień : stopnie) {
			if  (!stopień.poprzedni.equals(poprzednie))
				continue;
			if (stopień.nazwa.equals(str))
				return stopień;
			for (String alias : stopień.aliasy)
				if (alias.equals(str))
					return stopień;
		}
		return null;
	}
	Stopień znajdzStopień(String[] args) {
		Stopień s = null;
		String poprzednie = " ";
		for (String arg : args) {
			Stopień nowy = znajdzStopień(poprzednie.substring(0, poprzednie.length() - 1), arg);
			if (nowy == null)
				return s;
			s = nowy;
			poprzednie += s.nazwa + " ";
		}
		return s;
	}
	
	/*
	@Argument(nazwa = "a", aliasy = {"aa", "-a"})
	void a() { // /komenda a ... // ["a", "aa", "-a"].containt(args[0])
		
	}
	*/
	
	@Override
	public final List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Stopień stopień = znajdzStopień(args);
		if (stopień == null)
			return false;
		
		return stopień.wykonaj(sender, args);
	}
}

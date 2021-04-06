package me.jomi.mimiRPG.util;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;

public abstract class KomendaZMapowanymiItemami<T extends Mapowany> extends KomendaZItemami<T> implements Przeładowalny {
	protected final Config config;
	protected final String permEdytor;
	protected final EdytorOgólny<T> edytor;

	public KomendaZMapowanymiItemami(String komenda, Config config, Class<T> clazz) {
		super(komenda);
		this.config = config;
		edytor = new EdytorOgólny<>(komenda, clazz);
		Main.dodajPermisje(permEdytor = Func.permisja(komenda + ".edytor"));
		
		edytor.zarejestrujPoZatwierdz((przed, po) -> przeładuj());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		switch (args.length) {
		case 0:
		case 1:
			List<String> lista = super.onTabComplete(sender, cmd, label, args);
			if (sender.hasPermission(permEdytor))
				lista.add("edytor");
			return utab(args, lista);
		case 2:
			if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor))
				return utab(args, "-t", "-u");
			break;
		case 3:
			if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor))
				return utab(args, getItemy());
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		
		if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor)) {
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, getPrefix() + "/itemrpg edytor -t <itemrpg>");
			else if (args.length >= 2 && args[1].equals("-t"))
				args[2] = config.path().substring(Main.path.length()) + "|" + args[2];
			return edytor.onCommand(sender, "itemrpg", args);
		}
		
		return super.wykonajKomende(sender, cmd, label, args);
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane " + edytor.clazz.getSimpleName() + " w " + this.getClass().getSimpleName(), getItemy().size());
	}
	
	@Override
	public T getItem(String nazwa) {
		return config.wczytajPewny(nazwa);
	}
	@Override
	public Collection<String> getItemy() {
		return config.klucze();
	}
}

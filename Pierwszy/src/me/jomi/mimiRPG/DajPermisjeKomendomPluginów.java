package me.jomi.mimiRPG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import me.jomi.mimiRPG.util.Func;

@Moduł
public class DajPermisjeKomendomPluginów {
	public static final String prefix = Func.prefix("Permisje Komend");
	
	public static class MimiCommand extends Command {
		final Command cmd;

		protected MimiCommand(Command cmd) {
			super(cmd.getName(), cmd.getDescription(), cmd.getUsage(), cmd.getAliases());
			this.cmd = cmd;
		}


		@Override
		public boolean execute(CommandSender sender, String label, String[] args) {
			return cmd.execute(sender, label, args);
		}
		@Override
		public int hashCode() {
			return cmd.hashCode();
		}
		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
			return cmd.tabComplete(sender, alias, args);
		}
		@Override
		public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
			return cmd.tabComplete(sender, alias, args, location);
		}
		@Override
		public boolean equals(Object obj) {
			return cmd.equals(obj);
		}
		@Override
		public String getName() {
			return cmd.getName();
		}
		@Override
		public boolean setName(String name) {
			return cmd.setName(name);
		}
		@Override
		public String getPermission() {
			return super.getPermission();
		}
		@Override
		public void setPermission(String permission) {
			super.setPermission(permission);
		}
		@Override
		public boolean testPermission(CommandSender target) {
			return super.testPermission(target);
		}
		@Override
		public boolean testPermissionSilent(CommandSender target) {
			return super.testPermissionSilent(target);
		}
		@Override
		public String getLabel() {
			return cmd.getLabel();
		}
		@Override
		public boolean setLabel(String name) {
			return cmd.setLabel(name);
		}
		@Override
		public boolean register(CommandMap commandMap) {
			return super.register(commandMap);
		}
		@Override
		public boolean unregister(CommandMap commandMap) {
			return super.unregister(commandMap);
		}
		@Override
		public boolean isRegistered() {
			return super.isRegistered();
		}
		@Override
		public List<String> getAliases() {
			return cmd.getAliases();
		}
		@Override
		public String getPermissionMessage() {
			return cmd.getPermissionMessage();
		}
		@Override
		public String getDescription() {
			return cmd.getDescription();
		}
		@Override
		public String getUsage() {
			return cmd.getUsage();
		}
		@Override
		public Command setAliases(List<String> aliases) {
			return cmd.setAliases(aliases);
		}
		@Override
		public Command setDescription(String description) {
			return cmd.setDescription(description);
		}
		@Override
		public Command setPermissionMessage(String permissionMessage) {
			return cmd.setPermissionMessage(permissionMessage);
		}
		@Override
		public Command setUsage(String usage) {
			return cmd.setUsage(usage);
		}
		@Override
		public String toString() {
			return this.getClass().getName() + "(" + cmd.toString() + ")";
		}
	}
	
	PluginManager pluginManager = Main.plugin.getServer().getPluginManager();
	
	public DajPermisjeKomendomPluginów() {
		Func.opóznij(1, this::zainicjuj);
	}

	
	Set<String> zrobione = new HashSet<>();
	void ustawOgólne(Command cmd) {
		List<String> aliasyStare = cmd.getAliases();
		aliasyStare.add(cmd.getName());
		List<String> aliasyNowe = new ArrayList<>();
		for (String alias : aliasyStare)
			if (!alias.equals(cmd.getName()) && !(alias.length() > 1 && alias.charAt(0) == 'e' && aliasyStare.contains(alias.substring(1))))
				aliasyNowe.add(alias);
		cmd.setAliases(aliasyNowe);
		
		String perm;
		if (cmd instanceof PluginCommand) {
			PluginCommand pcmd = (PluginCommand) cmd;
			perm = pcmd.getPlugin().getName() + "." + pcmd.getName();
		} else {
			if (cmd.getPermission() == null) {
				perm = "command." + cmd.getName();
			} else {
				Func.wykonajDlaNieNull(Bukkit.getPluginManager().getPermission(cmd.getPermission()), p -> p.setDefault(PermissionDefault.OP));					
				perm = cmd.getPermission();
			}
		}
		if (pluginManager.getPermission(perm) == null)
			pluginManager.addPermission(new Permission(perm));
		cmd.setPermission(perm.toLowerCase());

		if (cmd.getPermissionMessage() == null)
			cmd.setPermissionMessage("§cNie możesz tego użyć");
	}
	void zainicjuj() {
		zasłonięte(Main.ust.wczytajListe("DajPermisjeKomendomPluginów.zasłonięte"));
		przekierowania(Main.ust.wczytajListe("DajPermisjeKomendomPluginów.przekierowane"));
		
		Komenda.mapaKomend.values().forEach(this::ustawOgólne);
		
		Komenda.syncCommands();
	}
	void przekierowania(List<String> przekierowane) {
		przekierowane.forEach(przekierowywane -> {
			List<String> oba = Func.tnij(przekierowywane, " ");
			if (oba.size() != 2)
				Main.warn(prefix + "Niepoprawne przekierowywanie komend: " + przekierowywane);
			else {
				Command cmd1 = Komenda.mapaKomend.get(oba.get(0));// tą kopiuje
				Command cmd2 = Komenda.mapaKomend.get(oba.get(1));// tej ustawia
				
				if (cmd1 != null) {
					Main.warn(prefix + "Nieodnaleziono przekierowywanej komendy: " + oba.get(0));
					return;
				} else if (cmd2 != null)
					Main.log(prefix + Func.msg("Utworzono alias %s dla komendy %s", oba.get(1), oba.get(0)));
				else
					Main.log(prefix + Func.msg("Przekierowano komendę %s na %s", oba.get(1), oba.get(0)));
				Komenda.mapaKomend.put(oba.get(1), cmd1);
			}
		});
	}
	void zasłonięte(List<String> zasłonięte) {
		Map<String, MimiCommand> zrobione = new HashMap<>();
		zasłonięte.forEach(zasłonięta -> {
			Command cmd = Komenda.mapaKomend.get(zasłonięta);
			if (cmd == null)
				Main.warn(prefix + "Nie odnaleziono komendy do przesłonienia: " + zasłonięta);
			else if (zrobione.containsKey(cmd.getName())) {
				Komenda.mapaKomend.put(zasłonięta, zrobione.get(cmd.getName()));
			} else {
				cmd.unregister(Komenda.commandMap);
				
				String namespace;
				if (cmd instanceof PluginCommand)
					namespace = ((PluginCommand) cmd).getPlugin().getName();
				else
					namespace = cmd.getPermission().substring(0, cmd.getPermission().indexOf('.'));
				
				MimiCommand mimicmd = new MimiCommand(cmd);
				
				namespace = namespace.toLowerCase();
				
				List<String> aliasy = new ArrayList<>(mimicmd.getAliases());
				aliasy.add(mimicmd.getName());
				
				String fnamespace = namespace;
				aliasy.forEach(alias -> {
					String alias1 = alias.toLowerCase();
					Func.wykonajDlaNieNull(Komenda.mapaKomend.get(alias1), znaleziona -> {
						if (znaleziona.getName().equals(mimicmd.getName()))
							Komenda.mapaKomend.remove(alias1);
					});
					String alias2 = (fnamespace + ":" + alias).toLowerCase();
					Func.wykonajDlaNieNull(Komenda.mapaKomend.get(alias2), znaleziona -> {
						if (znaleziona.getName().equals(mimicmd.getName()))
							Komenda.mapaKomend.remove(alias2);
					});
				});
				
				Komenda.commandMap.register(namespace, cmd);
			}	
		});
		
	}
}

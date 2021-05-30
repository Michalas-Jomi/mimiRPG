package me.jomi.mimiRPG.Chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class PySocket extends Komenda implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix(PySocket.class);
	public static class Taski {
		void broadcast(DataInputStream in) throws IOException {
			Bukkit.broadcastMessage(in.readUTF());
		}
		void log(DataInputStream in) throws IOException {
			Main.log(in.readUTF());
		}
		void warn(DataInputStream in) throws IOException {
			Main.warn(in.readUTF());
		}
		void error(DataInputStream in) throws IOException {
			Main.error(in.readUTF());
		}
		void info(DataInputStream in) throws IOException {
			String nick = in.readUTF();
			String msg  = in.readUTF();
			
			Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> p.sendMessage(msg), () -> {
				if (nick.equalsIgnoreCase("Console"))
					Bukkit.getConsoleSender().sendMessage(msg);
			});
		}
	}

	public PySocket() {
		super("discord", "/discord <nick i tag discord> - aby zarejestować konto discord", "dc");
		ustawKomende("dclog", "/dcLog <źródło> <wiadomość>", null);
		ustawKomende("dcrola", "/dcrola <nadaj | zabierz> <nick> <rola>", null);
	}
	
    static Socket socket;
    static String socket_ip;
    static int socket_port;
    static Thread socket_thread;
    
    public static void connectToSocketServer() {
    	try {
    		if (socket_thread != null && socket_thread.isAlive())
    			socket_thread.interrupt();
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    	
    	(socket_thread = new Thread(() -> {
            try {
                connect();
            } catch (Throwable e) {
            	e.printStackTrace();
                throw new RuntimeException(e);
            }
        })).start();
    }
    private static void connect() throws IOException, InterruptedException {
    	while (true) {
    		try {
    			if (socket != null && !socket.isClosed())
    				try {
    					socket.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			
    			socket = new Socket(socket_ip, socket_port);
    			socket.setKeepAlive(true);
    			
    			Main.log(prefix + Func.msg("socket nasłuchuje teraz na adresie %s:%s", socket_ip, socket_port));
    			
    			nasłuchuj();
    			
    		} catch (ConnectException e2) {
    			Main.warn("Brak łączności socketów");
    		} catch (Throwable e) {
    			Main.warn("Błąd z socketami!");
    		}
	        Thread.sleep(1000L);
    	}
    }
    private static void nasłuchuj() throws IOException {
    	DataInputStream in = new DataInputStream(socket.getInputStream());
    	Taski taski = new Taski();
    	
    	while (true) {
    		String task = in.readUTF();
    		try {
    			Func.dajMetode(taski.getClass(), task, DataInputStream.class).invoke(taski, in);
    		} catch (Throwable e) {
    			Main.warn(prefix + Func.msg("Problem z taskiem %s", task));
    		}
    	}
    }
    
    public static interface DataOutputStreamConsumer {
    	public void accept(DataOutputStream out) throws IOException;
    }
    public static void wyślij(String task, DataOutputStreamConsumer cons) {
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF(task);
			cons.accept(out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
	
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void pisanie(AsyncPlayerChatEvent ev) {
    	wyślij("log", out -> {
    		out.writeUTF("chat");
    		out.writeUTF(Func.usuńKolor(String.format(ev.getFormat(), ev.getPlayer().getDisplayName(), ev.getMessage())));
    	});
    }
    
    
    @Override
	public void przeładuj() {
    	socket_ip 	= Main.ust.wczytajPewnyD("PySocket.socket.ip");
    	socket_port = Main.ust.wczytajPewnyD("PySocket.socket.port");
    	connectToSocketServer();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Soket aktywny", socket != null);
	}
    
    @Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
    Pattern dcNamePattern = Pattern.compile("(.+)(#\\d{4})?");
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		switch (cmd.getName()) {
		case "dcrola":
			if (args.length < 3) return false;
			
			if 		(args[0].equalsIgnoreCase("nadaj"));
			else if (args[0].equalsIgnoreCase("zabierz"));
			else return false;
			
			String nick = args[1];
			String rola = args[2];
			
			wyślij(args[0].toLowerCase() + "Role", out -> {
				out.writeUTF(sender.getName());
				out.writeUTF(nick);
				out.writeUTF(rola);
			});
			
			
			return true;
		case "dclog":
			if (args.length < 2) return false;
			
			String źródło = args[0];
			String msg = Func.listToString(args, 1);
			
			wyślij("log", out -> {
				out.writeUTF(źródło);
				out.writeUTF(msg);
			});
			
			sender.sendMessage(prefix + "Wysłano na discorda log \"" + źródło + "\": " + msg);
			
			return true;
		case "discord":
			if (args.length != 1) return false;
			
			Matcher matcher = dcNamePattern.matcher(args[0]); 
			if (matcher.matches()) {
				Runnable runnable = () -> wyślij("zarejestruj", out -> {
					out.writeUTF(sender.getName());
					out.writeUTF(matcher.group(1));
				});
				
				if (sender instanceof Player)
					Main.panelTakNie((Player) sender,
							"&1&lKonto discord &9&l" + matcher.group(1) + " &1&ljest twoje?",
							"&6Tak &e" + matcher.group(1) + " &6to moje konto",
							"&cNie, to nie moje konto",
							runnable,
							null);
				else
					runnable.run();
				
			} else
				throwFormatMsg("Niepoprawny nick discorda");
			
		}
		
		return true;
	}
}

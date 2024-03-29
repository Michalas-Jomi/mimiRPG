package me.jomi.mimiRPG.RPG;

import java.io.DataInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.util.Func;

public class BazaDanych {
	public static final String prefix = Func.prefix(BazaDanych.class);
	
    private static Connection connection;
    private static Statement stat;
    
    
	public static void otwórz() {
		if (connection != null)
			return;
		
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Brak sterownika JDBC");
            return;
        }

        try {
        	connection = DriverManager.getConnection("jdbc:sqlite:" + Main.path + "configi/mimiRPG.db");
            stat = connection.createStatement();
        } catch (SQLException e) {
            System.err.println("Problem z otwarciem polaczenia");
            e.printStackTrace();
            return;
        }
        
        try {
        	utwórz();
            Main.log(prefix + "Otwarto bazę danych");
        } catch (Throwable e) {
        	Main.error(prefix + "Otwarcie bazy danych nie powiodło się");
        	e.printStackTrace();
        }
        
        synchronized (odłożone) {
        	while (!odłożone.isEmpty())
				try {
        			stat.execute(odłożone.remove(0));
        		} catch (Throwable e) {
        			e.printStackTrace();
        		}
        }
        
	}
	private static void utwórz() {
		execute("CREATE TABLE IF NOT EXISTS itemy ("
				+ "id varchar(32) PRIMARY KEY UNIQUE NOT NULL,"
				+ "nazwa TEXT,"
				+ "opis TEXT,"
				+ "ranga TEXT NOT NULL DEFAULT ZWYCZAJNY,"
				+ "typ_itemu TEXT NOT NULL DEFAULT BRAK,"
				+ "bazowy_item TEXT NOT NULL DEFAULT STONE,"
				+ "bonusy BLOB"
				+ ")");
	}
	
	private static final List<String> odłożone = new ArrayList<>();
	public static void execute(String sql) {
		if (stat == null)
			synchronized (odłożone) {
				odłożone.add(sql);
			}
		else
			try {
				stat.execute(sql);
			} catch (SQLException e) {
				Func.throwEx(e);
			}
	}
	public static ResultSet executeQuery(String sql) {
		try {
			return stat.executeQuery(sql);
		} catch (SQLException e) {
			throw Func.throwEx(e);
		}
	}
	public static ResultSet executeQuery1(String sql) {
		ResultSet set = executeQuery(sql);
		try {
			set.next();
		} catch (SQLException e) {
			Func.throwEx(e);
		}
		return set;
	}
	public static PreparedStatement prepare(String sql) {
		try {
			return connection.prepareStatement(sql);
		} catch (SQLException e) {
			throw Func.throwEx(e);
		}
	}
	
	public static boolean włączona() {
		return stat != null;
	}
	
	public static DataInputStream readBlob(ResultSet set, String kolumna) {
		try {
			InputStream stream = set.getBinaryStream(kolumna);
			return stream != null ? new DataInputStream(stream) : null;
		} catch (SQLException e) {
			throw Func.throwEx(e);
		}
	}


	public static void onDisable() {
		if (stat != null)
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
}

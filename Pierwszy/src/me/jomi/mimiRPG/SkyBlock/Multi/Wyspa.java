package me.jomi.mimiRPG.SkyBlock.Multi;

import static me.jomi.mimiRPG.SkyBlock.Multi.MultiSkyBlock.prefix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.util.Func;

public class Wyspa {
	final String nazwaŚwiata;
	// nick.toLowerCase() : permisja
	Map<String, String> członkowie = new HashMap<>();
	
	long dataUtworzenia;
	
	public final World world;
	
	// pierwsze utworzenie
	Wyspa(Player właściciel) {
		this(MultiSkyBlock.nazwaŚwiata(właściciel), true);
		
		członkowie.put(właściciel.getName().toLowerCase(), "owner");
		
		dataUtworzenia = System.currentTimeMillis();
		
		zapisz();
		
		Main.log(prefix + Func.msg("%s utworzył nową wyspę (%s)", właściciel.getName(), nazwaŚwiata));
	}
	Wyspa(String nazwaŚwiata) {
		this(nazwaŚwiata, false);
		wczytaj();
	}
	private Wyspa(String nazwaŚwiata, boolean pierwszeUtworzenie) {
		this.nazwaŚwiata = nazwaŚwiata;
		world = MultiSkyBlock.wczytajŚwiat(nazwaŚwiata);
		Func.ustawMetadate(world, "mimiSkyBlock", this);
	}
	
	
	public void tpSpawn(Player p) {
		p.teleport(world.getSpawnLocation());
	}
	
	
	
	public void wczytaj() {
		try {
			wczytaj(new DataInputStream(new FileInputStream(new File(nazwaŚwiata + "/wyspa.mimi"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void zapisz() {
		try {
			File file = new File(nazwaŚwiata + "/wyspa.mimi");
			if (!file.exists())
				file.createNewFile();
			
			zapisz(new DataOutputStream(new FileOutputStream(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void wczytaj(DataInputStream in) throws IOException {
		int len;
		
		len = in.readShort();
		for (int i=0; i < len; i++)
			członkowie.put(in.readUTF(), in.readUTF());
		
		dataUtworzenia = in.readLong();
	}
	private void zapisz(DataOutputStream out) throws IOException {
		out.writeShort(członkowie.size());
		for (Map.Entry<String, String> entry : członkowie.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}
		
		out.writeLong(dataUtworzenia);
	}

	
	public void wyszedł(Player player) {
		if (world.getPlayers().isEmpty())
			MultiSkyBlock.usuńŚwiat(world.getName());
	}
}

package me.jomi.mimiRPG.Customizacja;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;

import net.minecraft.server.v1_16_R3.PacketPlayOutMap;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotki.QuadKrotka;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class CustomoweMapy extends Komenda implements Listener {
	public static final String prefix = Func.prefix(CustomoweMapy.class);
	static final List<QuadKrotka<Byte, Integer, Integer, Integer>> lista = new ArrayList<>();
	public static byte znajdzKolor(int alpha, int r, int g, int b) {
		int minRóżnica = 255 * 255 * 255;
		byte wynik = 0;
		
		if (alpha == 0)
			return 0;
		
		for (QuadKrotka<Byte, Integer, Integer, Integer> krotka : lista) {
			int różnica = Math.abs(r - krotka.b) + Math.abs(g - krotka.c) + Math.abs(b - krotka.d);
			if (różnica == 0) {
				return krotka.a;
			}
			if (różnica < minRóżnica) {
				minRóżnica = różnica;
				wynik = krotka.a;
			}
		}
		
		return wynik;
	}
	private static void wstawKolor(byte nr, int r, int g, int b) {
		lista.add(new QuadKrotka<>(nr, r, g, b));
	}
	private static void kolor(int nr, int r, int g, int b) {
		BiConsumer<Integer, Double> wstaw = (bajty, mn) -> wstawKolor((byte) (int) bajty, (int) (r * mn), (int) (g * mn), (int) (b * mn));
		wstaw.accept(nr * 4 + 0, 0.71);
		wstaw.accept(nr * 4 + 1, 0.86);
		wstaw.accept(nr * 4 + 2, 1.00);
		wstaw.accept(nr * 4 + 3, 0.53);
	}
	static {
		kolor(1, 127, 178, 56);
		kolor(2, 247, 233, 163);
		kolor(3, 199, 199, 199);
		kolor(4, 255, 0, 0);
		kolor(5, 160, 160, 255);
		kolor(6, 167, 167, 167);
		kolor(7, 0, 124, 0);
		kolor(8, 255, 255, 255);
		kolor(9, 164, 168, 184);
		kolor(10, 151, 109, 77);
		kolor(11, 112, 112, 112);
		kolor(12, 64, 64, 255);
		kolor(13, 143, 119, 72);
		kolor(14, 255, 252, 245);
		kolor(15, 216, 127, 51);
		kolor(16, 178, 76, 216);
		kolor(17, 102, 153, 216);
		kolor(18, 229, 229, 51);
		kolor(19, 127, 204, 25);
		kolor(20, 242, 127, 165);
		kolor(21, 76, 76, 76);
		kolor(22, 153, 153, 153);
		kolor(23, 76, 127, 153);
		kolor(24, 127, 63, 178);
		kolor(25, 51, 76, 178);
		kolor(26, 102, 76, 51);
		kolor(27, 102, 127, 51);
		kolor(28, 153, 51, 51);
		kolor(29, 25, 25, 25);
		kolor(30, 250, 238, 77);
		kolor(31, 92, 219, 213);
		kolor(32, 74, 128, 255);
		kolor(33, 0, 217, 58);
		kolor(34, 129, 86, 49);
		kolor(35, 112, 2, 0);
		kolor(36, 209, 177, 161);
		kolor(37, 159, 82, 36);
		kolor(38, 149, 87, 108);
		kolor(39, 112, 108, 138);
		kolor(40, 186, 133, 36);
		kolor(41, 103, 117, 53);
		kolor(42, 160, 77, 78);
		kolor(43, 57, 41, 35);
		kolor(44, 135, 107, 98);
		kolor(45, 87, 92, 92);
		kolor(46, 122, 73, 88);
		kolor(47, 76, 62, 92);
		kolor(48, 76, 50, 35);
		kolor(49, 76, 82, 42);
		kolor(50, 142, 60, 46);
		kolor(51, 37, 22, 16);
		kolor(52, 189, 48, 49);
		kolor(53, 148, 63, 97);
		kolor(54, 92, 25, 29);
		kolor(55, 22, 126, 134);
		kolor(56, 58, 142, 140);
		kolor(57, 86, 44, 62);
		kolor(58, 20, 180, 133);
	}
	

	public CustomoweMapy() {
		super("customowemapy", "/customowemapy [wczytaj <nazwa> <plik> | wyślij <nazwa> <idMapy> <nick>]");
		
		
	}
	
	
	static final File dir = new File(Main.path + "configi/Mapy");
	public static void zapisz(String nazwa, String plik) throws IOException {
		zapisz(nazwa, przerób(plik));
	}
	public static void zapisz(String nazwa, byte[] mapa) throws IOException {
		dir.mkdirs();
		File f = new File(dir, nazwa);
		
		if (!f.exists())
			f.createNewFile();
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
		
		out.write(mapa);
		
		out.flush();
		out.close();
	}
	public static byte[] przerób(String plik) throws IOException {
		return przerób(ImageIO.read(new File(plik)));
	}
	public static byte[] przerób(BufferedImage image) {
		if (image.getWidth() != 128 || image.getHeight() != 128)
			image = resize(image, 128, 128);
	    return przeróbUnsafe(image);
	}
	public static byte[] przeróbUnsafe(BufferedImage image) {
	      final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      final boolean hasAlphaChannel = image.getAlphaRaster() != null;
	      
	      byte[] result = new byte[image.getWidth() * image.getHeight()];
	      final int pixelLength = hasAlphaChannel ? 4 : 3;
	  	  int i = 0;
	  	  for (int pixel = 0; pixel + pixelLength - 1 < pixels.length; pixel += pixelLength) {
	  		  int index = 0;
			  int alpha = hasAlphaChannel ? pixels[pixel + index++] & 0xff : 255;
			  int b = pixels[pixel + index++] & 0xff;
			  int g = pixels[pixel + index++] & 0xff;
			  int r = pixels[pixel + index++] & 0xff;
			  result[i++] = znajdzKolor(alpha, r, g, b);
	  	  }

	      return result;
	}
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_4BYTE_ABGR);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	} 
	
	public static List<byte[]> przeróbGif(File gif) throws IOException {
	    List<byte[]> wynik = new ArrayList<>();
	    BufferedImage master = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);

	    ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
	    ir.setInput(ImageIO.createImageInputStream(gif));
	    for (int i = 0; i < ir.getNumImages(true); i++) {
	    	BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
	        master.getGraphics().drawImage(ir.read(i), 0, 0, null);
	        img.setData(master.getData());
	        
	        wynik.add(przerób(img));
	    }
	    
	    return wynik;
	}

	@SuppressWarnings("resource")
	public static byte[] wczytaj(String nazwa) throws FileNotFoundException, IOException {
		return new DataInputStream(new FileInputStream(new File(dir, nazwa))).readAllBytes();
	}
	
	public static PacketPlayOutMap packet(int idMapy, String nazwa) throws IOException {
		return packet(idMapy, wczytaj(nazwa));
	}
	public static PacketPlayOutMap packet(int idMapy, byte[] dane) {
		return new PacketPlayOutMap(idMapy, (byte) 0, false, true, new ArrayList<>(), dane, 0, 0, 128, 128);
	}
	
	public static void umieść(BufferedImage image, byte[] mapa, int X, int Y) {
		byte[] bajty = przeróbUnsafe(image);
		
		int i = 0;
		for (int y = Y; y < Y + image.getHeight(); y++)
			for (int x = X; x < X + image.getWidth(); x++) {
				byte bajt = bajty[i++];
				if (bajt == 0)
					continue;
				mapa[128 * y + x] = bajt;
			}
	}
	
	public void wyślijGif(Player p, int id, int powtórzenia, List<byte[]> klatki, List<byte[]> doWysłania) {
		if (doWysłania.isEmpty()) {
			klatki.forEach(klatka -> doWysłania.add(0, klatka));
			if (powtórzenia-- < 0)
				return;
		}
		
		byte[] dane = doWysłania.remove(0);
		NMS.wyślij(p, packet(id, dane));
		int powt = powtórzenia;
		Func.opóznij(2, () -> wyślijGif(p, id, powt, klatki, doWysłania));
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "wczytaj", "wyślij", "wyślijgif");
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("wyślijgif"))
				return null;
			File[] pliki = dir.listFiles();
			if (pliki != null)
				return utab(args, Func.wykonajWszystkim(pliki, File::getName));
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (args.length < 3)
			return false;
		String nazwa;
		String plik;
		int id;
		switch (args[0].toLowerCase()) {
		case "wczytaj":
			nazwa = args[1];
			plik  = Func.listToString(args, 2);
			
			try {
				zapisz(nazwa, plik);
			} catch (IOException e) {
				throwFormatMsg("Niepoprawny plik %s", plik);
			}
			
			throwFormatMsg("Wczytano mape %s z pliku %s", nazwa, plik);
		case "wyślij":
			if (args.length < 4)
				return false;
			
			nazwa = args[1];
			id = Func.Int(args[2], -1);
			String selektor = Func.listToString(args, 3);
			List<Player> gracze = new ArrayList<>();
			
			if (id == -1)
				throwFormatMsg("Niepoprawne id mapy: %s", args[2]);
			try {
				Bukkit.selectEntities(sender, selektor).forEach(e -> {
					if (e instanceof Player)
						gracze.add((Player) e);
				});
			} catch (Throwable e) {
				throwFormatMsg("Niepoprawny gracz %s", selektor);
			}
			
			if (gracze.isEmpty())
				throwFormatMsg("Nie wysłano mapy %s pod id %s do nikogo", nazwa, id);
			
			try {
				PacketPlayOutMap packet = packet(id, nazwa);
				gracze.forEach(gracz -> NMS.wyślij(gracz, packet));
			} catch (IOException e) {
				throwFormatMsg("Niepoprawna mapa: %s", nazwa);
			}
			
			throwFormatMsg("Wysłano mapę %s pod id %s do %s", nazwa, id, selektor);
			break;
		}
		return false;
	}
}

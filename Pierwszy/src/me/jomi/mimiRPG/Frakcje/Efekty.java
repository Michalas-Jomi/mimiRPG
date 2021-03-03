package me.jomi.mimiRPG.Frakcje;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Efekty implements Listener, Przeładowalny {
  static void dajEfekty(Player p) {
    byte b;
    int i;
    String[] arrayOfString;
    for (i = (arrayOfString = Main.perms.getPlayerGroups(p)).length, b = 0; b < i; ) {
      String grupa = arrayOfString[b];
      for (String efekt : Main.ust.wczytajListe("Efekty." + p.getWorld().getName() + "." + grupa)) {
        int stopień = 0;
        List<String> części = Func.tnij(efekt, " ");
        efekt = części.get(0);
        if (części.size() > 1)
          try {
            stopień = Integer.parseInt(części.get(1));
          } catch (Throwable throwable) {} 
        p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(efekt), 432000, stopień, false, false, false));
      } 
      b++;
    } 
  }
  
  static void wyczyśćEfekty(Player p) {
    for (PotionEffect effekt : p.getActivePotionEffects())
      p.removePotionEffect(effekt.getType()); 
  }
  
  @Override
  public void przeładuj() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      wyczyśćEfekty(p);
      dajEfekty(p);
    } 
  }
  @Override
  public Krotka<String, Object> raport() {
  	return Func.r("Wczytane Efekty", "Włączone");
  }
  
  @EventHandler
  public void śmierć(PlayerDeathEvent ev) {
    dajEfekty(ev.getEntity());
  }
  
  @EventHandler
  public void dołączanie(PlayerJoinEvent ev) {
    dajEfekty(ev.getPlayer());
  }
  
  @EventHandler
  public void opuszczanie(PlayerQuitEvent ev) {
    wyczyśćEfekty(ev.getPlayer());
  }
  
  @EventHandler
  public void zmianaświata(PlayerChangedWorldEvent ev) {
    Func.opóznij(1, () -> {
          wyczyśćEfekty(ev.getPlayer());
          dajEfekty(ev.getPlayer());
        });
  }
}

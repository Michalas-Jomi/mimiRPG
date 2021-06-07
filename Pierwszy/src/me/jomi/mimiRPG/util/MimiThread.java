package me.jomi.mimiRPG.util;

import java.util.HashMap;
import java.util.Map;

import me.jomi.mimiRPG.Main;

public class MimiThread extends Thread {
	static final Map<Long, MimiThread> mapa = new HashMap<>();
	
	public MimiThread(Runnable target) {
		super(target);
		synchronized(mapa) {
			mapa.put(getId(), this);
		}
	}
	
	@Override
	public void run() {
		if (!Main.pluginWyłączany)
			super.run();
		synchronized(mapa) {
			mapa.remove(getId());
		}
	}

	
	public static void onDisable() {
		synchronized (mapa) {
			while (!mapa.isEmpty()) {
				long id = mapa.keySet().iterator().next();
				try {
					mapa.remove(id).interrupt();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
}

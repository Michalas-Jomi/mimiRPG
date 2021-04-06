package me.jomi.mimiRPG.util;

import java.util.List;

import com.google.common.collect.Lists;

public interface Zegar {
	static final List<_Zegar> zegary = Lists.newArrayList();
	static void aktywuj() {
		Func.opóznij(1, Zegar::aktywuj);
		for (_Zegar zegar : zegary)
			zegar.tick();
	}
	static void zarejestruj(Zegar zegar) {
		zegary.add(new _Zegar(zegar));
	}
	static void wyrejestruj(Zegar zegar) {
		for (int i=0; i<zegary.size(); i++)
			if (zegary.get(i).zegar.getClass().getSimpleName().equals(zegar.getClass().getSimpleName())) {
				zegary.remove(i);
				return;
			}
	}
	
	int czas();
}

class _Zegar {
	int timer = 0;
	int maxTimer = 2;
	Zegar zegar;
	
	_Zegar(Zegar zegar) {
		this.zegar = zegar;
	}
	
	void tick() {
		if (++timer >= maxTimer) {
			timer = 0;
			maxTimer = zegar.czas();
		}
	}
}
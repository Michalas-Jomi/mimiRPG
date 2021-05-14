package me.jomi.mimiRPG.SkyBlock.Multi;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class GeneratorChunków extends ChunkGenerator {
	static final int maxChunk = 5;
	
	Set<String> blokady = new HashSet<>();
	
	@Override
	public boolean canSpawn(World world, int x, int z) {
		return !blokady.contains(x + "/" + z);
	}

	@Override
	public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biomeGrid) {
		ChunkData data = createChunkData(world);
		
		int x = 5;
		int z = 5;
		
		if (Math.abs(cx) >= maxChunk) x = cx >= maxChunk ? 0 : 15;
		if (Math.abs(cz) >= maxChunk) z = cz >= maxChunk ? 0 : 15;
		
		if (x != 5 || z != 5) {
			blokady.add(cx + "/" + cz);
			boolean incrX = x == 5;
			if (!(z != 5 && x != 5))
				for (int y = 0; y < data.getMaxHeight(); y++)
					for (int xz = 0; xz < 16; xz++)
						data.setBlock(incrX ? xz : x, y, incrX ? z : xz, Material.BARRIER);
		}
		
		return data;
	}
}

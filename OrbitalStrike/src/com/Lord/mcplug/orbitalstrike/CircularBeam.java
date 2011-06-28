package com.Lord.mcplug.orbitalstrike;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CircularBeam extends Beam {

	public CircularBeam(int refX, int refZ, World world, int radius) {
		super(refX, refZ, world, radius);
	}

	@Override
	protected boolean initPlacement(int refX, int refZ, int radius, World world) {
		int refY = 5;
		OrbitalStrike.logInfo("Placing at " + new Location(world, refX, refY, refZ).toString(), 1);
		for (int r = 1; r <= radius; r++) {
			Material mat;
			if (r == radius)
				mat = Material.DIAMOND_BLOCK;
			else
				mat = Material.TNT;
			for (int y = 125; y >= refY; y--) {
				int z = r;
				int x = 0;
				int e = 0;
				while (z >= x) {
					if (e > 0) {
						e -= (2 * z - 1);
						z--;
					}
					e += (2 * x + 1);
					x++;
					if (setBlockReflections(x, z, refX, y, refZ, world, mat))
						return true;
				}
			}
		}
		for (int y = 125; y >= refY; y--) {	// Center case
			if (setBlock(refX, refZ, y, world, Material.TNT))
				return true;
		}
		
		return true;
	}

	private boolean setBlockReflections(int x, int z, int xRef, int yRef, int zRef, World world, Material mat) {
		if (setBlock(xRef + x, zRef + z, yRef, world, mat))
			return true;
		if (setBlock(xRef + x, zRef - z, yRef, world, mat))
			return true;
		if (setBlock(xRef - x, zRef + z, yRef, world, mat))
			return true;
		if (setBlock(xRef - x, zRef - z, yRef, world, mat))
			return true;
		if (setBlock(xRef + z, zRef + x, yRef, world, mat))
			return true;
		if (setBlock(xRef + z, zRef - x, yRef, world, mat))
			return true;
		if (setBlock(xRef - z, zRef + x, yRef, world, mat))
			return true;
		if (setBlock(xRef - z, zRef - x, yRef, world, mat))
			return true;
		return false;
	}
	
	private boolean setBlock(int x, int z, int yRef, World world, Material mat) {
		Block curBlock = world.getBlockAt(x, yRef, z);
		if (curBlock == null) return false;
		if (curBlock.getType() != Material.AIR) return true;
		curBlock.setType(mat);
		m_PlacedLocations.add(curBlock.getLocation());
		return false;
	}
}

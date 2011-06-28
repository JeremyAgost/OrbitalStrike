package com.Lord.mcplug.orbitalstrike;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CircularPenetratingBeam extends Beam {

	public CircularPenetratingBeam(int refX, int refZ, World world, int radius) {
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
					setBlockReflections(x, z, refX, y, refZ, world, mat);
				}
			}
		}
		for (int y = 125; y >= refY; y--) {	// Center case
			setBlock(refX, refZ, y, world, Material.TNT);
		}
		
		return true;
	}

	private void setBlockReflections(int x, int z, int xRef, int yRef, int zRef, World world, Material mat) {
		setBlock(xRef + x, zRef + z, yRef, world, mat);
		setBlock(xRef + x, zRef - z, yRef, world, mat);
		setBlock(xRef - x, zRef + z, yRef, world, mat);
		setBlock(xRef - x, zRef - z, yRef, world, mat);
		setBlock(xRef + z, zRef + x, yRef, world, mat);
		setBlock(xRef + z, zRef - x, yRef, world, mat);
		setBlock(xRef - z, zRef + x, yRef, world, mat);
		setBlock(xRef - z, zRef - x, yRef, world, mat);
	}
	
	private void setBlock(int x, int z, int yRef, World world, Material mat) {
		Block curBlock = world.getBlockAt(x, yRef, z);
		if (curBlock == null) return;
		curBlock.setType(mat);
		m_PlacedLocations.add(curBlock.getLocation());
	}
}

package com.Lord.mcplug.orbitalstrike.beams;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.Lord.mcplug.orbitalstrike.BasicBeam;
import com.Lord.mcplug.orbitalstrike.OrbitalStrike;

public class CleanSquareBeam extends BasicBeam {

	private World m_World; 
	
	public CleanSquareBeam(int refX, int refZ, World world, int radius) {
		super(refX, refZ, world, radius);
		m_World = world;
	}

	@Override
	protected boolean initPlacement(int refX, int refZ, int radius, World world) {
		int refY = world.getHighestBlockYAt(refX, refZ);	
		if (refY > 110) return false;	//arbitrary maximum touchdown height
		OrbitalStrike.logInfo("Placing at " + new Location(world, refX, refY, refZ).toString(), 1);
		Block curBlock;
		for (int x = refX - radius; x <= refX + radius; x++)
			for (int z = refZ - radius; z <= refZ + radius; z++)
				for (int y = 125; y >= refY; y--) {
					curBlock = world.getBlockAt(x, y, z);
					if (curBlock.getType() != Material.AIR) break; //will only go down to the first thing it hits
					if (x == refX - radius || x == refX + radius || z == refZ - radius || z == refZ + radius)
						curBlock.setType(Material.DIAMOND_BLOCK);
					m_PlacedLocations.add(curBlock.getLocation());
				}
		return true;
	}

	public void detonate() {
		if (canDetonate()) {
			for (Location explosionLoc : m_PlacedLocations)
				m_World.createExplosion(explosionLoc, 4.0f);	//Explosive force of TNT
		}
		super.detonate();
	}
}

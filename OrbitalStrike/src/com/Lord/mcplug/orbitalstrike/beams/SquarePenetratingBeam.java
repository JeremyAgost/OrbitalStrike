package com.Lord.mcplug.orbitalstrike.beams;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.Lord.mcplug.orbitalstrike.BasicBeam;
import com.Lord.mcplug.orbitalstrike.OrbitalStrike;

public class SquarePenetratingBeam extends BasicBeam {

	public SquarePenetratingBeam(int refX, int refZ, World world, int radius) {
		super(refX, refZ, world, radius);
	}
	
	@Override
	protected boolean initPlacement(int refX, int refZ, int radius, World world) {
		int refY = 5;	
		if (refY > 110) return false;	//arbitrary maximum touchdown height
		OrbitalStrike.logInfo("Placing at " + new Location(world, refX, refY, refZ).toString(), 1);
		Block curBlock;
		for (int x = refX - radius; x <= refX + radius; x++)
			for (int z = refZ - radius; z <= refZ + radius; z++)
				for (int y = 125; y >= refY; y--) {
					curBlock = world.getBlockAt(x, y, z);
					if (x == refX - radius || x == refX + radius || z == refZ - radius || z == refZ + radius)
						curBlock.setType(Material.DIAMOND_BLOCK);
					else
						curBlock.setType(Material.TNT);
					
					m_PlacedLocations.add(curBlock.getLocation());
				}
		return true;
	}

}

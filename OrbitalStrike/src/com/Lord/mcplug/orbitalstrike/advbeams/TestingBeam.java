package com.Lord.mcplug.orbitalstrike.advbeams;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;

import com.Lord.mcplug.orbitalstrike.AdvancedBeam;
import com.Lord.mcplug.orbitalstrike.OrbitalStrike;

public class TestingBeam extends AdvancedBeam {

	public TestingBeam(int refX, int refY, int refZ, World world, int radius,
			AdvancedBeamSettings advSettings) {
		super(refX, refY, refZ, world, radius, advSettings);
	}

	@Override
	protected boolean subPlace() {
		
		m_World.getBlockAt(m_iCenterX, m_iCenterY + 1, m_iCenterZ).setType(Material.DIAMOND_BLOCK);
		
		return true;
	}

	@Override
	protected boolean subDetonate() {
		
		try {
			TNTPrimed exploder = m_World.spawn(new Location(m_World, m_iCenterX, m_iCenterY + 1, m_iCenterZ), TNTPrimed.class);	// Entity spawn
			OrbitalStrike.logInfo("Default TNT yield:" + exploder.getYield());
			exploder.setYield(10.0f);
			exploder.setFuseTicks(1);
			
		}
		catch (IllegalArgumentException e) {
			OrbitalStrike.logInfo("Couldn't spawn the entity of great importance", 1);
		}
		
		
		return true;
	}

}

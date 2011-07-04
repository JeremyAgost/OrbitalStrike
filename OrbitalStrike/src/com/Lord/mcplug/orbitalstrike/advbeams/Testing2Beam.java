package com.Lord.mcplug.orbitalstrike.advbeams;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.Lord.mcplug.orbitalstrike.AdvancedBeam;
import com.Lord.mcplug.orbitalstrike.OSStateHandler.ExplosiveEntityStateChangeOrder;
import com.Lord.mcplug.orbitalstrike.OSStateHandler.MaterialStateChangeOrder;
import com.Lord.mcplug.orbitalstrike.OrbitalStrike;

public class Testing2Beam extends AdvancedBeam {
	
	public Testing2Beam(int refX, int refY, int refZ, World world, int radius,
			AdvancedBeamSettings advSettings) {
		super(refX, refY, refZ, world, radius, advSettings);
	}

	@Override
	protected boolean subPlace() {
		/*
		Block curBlock;
		for (int x = m_iCenterX - m_iRadius; x <= m_iCenterX + m_iRadius; x++)
			for (int z = m_iCenterZ - m_iRadius; z <= m_iCenterZ + m_iRadius; z++)
				for (int y = 125; y >= refY; y--) {
					curBlock = world.getBlockAt(x, y, z);
					if (curBlock.getType() != Material.AIR) break; //will only go down to the first thing it hits
					if (x == m_iCenterX - m_iRadius || x == m_iCenterX + m_iRadius || z == m_iCenterZ - m_iRadius || z == m_iCenterZ + m_iRadius)
						curBlock.setType(Material.DIAMOND_BLOCK);
					else
						curBlock.setType(Material.TNT);
					
					m_PlacedLocations.add(curBlock.getLocation());
				}
		*/
		ArrayList<Pair> outerPairs = new ArrayList<Pair>();
		ArrayList<Pair> explosionPairs = new ArrayList<Pair>();
		for (int x = m_iCenterX - m_iRadius; x <= m_iCenterX + m_iRadius; x++)
			for (int z = m_iCenterZ - m_iRadius; z >= m_iCenterZ + m_iRadius; z++) {
				if (x == m_iCenterX - m_iRadius || x == m_iCenterX + m_iRadius || z == m_iCenterZ - m_iRadius || z == m_iCenterZ + m_iRadius) {
					outerPairs.add(new Pair(x,z));
					// TODO: separate conditions for determining explosive density
					explosionPairs.add(new Pair(x,z));
				}
			}
		
		MaterialStateChangeOrder beamMaterialOrder = OrbitalStrike.getStateHandler().new MaterialStateChangeOrder(m_World, Material.DIAMOND_BLOCK);
		m_ExplosionOrderList = new ArrayList<ExplosiveEntityStateChangeOrder>();
		Location loc;
		int filledLocs;
		int entityCooldown = this.getExplosiveRowFrequency();
		int runningLength = 0;
		for (int y = 125; y >= 5; y--) {
			filledLocs = 0;
			for (Pair pair : outerPairs) {
				loc = new Location(m_World, pair.x, y, pair.z);
				if (loc.getBlock().getTypeId() == 0) {
					beamMaterialOrder.add(pair.x, y, pair.z);
					m_PlacedLocations.add(loc);
				}
				else
					filledLocs++;
			}
			if (filledLocs > outerPairs.size() / 2)
				break;	// Beam has hit a level where more than half the beam blocks would be obstructed
			
			entityCooldown--;
			if (entityCooldown <= 0) {
				ExplosiveEntityStateChangeOrder explosiveOrder = OrbitalStrike.getStateHandler().new ExplosiveEntityStateChangeOrder(m_World, getExplosivePower());
				for (Pair pair : explosionPairs) {
					explosiveOrder.add(pair.x, y, pair.z);
				}
				m_ExplosionOrderList.add(explosiveOrder);
				entityCooldown = this.getExplosiveRowFrequency();
			}
			
			runningLength++;
			if (this.getMaxLength() > 0 && runningLength >= this.getMaxLength())
				break;	// Beam has hit max length
		}
		
		return false;
	}

	@Override
	protected boolean subDetonate() {
		// TODO Auto-generated method stub
		return false;
	}

	private ArrayList<ExplosiveEntityStateChangeOrder> m_ExplosionOrderList;
	
	private class Pair {
		public Pair(int x, int z) {
			this.x = x;
			this.z = z;
		}
		public int x;
		public int z;
	}
}

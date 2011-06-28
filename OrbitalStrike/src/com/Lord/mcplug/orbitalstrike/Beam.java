package com.Lord.mcplug.orbitalstrike;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Beam - Superclass for various beam types
 * Extend this class and implement initPlacment() to create a placement arrangement
 * @author lord_jeremy
 *
 */
public abstract class Beam {
	protected ArrayList<Location> m_PlacedLocations = new ArrayList<Location>();
	
	/**
	 * Override this to create a custom placement arrangement (required)
	 * @param refX The X location of the beam center
	 * @param refZ The Z location of the beam center
	 * @param radius The intended radius of the beam (by edge)
	 * @param world The world the beam is placed in
	 * @return True if the placement was successful
	 */
	protected abstract boolean initPlacement(int refX, int refZ, int radius, World world);
	
	private float getConstructionLifetime() {
		return 300.0f;	// Hardcoded
	}
	
	/**
	 * Override this to change the maximum beam lifetime after detonation.
	 * TODO Make this a configurable option
	 * @return The maximum beam lifetime after detonation
	 */
	protected float getDetonationLifetime() {
		return OrbitalStrike.getConfig().getDetonationLifetime();
	}
	
	/**
	 * Override this to change the maximum beam lifetime after placement.
	 * TODO Make this a configurable option
	 * @return The maximum beam lifetime after placement
	 */
	protected float getPlacementLifetime() {
		return OrbitalStrike.getConfig().getPlacementLifetime();
	}

	protected Beam(int refX, int refZ, World world, int radius) {
		if (radius < 1)
			radius = 1;
		
		m_World = world;
		m_iCenterX = refX;
		m_iCenterZ = refZ;
		m_iRadius = radius;
		m_bRemoved = false;
		m_bInPlace = false;
		m_iConstructionTime = Calendar.getInstance().getTimeInMillis();
	}

	private long m_iConstructionTime;
	private long m_iPlacementTime;
	private long m_iDetonationTime;
	private World m_World;
	private int m_iCenterX;
	private int m_iCenterZ;
	private int m_iRadius;
	private boolean m_bInPlace;
	private boolean m_bRemoved;
	
	/**
	 * Place the beam in the location defined during construction.
	 */
	public void place() {
		m_bInPlace = initPlacement(m_iCenterX, m_iCenterZ, m_iRadius, m_World);
		m_iPlacementTime = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Detonate the beam if it has been placed.
	 * TODO rework with pluggable detonation methods (i.e. detonate all down the center)
	 */
	public void detonate() {
		if (canDetonate()) {
			OrbitalStrike.detonationBegin();
			int centerY = m_World.getHighestBlockYAt(m_iCenterX, m_iCenterZ);	//Assumes that the beam is the highest thing and that there are TNT blocks within reach
			Location detonatorLoc = new Location(m_World, m_iCenterX, centerY, m_iCenterZ);
			OrbitalStrike.logInfo("Igniting at " + detonatorLoc.toString(), 1);
			m_World.createExplosion(detonatorLoc, 4.0f);	//Explosive force of TNT
			m_bInPlace = false;
		}
		m_iDetonationTime = Calendar.getInstance().getTimeInMillis();
	}

	public boolean canDetonate() {
		return m_bInPlace;
	}

	public void remove() {
		if (m_bRemoved) return;
		
		OrbitalStrike.deonationEnd();	// Cruddy hack
		Block checkBlock;
		Iterator<Location> itr = m_PlacedLocations.iterator();
		while (itr.hasNext()) {
			checkBlock = m_World.getBlockAt(itr.next());
			checkBlock.setType(Material.AIR);
		}
		m_bRemoved = true;
	}

	public boolean shouldRemove() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if ((m_iDetonationTime - currentTime) / 1000 > this.getDetonationLifetime() || (m_iPlacementTime - currentTime) / 1000 > this.getPlacementLifetime() || (m_iConstructionTime - currentTime) / 1000 > this.getConstructionLifetime())
			return true;
		
		return false;
	}
	
	protected void finalize() {
		this.remove();
	}

}

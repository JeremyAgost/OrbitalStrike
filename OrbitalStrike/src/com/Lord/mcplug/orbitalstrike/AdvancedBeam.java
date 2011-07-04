package com.Lord.mcplug.orbitalstrike;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public abstract class AdvancedBeam implements Beam {
	public class AdvancedBeamSettings {
		public int maxLength = 0;	// If nonzero, the beam length will not be larger than this value
		public boolean penetrateBlocks = false;	// If true, will replace blocks in the path of the beam
		public float explosivePower = 4.0f;	// Default TNT yield, should probably be replaced with a config option
		public int explosiveRowFrequency = 1;	// How often the explosive entity rows are placed
		public float constructionLifetime = 300.0f;	// Hardcoded
		public float placementLifetime = OrbitalStrike.getConfig().getPlacementLifetime();	// Default pulled from global config
		public float detonationLifetime = OrbitalStrike.getConfig().getDetonationLifetime();
	}
	
	protected ArrayList<Location> m_PlacedLocations = new ArrayList<Location>();
	protected final World m_World;
	protected final int m_iCenterX;
	protected final int m_iCenterY;
	protected final int m_iCenterZ;
	protected final int m_iRadius;
	
	protected abstract boolean subPlace();
	protected abstract boolean subDetonate();
	
	protected AdvancedBeam(int refX, int refY, int refZ, World world, int radius, AdvancedBeamSettings advSettings) {
		m_iCenterX = refX;
		m_iCenterY = refY;
		m_iCenterZ = refZ;
		m_World = world;
		m_iRadius = radius;
		
		if (advSettings == null)
			m_AdvancedSettings = new AdvancedBeamSettings();
		else
			m_AdvancedSettings = advSettings;
		
		m_bRemoved = false;
		m_bInPlace = false;
		m_iConstructionTime = Calendar.getInstance().getTimeInMillis();
	}
	
	public void place() {
		if (!m_bInPlace) {
			OrbitalStrike.logInfo("Placing at " + new Location(m_World, m_iCenterX, m_iCenterY, m_iCenterZ).toString(), 0);
			if (this.subPlace()) {
				m_bInPlace = true;
				m_iPlacementTime = Calendar.getInstance().getTimeInMillis();
			}
		}
	}
	
	public void detonate() {
		if (this.canDetonate()) {
			OrbitalStrike.logInfo("Igniting at " + new Location(m_World, m_iCenterX, m_iCenterY, m_iCenterZ).toString(), 0);
			if (this.subDetonate()) {
				m_bInPlace = false;
				m_iDetonationTime = Calendar.getInstance().getTimeInMillis();
			}
		}
	}
	
	public boolean canDetonate() {
		return m_bInPlace;
	}
	
	public void remove() {
		if (m_bRemoved) return;
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
		if (	(m_iDetonationTime - currentTime) / 1000 > this.getDetonationLifetime() ||
				(m_iPlacementTime - currentTime) / 1000 > this.getPlacementLifetime() ||
				(m_iConstructionTime - currentTime) / 1000 > this.getConstructionLifetime())
			return true;
		return false;
	}
	
	protected void finalize() {
		this.remove();
	}
	
	protected int getMaxLength() {
		return (m_AdvancedSettings.maxLength < 0 ? 0 : m_AdvancedSettings.maxLength);
	}
	
	protected boolean shouldPenetrateBlocks() {
		return m_AdvancedSettings.penetrateBlocks;
	}
	
	protected float getExplosivePower() {
		return (m_AdvancedSettings.explosivePower < 0 ? 0 : m_AdvancedSettings.explosivePower);
	}
	
	protected int getExplosiveRowFrequency() {
		return (m_AdvancedSettings.explosiveRowFrequency < 1 ? 1 : m_AdvancedSettings.explosiveRowFrequency);
	}
	
	private float getConstructionLifetime() {
		return (m_AdvancedSettings.constructionLifetime > 0 ? m_AdvancedSettings.constructionLifetime : 0.1f);
	}
	
	private float getPlacementLifetime() {
		return (m_AdvancedSettings.placementLifetime > 0 ? m_AdvancedSettings.placementLifetime : 0.1f);
	}
	
	private float getDetonationLifetime() {
		return (m_AdvancedSettings.detonationLifetime > 0 ? m_AdvancedSettings.detonationLifetime : 0.1f);
	}
	
	private AdvancedBeamSettings m_AdvancedSettings;
	private long m_iConstructionTime;
	private long m_iPlacementTime;
	private long m_iDetonationTime;
	private boolean m_bInPlace;
	private boolean m_bRemoved;
}

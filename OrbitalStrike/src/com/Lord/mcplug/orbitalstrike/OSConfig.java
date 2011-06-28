package com.Lord.mcplug.orbitalstrike;

import com.Lord.mcplug.orbitalstrike.OSHandler.BeamType;

public class OSConfig {
	private static final BeamType DEFAULT_BEAMTYPE = BeamType.SQUARE;
	private static final int DEFAULT_RADIUS = 1;
	private static final int MAX_RADIUS = 10;
	private static final float PLACEMENT_DELAY = 4.0f;
	private static final float DETONATION_DELAY = 1.0f;
	private static final float REMOVAL_INTERVAL = 1.0f;
	private static final float STAGGERED_PLACEMENT_DELAY = 3.0f;
	private static final float DETONATION_LIFETIME = 15.0f;
	private static final float PLACEMENT_LIFETIME = 25.0f;
	
	public BeamType getDefaultBeamType() {
		return DEFAULT_BEAMTYPE;
	}
	
	public int getDefaultRadius() {
		return DEFAULT_RADIUS;
	}
	
	public int getMaxRadius() {
		return MAX_RADIUS;
	}
	
	public float getPlacementDelay() {
		return PLACEMENT_DELAY;
	}
	
	public float getDetonationDelay() {
		return DETONATION_DELAY;
	}
	
	public float getRemovalInterval() {
		return REMOVAL_INTERVAL;
	}
	
	public float getStaggeredPlacementDelay() {
		return STAGGERED_PLACEMENT_DELAY;
	}
	
	public float getDetonationLifetime() {
		return DETONATION_LIFETIME;
	}
	
	public float getPlacementLifetime() {
		return PLACEMENT_LIFETIME;
	}
}

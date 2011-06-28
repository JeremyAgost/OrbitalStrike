package com.Lord.mcplug.orbitalstrike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class OSHandler {
	private Random m_Random = new Random();
	private Timer m_Timer = new Timer();
	private HashMap<Integer, Beam> m_StrikeMap = new HashMap<Integer, Beam>();
	
	public enum BeamType {
		INVALID,
		SQUARE,
		SQUARE_PENETRATING,
		THIN,
//		CLEAN_SQUARE,
//		CLEAN_SQUARE_PENETRATING,
//		CIRCULAR,
		CIRCULAR_PENETRATING
	}
	
	public class Vec2D {
		public Vec2D(double a, double b) {
			this.a = a;
			this.b = b;
		}
		public Vec2D subtract(Vec2D other) {
			return new Vec2D(this.a - other.a, this.b - other.b);
		}
		public Vec2D normalizedForm() {
			double mag = Math.sqrt(a * a + b * b);
			return new Vec2D(a / mag, b / mag);
		}
		public String toString() {
			return "(" + a + "," + b + ")";
		}
		public double a;
		public double b;
	}
	
	public class BeamLocation {
		public BeamLocation(int x, int z, World world) {
			this.x = x;
			this.z = z;
			this.world = world;
		}
		public BeamLocation(Location loc) {
			this.x = loc.getBlockX();
			this.z = loc.getBlockZ();
			this.world = loc.getWorld();
		}
		public BeamLocation(BeamLocation bloc) {
			this.x = bloc.x;
			this.z = bloc.z;
			this.world = bloc.world;
		}
		public String toString() {
			return world.toString() + "(x:" + x + ",z:" + z + ")";
		}
		public String toStringLocalized() {
			return "(x:" + x + ",z:" + z + ")";
		}
		public Vec2D vector() {
			return new Vec2D(x, z);
		}
		public void add(int x, int z) {
			this.x += x;
			this.z += z;
		}
		public int x;
		public int z;
		public World world;
	}
	
	public class BeamSetting {
		public BeamSetting(int radius, BeamType beamType) {
			this.radius = radius;
			this.beamType = beamType;
		}
		public String toString() {
			return "Radius: " + radius + ", Type: " + beamType;
		}
		public int radius;
		public BeamType beamType;
	}
	
	public int getMaxRadius() {
		return OrbitalStrike.getConfig().getMaxRadius();
	}
	
	public long getPlacementDelay() {
		return (long)(OrbitalStrike.getConfig().getPlacementDelay() * 1000);
	}
	
	public long getDetonationDelay() {
		
		return (long)(OrbitalStrike.getConfig().getDetonationDelay() * 1000);
	}
	
	public long getRemovalInterval() {
		return (long)(OrbitalStrike.getConfig().getRemovalInterval() * 1000);
	}
	
	
	public long getDeconstructionDelay() {
		return 1000;	// Hardcoded
	}
	
	public long getStaggeredPlacementDelay() {
		return (long)(OrbitalStrike.getConfig().getStaggeredPlacementDelay() * 1000);
	}
	
	public void initiateOrbitalStrike(OSHandler.BeamLocation location, OSHandler.BeamSetting settings) {
		if (settings.radius > getMaxRadius())
			settings.radius = getMaxRadius();
		
		int key = requestBeam(location, settings);
		if (key == -1) {
			OrbitalStrike.logInfo("Error: Unable to initiate strike", 1);
			return;
		}
		m_Timer.schedule(new BeamPlace_TimerTask(this, key), getPlacementDelay());
		m_Timer.schedule(new BeamRemove_TimerTask(this, key), getRemovalInterval());	// Will try to remove every interval

		// Broadcast warning message to all players in the world
		Iterator<Player> itr = location.world.getPlayers().iterator();
		while (itr.hasNext())
			OrbitalStrike.sendMessage(itr.next(), "Splash at " + location.toStringLocalized() + " in " + ((getPlacementDelay() + getDetonationDelay()) / 1000.0f) + " seconds!");
	}

	public void initiateOrbitalSwath(OSHandler.BeamLocation startLoc, OSHandler.BeamLocation endLoc, OSHandler.BeamSetting settings) {
		if (!startLoc.world.equals(endLoc.world)) {
			OrbitalStrike.logInfo("Error: Start and end location are in different worlds!", 1);
			return;
		}
		
		if (settings.radius > getMaxRadius())
			settings.radius = getMaxRadius();
		
		Vec2D start = startLoc.vector();
		Vec2D end = endLoc.vector();
		Vec2D offset = end.subtract(start).normalizedForm();
		OrbitalStrike.logInfo("Start:" + start + " End:" + end + " Offset:" + offset, 0);
		
		double effectiveRadius;
		if (/*settings.beamType == BeamType.CIRCULAR ||*/ settings.beamType == BeamType.CIRCULAR_PENETRATING || settings.beamType == BeamType.THIN) {	// Circular types
			effectiveRadius = settings.radius;
		}
		else if (settings.beamType == BeamType.SQUARE || settings.beamType == BeamType.SQUARE_PENETRATING) {	// Square types
			effectiveRadius = Math.sqrt(2 * settings.radius * settings.radius);// / Math.cos(Math.atan((end.b - start.b) / (end.a - start.a)));	
		}
		else {
			OrbitalStrike.logInfo("Error: Unrecognized beam type", 1);
			return;
		}
		
		int stepX = (int) (Math.ceil(Math.abs(effectiveRadius * offset.a)) * Math.signum(offset.a));
		int stepZ = (int) (Math.ceil(Math.abs(effectiveRadius * offset.b)) * Math.signum(offset.b));
		OrbitalStrike.logInfo("stepX:" + stepX + " stepZ:" + stepZ + " eRadius:" + effectiveRadius, 0);

		BeamLocation curLoc = new BeamLocation(startLoc);
		double linearDistance = Math.sqrt(Math.pow(endLoc.x - startLoc.x, 2) + Math.pow(endLoc.z - startLoc.z, 2));
		double currentDistance = 0;
		OrbitalStrike.logInfo("ldist:" + linearDistance, 0);
		ArrayList<OSHandler.BeamLocation> locationList = new ArrayList<OSHandler.BeamLocation>();
		int loopcount = 0;
		final int loopthresh = 50;
		do {
			locationList.add(new BeamLocation(curLoc));
			OrbitalStrike.logInfo("Marking " + curLoc + " for sweep strike (" + currentDistance + ")", 0);
			curLoc.add(stepX, stepZ);
			currentDistance = Math.sqrt(Math.pow(curLoc.x - startLoc.x, 2) + Math.pow(curLoc.z - startLoc.z, 2));
			loopcount++;
			if (loopcount > loopthresh) break;
		} while(linearDistance > currentDistance);
		
		// Current implementation constructs all the beams in their target locations and sets timers to place them in sequence
		long lastPlaceDelay = 0;
		for (BeamLocation beamLocation : locationList) {
			int key = requestBeam(beamLocation, settings);
			if (key == -1) {
				OrbitalStrike.logInfo("Error: Unable to initiate strike as part of sweep", 1);
				return;
			}
			m_Timer.schedule(new BeamPlace_TimerTask(this, key), (lastPlaceDelay += getStaggeredPlacementDelay()));
			m_Timer.schedule(new BeamRemove_TimerTask(this, key), getRemovalInterval());	
		}
		
		// Broadcast warning message to all players in the world
		Iterator<Player> itr = startLoc.world.getPlayers().iterator();
		while (itr.hasNext())
			OrbitalStrike.sendMessage(itr.next(), "Swath from " + startLoc.toStringLocalized() + " to " + endLoc.toStringLocalized() + " beginnning in " + ((getStaggeredPlacementDelay() + getDetonationDelay()) / 1000.0f) + " seconds!");
	}
	
	/**
	 * Cancels all timers and removes any beams still active.
	 * Should be called when the plugin is shutting down, but before finalization because it potentially modifies the world state.
	 */
	public void destroy() {
		m_Timer.cancel();
		Iterator<Beam> itr = m_StrikeMap.values().iterator();
		while (itr.hasNext())
			itr.next().remove();
	}
	
	private int requestBeam(BeamLocation location, BeamSetting settings) {
		int key;
		do {
			key = m_Random.nextInt();
		} while (key < 1 || m_StrikeMap.containsKey(Integer.valueOf(key)));
		
		Beam beam;
		if (settings.beamType == BeamType.SQUARE)
			beam = new SquareBeam(location.x, location.z, location.world, settings.radius);
		else if (settings.beamType == BeamType.SQUARE_PENETRATING)
			beam = new SquarePenetratingBeam(location.x, location.z, location.world, settings.radius);
		else if (settings.beamType == BeamType.THIN)
			beam = new ThinPenetratingBeam(location.x, location.z, location.world, settings.radius);
//		else if (settings.beamType == BeamType.CLEAN_SQUARE)
//			beam = new CleanSquareBeam(location.x, location.z, location.world, settings.radius);
//		else if (settings.beamType == BeamType.CLEAN_SQUARE_PENETRATING)
//			beam = new CleanSquarePenetratingBeam(location.x, location.z, location.world, settings.radius);
//		else if (settings.beamType == BeamType.CIRCULAR)
//			beam = new CircularBeam(location.x, location.z, location.world, settings.radius);
		else if (settings.beamType == BeamType.CIRCULAR_PENETRATING)
			beam = new CircularPenetratingBeam(location.x, location.z, location.world, settings.radius);
		else {
			OrbitalStrike.logInfo("Error: Unrecognized beam type", 1);
			return -1;
		}
		m_StrikeMap.put(key, beam);
		
		return key;
	}
	
	protected void timerBeamPlaceCallback(int uid) {
		Beam beam = m_StrikeMap.get(uid);
		if (beam == null)
			OrbitalStrike.logInfo("Warning: Placement callback for nonexistant UID " + uid, 1);
		else {	
			beam.place();
			m_Timer.schedule(new BeamDetonate_TimerTask(this, uid), getDetonationDelay());
		}
	}
	
	protected void timerBeamDetonateCallback(int uid) {
		Beam beam = m_StrikeMap.get(uid);
		if (beam == null)
			OrbitalStrike.logInfo("Warning: Detonation callback for nonexistant UID " + uid, 1);
		else
			beam.detonate();
	}
	
	protected void timerBeamRemoveCallback(int uid) {
		Beam beam = m_StrikeMap.get(uid);
		if (beam == null) {
			OrbitalStrike.logInfo("Warning: Removal callback for nonexistant UID " + uid, 1);
		}
		else {
			if (beam.shouldRemove()) {
				beam.remove();
				m_Timer.schedule(new BeamDeconstruct_TimerTask(this, uid), getDeconstructionDelay());
			}
			else
				m_Timer.schedule(new BeamRemove_TimerTask(this, uid), getRemovalInterval());
		}
	}
	
	protected void timerBeamDeconstructCallback(int uid) {
		m_StrikeMap.remove(Integer.valueOf(uid));
	}
	
	class BeamPlace_TimerTask extends TimerTask {
		private OSHandler m_Handler;
		private int m_uid;
		public BeamPlace_TimerTask(OSHandler handler, int uid) {
			m_Handler = handler;
			m_uid = uid;
		}
		@Override
		public void run() {
			m_Handler.timerBeamPlaceCallback(m_uid);
		}
	}
	
	class BeamDetonate_TimerTask extends TimerTask {
		private OSHandler m_Handler;
		private int m_uid;
		public BeamDetonate_TimerTask(OSHandler handler, int uid) {
			m_Handler = handler;
			m_uid = uid;
		}
		@Override
		public void run() {
			m_Handler.timerBeamDetonateCallback(m_uid);
		}
	}
	
	class BeamRemove_TimerTask extends TimerTask {
		private OSHandler m_Handler;
		private int m_uid;
		public BeamRemove_TimerTask(OSHandler handler, int uid) {
			m_Handler = handler;
			m_uid = uid;
		}
		@Override
		public void run() {
			m_Handler.timerBeamRemoveCallback(m_uid);
		}
	}
	
	class BeamDeconstruct_TimerTask extends TimerTask {
		private OSHandler m_Handler;
		private int m_uid;
		public BeamDeconstruct_TimerTask(OSHandler handler, int uid) {
			m_Handler = handler;
			m_uid = uid;
		}
		@Override
		public void run() {
			m_Handler.timerBeamDeconstructCallback(m_uid);
		}
	}
}

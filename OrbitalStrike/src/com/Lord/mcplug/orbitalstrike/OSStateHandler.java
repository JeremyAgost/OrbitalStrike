package com.Lord.mcplug.orbitalstrike;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class OSStateHandler {	
	public class StateChangeOrderLocation {
		public StateChangeOrderLocation(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		public int x;
		public int y;
		public int z;
	}
	public abstract class StateChangeOrder {
		protected StateChangeOrder(World world) {
			m_World = world;
		}
		public void add(StateChangeOrderLocation loc) {
			m_LocationsList.add(loc);
		}
		public void add(Location loc) {
			m_LocationsList.add(new StateChangeOrderLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		}
		public void add(int x, int y, int z) {
			m_LocationsList.add(new StateChangeOrderLocation(x, y, z));
		}
		protected ArrayList<StateChangeOrderLocation> m_LocationsList = new ArrayList<StateChangeOrderLocation>();
		protected World m_World;
	}
	public class MaterialStateChangeOrder extends StateChangeOrder {
		public MaterialStateChangeOrder(World world, Material material) {
			super(world);
			m_Material = material;
		}
		public Material getMaterial() {
			return m_Material;
		}
		private Material m_Material;
	}
	public class ExplosiveEntityStateChangeOrder extends StateChangeOrder {
		public ExplosiveEntityStateChangeOrder(World world, float power) {
			super(world);
			m_flPower = power;
		}
		public float getPower() {
			return m_flPower;
		}
		private float m_flPower;
	}
}

package com.Lord.mcplug.orbitalstrike;

import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class OSEntityListener extends EntityListener {
	public static OrbitalStrike m_Plugin;
	public OSEntityListener(OrbitalStrike plugin) {
		m_Plugin = plugin;
	}
	
	public void onEntityExplode(EntityExplodeEvent event) {
		// This is a cruddy hacky way of decided which explosions need to be modified
		if (OrbitalStrike.detionationsActive() && event.getEntity() instanceof TNTPrimed) {
			event.setYield(0.0f);
		}
	}

}

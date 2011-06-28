package com.Lord.mcplug.orbitalstrike;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OSPlayerListener extends PlayerListener {
	public static OrbitalStrike m_Plugin;
	public OSPlayerListener(OrbitalStrike plugin) {
		m_Plugin = plugin;
	}
	
	private HashSet<Player> m_ActivePlayerSet = new HashSet<Player>();
	
	public void listenTo(Player player) {
		m_ActivePlayerSet.add(player);
		OrbitalStrike.logInfo("Listening to " + player.getDisplayName(), 0);
	}
	
	public void stopListeningTo(Player player) {
		m_ActivePlayerSet.remove(player);
		OrbitalStrike.logInfo("Stopped listening to " + player.getDisplayName(), 0);
	}
	
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Player eventPlayer = event.getPlayer();		
		if (m_ActivePlayerSet.contains(eventPlayer)) {
			if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
				m_Plugin.playerListenerFiringCallback(eventPlayer);
			}
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player eventPlayer = event.getPlayer();
		m_ActivePlayerSet.remove(eventPlayer);
		m_Plugin.playerListenerLeavingCallback(eventPlayer);
	}
}

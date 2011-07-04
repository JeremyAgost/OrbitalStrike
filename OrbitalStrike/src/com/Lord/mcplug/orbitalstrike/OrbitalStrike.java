package com.Lord.mcplug.orbitalstrike;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.Lord.mcplug.orbitalstrike.OSHandler.BeamSettings;
import com.Lord.mcplug.orbitalstrike.OSHandler.BeamType;

public class OrbitalStrike extends JavaPlugin {
	public static boolean m_bHasPermissions;
	public static PermissionHandler m_PermissionHandler;
	private static OSHandler m_Handler;
	private final OSPlayerListener m_PlayerListener = new OSPlayerListener(this);
	private final OSEntityListener m_EntityListener = new OSEntityListener(this);
	private HashMap<CommandSender, ActionState> m_CSActionStates = new HashMap<CommandSender, ActionState>();
	private HashMap<CommandSender, Location> m_CSUnconfirmedLocations = new HashMap<CommandSender, Location>();
	private HashMap<CommandSender, BeamSettings> m_CSBeamSettings = new HashMap<CommandSender, BeamSettings>();
	private HashMap<CommandSender, Location> m_CSUnconfirmedSwathStartLocations = new HashMap<CommandSender, Location>();
	private HashMap<CommandSender, Location> m_CSUnconfirmedSwathEndLocations = new HashMap<CommandSender, Location>();
	
	protected enum ActionState {
		NOACTION, FM_TARGETING, FM_CONFIRMING, FS_TARGETING, FS_CONFIRMING
	}
	
	@Override
	public void onDisable() {
		m_Handler.destroy();
	}

	@Override
	public void onEnable() {
		m_Config = new OSConfig();
		m_Handler = new OSHandler();
		setupPermissions();
		
		/* Register event listeners */
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_ANIMATION, m_PlayerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, m_PlayerListener, Priority.High, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, 	m_EntityListener, 	Event.Priority.High, this);
		
		OrbitalStrike.logInfo("Ready", 1);
		if (!m_bHasPermissions)
			OrbitalStrike.logInfo("Warning: Permissions not available! Plugin will only be available to OPs.", 1);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		OrbitalStrike.logInfo("Received command " + commandLabel, 0);
		
		Player senderPlayer = null;
		Player[] allPlayers = sender.getServer().getOnlinePlayers();
		for (int i = 0; i < allPlayers.length; i++)
			if (sender.equals(allPlayers[i]))
				senderPlayer = allPlayers[i];
		if (senderPlayer == null) {
			OrbitalStrike.logInfo("Couldn't find player represented by " + sender.toString(), 0);
			return false;
		}
		
		if (!hasPermissions(senderPlayer))
			return false;
		
		if (!m_CSActionStates.containsKey(sender)) {
			m_CSActionStates.put(sender, ActionState.NOACTION);
			OrbitalStrike.logInfo("Added " + sender.toString() + " to states list", 0);
		}
		
		if (command.getName().equalsIgnoreCase("orbitalstrike")) {
			OrbitalStrike.sendMessage(sender, "Use the command 'firemission' to activate the plugin and the command 'disregard' to deactivate it.");
			return true;
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
			OrbitalStrike.sendMessage(sender, this.getCommand("firemission").getUsage());
			return true;
		}
		else if (args.length > 0 && args[0].equalsIgnoreCase("types")) {
			BeamType[] types = BeamType.values();
			String typesString = "";
			for (int i = 0; i < types.length; i++) {
				if (i > 0) typesString = typesString + ", ";
				typesString = typesString + types[i].name();
			}
			OrbitalStrike.sendMessage(sender, "Valid types: " + typesString);
			return true;
		}
		else if (command.getName().equalsIgnoreCase("firemission") && m_CSActionStates.get(sender) == ActionState.NOACTION) {
			commandFiremission(senderPlayer, args);
			return true;
		}
		else if (command.getName().equalsIgnoreCase("firesweep") && m_CSActionStates.get(sender) == ActionState.NOACTION) {
			commandFiresweep(senderPlayer, args);
		}
		else if (command.getName().equalsIgnoreCase("disregard") && m_CSActionStates.get(sender) != ActionState.NOACTION) {
			commandDisregard(senderPlayer);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Status check for a given player to use this plugin
	 * @param player The player to be checked by Permissions (fallback to OP is Permissions is not available)
	 * @return True if the player is authorized to use this plugin
	 */
	public boolean hasPermissions(Player player) {
		if (!m_bHasPermissions && player.isOp())
			return true;
		else if (m_bHasPermissions && m_PermissionHandler.has(player, "lord.orbitalstrike"))
			return true;
		else {
			OrbitalStrike.sendMessage(player, "You do not have permission to use this plugin.");
			OrbitalStrike.logInfo("Player " + player + " does not have permission.", 1);
			return false;
		}
	}
	
	/**
	 * Callback function from the PlayerListener
	 * Handles all targeting operations depending on the starting state of the called player
	 * (NOACTION will cause the player to be ignored until the state changes)
	 * FM_ actions correspond to the single-target firemission command path
	 * FS_ actions correspond to the start/end-target firesweep command path
	 * @param player The player that caused the event
	 */
	public void playerListenerFiringCallback(Player player) {
		if (!m_CSActionStates.containsKey(player) || m_CSActionStates.get(player) == ActionState.NOACTION) {
			m_PlayerListener.stopListeningTo(player);
			return;
		}
		
		if (m_CSActionStates.get(player) == ActionState.FM_TARGETING) {
			Location curLoc = player.getTargetBlock(null, 256).getLocation();
			m_CSUnconfirmedLocations.put(player, curLoc);
			m_CSActionStates.put(player, ActionState.FM_CONFIRMING);
			OrbitalStrike.sendMessage(player, "Confirm target (" + curLoc.getBlockX() + "," + curLoc.getBlockZ() + ") for fire mission");
		}
		else if (m_CSActionStates.get(player) == ActionState.FM_CONFIRMING) {
			Location prevLoc = m_CSUnconfirmedLocations.get(player);
			Location curLoc = player.getTargetBlock(null, 256).getLocation();
			if (prevLoc.equals(curLoc)) {
				m_Handler.initiateOrbitalStrike(m_Handler.new BeamLocation(curLoc), m_CSBeamSettings.get(player));
				senderStateCleanup(player);
				OrbitalStrike.sendMessage(player, "Fire mission confirmed, firing for effect!");
			}
			else {
				m_CSActionStates.put(player, ActionState.FM_TARGETING);
				OrbitalStrike.sendMessage(player, "Cannot confirm target location, awaiting new target");
			}
		}
		else if (m_CSActionStates.get(player) == ActionState.FS_TARGETING) {
			if (!m_CSUnconfirmedSwathStartLocations.containsKey(player)) {	// STEP 1: target start
				Location startLoc = player.getTargetBlock(null, 256).getLocation();
				m_CSUnconfirmedSwathStartLocations.put(player, startLoc);
				m_CSActionStates.put(player, ActionState.FS_CONFIRMING);
				OrbitalStrike.sendMessage(player, "Confirm start position (" + startLoc.getBlockX() + "," + startLoc.getBlockZ() + ") for fire sweep");
			}
			else if (!m_CSUnconfirmedSwathEndLocations.containsKey(player)) {	// STEP 3: target end
				Location endLoc = player.getTargetBlock(null, 256).getLocation();
				m_CSUnconfirmedSwathEndLocations.put(player, endLoc);
				m_CSActionStates.put(player, ActionState.FS_CONFIRMING);
				OrbitalStrike.sendMessage(player, "Confirm end position (" + endLoc.getBlockX() + "," + endLoc.getBlockZ() + ") for fire sweep");
			}
			else {
				OrbitalStrike.logInfo("Error: Logic error in swath targetting " + player, 1);
				senderStateCleanup(player);
				return;
			}
		}
		else if (m_CSActionStates.get(player) == ActionState.FS_CONFIRMING) {
			if (m_CSUnconfirmedSwathStartLocations.containsKey(player) && !m_CSUnconfirmedSwathEndLocations.containsKey(player)) {	// STEP 2: confirm start
				Location prevLoc = m_CSUnconfirmedSwathStartLocations.get(player);
				Location startLoc = player.getTargetBlock(null, 256).getLocation();
				if (prevLoc.equals(startLoc)) {
					m_CSActionStates.put(player, ActionState.FS_TARGETING);
					OrbitalStrike.sendMessage(player, "Start position confirmed, designate end position");
				}
				else {
					m_CSUnconfirmedSwathStartLocations.remove(player);
					m_CSActionStates.put(player, ActionState.FS_TARGETING);
					OrbitalStrike.sendMessage(player, "Cannot confirm target location, awaiting new start position");
				}
			}
			else if (m_CSUnconfirmedSwathStartLocations.containsKey(player) && m_CSUnconfirmedSwathEndLocations.containsKey(player)) {	//STEP 4: confirm end
				Location prevLoc = m_CSUnconfirmedSwathEndLocations.get(player);
				Location endLoc = player.getTargetBlock(null, 256).getLocation();
				if (prevLoc.equals(endLoc)) {
					Location startLoc = m_CSUnconfirmedSwathStartLocations.get(player);
					m_Handler.initiateOrbitalSwath(m_Handler.new BeamLocation(startLoc), m_Handler.new BeamLocation(endLoc), m_CSBeamSettings.get(player));
					senderStateCleanup(player);
					OrbitalStrike.sendMessage(player, "Fire sweep confirmed, firing for effect!");
				}
				else {
					m_CSUnconfirmedSwathEndLocations.remove(player);
					m_CSActionStates.put(player, ActionState.FS_TARGETING);
					OrbitalStrike.sendMessage(player, "Cannot confirm target location, awaiting new end position");
				}
			}
			else {
				OrbitalStrike.logInfo("Error: Logic error in swath targetting " + player, 1);
				senderStateCleanup(player);
				return;
			}
		}
	}
	
	public void playerListenerLeavingCallback(Player player) {
		senderStateCleanup(player);
	}
	
	private void commandFiresweep(Player player, String[] args) {
		OrbitalStrike.sendMessage(player, "Type '/disregard' to cancel fire sweep");
		
		int radius = 1;
		BeamType beamType = m_Config.getDefaultBeamType();			
		if (args.length > 0) {
			if (args.length >= 1)
				radius = parseBeamRadius(args[0]);
			if (args.length >= 2)
				beamType = parseBeamType(args[1]);
		}
		BeamSettings beamSetting = m_Handler.new BeamSettings(radius, beamType);
		OrbitalStrike.sendMessage(player, beamSetting.toString());
		
		m_PlayerListener.listenTo(player);
		m_CSActionStates.put(player, ActionState.FS_TARGETING);
		m_CSBeamSettings.put(player, beamSetting);
		OrbitalStrike.sendMessage(player, "Designate start position for fire sweep");
	}

	private void commandFiremission(Player player, String[] args) {
		OrbitalStrike.sendMessage(player, "Type '/disregard' to cancel fire mission");
		
		int radius = 1;
		BeamType beamType = m_Config.getDefaultBeamType();			
		if (args.length > 0) {
			if (args.length >= 1)
				radius = parseBeamRadius(args[0]);
			if (args.length >= 2)
				beamType = parseBeamType(args[1]);
		}
		BeamSettings beamSetting = m_Handler.new BeamSettings(radius, beamType);
		OrbitalStrike.sendMessage(player, beamSetting.toString());
		
		m_PlayerListener.listenTo(player);
		m_CSActionStates.put(player, ActionState.FM_TARGETING);
		m_CSBeamSettings.put(player, beamSetting);
		OrbitalStrike.sendMessage(player, "Designate target for fire mission");
	}
	
	private void commandDisregard(Player player) {
		senderStateCleanup(player);
		OrbitalStrike.sendMessage(player, "Fire mission cancelled");
		
		m_PlayerListener.stopListeningTo(player);
	}
	
	private void senderStateCleanup(CommandSender sender) {
		m_CSActionStates.put(sender, ActionState.NOACTION);
		m_CSBeamSettings.remove(sender);
		m_CSUnconfirmedLocations.remove(sender);
		m_CSUnconfirmedSwathStartLocations.remove(sender);
		m_CSUnconfirmedSwathEndLocations.remove(sender);
	}

	private int parseBeamRadius(String arg) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			return 1;
		}
	}
	
	private BeamType parseBeamType(String arg) {
		try {
			return BeamType.valueOf(arg.toUpperCase());
		} catch (IllegalArgumentException e) {
			return m_Config.getDefaultBeamType();	// Default type
		}
	}
	
	private void setupPermissions() {
	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

	      if (OrbitalStrike.m_PermissionHandler == null) {
	          if (permissionsPlugin != null) {
	              OrbitalStrike.m_PermissionHandler = ((Permissions) permissionsPlugin).getHandler();
	              m_bHasPermissions = true;
	          } else {
	        	  m_bHasPermissions = false;
	              OrbitalStrike.logInfo("Permission system not detected, defaulting to OP", 1);
	          }
	      }
	}

	private static OSStateHandler m_StateHandler;
	
	public static OSStateHandler getStateHandler() {
		return m_StateHandler;
	}

	private static OSConfig m_Config;
	
	public static OSConfig getConfig() {
		return m_Config;
	}
	
	public static boolean m_bDebug = true;
	private static Logger m_Logger = Logger.getLogger("Minecraft");
	
	/* Log levels: 0 = debug, 1 = normal */
	public static void logInfo(String info, int level) {
		if (!m_bDebug && level < 1) return;
		m_Logger.info("[OrbitalStrike] " + info);
	}
	
	public static void logInfo(String info) {
		OrbitalStrike.logInfo(info, 1);
	}
	
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage("[OrbitalStrike] " + message);
	}
	
	private static int m_iActiveDetonations = 0;
	
	public static void detonationBegin() {
		m_iActiveDetonations++;
	}
	
	public static void deonationEnd() {
		m_iActiveDetonations--;
	}
	
	public static boolean detionationsActive() {
		return m_iActiveDetonations > 0;
	}
}
